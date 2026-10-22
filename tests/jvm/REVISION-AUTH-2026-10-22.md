# Validación JVM — login, perfil y renovación de sesión

## Alcance y resultado

- Fecha: **22/10/2026**.
- Código validado: commit `14d350a`, rama local `fix/SYM-037-Login-session-tests-and-testing-docs`.
- Entorno: Windows, JDK 17 (Corretto 17.0.18), Gradle Wrapper 8.0.
- **Debug: 46 tests, 0 fallos, 0 errores, 0 omitidos.**
- **Release: 46 tests, 0 fallos, 0 errores, 0 omitidos.**
- Reportes generados: `app/build/reports/tests/testDebugUnitTest/` y
  `app/build/reports/tests/testReleaseUnitTest/`; XML en `app/build/test-results/`.

Se validó la suite JVM completa para comprobar las modificaciones. Los estados de
la planilla y la cobertura JaCoCo publicada se conservan con sus referencias originales.
Los HTML/XML de `reportes/` siguen siendo los originales de cierre del 09/10.

## Conteos, sin mezclar inventario con evidencia histórica

| Referencia | Unitarios sin MockWebServer | Con MockWebServer | Total por variante |
|---|---:|---:|---:|
| Evidencia histórica del 09/10 | 24 | 5 | 29 |
| Código base revisado (`e3bc963`) | 24 | 9 | 33 |
| Rama de esta revisión (`14d350a`) | 29 | 17 | 46 |

Los cuatro métodos adicionales ya presentes en `CampaignApiTest` explican la
diferencia entre el inventario base y el reporte histórico. Esta revisión agrega
13 métodos: tres de login, dos de perfil y ocho del authenticator. Contar métodos
no equivale a contar casos de la planilla ni demuestra cobertura exhaustiva.

## Trazabilidad de la ampliación

Se amplían escenarios de casos existentes; no se asignan nuevos números TC ni bugs.
Los prefijos de commit corresponden a las TK reales del IEEE830.

| Caso / requisito | Clase y método | Oráculo | TK |
|---|---|---|---|
| TC-UNIT-12; RF-02 / HU-02 / CA-05 / RNF-04 | `LoginViewModelTest#loginExitosoPersisteSesionYNotificaResultado` | Guarda la respuesta y notifica éxito, sin error ni limpieza | TK-37 |
| TC-UNIT-12; RF-02 / HU-02 / CA-06 / RNF-04 | `LoginViewModelTest#login401LimpiaSesionYNotificaErrorHttp` | Notifica 401, limpia sesión, no guarda ni notifica éxito | TK-37 |
| TC-UNIT-12; RF-02 / HU-02; variante de transporte de TC-AUTH-18 | `LoginViewModelTest#loginSinRedNotificaFalloSinModificarSesion` | Propaga el fallo, sin modificar sesión ni emitir éxito | TK-37 |
| TC-UNIT-05; RF-15 / HU-11 | `ProfileViewModelTest#error400ConservaFormularioSinActualizarSesion` | HTTP 400, carga finalizada, conserva formulario, no actualiza nombre | TK-58 |
| TC-UNIT-05; RF-15 / HU-11; apoyo a TC-PERF-21 | `ProfileViewModelTest#errorDeRedConservaFormularioYPermiteReintentar` | Conserva formulario, termina carga, permite nuevo intento exitoso | TK-58 |
| TC-AUTH-40; RF-03 / HU-03 / CA-09 / RNF-04 | `TokenAuthenticatorTest#renuevaTokensYReintentaUnaVezConAccessNuevo` | 401 → refresh 200 → recurso 200; rota tokens y siguiente petición usa access nuevo | TK-39 |
| TC-AUTH-40; mismos requisitos | `TokenAuthenticatorTest#segundo401DetieneReintentos` | Solo tres requests: recurso, refresh, recurso; sin bucle | TK-39 |
| TC-AUTH-41; RF-03 / HU-03 / CA-10 / RNF-04 | `TokenAuthenticatorTest#refresh401SolicitaExpiracionSinReintentarRecurso` | Refresh 401: una orden de expiración, sin repetir recurso | TK-40 |
| TC-AUTH-41; mismos requisitos | `TokenAuthenticatorTest#refresh400SolicitaExpiracionSinReintentarRecurso` | Refresh 400: una orden de expiración, sin repetir recurso | TK-40 |
| TC-AUTH-40/41; RNF-04 | `TokenAuthenticatorTest#errorDeRedEnRefreshConservaSesion` | Fallo de transporte inyectado: no expira ni cambia tokens | TK-39 |
| TC-AUTH-40; RNF-04 | `TokenAuthenticatorTest#sinRefreshDevuelve401SinRenovar` | Una petición protegida, sin refresh ni reintento | TK-39 |
| TC-AUTH-40; RNF-04 | `TokenAuthenticatorTest#reutilizaAccessYaActualizadoSinOtroRefresh` | Reutiliza token actualizado sin pedir otra renovación | TK-39 |
| TC-AUTH-40; RNF-04, variante negativa | `TokenAuthenticatorTest#recurso400NoRenuevaNiExpiraSesion` | Un 400 del recurso no dispara refresh ni limpia sesión | TK-39 |

El almacenamiento de sesión se modela en memoria. Las pruebas comprueban el
authenticator y el intercambio HTTP real en loopback. Las pruebas de ViewModel
comprueban callbacks, estados de carga, conservación de formulario y reintento.

## Reproducción

Desde la raíz, con JDK 17:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest --console=plain --no-daemon
```

Para ejecutar solo las tres clases modificadas, ejecutar **un comando por variante**:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests '*LoginViewModelTest' --tests '*ProfileViewModelTest' --tests '*TokenAuthenticatorTest' --console=plain
.\gradlew.bat :app:testReleaseUnitTest --tests '*LoginViewModelTest' --tests '*ProfileViewModelTest' --tests '*TokenAuthenticatorTest' --console=plain
```

Cada selección contiene 15 métodos (4 de login, 3 de perfil y 8 del authenticator).
MockWebServer usa un puerto efímero local, bloquea destinos externos, limita tiempos
de llamada y cierra sus recursos. No requiere credenciales ni datos de Deploy QA.
