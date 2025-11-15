# Cervezas – Backend Ciclo 1

Backend Spring Boot que cubre los requisitos funcionales del Ciclo 1 (RQ1-RQ8, RQ21-RQ24) de la red social de cerveza.

## Requisitos implementados

| RQ | Descripción | Endpoint/Servicio |
|----|-------------|-------------------|
| RQ1 | Verificar edad | `AgeVerificationService` durante registro |
| RQ2-RQ5 | Registro, datos personales, creación y activación de cuenta | `POST /api/auth/register`, `POST /api/auth/activate` |
| RQ6 | Iniciar sesión | `POST /api/auth/login` (token de sesión `X-Auth-Token`) |
| RQ7 | Recuperar contraseña | `POST /api/auth/password/recover`, `POST /api/auth/password/reset` |
| RQ8 | Menú de interacción | `GET /api/menu` |
| RQ21 | Registrar degustación | `POST /api/tastings` |
| RQ22 | Consultar información de cerveza | `GET /api/beers`, `GET /api/beers/{id}` |
| RQ23 | Alta de cerveza | `POST /api/beers` |
| RQ24 | Valorar cerveza | `POST /api/beers/rate` |

## Modelos principales

- `User` (ILF Usuario): datos personales, estado de activación y tokens de recuperación.
- `Beer` (ILF Cerveza): estilos, origen, creador y métricas.
- `Tasting` (ILF Degustación): notas y puntuaciones organolépticas.
- `BeerRating`: valoración única por usuario y cerveza.
- `SessionToken`: gestiona sesiones tipo token en cabecera `X-Auth-Token`.

## Servicios expuestos

### Autenticación (`/api/auth`)
- `POST /register`: crea cuenta (verifica edad y unicidad de email).
- `POST /activate`: activa mediante token simulado.
- `POST /login`: devuelve token de sesión.
- `POST /logout`: invalida token.
- `POST /password/recover`: inicia flujo de recuperación (token simulado).
- `POST /password/reset`: restablece contraseña con token vigente.

### Usuario (`/api/users/me`)
- `GET`: obtiene perfil completo.
- `PUT`: actualiza nombre, ciudad, país y bio.

### Cervezas (`/api/beers`)
- `GET`: lista todas con rating medio y nº de valoraciones.
- `GET /{id}`: detalle.
- `POST`: alta (requiere token).
- `POST /rate`: crea/actualiza valoración del usuario.

### Degustaciones (`/api/tastings`)
- `POST`: registra degustación de una cerveza.
- `GET /me`: degustaciones del usuario autenticado.
- `GET /beer/{beerId}`: degustaciones públicas de una cerveza.

### Menú (`/api/menu`)
- Da visibilidad del estado de activación, perfil y acciones disponibles según RQ8.

## Ejecución local

```bash
./mvnw spring-boot:run
```

> El proyecto usa H2 en memoria (`spring.jpa.hibernate.ddl-auto=update`) y expone consola en `/h2-console`.

## Tests

Se añadió la infraestructura para pruebas (`spring-boot-starter-test`). La descarga del wrapper Maven requiere acceso a Internet; en este entorno restringido se debe reutilizar una caché local o descargar las dependencias manualmente antes de ejecutar `./mvnw test`.
# ISII
