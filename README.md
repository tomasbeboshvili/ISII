# Cervezas – Backend Ciclo 1

Backend Spring Boot que cubre los requisitos funcionales del Ciclo 1 (RQ1-RQ8, RQ21-RQ24) de la red social de cerveza, incluyendo la gestión de galardones descrita en el conteo de Puntos de Función.

## Requisitos implementados

| RQ | Descripción | Endpoint/Servicio |
|----|-------------|-------------------|
| RQ1 | Verificar edad | `AgeVerificationService` durante registro |
| RQ2-RQ5 | Registro y datos personales (activación automática) | `POST /api/auth/register` |
| RQ6 | Iniciar sesión | `POST /api/auth/login` (token de sesión `X-Auth-Token`) |
| RQ7 | Recuperar contraseña | `POST /api/auth/password/recover`, `POST /api/auth/password/reset` |
| RQ8 | Menú de interacción | `GET /api/menu` |
| RQ21 | Registrar degustación | `POST /api/tastings` |
| RQ22 | Consultar información de cerveza | `GET /api/beers`, `GET /api/beers/{id}` |
| RQ23 | Alta de cerveza | `POST /api/beers` |
| RQ24 | Valorar cerveza | `POST /api/beers/rate` |
| Galardones (ILF) | Definición, consulta y asignación manual/automática según puntos de gamificación | `POST /api/achievements`, `GET /api/achievements`, `POST /api/achievements/{id}/claim` |

## Modelos principales

- `User` (ILF Usuario): datos personales ampliados (nombre, apellidos, foto, origen, intro, ubicación, género, país, bio, puntos de gamificación y galardón actual) y campos de control (tokens de recuperación, contador de logros).
- `Beer` (ILF Cerveza): estilos, origen, creador y métricas.
- `Tasting` (ILF Degustación): notas y puntuaciones organolépticas.
- `Achievement` (ILF Galardones): definición del galardón, niveles, criterios y umbrales de desbloqueo.
- `BeerRating`: valoración única por usuario y cerveza.
- `SessionToken`: gestiona sesiones tipo token en cabecera `X-Auth-Token`.

## Servicios expuestos

### Autenticación (`/api/auth`)
- `POST /register`: crea cuenta (verifica edad y unicidad de email), activa automáticamente y envía un correo de bienvenida simulado.
- `POST /login`: devuelve token de sesión y lo persiste en `session_tokens`.
- `POST /logout`: invalida token.
- `POST /password/recover`: genera un código único y lo envía por correo simulado.
- `POST /password/reset`: restablece contraseña introduciendo el código recibido.

### Usuario (`/api/users/me`)
- `GET`: obtiene perfil completo.
- `PUT`: actualiza nombre, ciudad, país y bio.

### Cervezas (`/api/beers`)
- `GET`: lista todas con rating medio y nº de valoraciones.
- `GET /{id}`: detalle.
- `POST`: alta (requiere token).
- `POST /rate`: crea/actualiza valoración del usuario (puntuaciones 1-10, media en `double`).

### Degustaciones (`/api/tastings`)
- `POST`: registra degustación de una cerveza.
- `GET /me`: degustaciones del usuario autenticado.
- `GET /beer/{beerId}`: degustaciones públicas de una cerveza.

### Galardones (`/api/achievements`)
- `GET`: consulta la librería de galardones disponibles.
- `POST`: crea nuevos galardones (para escenarios administrativos).
- `POST /{id}/claim`: permite que un usuario autenticado asigne un galardón concreto a su perfil.
- `GET /user`: devuelve los galardones que el usuario ya ha desbloqueado (automáticos por degustaciones/valoraciones/altas o manuales).

Cada degustación, valoración o alta de cerveza suma puntos de gamificación; la lógica de `AchievementService` asigna automáticamente el galardón cuyo umbral se cumpla tras cada acción, manteniendo sincronizados `id_galardón_actual`, `nivel_galardón` y `puntos_gamificación` del usuario.

## Ejecución local

```bash
./mvnw spring-boot:run
```

> El proyecto usa H2 en fichero (`jdbc:h2:file:./database/cervezasdb`) con actualización automática de esquema (`spring.jpa.hibernate.ddl-auto=update`). La consola está disponible en `/h2-console` y conserva los datos mientras no elimines la carpeta `database/`.

## Tests

Se añadieron pruebas unitarias con Mockito/JUnit para todos los servicios clave:

- `AgeVerificationServiceTest`, `AuthServiceTest`, `TokenAuthenticationServiceTest`
- `UserProfileServiceTest`, `MenuServiceTest`
- `BeerServiceTest`, `TastingServiceTest`, `AchievementServiceTest`

> La descarga inicial del wrapper Maven requiere acceso a Internet; en este entorno restringido se debe reutilizar una caché local o descargar las dependencias manualmente antes de ejecutar `./mvnw test`.
# ISII

## Frontend ligero (incluido en `src/main/resources/static`)

- `index.html`: página de inicio y navegación hacia los módulos.
- `auth.html`, `profile.html`, `beers.html`, `tastings.html`, `achievements.html`: vistas separadas para cada caso de uso del ciclo 1.
- `beers.html`: catálogo completo con buscador, paginación (15 ítems) y modales flotantes para añadir/valorar; cada fila incluye acciones para valorar o eliminar la cerveza.
- `tastings.html`: listado con filtros/paginación para degustaciones propias o por cerveza y formulario modal para registrar nuevas catas.
- `achievements.html`: panel del menú contextual y galardones con botón de refresco y modal para reclamar logros. También lista los logros automáticos (degustar X, valorar X, añadir X) ya desbloqueados.
- `app.js`: gestiona peticiones `fetch` hacia los endpoints REST, guarda el token en `localStorage` y añade listeners solo donde corresponda.
- `styles.css`: estilo básico responsive compartido.
- `EmailService`: servicio auxiliar que simula los correos de bienvenida y recuperación registrándolos en los logs.

Arranca con `./mvnw spring-boot:run` y visita `http://localhost:8080/` para acceder a cada página. El token de sesión se comparte automáticamente entre vistas mediante `localStorage`.
