param(
    [scriptblock]$CleanupCommand,
    [string]$CleanupConnectionPath = (Join-Path $PSScriptRoot 'Cleanup-Connection.private.json')
)

$ErrorActionPreference = 'Stop'
$collectionPath = Join-Path $PSScriptRoot 'SangreYa_API_Deploy.postman_collection.json'
$environmentPath = Join-Path $PSScriptRoot 'SangreYa_Deploy.private.postman_environment.json'
$operationsPath = Join-Path $PSScriptRoot 'Qa-Operations.js'
$cleanupPath = Join-Path $PSScriptRoot 'Cleanup-Newman-QA.py'

if (-not (Test-Path -LiteralPath $collectionPath -PathType Leaf)) {
    throw "Falta la coleccion para deploy: $collectionPath. Coloca el JSON junto a este script."
}

if (-not (Test-Path -LiteralPath $environmentPath -PathType Leaf)) {
    throw "Falta el environment privado: $environmentPath. Copia el template y completa las credenciales QA."
}

$collection = Get-Content -LiteralPath $collectionPath -Raw -Encoding UTF8 | ConvertFrom-Json
$environment = Get-Content -LiteralPath $environmentPath -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($key in @('baseUrl', 'adminEmail', 'adminPassword')) {
    $entry = @($environment.values | Where-Object { $_.key -eq $key -and $_.enabled })
    if (-not $entry -or [string]::IsNullOrWhiteSpace([string]$entry[0].value)) {
        throw "Completa $key en el environment privado antes de ejecutar."
    }
}
if (-not (Test-Path -LiteralPath $operationsPath -PathType Leaf)) { throw "Falta $operationsPath" }
if (-not $CleanupCommand) {
    $pythonCommand = Get-Command 'python' -ErrorAction SilentlyContinue
    if (-not $pythonCommand) { throw 'No encuentro Python 3 para ejecutar la limpieza independiente.' }
    & $pythonCommand.Source $cleanupPath --check --connection-file $CleanupConnectionPath
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo comprobar el acceso de limpieza QA; Newman todavía no ejecutó mutaciones.' }
}

function Set-CollectionVariable([string]$key, [string]$value) {
    $collection.variable = @($collection.variable | Where-Object { $_.key -ne $key }) + @(
        [pscustomobject]@{ key = $key; value = $value; type = 'string' }
    )
}
Set-CollectionVariable '__qaOperations' (Get-Content -LiteralPath $operationsPath -Raw -Encoding UTF8)
$runId = [string][DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() + ('{0:D6}' -f (Get-Random -Minimum 0 -Maximum 1000000))
Set-CollectionVariable 'runId' $runId

function Normalize-PostmanRequestUrls($items) {
    foreach ($item in $items) {
        if ($item.request -and ($item.request.url -is [System.Management.Automation.PSCustomObject]) -and $item.request.url.raw) {
            $item.request.url = [string]$item.request.url.raw
        }
        if ($item.item) {
            Normalize-PostmanRequestUrls @($item.item)
        }
    }
}
Normalize-PostmanRequestUrls @($collection.item)

$collectionName = [string]$collection.info.name
$collectionDescription = [string]$collection.info.description
if ($collection.info.schema -notlike '*collection/v2.1.0/collection.json') {
    throw 'La coleccion debe estar exportada como Postman Collection v2.1.'
}
if ($collectionName -match '(?i)DRAFT|CANDIDATE' -or $collectionDescription -match '(?i)no est\u00E1 aprobada para ejecutar') {
    throw 'La coleccion esta identificada como borrador/candidata, no como version aprobada para deploy.'
}
if ($collectionDescription -match '(?i)s[o\u00F3]lo en base de pruebas aislada') {
    throw 'La coleccion todavia declara ejecucion exclusiva en base aislada; no se enviaron requests al deploy.'
}

$reportDirectory = Join-Path $PSScriptRoot 'reportes'
if (-not (Test-Path -LiteralPath $reportDirectory -PathType Container)) {
    New-Item -ItemType Directory -Path $reportDirectory | Out-Null
}
$exportedEnvironmentPath = Join-Path $reportDirectory 'ultima-ejecucion.private.postman_environment.json'
if (Test-Path -LiteralPath $exportedEnvironmentPath -PathType Leaf) {
    $previousEnvironment = Get-Content -LiteralPath $exportedEnvironmentPath -Raw -Encoding UTF8 | ConvertFrom-Json
    foreach ($key in @('__newmanLoginAttemptTimes', '__newmanRecoveryAttemptTimes')) {
        $previous = @($previousEnvironment.values | Where-Object { $_.key -eq $key })
        if ($previous) { Set-CollectionVariable $key ([string]$previous[0].value) }
    }
}
# Written before mutation: also usable after interruption or failed setup.
$manifestPath = Join-Path $reportDirectory "cleanup-$runId.json"
$manifest = [ordered]@{
    runId = $runId
    baseUrl = [string](@($environment.values | Where-Object { $_.key -eq 'baseUrl' })[0].value)
    collectionSha256 = (Get-FileHash -LiteralPath $collectionPath -Algorithm SHA256).Hash
    operationsSha256 = (Get-FileHash -LiteralPath $operationsPath -Algorithm SHA256).Hash
    cleanupComplete = $false
    blocked = @('TC-DASH-05: requiere limpieza completa de datos de negocio y ejecución separada')
}
[System.IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 10), [System.Text.UTF8Encoding]::new($false))
$runtimeCollectionPath = Join-Path $reportDirectory 'SangreYa_API_Deploy.runtime.postman_collection.json'
$runtimeCollectionJson = ConvertTo-Json -InputObject $collection -Depth 100
[System.IO.File]::WriteAllText($runtimeCollectionPath, $runtimeCollectionJson, [System.Text.UTF8Encoding]::new($false))
$junitReportPath = Join-Path $reportDirectory 'junit.xml'
$htmlReportPath = Join-Path $reportDirectory 'reporte.html'
$newmanArguments = @(
    'run', $runtimeCollectionPath,
    '-e', $environmentPath,
    '--timeout-script', '4600000',
    '--export-environment', $exportedEnvironmentPath,
    '-r', 'cli,junit,html',
    '--reporter-junit-export', $junitReportPath,
    '--reporter-html-export', $htmlReportPath
)

