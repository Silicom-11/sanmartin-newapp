# IEP Continental Americano - App Android

Aplicacion movil nativa desarrollada en Java para la IEP Continental Americano.

## Alcance

- Autenticacion por roles.
- Panel administrador/director.
- Gestion de estudiantes, docentes y padres.
- Registro y consulta de asistencia.
- Registro y consulta de calificaciones.
- Justificaciones.
- Ubicacion GPS.
- Calendario, notificaciones y reportes institucionales.

## Arquitectura

- `view/activity` y `view/fragment`: vistas Android.
- `controller`: controladores de validacion y flujo.
- `dao`: contratos Retrofit para consumo del backend.
- `model`: entidades y DTO usados por la app.
- `service`: servicios moviles como GPS y FCM.

## Rubrica

El proyecto evidencia MVC, DAO, TDD, SOLID, Apache Commons, Logback, pruebas unitarias y control de versiones.
