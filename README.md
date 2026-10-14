# 🩸 SangreYa Android

Aplicación móvil Android nativa para la difusión y gestión de campañas de donación voluntaria de sangre.

## Descripción del proyecto

SangreYa centraliza la información sobre campañas de donación de sangre: fechas, ubicaciones, cupos y centros de salud asociados. Permite a la comunidad consultar campañas, registrarse, iniciar sesión e inscribirse, y a las personas administradoras gestionar campañas, usuarios, mensajes de contacto y estadísticas desde la misma app.

Está dirigida a donantes potenciales y a instituciones de salud que organizan campañas.

Es la continuación mobile del [Proyecto Integrador II (SPA Angular + Django)](https://github.com/AMMAIA-2026/Proyecto2026), desarrollado por el mismo equipo. La app reutiliza el backend Django REST existente (misma API, misma autenticación JWT) con una interfaz 100 % nativa nueva.

Se desarrolla en el marco del espacio curricular **Aplicaciones para Móviles** (Programador de Aplicaciones para Móviles) del ISPC — Tecnicatura Superior en Desarrollo Web y Aplicaciones Digitales. Docente: Mario Gonzalez.

---

## Tecnologías utilizadas

**App móvil**

| Componente | Detalle |
|---|---|
| Lenguaje | Java (JDK 17) |
| IDE | Android Studio |
| Android | `minSdk 24` (Android 7.0), `targetSdk` / `compileSdk 33` |
| Build | Gradle 8.0 · Android Gradle Plugin 8.1 |
| Red | Retrofit 2.9.0 + Gson + OkHttp 4.11.0 |
| UI | AppCompat, Material Components, ConstraintLayout, DrawerLayout |
| Sesión segura | `androidx.security:security-crypto` (EncryptedSharedPreferences) |
| Tests | JUnit 4, Mockito, MockWebServer, Espresso (+ accesibilidad), JaCoCo |
| CI/CD | GitHub Actions (build, tests, lint, release firmado por tag) |

**Backend** (reutilizado del proyecto anterior)

- Python + Django + Django REST Framework
- Autenticación JWT (`djangorestframework-simplejwt`): access token de 60 minutos, refresh token de 1 día con rotación
- SQLite
- Desplegado en PythonAnywhere (HTTPS)

---

## Integrantes del equipo (AMMAIA)

| Integrante | Rol |
|---|---|
| Abigail Picone | Scrum Master + Desarrollo |
| Astrid Luppi | Desarrollo |
| Marcela Villanueva | Desarrollo |
| Irina Pirles | Desarrollo |
| Melina Belén Bruvera | Desarrollo |
| Guillermo Mauricio Diván | Desarrollo |

Metodología: **Scrum**, sin roles fijos de desarrollo; todo el equipo trabaja en frontend, backend y testing según el sprint.

---

## Funcionalidades

Todas las pantallas consumen la API real del backend desplegado (no hay datos simulados). La app tiene tres perfiles de uso y el menú lateral cambia según el rol.

### Visitante (sin sesión)

| Pantalla | Qué permite |
|---|---|
| **Splash** | Pantalla de bienvenida con el logo. Si hay una sesión guardada, la restaura automáticamente y entra a la app; si no, entra como visitante. |
| **Listado de campañas** | Muestra todas las campañas con título, ubicación, fechas, estado (Activa, Próximamente o Finalizada) y cupo. Permite buscar por texto y filtrar por estado. |
| **Detalle de campaña** | Descripción, fechas, ubicación, cupo, cantidad de inscriptos y datos del centro de salud asociado (dirección, teléfono y sitio web). Si no hay sesión, el botón de inscripción informa que hay que iniciar sesión. |
| **Sobre nosotros** | Información institucional del proyecto y acceso al formulario de contacto. |
| **Video** | "¿Por qué donar sangre?", con reproducción integrada: reproducir, pausar, detener y pantalla completa. |
| **Contacto** | Envío de consultas (email, asunto y mensaje) con validación de campos obligatorios, formato de email y longitudes máximas. |
| **Registro** | Alta de cuenta con usuario, email, contraseña, DNI, nombre, apellido y fecha de nacimiento. Los errores del servidor (email o DNI duplicado, contraseña inválida) se muestran en el formulario y se conservan los datos ingresados. |
| **Inicio de sesión** | Login con email y contraseña. Según el rol, lleva a la vista de usuario o al panel administrativo. |
| **Recuperar contraseña** | Flujo de dos pasos (email y nueva contraseña). La respuesta es neutral: no revela si el email existe. |

### Usuario estándar

| Pantalla | Qué permite |
|---|---|
| **Inscripción a campañas** | Desde el detalle, inscribirse a una campaña. La app informa el resultado: edad no permitida, inscripción duplicada, cupo completo, campaña finalizada o inscripción exitosa con el total actualizado. |
| **Mis inscripciones** | Lista separada entre inscripciones actuales/próximas e historial. Cada tarjeta muestra la etiqueta "Inscripto" y permite cancelar la inscripción con confirmación previa. |
| **Perfil** | Consulta y edición de los datos propios permitidos (el rol no se puede modificar y no se accede a perfiles ajenos). |
| **Cerrar sesión** | Borra la sesión local y revoca el acceso; vuelve a una pantalla pública. |

### Administrador

| Pantalla | Qué permite |
|---|---|
| **Dashboard** | Totales de campañas, inscripciones y donantes; campañas recientes; campañas agrupadas por estado y gráficos mensuales de inscripciones y donantes. |
| **Gestión de campañas** | Listado administrativo, creación, edición y eliminación (con confirmación). El formulario incluye título, descripción, ubicación, centro de salud (opcional, se elige de una lista), fechas y cupo máximo (opcional; vacío significa sin límite). |
| **Inscriptos por campaña** | Desde el detalle de una campaña, ver los usuarios inscriptos, ordenados por apellido y nombre. |
| **Gestión de usuarios** | Listado, edición y eliminación de usuarios (con confirmación). |
| **Mensajes de contacto** | Bandeja con los mensajes pendientes primero e indicación visual de los no leídos; detalle de cada mensaje y marcado/desmarcado como seguido. |

### Reglas de negocio que aplica la app

- **Contraseña:** mínimo 10 caracteres, con mayúscula, minúscula, número y símbolo.
- **Inscripción:** solo usuarios estándar de **18 a 64 años** (calculado al día de la inscripción), sin inscripción previa a esa campaña, con campaña no finalizada y cupo disponible.
- **Cupo:** entero mínimo 1; si no se define, la campaña no tiene límite. Al alcanzar el cupo, el estado pasa a **Finalizada**.
- **Fechas de campaña:** el inicio no puede ser anterior a hoy y el fin no puede ser anterior al inicio ni a hoy.
- **Permisos por rol:** las operaciones administrativas están restringidas a administradores; el backend responde 401/403 si un usuario sin permisos intenta usarlas.
- **Errores:** se diferencian los errores de validación (400), de permisos (401/403), de recurso inexistente (404), de conflicto (409) y de red, con mensajes visibles y recuperables.

### Seguridad y sesión

- Login con JWT; el rol define el menú y las pantallas disponibles.
- El refresh token se guarda cifrado (EncryptedSharedPreferences) y el access token se mantiene solo en memoria.
- Renovación automática del access token ante un 401 mediante un `Authenticator` de OkHttp, con un único reintento (sin ciclos de renovación).
- Si el refresh no es válido, se elimina la sesión local y se redirige al login.
- Tráfico exclusivamente por HTTPS (`usesCleartextTraffic="false"`).

### Fuera de alcance de esta versión

- Mapas y geolocalización (se muestra la dirección del centro de salud como dato de la campaña).
- Notificaciones push.

---

## Arquitectura del proyecto

```text
SangreYa/
├── app/src/main/java/com/ammaia_ispc/sangreyamobile/
│   ├── activities/   # Pantallas (Splash, Login, Register, CampaignList, CampaignDetail, Profile, ...)
│   ├── data/         # Repositorios que consumen la API (Auth, Campaign, User, Contact, Dashboard, HealthCenter)
│   ├── model/        # Modelos y requests/responses de la API
│   ├── helpers/      # ApiClient, ApiService, ApiConfig, SessionManager, TokenAuthenticator, validadores, navegación
│   └── res/          # Layouts, drawables, strings, colores y dimensiones
├── app/src/test/          # Tests unitarios y de red (JUnit + MockWebServer)
├── app/src/androidTest/   # Tests instrumentados (Espresso y accesibilidad)
├── tests/                 # Evidencias de pruebas (JVM, API/Newman, UI/Espresso, informe de accesibilidad)
├── docs/                  # Guía de compilación y publicación Android
├── postman/ .postman/     # Recursos de Postman
└── .github/workflows/     # CI y release
```

---

## Instalación y ejecución

### Requisitos

- JDK 17 (sirve el incluido en Android Studio si es versión 17)
- Android Studio con Android SDK 33 y Build Tools 33.0.1
- Dispositivo o emulador con Android 7.0 (API 24) o superior
- Conexión a Internet (dependencias y backend)

### Pasos

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/AMMAIA-2026/SangreYa.git
   ```
2. Abrirlo en Android Studio y esperar la sincronización de Gradle.
3. Conectar un dispositivo (con depuración USB) o iniciar un emulador.
4. Ejecutar la app con ▶ (`app`).

### Configuración de la API

La URL del backend está centralizada en una única constante, `ApiConfig.BASE_URL`
(`app/src/main/java/com/ammaia_ispc/sangreyamobile/helpers/ApiConfig.java`):

```java
public static final String BASE_URL = "https://sangreyaispc.pythonanywhere.com/";
```

- La app **no requiere archivos `.env` ni claves** para ejecutarse contra el backend desplegado.
- Para apuntar a otro backend, cambiá esa constante. Debe ser una URL **HTTPS** que termine en `/`.
- Para correr el backend localmente, seguí las instrucciones del [repositorio del backend](https://github.com/AMMAIA-2026/Proyecto2026) (requiere variables de entorno propias, ver `.env_modelo`).

### Perfiles para probar

| Perfil | Cómo obtenerlo |
|---|---|
| Usuario estándar | Registrarse desde la app (**Registrarse** en el menú). |
| Administrador | Solicitar las credenciales de prueba al equipo (no se publican en el repositorio). |

---

## Pruebas

| Tipo | Herramienta | Ubicación / evidencia |
|---|---|---|
| Unitarias y de red | JUnit 4 + Mockito + MockWebServer | `app/src/test` · reporte en [`tests/jvm`](tests/jvm/README.md): 29/29 aprobados en debug y release |
| API | Postman / Newman contra el deploy | [`tests/api/Newman`](tests/api/Newman/README.md) |
| UI (flujos críticos) | Espresso | [`tests/ui/espresso`](tests/ui/espresso/README.md): 3/3 aprobados (registro y login, CRUD de campañas, consulta e inscripción) |
| Accesibilidad | TalkBack + Espresso Accessibility | [`tests/ui`](tests/ui) (Informe de Accesibilidad, Sprint 2) |

Ejecutar los tests unitarios:

```bash
./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest
```

Los tests instrumentados (`app/src/androidTest`) requieren un emulador o dispositivo conectado. El plan de pruebas, los casos y el reporte de bugs están en la [Wiki](https://github.com/AMMAIA-2026/SangreYa/wiki).

---

## Entrega y release

- **Backend en producción:** <https://sangreyaispc.pythonanywhere.com/>
- **Integración continua:** cada push y pull request ejecuta tests unitarios, lint y genera un APK de prueba (**Actions → Android CI**, retención de 14 días).
- **Release firmado:** se publica automáticamente al subir un tag `v*` (por ejemplo `v1.0`, que debe coincidir con `versionName`) en [GitHub Releases](https://github.com/AMMAIA-2026/SangreYa/releases), con el APK `SangreYa-<tag>.apk` y su checksum SHA-256.
- **Guía de compilación, firma y publicación:** [`docs/ANDROID_RELEASE.md`](docs/ANDROID_RELEASE.md)
- **Versión actual:** `1.0` (`versionCode 1`)
- **Video demo (hasta 3 min):** _completar con el enlace_
- **Video de autenticación y autorización:** _completar con el enlace_

Instalación manual del APK en un teléfono Android 7.0+: descargarlo desde Releases y permitir la instalación desde esa fuente, o con `adb install -r app-release.apk`.

---

## Documentación adicional

Requerimientos (IEEE 830), historias de usuario, diagramas, plan de pruebas, seguridad y actas de las ceremonias Scrum están en la [Wiki del repositorio](https://github.com/AMMAIA-2026/SangreYa/wiki). El seguimiento de tareas está en el tablero Kanban del proyecto de GitHub.

---

## Flujo de trabajo

- Ramas: `main` (entrega) ← `develop` ← `feature/<nº de tarea>-<descripción>` (por ejemplo `feature/009-contact-activity`); correcciones en `fix/BUG-NNN-descripcion`.
- Los cambios entran a `develop` por Pull Request y luego se integran a `main`. Solo lo que está en `main` se entrega.

---

> Grupo AMMAIA - 2026
> Tecnicatura Superior en Desarrollo Web y Aplicaciones Digitales. Cohorte 2025
> ISPC
