# Evidencia Newman

`reportes/reporte.html`, `reportes/junit.xml` y `reportes/cleanup-*.json` se
conservan como evidencia requerida de entrega. La colección fuente, los scripts
QA y las plantillas sin credenciales también se versionan.

La colección `*.runtime.postman_collection.json` es una copia generada por el
runner y se mantiene sólo localmente. También quedan fuera de Git los environments
privados, credenciales, logs y archivos temporales. Las reglas no excluyen la
carpeta de evidencia ni habilitan indiscriminadamente su contenido.

Después de generar reportes, desde la raíz del proyecto:

```powershell
python tests/Sanitize-Evidence.py --write
python tests/Sanitize-Evidence.py --check
```

El control revisa los reportes públicos y no lee los environments privados.
Los HTML/XML de evidencia deben contener resultados y datos de prueba; si el
control detecta un token, una clave privada o una ruta local no reconocida, indica
el archivo para corregirlo antes de publicarlo. No ejecutar Newman sólo para
sanitizar: la corrida realiza operaciones contra el backend.
