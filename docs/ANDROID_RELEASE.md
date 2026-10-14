# Compilación y publicación Android

## Requisitos y APK local

- JDK 17 (también sirve el incluido en Android Studio si es versión 17).
- Android SDK con plataforma 33 y Build Tools 33.0.1.
- Conexión a Internet para descargar dependencias y usar el backend de la app.

Desde la raíz del repositorio, en PowerShell:

```powershell
# Si tu Java predeterminado no es 17, ajustá esta ruta a tu instalación.
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:lintDebug :app:assembleDebug --console=plain
```

APK generado: `app/build/outputs/apk/debug/app-debug.apk`.
Copialo a un teléfono con Android 7.0 o superior y permití la instalación desde
esa fuente, o ejecutá con un dispositivo conectado y depuración USB habilitada:

```powershell
adb install -r "app/build/outputs/apk/debug/app-debug.apk"
```

La app usa `https://sangreyaispc.pythonanywhere.com/`. La distribución del APK no
despliega ni modifica el backend. El APK debug es para pruebas; cada entorno
puede tener una firma debug diferente. Si Android rechaza una actualización por
firma incompatible, desinstalá la versión anterior (se borran sus datos locales)
antes de instalar el APK nuevo.

## CI en GitHub Actions

`.github/workflows/android.yml` se ejecuta en pushes a ramas, pull requests y
manualmente. Usa Java 17 y el Gradle Wrapper del proyecto, ejecuta tests unitarios
debug/release y lint debug, y genera el APK de prueba.

Después de subir el commit:

1. Abrí **Actions → Android CI** y elegí la ejecución de tu commit.
2. Revisá los resultados y descargá el artifact `SangreYa-debug-<commit>`.
3. Extraé el ZIP e instalá `app-debug.apk`.

Los artifacts se conservan 14 días y su descarga requiere una sesión en GitHub.
Los reportes de tests y lint se adjuntan en un artifact separado, también cuando
falla una verificación. Los tests instrumentados de `app/src/androidTest` requieren
un emulador o dispositivo y no forman parte de estos workflows.

## Preparar la firma de release (una sola vez)

Si ya existe una clave de publicación de SangreYa, reutilizala. Para una app
nueva, generá el keystore fuera del repositorio, en una carpeta privada existente:

```powershell
& "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -v -storetype JKS -keystore "C:\ruta-privada\sangreya-release.jks" -alias sangreya -keyalg RSA -keysize 2048 -validity 10000
```

`keytool` solicita las contraseñas y datos del certificado de forma interactiva.
Guardá una copia de respaldo del archivo, del alias y de las contraseñas. Todas
las actualizaciones de la app deben conservar esta firma y el `applicationId`.

En **Settings → Secrets and variables → Actions → New repository secret**, cargá:

| Secret | Valor |
| --- | --- |
| `SANGREYA_KEYSTORE_BASE64` | Contenido completo del keystore codificado en Base64 |
| `SANGREYA_KEYSTORE_PASSWORD` | Contraseña del keystore |
| `SANGREYA_KEY_ALIAS` | Alias usado al crear la clave, por ejemplo `sangreya` |
| `SANGREYA_KEY_PASSWORD` | Contraseña de la clave; puede ser igual a la del keystore |

Para copiar el Base64 al portapapeles sin imprimirlo ni crear otro archivo:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\ruta-privada\sangreya-release.jks')) | Set-Clipboard
```

Pegalo en el secret y después limpiá el portapapeles. El keystore y sus
contraseñas no deben incorporarse a Git; el `.gitignore` ya excluye estos archivos.
Los workflows usan el `GITHUB_TOKEN` provisto por GitHub, sin un token personal.
La organización debe permitir GitHub Actions y escritura de Releases para ese
workflow.

### Compilar un release local

`app/build.gradle` toma la firma de cuatro variables de entorno:

- `SANGREYA_KEYSTORE_PATH`: ruta absoluta al keystore.
- `SANGREYA_KEYSTORE_PASSWORD`.
- `SANGREYA_KEY_ALIAS`.
- `SANGREYA_KEY_PASSWORD`.

Con esas variables configuradas en la sesión, ejecutá:

```powershell
.\gradlew.bat :app:assembleRelease --console=plain
```

Resultado firmado: `app/build/outputs/apk/release/app-release.apk`. Si no se
configura ninguna variable de firma, Gradle mantiene el comportamiento original:
puede generar un release **sin firmar**, que no sirve para instalación directa.
Una configuración parcial falla con un mensaje explícito.

## Publicar una versión en GitHub Releases

`.github/workflows/android-release.yml` se ejecuta al subir un tag `v*`.
Antes de publicarlo:

1. Revisá los cambios y realizá tu commit y push a la rama de trabajo.
2. Confirmá que Android CI pasó y probá el APK en un dispositivo.
3. Confirmá que los cuatro secrets de firma estén configurados.
4. Para versiones nuevas, incrementá `versionCode` y actualizá `versionName` en
   `app/build.gradle`, incluyendo esos cambios en el commit de la versión.
5. Creá el tag sobre el commit revisado y subilo. El tag debe ser exactamente
   `v` seguido de `versionName`.

El proyecto tiene actualmente `versionCode 1` y `versionName "1.0"`. Para esa
primera versión, los comandos son:

```powershell
git tag -a v1.0 -m "SangreYa 1.0"
git push origin v1.0
```

Si cambiás `versionName` a `"1.0.0"`, el tag correspondiente será `v1.0.0`.
Una vez publicada una versión, usá un tag nuevo para la siguiente entrega.

El workflow recupera la clave en un archivo temporal, ejecuta los tests y lint
release, compila el APK, verifica su firma y comprueba la coincidencia del tag
con la versión real del APK. Finalmente crea una Release con notas automáticas,
el APK `SangreYa-<tag>.apk` y su checksum SHA-256. La copia temporal del keystore
se elimina al finalizar, incluso ante errores.

Las descargas estables estarán en:
<https://github.com/AMMAIA-2026/SangreYa/releases>.
En un repositorio privado solo podrán descargarlas quienes tengan acceso.
GitHub Releases distribuye el instalador; la instalación en el teléfono sigue
siendo manual. La publicación en Google Play requiere otro flujo con AAB y
Play Console.
