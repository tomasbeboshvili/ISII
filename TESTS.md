# Estrategia de Pruebas

Este documento recoge la trazabilidad entre requisitos y casos de prueba, el detalle de las pruebas unitarias implementadas (JUnit/Mockito) y las pruebas del sistema ejecutadas sobre la web estática (`src/main/resources/static`). También se incluye el registro de defectos detectados y la gestión de su corrección.

## 1. Trazabilidad requisito ↔ caso de prueba

| RQ | Descripción (según README) | Casos unitarios | Casos del sistema |
|----|----------------------------|-----------------|-------------------|
| RQ1 | Verificación de edad | UT-01 | ST-02 |
| RQ2-RQ5 | Registro y datos personales/activación | UT-02, UT-03 | ST-01 |
| RQ6 | Inicio de sesión con token | UT-04 | ST-03 |
| RQ7 | Recuperar/restablecer contraseña | UT-05 | ST-04 |
| RQ8 | Menú contextual y estado de perfil | UT-08 | ST-05 |
| RQ21 | Registrar degustaciones | UT-11 | ST-08 |
| RQ22 | Consultar información de cerveza | UT-09 | ST-06 |
| RQ23 | Alta de cerveza | UT-09 | ST-06 |
| RQ24 | Valorar cerveza | UT-10 | ST-07 |
| Galardones | Gestión ILF logros | UT-12 | ST-09 |

## 2. Pruebas unitarias (JUnit + Mockito)

| ID | Requisito(s) | Caso / Clase | Procedimiento resumido | Resultado esperado | Resultado real |
|----|--------------|--------------|------------------------|--------------------|----------------|
| UT-01 | RQ1 | `AgeVerificationServiceTest.allowsAdults`, `.rejectsMinors` | Construir fechas ≥18 y <18 años y ejecutar `verifyOrThrow`. | Adultos pasan sin excepción; menores lanzan `IllegalArgumentException`. | PASS – métodos cumplen lo esperado. |
| UT-02 | RQ2-RQ5 | `AuthServiceTest.registerCreatesPendingUser` | Simular `RegistrationRequest`, mocks de repositorios y encoder. | Usuario pendiente con token, email único, contraseña codificada y correo enviado. | PASS – asserts de DTO y mocks verificados. |
| UT-03 | RQ2-RQ5 | `AuthServiceTest.activateMarksUser` | Buscar usuario por token y llamar `activate`. | Se limpia el token y se marca `activated=true`. | PASS. |
| UT-04 | RQ6 | `AuthServiceTest.loginGeneratesSession`, `loginFailsIfNotActivated` | Login con credenciales válidas/usuario inactivo. | Caso feliz genera `SessionToken`; caso inactivo lanza `IllegalStateException`. | PASS – cubre 200 y 409/403. |
| UT-05 | RQ7 | `AuthServiceTest.passwordRecoverySilentWhenUnknownEmail`, `resetPasswordFailsOnExpiredToken` | Recuperación con email inexistente y reset con token caducado. | Respuesta silenciosa (token nulo) y excepción por expiración. | PASS. |
| UT-06 | RQ6 (token) | `TokenAuthenticationServiceTest` | Consultar repositorio por token válido, inválido y caducado. | Usuario presente solo si token existe y no expira. | PASS. |
| UT-07 | RQ2-RQ5 | `UserProfileServiceTest.profileResponseContainsUserData`, `.updateProfileMutatesEntity` | Obtener perfil y actualizar campos desde DTO. | DTO devuelve datos completos; entidad se actualiza en memoria. | PASS. |
| UT-08 | RQ8 | `MenuServiceTest` | Construir menú para anónimo y usuario activado con perfil completo. | Menú anónimo ofrece acciones “Registrarse/Entrar”; menú autenticado agrega acciones de degustación y creación. | PASS. |
| UT-09 | RQ22-RQ23 | `BeerServiceTest.createBeerPersistsData`, `.listReturnsRatings`, `.getBeerThrowsWhenMissing` | Crear cerveza con usuario autenticado y mocks; listar y buscar inexistentes. | Persistencia correcta, cálculo de promedios y excepción `EntityNotFoundException` cuando corresponde. | PASS. |
| UT-10 | RQ24 | `BeerServiceTest.rateBeerCreatesOrUpdatesRating` | Valorar cerveza nueva, guardando `BeerRating` único por usuario. | Se persiste rating, devuelve DTO y dispara logros/puntos. | PASS. |
| UT-11 | RQ21 | `TastingServiceTest.createPersistsTasting`, `.listsForUser`, `.listsForBeer` | Crear degustación y consultar listas. | Guardado con referencias correctas y DTOs filtrados. | PASS. |
| UT-12 | Galardones | `AchievementServiceTest.createPersistsAchievement`, `.assignToCurrentUser`, `.refreshProgressChoosesBestAchievement` | Crear logros, asignar manualmente y refrescar automático según puntos. | DTO correcto, usuario actualizado y galardón más alto asignado. | PASS. |

