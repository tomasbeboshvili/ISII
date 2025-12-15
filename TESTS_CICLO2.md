# Estrategia de Pruebas - Ciclo 2

Este documento recoge la trazabilidad entre requisitos y casos de prueba, el detalle de las pruebas unitarias implementadas (JUnit/Mockito) y las pruebas del sistema ejecutadas sobre las nuevas funcionalidades del Ciclo 2 (Amigos, Feed, Estadísticas, Autenticación mejorada).

## 1. Trazabilidad requisito ↔ caso de prueba

| RQ | Descripción | Casos unitarios | Casos del sistema |
|----|-------------|-----------------|-------------------|
| RQ-AUTH-1 | Registro con redirección y validación | UT-C2-01 | ST-C2-01 |
| RQ-AUTH-2 | Recuperación y reseteo de contraseña (páginas separadas) | UT-C2-02 | ST-C2-02 |
| RQ-FRIEND-1 | Enviar solicitud de amistad | UT-C2-03 | ST-C2-03 |
| RQ-FRIEND-2 | Aceptar/Rechazar solicitud | UT-C2-04 | ST-C2-04 |
| RQ-FRIEND-3 | Listar amigos | UT-C2-05 | ST-C2-05 |
| RQ-STATS-1 | Estadísticas de usuario (puntos, medias) | UT-C2-06 | ST-C2-06 |
| RQ-FEED-1 | Feed de actividad de amigos | UT-C2-07 | ST-C2-07 |

## 2. Pruebas unitarias (JUnit + Mockito)

| ID | Requisito(s) | Caso / Clase | Procedimiento resumido | Resultado esperado | Resultado real |
|----|--------------|--------------|------------------------|--------------------|----------------|
| UT-C2-01 | RQ-AUTH-1 | `AuthServiceTest` (existente + nuevos flujos) | Verificar registro de usuarios y envío de email. | Usuario creado, email enviado. | PASS |
| UT-C2-02 | RQ-AUTH-2 | `AuthServiceTest` | Verificar generación de token de reset y cambio de password. | Token generado, password actualizado. | PASS |
| UT-C2-03 | RQ-FRIEND-1 | `FriendshipServiceTest.sendRequest_Success` | Enviar solicitud a usuario existente no amigo. | Solicitud guardada con estado PENDING. | PASS |
| UT-C2-04 | RQ-FRIEND-2 | `FriendshipServiceTest.resolveRequest_Accept_Success` | Aceptar solicitud pendiente. | Estado cambia a ACCEPTED. | PASS |
| UT-C2-05 | RQ-FRIEND-3 | `FriendshipServiceTest.getFriends_ReturnsList` | Consultar amigos de un usuario. | Retorna lista de usuarios con amistad ACCEPTED. | PASS |
| UT-C2-06 | RQ-STATS-1 | `StatisticsServiceTest.getUserStatistics_CalculatesCorrectly` | Mockear ratings y tastings y llamar al servicio. | Medias y conteos coinciden con los datos mockeados. | PASS |
| UT-C2-07 | RQ-FEED-1 | `ActivityServiceTest.getFriendActivity_ReturnsSortedFeed` | Mockear actividad de amigos (tastings, ratings). | Lista combinada y ordenada por fecha descendente. | PASS |

## 3. Pruebas del sistema (frontend estático + API)

| ID | Requisito(s) | Pasos (UI / API) | Resultado esperado | Resultado real |
|----|--------------|------------------|--------------------|----------------|
| ST-C2-01 | RQ-AUTH-1 | Ir a `register.html`, crear cuenta. | Redirección a `auth.html` tras éxito. Login exitoso. | PASS |
| ST-C2-02 | RQ-AUTH-2 | Ir a `recover.html`, pedir código. Ir a `reset.html`, usar código. | Password cambiado, login exitoso con nueva password. | PASS |
| ST-C2-03 | RQ-FRIEND-1 | En `friends.html`, buscar usuario y dar "Enviar Solicitud". | Botón cambia a "Pendiente" o mensaje de éxito. | PASS |
| ST-C2-04 | RQ-FRIEND-2 | Loguear con el otro usuario, ir a `friends.html`, ver solicitud y "Aceptar". | Usuario aparece en lista de amigos. | PASS |
| ST-C2-05 | RQ-FRIEND-3 | Ver lista de amigos en `friends.html`. | Aparecen los amigos aceptados con botón "Eliminar". | PASS |
| ST-C2-06 | RQ-STATS-1 | Ir a `statistics.html`. | Se muestran contadores y medias correctas. | PASS |
| ST-C2-07 | RQ-FEED-1 | Ir a `feed.html`. | Se muestra actividad reciente de los amigos (si la hay). | PASS |
| ST-C2-08 | UI | Activar modo oscuro con el botón del sol/luna. | Toda la interfaz cambia a colores oscuros (GitHub Dark), incluyendo tarjetas y nav. | PASS |

## 4. Registro y gestión de defectos (Ciclo 2)

| ID | Descripción | Corrección | Estado |
|----|-------------|------------|--------|
| DF-C2-01 | `SecurityException` al resolver amistad por comparación incorrecta de objetos `User`. | Se implementó `equals/hashCode` en `User` y se compararon IDs en `FriendshipService`. | Cerrado |
| DF-C2-02 | Las tarjetas de perfil y logros se veían blancas en modo oscuro. | Se añadieron reglas CSS específicas (`html.dark-mode .card`, etc.) para usar `var(--surface)`. | Cerrado |
| DF-C2-03 | "Flash of Unstyled Content" (pantalla blanca) al cargar en modo oscuro. | Se inyectó script en `<head>` para aplicar la clase `dark-mode` antes del renderizado. | Cerrado |
| DF-C2-04 | La barra de navegación perdía el fondo oscuro en modo oscuro. | Se restauró la regla CSS para `html.dark-mode .nav-links`. | Cerrado |
