# Reporte JUnit y MockWebServer

Ejecución final: 2026-10-09T15:43:05-03:00.
Commit base: `423f705f3bb0ee0592d7c2fccec8c99c3cba0785`; fuentes probadas SHA-256 `48a698a287ed132c7dc1cd86f0cfc58bbc9bed762ed53937eb9ff35b7419817e`.

| Variante | Tests | Aprobados | Fallos | Omitidos | JUnit sin MockWebServer | MockWebServer |
|---|---:|---:|---:|---:|---:|---:|
| debug | 29 | 29 | 0 | 0 | 24 | 5 |
| release | 29 | 29 | 0 | 0 | 24 | 5 |

Los 29 métodos corresponden a 20 casos TC-UNIT/TC-NET. Todos están implementados y aprobados en ambas variantes.

## Casos

| Caso | Métodos | Debug | Release |
|---|---:|---|---|
| TC-NET-01 | 1 | PASS | PASS |
| TC-NET-02 | 1 | PASS | PASS |
| TC-NET-03 | 1 | PASS | PASS |
| TC-NET-04 | 1 | PASS | PASS |
| TC-NET-05 | 1 | PASS | PASS |
| TC-UNIT-01 | 1 | PASS | PASS |
| TC-UNIT-02 | 1 | PASS | PASS |
| TC-UNIT-03 | 1 | PASS | PASS |
| TC-UNIT-04 | 1 | PASS | PASS |
| TC-UNIT-05 | 1 | PASS | PASS |
| TC-UNIT-06 | 1 | PASS | PASS |
| TC-UNIT-07 | 1 | PASS | PASS |
| TC-UNIT-08 | 1 | PASS | PASS |
| TC-UNIT-09 | 1 | PASS | PASS |
| TC-UNIT-10 | 1 | PASS | PASS |
| TC-UNIT-11 | 1 | PASS | PASS |
| TC-UNIT-12 | 1 | PASS | PASS |
| TC-UNIT-13 | 1 | PASS | PASS |
| TC-UNIT-14 | 8 | PASS | PASS |
| TC-UNIT-15 | 3 | PASS | PASS |

## Configuración de prueba

Dependencia exclusiva de tests org.json:json:20231013 para ejecutar el mapper con JSONObject real en JVM.

Comando: `.\gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:jacocoDebugReport --rerun-tasks --continue --console=plain`

Cobertura debug de líneas: 7.48% (235/3140); ramas: 9.59% (101/1053).
Cobertura global de todas las clases compiladas de producción debug; excluye clases generadas R, BuildConfig y Manifest. Incluye Activities y código no ejercitado; no es cobertura aislada por caso.

Reporte navegable: [indice.html](indice.html). Resultados originales: [debug](resultados-debug/index.html), [release](resultados-release/index.html). Cobertura: [HTML](cobertura-debug/index.html), [XML](cobertura-debug/coverage.xml). JUnit XML: `junit-debug/` y `junit-release/`. Metadatos: `ejecucion.json`; integridad: `sha256.json`.
