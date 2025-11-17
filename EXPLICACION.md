# EXPLICACIÓN DETALLADA – BeerSP Backend (Ciclo 1)

Este documento complementa el `README.md` con una descripción extensa del backend implementado para el Ciclo 1 del sistema BeerSP. Se cubren los requisitos seleccionados para este ciclo, su relación con el conteo de Puntos de Función (PF) y los componentes técnicos desarrollados en Spring Boot.

---

## 1. Alcance funcional del Ciclo 1

El sistema BeerSP se planificó en dos ciclos, priorizando en C1 las funciones esenciales para operar la red social:

| Requisito | Descripción | PF asociados |
|-----------|-------------|--------------|
| RQ1 | Verificación de edad mediante `AgeVerificationService`. | ILF Usuario (10 PF) |
| RQ2-RQ5 | Registro y datos personales extendidos (activación automática). | EI Registro (4 PF) |
| RQ6 | Inicio de sesión con tokens de sesión (`SessionToken`). | EQ Login (3 PF) |
| RQ7 | Recuperación de contraseña con tokens de un solo uso. | EQ Recuperar (3 PF) |
| RQ8 | Menú de interacción contextual (`MenuService`). | EO Feed Menú (4 PF) |
| RQ21 | Registro de degustaciones. | ILF Degustación (10 PF) + EI Alta Degustación (6 PF) |
| RQ22 | Consulta de info de cervezas. | ILF Cerveza (10 PF) |
| RQ23 | Alta de cervezas. | EI Alta cerveza (4 PF) |
| RQ24 | Valoración de cervezas. | EO Promedio cerveza (4 PF) |
| Galardones | Gestión del ILF “Galardón” y asignación automática según gamificación. | ILF Galardones (10 PF) |

Total PF del ciclo: aprox. 63 PF, alineados con el plan presentado (aunque 4 PF por galardones quedan parcialmente preparados para C2 al extender la librería de premios).

---

## 2. Modelos y persistencia

### 2.1 Usuario (`User`)
- Atributos: email, `username`, nombre, apellidos, intro, ciudad, país, bio.
- Datos de control: `activated`, tokens de recuperación de contraseña y timestamps de control.
- Gamificación: `gamificationPoints`, `badgeLevel`, `currentAchievementId`.
- JPA: tabla `users`, timestamps gestionados con `@PrePersist/@PreUpdate`.

### 2.2 Cervezas (`Beer`)
- Campos de ficha: nombre, estilo, cervecera, país de origen, ABV, IBU, descripción.
- Relación con `User` (creador) y `BeerRating`.

### 2.3 Degustaciones (`Tasting`)
- Relación N:1 con usuario y cerveza.
- Datos de cata: fecha, localización, notas, aroma/flavor/appearance.
- Cada registro suma puntos al usuario.

### 2.4 Valoraciones (`BeerRating`)
- Score 1-10 y comentario opcional.
- Restricción única por usuario+cerveza.

### 2.5 Galardones (`Achievement`)
- Nombre, temática, imagen, nivel, criterio y umbral de puntos.
- Repositorio `AchievementRepository` permite determinar el galardón que corresponde a un usuario según sus puntos (`findFirstByThresholdLessThanEqualOrderByThresholdDesc`).

### 2.6 Tokens de sesión (`SessionToken`)
- Gestionan las sesiones “stateless” mediante el header `X-Auth-Token`.

---

## 3. Servicios y lógica de negocio

### 3.1 Autenticación (`AuthService`)
1. **Registro**: valida email y username únicos, verifica edad y activa automáticamente la cuenta; se envía un correo de bienvenida simulado (solo informativo).
2. **Activación (compatibilidad)**: el endpoint `/activate` permanece expuesto pero únicamente informa de que la activación manual ya no es necesaria.
3. **Login**: valida credenciales, emite `SessionToken` con duración de 12h.
4. **Recuperación**: genera token temporal de 30 min para restablecer contraseña.
5. **Reset**: comprueba token y actualiza la contraseña.

### 3.2 TokenAuthenticationService
- Resuelve usuarios a partir del token, verificando que la sesión esté activa y sin expirar.

