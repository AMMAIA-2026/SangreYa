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
- `reportes/junit-debug/` y `reportes/junit-release/`: resultados XML de la corrida, sin el hostname local.
- `reportes/cobertura-debug/`: cobertura JaCoCo en HTML y XML.
- `reportes/ejecucion.json`: versión, commit, hashes de fuentes y resultados por método.
- `reportes/planilla-actualizada.json`: registro de actualización de la planilla.
- `reportes/sha256.json`: hashes de integridad de los archivos del reporte.

## Publicación de evidencia

Los reportes HTML, XML, JSON y sus recursos se conservan como evidencia de
entrega. La sanitización elimina rutas absolutas y el hostname, y sustituye los
identificadores de sesión JaCoCo por nombres neutros. Se conservan los casos,
resultados, tiempos, cobertura, hashes de fuentes y atribuciones del equipo.
Los binarios `.exec` son locales y están excluidos de Git.

Después de copiar una nueva corrida a `reportes/`, desde la raíz del proyecto:

```powershell
python tests/Sanitize-Evidence.py --write
python tests/Sanitize-Evidence.py --check
```

El primer comando sanitiza los metadatos y actualiza `sha256.json`; el segundo
verifica que no queden cambios pendientes de sanitización ni indicadores de
secretos o rutas locales en los reportes públicos. Los hashes de archivos de
texto se calculan con saltos de línea LF para ser estables entre Windows y Linux;
los recursos binarios se verifican sin transformación. El manifiesto no incluye
su propio archivo ni los artefactos ignorados. Los hashes históricos de fuentes
en `ejecucion.json` permanecen tal como se registraron en la corrida.
