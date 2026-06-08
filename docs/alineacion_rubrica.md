# Alineacion con Rubrica - App Android IEP Continental Americano

## Enfoque tecnologico

- Cliente movil principal desarrollado en Android nativo con Java.
- No se depende de dashboard web: el rol `administrativo` centraliza las funciones de administracion dentro de la APK.
- Comunicacion con backend Java Spring Boot mediante Retrofit + OkHttp.

## Arquitectura

- `view`: Activities, Fragments y adapters.
- `controller`: coordinacion de pantallas y acciones.
- `dao`: contratos Retrofit, equivalente DAO para consumo de API.
- `model`: entidades y DTOs de la app.
- `service`: GPS, Firebase Messaging y procesos de fondo.
- `config`: Retrofit, sesion, constantes y clase `Application`.
- `util`: utilidades de fechas, calificaciones y UI.

## Recursos Java requeridos

- Google Guava: dependencia Android para utilidades Java.
- Apache Commons: `commons-lang3` y `commons-io`.
- Logback/SLF4J: logging movil con `logback-android`.
- Testing: JUnit + Mockito.

## Funcionalidades alineadas

- Administrador: gestion de estudiantes, docentes, padres, cursos, eventos, notificaciones, reportes y justificaciones.
- Docente: asistencia, calificaciones, evaluaciones, cursos y mensajes.
- Padre: dashboard, hijos, notas, asistencia, justificaciones, mensajes y notificaciones.
- Estudiante: dashboard, notas, asistencia, calendario y GPS.
- Seguridad: JWT, sesion local, HTTPS/API preparada y roles.
- GPS: servicio nativo con Google Play Services.
- Push notifications: Firebase Cloud Messaging.

## Pruebas

- `GradeUtilsTest`
- `SessionManagerTest`

## Despliegue

- APK debug/release generado con Gradle.
- Pruebas en emulador Pixel 9.
- Backend local desde emulador: `http://10.0.2.2:5000/api/`.