### 3.3 UserProfileService
- `requireUser`: centraliza el control de acceso.
- `getProfile`/`updateProfile`: exponen todos los atributos relevantes del ILF Usuario, incluyendo los campos de gamificación y galardón.

### 3.4 MenuService
- Construye el menú dinámico (RQ8) en función del estado del usuario (activado, perfil completo).

### 3.5 BeerService
- Alta de cervezas: suma 15 puntos de gamificación y dispara la evaluación de galardón.
- Listado/detalle: aporta promedio de valoraciones (`BeerRatingRepository`).
- Valoraciones: crea/actualiza la puntuación del usuario, suma 5 puntos.

### 3.6 TastingService
- Registro de degustaciones: crea la entidad y suma 10 puntos al usuario.
- Consultas: catas propias (`/me`) y por cerveza.

### 3.7 AchievementService
- CRUD básico de galardones y asignación manual (`/claim`).
- `refreshProgress(User)`: se ejecuta tras altas/valoraciones/degustaciones para asignar automáticamente el galardón cuyo umbral se cumple; persiste el cambio en `User`.

---

## 4. API REST

| Recurso | Endpoints | Notas |
|---------|-----------|-------|
| Autenticación | `POST /api/auth/register`, `/activate (compat)`, `/login`, `/logout`, `/password/recover`, `/password/reset` | Usa DTOs validados con `jakarta.validation`. |
| Perfil | `GET /api/users/me`, `PUT /api/users/me` | Requiere `X-Auth-Token`. |
| Cervezas | `GET /api/beers`, `GET /api/beers/{id}`, `POST /api/beers`, `POST /api/beers/rate` | Las operaciones de escritura dependen de token. |
| Degustaciones | `POST /api/tastings`, `GET /api/tastings/me`, `GET /api/tastings/beer/{beerId}` | Registro incrementa puntos. |
| Menú | `GET /api/menu` | Puede recibir token opcional. |
| Galardones | `GET /api/achievements`, `POST /api/achievements`, `POST /api/achievements/{id}/claim` | `POST` pensado para admins; `claim` requiere token. |

Todas las respuestas de error se canalizan mediante `GlobalExceptionHandler`, devolviendo mensajes en castellano para errores de validación, faltas de autorización o entidades inexistentes.

---

## 5. Gamificación y galardones

1. **Puntos**:
   - Alta de cerveza: +15.
   - Degustación: +10.
   - Valoración: +5.
2. **Asignación automática**: tras cada acción se invoca `AchievementService.refreshProgress` para consultar el galardón de mayor umbral <= puntos actuales.
3. **Asignación manual**: `POST /api/achievements/{id}/claim` permite aplicar un galardón concreto (por ejemplo, para homologar logros anteriores).

Estos procesos mantienen sincronizados los campos `gamificationPoints`, `badgeLevel` e `currentAchievementId` del usuario.

---

## 6. Configuración técnica

- **Stack**: Spring Boot 3.5, Spring Data JPA, H2 (memoria), Spring Web, Validation, Security Crypto, Spring Session JDBC.
- **Datasource**: H2 en fichero (`jdbc:h2:file:./database/cervezasdb`, auto-servidor activado) con consola en `/h2-console`. El contenido persiste entre reinicios.
- **Security**: No se ha configurado Spring Security completo; el control se realiza con tokens personalizados.
- **Construcción**: Maven Wrapper (`./mvnw`). En este entorno el wrapper no puede descargarse por restricciones de red, por lo que las pruebas deben ejecutarse cuando se disponga de conexión o caché local.

---

## 7. Pruebas unitarias

Ubicadas en `src/test/java/es/upm/cervezas/service/`:

- `AgeVerificationServiceTest`
- `AuthServiceTest`
- `TokenAuthenticationServiceTest`
- `UserProfileServiceTest`
- `MenuServiceTest`
- `BeerServiceTest`
- `TastingServiceTest`
- `AchievementServiceTest`

Cubren flujos felices y errores (ej. usuario no activado, token caducado, asignaciones de galardones). Para ejecutarlas:

