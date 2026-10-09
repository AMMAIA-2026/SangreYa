# JUnit y MockWebServer

El reporte de la corrida final del 09/10/2026 está disponible en
[reportes/indice.html](reportes/indice.html). Abrilo en un navegador para consultar
los casos, los métodos ejecutados y los resultados de debug y release.

## Resultado

- **Debug:** 29/29 tests aprobados, sin fallos ni omitidos.
- **Release:** 29/29 tests aprobados, sin fallos ni omitidos.
- En cada variante: 24 tests JUnit sin MockWebServer y 5 con MockWebServer.
- Los 29 métodos corresponden a 20 casos documentados en la planilla.


## Ejecución

Con JDK 17:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:jacocoDebugReport --rerun-tasks --continue --console=plain
```

La configuración de tests incluye `org.json:json:20231013` para que el mapper
use una implementación real de `JSONObject` en JVM.

## Archivos del reporte

- `reportes/indice.html`: entrada navegable.
- `reportes/resumen.md`: resumen de la corrida.
- `reportes/resultados-debug/` y `reportes/resultados-release/`: reportes HTML de Gradle.
- `reportes/junit-debug/` y `reportes/junit-release/`: resultados XML originales.
- `reportes/cobertura-debug/`: cobertura JaCoCo en HTML y XML.
- `reportes/ejecucion.json`: versión, commit, hashes de fuentes y resultados por método.
- `reportes/planilla-actualizada.json`: registro de actualización de la planilla.
- `reportes/sha256.json`: hashes de integridad de los archivos del reporte.

