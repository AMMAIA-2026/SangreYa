# Evidencias de flujos críticos Espresso

La ejecución instrumentada registrada el **12/10/2026** contiene tres pruebas de
`com.ammaia_ispc.sangreyamobile.tests.CriticalFlowsTest`, con **3 aprobadas,
0 fallos y 0 omitidas**, en Pixel 3a, Android 16 / API 36.

| Caso | Método | Resultado | Duración registrada |
|---|---|---|---|
| TC-UI-01 | `tcUI01_registerAndLogin` | PASS | 25,535 s |
| TC-UI-02 | `tcUI02_adminCampaignCrud` | PASS | 28,997 s |
| TC-UI-03 | `tcUI03_consultAndEnroll` | PASS | 22,972 s |

## Archivos

- [`junit.xml`](junit.xml): resultado original de la ejecución instrumentada.
- [`html/index.html`](html/index.html): informe HTML nativo de Android Gradle Plugin.
- [`html/com.ammaia_ispc.sangreyamobile.tests.CriticalFlowsTest.html`](html/com.ammaia_ispc.sangreyamobile.tests.CriticalFlowsTest.html): detalle de los tres flujos y sus resultados.
- `html/css/` y `html/js/`: recursos del informe para abrirlo completo en un navegador.

El HTML se generó con el generador `TestReport` del Android Gradle Plugin del
proyecto a partir del XML registrado. La fecha del pie del HTML corresponde a
la generación del informe; la ejecución de las pruebas es la del 12/10/2026.

## Regenerar el informe

Con JDK 17 y las dependencias de Gradle disponibles, desde la raíz del proyecto:

```powershell
.\gradlew.bat --offline --init-script tests/ui/espresso/generate-report.init.gradle :app:reportRecordedEspresso --console=plain
```

La tarea utiliza únicamente `junit.xml` y genera `html/`; no requiere el
dispositivo ni ejecuta nuevamente los tests.