```bash
MAVEN_USER_HOME=./.m2 ./mvnw test
```

> Nota: necesita acceso a Maven Central para descargar el wrapper y dependencias. Si el entorno está restringido se debe usar una caché previa.

### 7.1 Matriz solicitada de casos de prueba

Para la memoria se detalla la división pedida (6 días de trabajo) indicando que todos los casos comparten un set de 10 datos simulados (usuarios, cervezas y degustaciones) y el número de elementos cubiertos por cada caso.

| Tipo | Caso | Datos de prueba | Elementos | Elementos de prueba |
|------|------|-----------------|-----------|----------------------|
| Pruebas Unitarias | Caso Prueba 1 | 10 | 4 | <ol><li>`AgeVerificationServiceTest.allowsAdults` confirma que una fecha ≥18 años no lanza error.</li><li>`AgeVerificationServiceTest.rejectsMinors` garantiza la excepción con menores.</li><li>`TokenAuthenticationServiceTest.returnsUserWhenTokenValid` recupera al usuario asociado al token activo.</li><li>`TokenAuthenticationServiceTest.returnsEmptyForUnknownToken/returnsEmptyWhenExpired` comprueban los rechazos ante tokens inválidos o caducados.</li></ol> |
| Pruebas Unitarias | Caso Prueba 2 | 10 | 6 | <ol><li>`AuthServiceTest.registerCreatesActiveUser` valida el alta con verificación de edad, activación automática y envío de correo.</li><li>`AuthServiceTest.activateEndpointIsNoOp` confirma que el endpoint responde informando que ya no es necesario activar.</li><li>`AuthServiceTest.loginGeneratesSession` crea `SessionToken` y devuelve credenciales al front.</li><li>`AuthServiceTest.loginFailsIfNotActivated` cubre el caso defensivo ante usuarios deshabilitados.</li><li>`AuthServiceTest.passwordRecoverySilentWhenUnknownEmail` responde sin filtrar información sensible.</li><li>`AuthServiceTest.resetPasswordFailsOnExpiredToken` bloquea tokens caducados de recuperación.</li></ol> |
| Pruebas Unitarias | Caso Prueba 3 | 10 | 12 | <ol><li>`UserProfileServiceTest.requireUserThrowsWhenTokenInvalid` centraliza los 401.</li><li>`UserProfileServiceTest.profileResponseContainsUserData` devuelve los campos del ILF Usuario.</li><li>`UserProfileServiceTest.updateProfileMutatesEntity` actualiza bio, ciudad y país.</li><li>`MenuServiceTest` valida menú anónimo y menú avanzado según el estado del usuario.</li><li>`BeerServiceTest.createBeerPersistsData` guarda la cerveza, suma puntos y dispara hitos.</li><li>`BeerServiceTest.getBeerThrowsWhenMissing` lanza `EntityNotFoundException` en consultas inexistentes.</li><li>`BeerServiceTest.listReturnsRatings` agrega promedio y contador de valoraciones.</li><li>`BeerServiceTest.rateBeerCreatesOrUpdatesRating` asegura que cada usuario tenga una única valoración.</li><li>`TastingServiceTest.createPersistsTasting` enlaza usuario, cerveza y puntos de gamificación.</li><li>`TastingServiceTest.listsForUser` expone degustaciones propias.</li><li>`TastingServiceTest.listsForBeer` permite revisar degustaciones públicas por cerveza.</li><li>`AchievementServiceTest` cubre creación, asignación manual y `refreshProgress` automático.</li></ol> |
| Pruebas del Sistema | Caso Prueba 4 | 10 | 22 | <ol><li>`index.html` carga y enlaza todas las vistas.</li><li>`auth.html` impide registrar menores verificando `AgeVerificationService`.</li><li>Registro exitoso informa que la cuenta está activa sin requerir token.</li><li>Login correcto guarda el token en `localStorage`.</li><li>Inicio de recuperación de contraseña dispara el correo simulado.</li><li>`password-reset` completa el cambio con el código recibido.</li><li>Menú anónimo ofrece acciones de registro.</li><li>Menú autenticado muestra acciones avanzadas (degustar, crear cerveza).</li><li>`profile.html` obtiene el perfil vía `GET /api/users/me`.</li><li>`profile.html` persiste cambios con `PUT /api/users/me`.</li><li>`beers.html` da de alta cervezas (`POST /api/beers`).</li><li>Listado de cervezas (`GET /api/beers`) refleja media y nº de valoraciones.</li><li>Detalle (`GET /api/beers/{id}`) abre la ficha individual.</li><li>Valorar (`POST /api/beers/rate`) actualiza la media y bloquea duplicados.</li><li>`tastings.html` registra nuevas catas (`POST /api/tastings`).</li><li>Listado de degustaciones del usuario (`GET /api/tastings/me`).</li><li>Listado por cerveza (`GET /api/tastings/beer/{id}`).</li><li>`achievements.html` lista la librería (`GET /api/achievements`).</li><li>`POST /api/achievements/{id}/claim` asigna logros manuales.</li><li>La UI verifica logros automáticos tras crear, valorar o degustar.</li><li>Expiración o logout borra el token y fuerza autenticación de nuevo.</li></ol> |