## 3. Pruebas del sistema (frontend estático + API)

Se ejecutan manualmente tras `./mvnw spring-boot:run` utilizando los formularios HTML. Cada caso se documenta con datos de entrada, pasos, resultados esperados y observados.

| ID | Requisito(s) | Pasos (UI / API) | Resultado esperado | Resultado real |
|----|--------------|------------------|--------------------|----------------|
| ST-01 | RQ2-RQ5 | Abrir `auth.html`, rellenar registro con datos válidos y contraseñas iguales. | Respuesta 200, mensaje “Registro correcto”, usuario en H2 con `activated=false`, token mostrado. | PASS – usuario creado y correo simulado en logs. |
| ST-02 | RQ1 | En `auth.html`, introducir fecha <18 años y enviar formulario. | API rechaza con 400 y mensaje “Debes ser mayor de edad”. | PASS – formulario muestra alerta. |
| ST-03 | RQ6 | Tras activar con token (POST `/api/auth/activate`), iniciar sesión. | Se guarda `X-Auth-Token` en `localStorage`, backend retorna 200. | PASS – botón “Cerrar sesión” elimina token y `/logout` desactiva sesión. |
| ST-04 | RQ7 | Solicitar recuperación, revisar token en logs y completar `password-reset` con nueva contraseña. | Endpoint devuelve 200, token queda invalidado y se puede hacer login con la nueva contraseña. | PASS. |
| ST-05 | RQ8 | Acceder a `profile.html` con token. | Menú muestra acciones avanzadas (“Crear degustación”, “Añadir cerveza”) y formulario precargado. | PASS – cambios guardados via `PUT /api/users/me`. |
| ST-06 | RQ22-RQ23 | Desde `beers.html`, crear cerveza y refrescar listado. | Nueva entrada aparece con datos completos y contador de valoraciones 0. | PASS – `GET /api/beers` refleja promedio 0.0, `CreatedBy` correcto. |
| ST-07 | RQ24 | Seleccionar cerveza creada y valorar (score 8). | API devuelve 200, listado actualiza promedio y bloquea nueva valoración (solo update). | PASS tras corregir defecto DF-01 (ver sección 4). |
| ST-08 | RQ21 | En `tastings.html`, registrar degustación y ver pestaña “Mis degustaciones”. | Tabla muestra nueva degustación con fecha/lugar, puntos de gamificación incrementan y aparecen en logros. | PASS. |
| ST-09 | Galardones | En `achievements.html`, pulsar “Refrescar logros” y reclamar uno manual. | Logros automáticos aparecen en verde, botón “Reclamar” asigna manual y panel muestra nivel actual. | PASS. |

## 4. Registro y gestión de defectos

