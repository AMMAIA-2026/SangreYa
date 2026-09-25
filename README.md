# SangreYa Android

## Descripción del proyecto

SangreYa es una aplicación móvil Android nativa que centraliza información sobre campañas de donación voluntaria de sangre. Permite consultar campañas, fechas, ubicaciones, cupos y centros de salud asociados, además de registrarse, iniciar sesión e inscribirse a una campaña.

Está dirigida a la comunidad en general (donantes potenciales) y a instituciones de salud que organizan campañas de donación.

El proyecto es la continuación mobile del [Proyecto Integrador II (SPA Angular + Django)](https://github.com/AMMAIA-2026/Proyecto2026), desarrollado por el mismo equipo, reutilizando el backend Django REST existente con una interfaz 100% nativa nueva.

Se desarrolla en el marco de la materia **Programación de Aplicaciones Móviles**, ISPC — Tecnicatura Superior en Desarrollo Web y Aplicaciones Digitales.

---

## Tecnologías utilizadas

**App móvil**
- Java 17
- Android Studio
- Android SDK (Activities, ConstraintLayout)

**Backend** (reutilizado del proyecto anterior)
- Python + Django + Django REST Framework
- MySQL
- Autenticación JWT

---

## Integrantes del equipo

| Integrante | Rol |
|---|---|
| Abigail Picone | Scrum Master + Desarrollo |
| Astrid Luppi | Desarrollo |
| Marcela Villanueva | Desarrollo |
| Irina Pirles | Desarrollo |
| Melina Belén Bruvera | Desarrollo |
| Guillermo Mauricio Diván | Desarrollo |

Metodología: **Scrum**, sin roles fijos de desarrollo — todo el equipo trabaja en frontend, backend y testing según el sprint.

---

## Funcionalidades implementadas

- Splash / pantalla de bienvenida
- Registro de usuarios
- Inicio de sesión
- Recuperar contraseña
- Listado y detalle de campañas
- Inscripción a campañas
- Sección "Sobre nosotros"
- Formulario de contacto
- Panel administrativo: campañas, usuarios, mensajes, dashboard

---

## Instalación

1. Cloná este repositorio.
2. Abrilo en Android Studio.
3. El backend de SangreYa ya está deployado en `https://sangreyaispc.pythonanywhere.com/`. Si preferís correrlo local, seguí las instrucciones en el [repositorio del backend](https://github.com/AMMAIA-2026/Proyecto2026).
4. Conectá un dispositivo o emulador y ejecutá la app (▶).

---

## Entrega y release

- **Backend en producción:** `https://sangreyaispc.pythonanywhere.com/`
- **APK / release:** _pendiente de publicar (ver TK-80)_
- **Video demo:** _pendiente de grabar (ver TK-79)_

---

## Documentación adicional

Requerimientos, historias de usuario, diagramas y actas de reunión están disponibles en la [Wiki del repositorio](https://github.com/AMMAIA-2026/SangreYa/wiki).

---

> Este documento se actualiza a medida que se agregan nuevas funcionalidades.