---

## 8. Flujo de uso recomendado

1. **Registro** → se valida edad y unicidad de email/username; la cuenta queda activa inmediatamente.
2. **Login** → genera `X-Auth-Token`.
3. **Completar perfil** → `PUT /api/users/me`.
4. **Alta de cervezas** y **degustaciones** → incrementan puntos de gamificación.
5. **Valoraciones** → reflejan promedios públicos y también suman puntos.
6. **Consulta de galardones / claim** → permite ver el progreso y aplicar logros específicos (automáticos/manuales).

Este flujo satisface la funcionalidad mínima del ciclo 1 descrita en el análisis PF, dejando para el ciclo 2 las funciones sociales (amistades, comentarios, locales, feeds avanzados, etc.).

---

## 9. Próximos pasos sugeridos (Ciclo 2)

A partir del backlog inicial, las ampliaciones más prioritarias son:

- ILF Amistad / Solicitudes y sus EI/EQ asociados.
- ILF Local y funciones relacionadas (alta, “me gusta”, mapa con Google Maps API).
- Comentarios y feed de actividad (ILF Comentario + EO Feed).
- Métricas KPI (resumen de perfil, top 3 favoritas, actividad de amigos).

La arquitectura y los servicios presentes ya contemplan parte de estas extensiones (ej. campos de usuario para gamificación), por lo que su incorporación futura será incremental.

---

## 10. Resumen

El backend de BeerSP Ciclo 1 implementa los requisitos esenciales de autenticación, gestión de perfil, catálogo de cervezas, degustaciones y gamificación mediante galardones. Se respeta el conteo de PF planificado, y la base técnica (JPA, DTOs validados, pruebas unitarias) permite evolucionar el sistema en ciclos posteriores sin reescrituras significativas.

---

## 11. Frontend demostrativo

Para facilitar la entrega universitaria se añadió un frontend estático servido por Spring Boot:

- `index.html`: landing con instrucciones y enlaces.
- `auth.html`: registro (activación automática), login y recuperación (RQ1-RQ7).
- `profile.html`: consulta/edición del ILF Usuario.
- `beers.html`: catálogo con buscador, paginación y modales flotantes para alta/valoración; cada ficha permite valorar o eliminar (RQ22-RQ24).
- `tastings.html`: listado filtrable/paginado con modales para registrar nuevas catas o consultar por cerveza (RQ21).
- `achievements.html`: tarjetón del menú contextual y listado de logros (incluye los desbloqueados automáticamente por degustar/valorar/añadir) con modal para reclamarlos (RQ8).
- `app.js`: lógica compartida; activa solo los formularios presentes en cada página y reutiliza el token entre vistas.
- `EmailService`: servicio que simula correos (se visualizan en los logs) para mensajes de bienvenida y códigos de recuperación.

El objetivo es didáctico: no se emplean frameworks, pero cubre todas las operaciones del ciclo con un flujo claro para la defensa/práctica.