| ID | Descripción | Corrección | Estado |
|----|-------------|------------|--------|
| DF-01 | Al valorar una cerveza desde la web, si el usuario no tenía valoraciones previas el frontend mostraba “Error 500” por no enviar el token. | Se añadió `attachAuthHeaders()` al formulario de valoración en `app.js` (commit local previo) y se repitió ST-07. | Cerrado (verificado). |
| DF-02 | La media en la zona de cervezas hacía un cálculo incorrecto al dividir solo por valoraciones nuevas. | Se ajustó `BeerRatingRepository.findAverageScoreByBeerId` para contar todas las valoraciones persistidas y se normalizó la presentación en `beers.html` tras refrescar la lista. | Cerrado (verificado). |
| DF-03 | En recuperación de contraseña el correo no se enviaba si el usuario existía. | Se aseguró la llamada a `EmailService.send()` tras generar el token en `AuthService.startPasswordRecovery` y se validó con los logs de pruebas. | Cerrado (verificado). |
| DF-04 | El acceso a la base de datos fallaba intermitentemente por sesiones H2 abiertas en la consola. | Se documentó cerrar la sesión `/h2-console` tras las pruebas y se añadió al README la recomendación de arrancar con `-Dh2.console.settings.web-allow-others=false` para evitar bloqueos. | Cerrado (monitorización). |
| DF-05 | La búsqueda de cervezas se hacía por ID en lugar de por nombre, impidiendo localizar cervezas recién creadas. | Se modificó el buscador de `beers.html`/`app.js` para filtrar client-side por `beer.name.toLowerCase().includes(query)` y se añadió placeholder explicativo. | Cerrado (verificado). |
| DF-06 | Los galardones desbloqueados requerían “reclamar” manualmente aunque el usuario ya hubiera alcanzado el umbral. | `AchievementService.refreshProgress` ahora crea automáticamente la relación `UserAchievement` y actualiza `currentAchievementId`, dejando el botón “Reclamar” solo para galardones manuales. | Cerrado (verificado). |
| DF-07 | No aparecía la lista de galardones no obtenidos, por lo que no se podía ver qué seguía pendiente. | `achievements.html` ahora muestra dos columnas (“Obtenidos”/“Pendientes”) y `app.js` construye ambos listados a partir del mismo `GET /api/achievements`. | Cerrado (verificado). |
| DF-08 | La lista de galardones era confusa al mezclar bloqueados y desbloqueados sin contexto. | Se agregó una leyenda, iconografía y badges de color para diferenciar estados y se ordena por nivel en `achievements.html`. | Cerrado (verificado). |
| DF-09 | El flujo de recuperación de contraseña fallaba al no validar el token antes de mostrar la pantalla. | Se añadió validación previa (`AuthService.validateResetToken`) y mensajes claros en `auth.html`; tras la corrección el restablecimiento pasa el caso ST-04. | Cerrado (verificado). |
| DF-10 | El sistema permitía crear perfiles con emails ya registrados porque la validación solo comprobaba username. | Se agregó la llamada a `userRepository.existsByEmailIgnoreCase` en `AuthService.register` y mensaje específico para la UI. | Cerrado (verificado). |
| DF-11 | El botón “Eliminar” en `beers.html` ejecutaba un borrado completo de la cerveza en lugar de retirar solo la valoración del usuario. | Se cambió la acción para llamar a `DELETE /api/beers/rate/{beerId}` (nuevo handler) y la UI ahora actualiza únicamente la puntuación personal. | Cerrado (verificado). |

No se identificaron otros defectos durante la última ejecución del plan. Cada vez que se corrija un bug se debe actualizar esta tabla con el identificador, requisito afectado, commit o archivo modificado y resultado tras reejecutar los casos pertinentes.

### 4.1 Distribución de defectos reportados

| Descripción del defecto | Defectos | Grave | Leve | JI | OS | A | B |
|-------------------------|----------|-------|------|----|----|---|---|
| La búsqueda de cervezas es por id en vez de por nombre | 1 |  | 1 | 1 | 1 |  |  |
| Los galardones tienen que ser reclamados | 1 | 1 |  | 1 |  |  |  |
| No sale la lista de los galardones no obtenidos | 1 | 1 |  | 1 |  |  |  |
| La lista de galardones era confusa de entender | 1 |  | 1 |  | 1 |  |  |
| Recuperar la contraseña falla | 1 |  | 1 |  | 1 |  |  |
| Se puede crear un perfil con un email ya asignado | 1 | 1 |  |  | 1 |  |  |
| El botón eliminar en la pantalla cervezas elimina la cerveza en vez de la valoración de la cerveza | 1 | 1 |  | 1 |  |  |  |
| **Totales** | **7** | **4** | **3** | **4** | **4** | **4** | **4** |
| **Defectos únicos** |  |  |  |  |  | **3** | **3** |

> Notas: Los datos originales no especificaban a qué defectos concretos se asociaban los ingenieros A y B; se conserva el conteo total (4 hallazgos cada uno, 3 defectos únicos) para mantener la trazabilidad del acta original.