$newmanCommand = Get-Command 'newman' -ErrorAction SilentlyContinue
if (-not $newmanCommand) {
    $npxCommand = Get-Command 'npx.cmd' -ErrorAction SilentlyContinue
    if (-not $npxCommand) {
        throw 'No encuentro npx ni newman. Instala Node.js/npm y ejecuta: npm install --global newman'
    }
}

$previousNodeOptions = $env:NODE_OPTIONS
if ([string]::IsNullOrWhiteSpace($previousNodeOptions)) {
    $env:NODE_OPTIONS = '--disable-warning=DEP0176'
} elseif ($previousNodeOptions -notmatch '(^|\s)--disable-warning=DEP0176(\s|$)') {
    $env:NODE_OPTIONS = "$previousNodeOptions --disable-warning=DEP0176"
}

try {
    if ($newmanCommand) {
        & $newmanCommand.Source @newmanArguments
    } else {
        & $npxCommand.Source --yes newman @newmanArguments
    }
    $newmanExitCode = $LASTEXITCODE
} finally {
    if ($null -eq $previousNodeOptions) {
        Remove-Item Env:NODE_OPTIONS -ErrorAction SilentlyContinue
    } else {
        $env:NODE_OPTIONS = $previousNodeOptions
    }
    if ($CleanupCommand) {
        $LASTEXITCODE = 0
        & $CleanupCommand $runId $manifestPath
        if (-not $? -or $LASTEXITCODE -ne 0) { throw "Falló limpieza posterior del runId $runId. Conserva $manifestPath para reintentar." }
        $manifest.cleanupComplete = $true
        [System.IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 10), [System.Text.UTF8Encoding]::new($false))
    } else {
        "Ejecutando limpieza independiente de QA para runId $runId desde esta consola..."
        $cleanupLines = & $pythonCommand.Source $cleanupPath --run-id $runId --connection-file $CleanupConnectionPath
        $cleanupExitCode = $LASTEXITCODE
        $cleanupLines
        if ($cleanupExitCode -ne 0) { throw "Falló limpieza/inspección QA del runId $runId. Resultado pendiente en $manifestPath." }
        $cleanupResult = ($cleanupLines -join [Environment]::NewLine) | ConvertFrom-Json
        if (-not $cleanupResult.cleanupComplete) { throw "No se confirmó limpieza completa del runId $runId." }
        $manifest.cleanupComplete = $true
        $manifest['cleanupResult'] = $cleanupResult
        [System.IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 10), [System.Text.UTF8Encoding]::new($false))
    }
}

exit $newmanExitCode
