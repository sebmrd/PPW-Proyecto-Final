# Proyecto Final

## API REST Segura para la Gestion de Eventos Academicos

### Integrantes

| Integrante | Correo | GitHub |
|------------|--------|--------|
| Orellana Mateo | morellana1@est.ups.edu.ec | MateOrellana |
| Alvarado Sebastian | salvaradom1@est.ups.edu.ec | sebmrd |

---

## 1. Resumen del Proyecto

Este proyecto implementa una API REST segura para la gestion de eventos academicos. El sistema permite administrar usuarios, roles, categorias, eventos, sesiones, inscripciones, reportes y estadisticas. La solucion fue desarrollada con Spring Boot, PostgreSQL, Redis, Spring Security, JWT, Flyway, Swagger/OpenAPI, Actuator, Docker y Render.

El objetivo principal fue construir un backend robusto y desplegable, con autenticacion, autorizacion por roles, validacion de propiedad de recursos, reglas de negocio, manejo centralizado de excepciones, rate limiting, reportes descargables y evidencias de funcionamiento en local y produccion.

---

## 2. Tecnologias Utilizadas

| Area | Tecnologia |
|------|------------|
| Backend | Java 17, Spring Boot 4.x |
| API REST | Spring Web |
| Persistencia | Spring Data JPA |
| Base de datos | PostgreSQL 15 |
| Migraciones | Flyway |
| Seguridad | Spring Security, JWT, BCrypt |
| Cache temporal y limites | Redis |
| Documentacion | Springdoc OpenAPI, Swagger UI |
| Reportes | Apache POI, OpenPDF |
| Observabilidad | Spring Boot Actuator |
| Pruebas | JUnit, Mockito |
| Contenedores | Docker, Docker Compose |
| Despliegue | Render |

---

## 3. Cumplimiento de los Puntos del Proyecto

### Punto 1. Arquitectura general

El backend se organizo como un monolito modular por dominios. La estructura separa controladores, servicios, repositorios, entidades, DTO y configuraciones. Los dominios principales son:

- `users`: usuarios, roles, autenticacion y administracion.
- `events`: categorias y eventos academicos.
- `registrations`: sesiones, inscripciones y auditoria.
- `reports`: reportes PDF/Excel y estadisticas.
- `security`: filtros JWT, Swagger Basic Auth y autorizacion por propiedad.
- `config`: CORS, OpenAPI, rate limiting y configuracion web.
- `exception`: respuestas uniformes de error.

El mapeo entre entidades y respuestas se realiza mediante DTO de entrada/salida y metodos de conversion como `from(...)`, evitando exponer directamente las entidades JPA en la API.

Evidencia sugerida: `assets/evidencia-01-arquitectura-paquetes.png`
Debe mostrar la estructura de paquetes del proyecto en el IDE.

### Punto 2. Modelo de datos

La base de datos contiene las tablas principales solicitadas: `users`, `roles`, `user_roles`, `categories`, `events`, `sessions`, `registrations`, `refresh_tokens` y `audit_logs`. El esquema se inicializa con Flyway y se valida desde JPA con `ddl-auto=validate`, evitando que Hibernate cree o modifique tablas automaticamente.

Tambien se agrego el script `00_create_database.sql` para crear la base `academic_events_db`, y la migracion principal crea tablas, relaciones, restricciones, indices y datos iniciales.

Evidencia sugerida: `assets/evidencia-02-tablas-postgresql.png`
Debe mostrar el resultado de `\dt` o una vista de las tablas creadas en PostgreSQL.

### Punto 3. Roles y permisos

El sistema maneja tres roles:

| Rol | Responsabilidad |
|-----|-----------------|
| `ADMIN` | Administra usuarios, categorias, eventos, inscripciones y reportes globales. |
| `ORGANIZER` | Gestiona unicamente sus propios eventos, sesiones e inscripciones. |
| `PARTICIPANT` | Consulta eventos, se inscribe, cancela sus inscripciones y descarga comprobantes propios. |

Ademas del rol, se valida la propiedad del recurso con `ResourceAuthorizationService`. Por ejemplo, un organizador no puede modificar eventos de otro organizador y un participante no puede descargar certificados ajenos.

Evidencia sugerida: `assets/evidencia-03-roles-403.png`
Debe mostrar una solicitud rechazada con `403 Forbidden` al intentar acceder a un recurso ajeno.

### Punto 4. Flujo funcional y endpoints minimos

Se implementaron los flujos principales de la API:

- Autenticacion: registro, login, refresh, logout y perfil.
- Usuarios: listado y cambio de estado por administrador.
- Categorias: listar, crear, actualizar y desactivar.
- Eventos: listar con filtros, consultar detalle, crear, actualizar, cambiar estado y eliminar.
- Sesiones: listar, crear, actualizar y eliminar sesiones por evento.
- Inscripciones: inscribirse, listar propias, cancelar, listar inscritos por evento y actualizar estado.
- Reportes: PDF, Excel, certificado y estadisticas.

Evidencia sugerida: `assets/evidencia-04-swagger-endpoints.png`
Debe mostrar Swagger UI con los grupos de endpoints cargados.

### Punto 5. Autenticacion y autorizacion

La autenticacion usa JWT. El `accessToken` tiene expiracion corta y el `refreshToken` se almacena hasheado en base de datos. Al renovar el token se rota el refresh token anterior, quedando revocado.

Las contrasenas se almacenan con BCrypt. Los endpoints protegidos usan Spring Security y reglas `@PreAuthorize`. Los mensajes de autenticacion son genericos para no revelar si un correo existe.

Evidencia incluida: `assets/swagger-login.png`
Muestra un login exitoso en Swagger con tokens redactados.

Evidencia sugerida: `assets/evidencia-05-refresh-logout.png`
Debe mostrar una prueba de `/api/auth/refresh` o `/api/auth/logout`.

### Punto 6. Redis y rate limiting

Redis se usa solo como almacenamiento temporal, no como fuente principal de datos. Se utiliza para:

- Contadores temporales de solicitudes.
- Bloqueo temporal por intentos fallidos de login.
- Claves con prefijos como `rate:` y `blocked-user:`.
- TTL en claves temporales.

Si Redis no esta disponible en desarrollo local, la API evita caerse completamente y permite continuar pruebas basicas.

Evidencia sugerida: `assets/evidencia-06-redis-keys.png`
Debe mostrar claves temporales en Redis o logs de funcionamiento del rate limiting.

### Punto 7. Limites de solicitudes

El rate limiting incrementa contadores por IP o usuario antes de procesar solicitudes. Si se supera el limite, la API responde `429 Too Many Requests` e incluye el header `Retry-After`.

Limites configurados:

| Operacion | Identificador | Limite |
|-----------|---------------|--------|
| Login | IP | 5 por minuto |
| Registro | IP | 3 por hora |
| Endpoints publicos | IP | 60 por minuto |
| Endpoints autenticados | Usuario | 120 por minuto |
| Reportes | Usuario | 5 por minuto |

Evidencia sugerida: `assets/evidencia-07-rate-limit-429.png`
Debe mostrar una respuesta `429` con header `Retry-After`.

### Punto 8. CORS restringido

CORS se configura desde la variable `ALLOWED_ORIGINS`. Se restringen metodos a `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`, y se permiten solo headers necesarios como `Authorization` y `Content-Type`.

En produccion no se usa `*`; en `render.yaml` se define un dominio especifico.

Evidencia sugerida: `assets/evidencia-08-cors-render-env.png`
Debe mostrar la variable `ALLOWED_ORIGINS` configurada en Render.

### Punto 9. Reglas de negocio y transacciones

Se implementaron reglas de negocio como:

- No permitir correos duplicados.
- No permitir categorias duplicadas.
- No permitir doble inscripcion del mismo participante al mismo evento.
- No permitir inscripciones fuera del periodo habilitado.
- No permitir inscripciones en eventos no publicados.
- No permitir inscripciones sin cupos disponibles.
- Actualizar disponibilidad dentro de transacciones.
- No eliminar fisicamente eventos publicados con inscripciones; se aplica eliminado logico.
- Listados con paginacion, filtros y busqueda.

Evidencia sugerida: `assets/evidencia-09-regla-negocio-duplicado.png`
Debe mostrar un error controlado por categoria/correo/inscripcion duplicada.

### Punto 10. Manejo centralizado de excepciones

Se implemento `GlobalExceptionHandler` con respuestas uniformes que incluyen:

- `timestamp`
- `status`
- `internalCode`
- `message`
- `path`

Se manejan errores de validacion, autenticacion, acceso prohibido, reglas de negocio y exceso de solicitudes.

Evidencia sugerida: `assets/evidencia-10-error-validacion.png`
Debe mostrar una respuesta JSON de error uniforme, por ejemplo enviando un DTO invalido.

### Punto 11. Swagger y OpenAPI protegidos

Swagger UI esta disponible y protegido con autenticacion Basic. Las credenciales se configuran por variables de entorno:

- Usuario: `evaluator`
- Contrasena: `evaluator123`

Dentro de Swagger se configura el esquema Bearer JWT para probar endpoints protegidos.

Evidencias incluidas:

- `assets/swagger-1.png`
- `assets/swagger-2.png`
- `assets/swagger-login.png`

Evidencia sugerida adicional: `assets/evidencia-11-swagger-basic-auth.png`
Debe mostrar la ventana de autenticacion Basic antes de entrar a Swagger.

### Punto 12. Actuator y observabilidad

Se agrego Spring Boot Actuator y se expone publicamente solo `/actuator/health`, sin detalles internos del sistema. Esto permite comprobar que la API esta disponible localmente o en produccion.

Evidencia incluida: `assets/actuator-health.png`
Muestra el endpoint de salud en Render con estado `UP`.

### Punto 13. Reportes, estadisticas y archivos descargables

El modulo de reportes genera archivos bajo demanda y respeta roles y propiedad de eventos.

Endpoints implementados:

| Endpoint | Acceso | Resultado |
|----------|--------|-----------|
| `GET /api/reports/events/{eventId}/registrations.pdf` | ADMIN u organizador propietario | PDF de inscritos. |
| `GET /api/reports/events/{eventId}/registrations.xlsx` | ADMIN u organizador propietario | Excel de inscritos. |
| `GET /api/registrations/{id}/certificate.pdf` | Participante propietario | Comprobante de inscripcion confirmada. |
| `GET /api/reports/statistics?from=...&to=...` | ADMIN | Estadisticas globales. |
| `GET /api/reports/events/{eventId}/statistics?from=...&to=...` | ADMIN u organizador propietario | Estadisticas de un evento. |

Los archivos responden con `Content-Type` y `Content-Disposition` para descarga.

Evidencias sugeridas:

- `assets/evidencia-13-reporte-pdf.png`: descarga de inscritos en PDF.
- `assets/evidencia-13-reporte-excel.png`: descarga de inscritos en Excel.
- `assets/evidencia-13-certificado-confirmado.png`: certificado de una inscripcion `CONFIRMED`.
- `assets/evidencia-13-estadisticas.png`: respuesta JSON de estadisticas con rango de fechas.

### Punto 14. Pruebas, cliente de prueba y evidencias

Aunque la guia no incluye un encabezado numerado como punto 14, los entregables solicitan pruebas, evidencias y un cliente de prueba. Por eso se incluyo una coleccion en `bruno/Coleccion_Eventos_Academicos.json` con endpoints principales para autenticacion, eventos, inscripciones y reportes.

Tambien se ejecutaron pruebas con Gradle:

```powershell
.\gradlew.bat clean test --console=plain
```

Evidencia sugerida: `assets/evidencia-14-gradle-build-success.png`
Debe mostrar `BUILD SUCCESSFUL`.

### Punto 15. Despliegue

El backend se despliega con Docker en Render. El repositorio incluye:

- `Dockerfile`
- `render.yaml`

Render crea o conecta servicios separados para:

- Backend Spring Boot.
- PostgreSQL.
- Redis.

La JVM se limita con `JAVA_TOOL_OPTIONS` para evitar exceder recursos de la instancia gratuita.

Evidencias incluidas:

- `assets/actuator-health.png`
- `assets/endpoint-exitoso.png`

Evidencia sugerida adicional: `assets/evidencia-15-render-dashboard.png`
Debe mostrar el dashboard de Render con el servicio web, PostgreSQL y Redis activos.

### Punto 16. Variables de entorno

Las credenciales y configuraciones sensibles se manejan con variables de entorno. El archivo `.env.example` documenta las variables necesarias para desarrollo local, sin publicar el `.env` real.

Variables principales:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `ALLOWED_ORIGINS`
- `PORT`
- `SWAGGER_USERNAME`
- `SWAGGER_PASSWORD`

En Render, `JWT_SECRET` se genera con `generateValue: true`.

Evidencia sugerida: `assets/evidencia-16-env-render.png`
Debe mostrar variables configuradas en Render, ocultando valores sensibles.

### Punto 17. Zona horaria

La API trabaja con `Instant` y configura Hibernate con zona horaria UTC:

```yaml
hibernate:
  jdbc:
    time_zone: UTC
```

Los valores de fecha se intercambian en formato ISO 8601. La zona de negocio indicada es `America/Guayaquil`, por lo que las conversiones para visualizacion o reportes se pueden realizar desde el cliente o capa de presentacion cuando sea necesario.

Evidencia sugerida: `assets/evidencia-17-fechas-iso8601.png`
Debe mostrar una respuesta JSON con fechas en formato ISO 8601.

---

## 4. Evidencias Incluidas Actualmente

| Archivo | Evidencia |
|---------|-----------|
| `assets/Diagrama.png` | Diagrama entidad-relacion del proyecto. |
| `assets/actuator-health.png` | Health check de Actuator en Render. |
| `assets/swagger-1.png` | Swagger UI cargado en produccion. |
| `assets/swagger-2.png` | Vista adicional de Swagger UI. |
| `assets/swagger-login.png` | Login exitoso en Swagger con tokens redactados. |
| `assets/endpoint-exitoso.png` | Consumo exitoso de endpoint protegido con token redactado. |

---

## 5. Evidencias Pendientes Recomendadas

Para que el informe se vea como proyecto final completo, se recomienda agregar las siguientes capturas en `assets/`:

| Nombre sugerido | Que debe mostrar |
|-----------------|------------------|
| `evidencia-01-arquitectura-paquetes.png` | Estructura modular del proyecto en el IDE. |
| `evidencia-02-tablas-postgresql.png` | Tablas creadas en PostgreSQL. |
| `evidencia-03-roles-403.png` | Error `403` por acceso a recurso ajeno. |
| `evidencia-05-refresh-logout.png` | Prueba de refresh token o logout. |
| `evidencia-06-redis-keys.png` | Claves temporales en Redis. |
| `evidencia-07-rate-limit-429.png` | Respuesta `429` con `Retry-After`. |
| `evidencia-08-cors-render-env.png` | Variable `ALLOWED_ORIGINS` en Render. |
| `evidencia-09-regla-negocio-duplicado.png` | Error por duplicado o regla de negocio. |
| `evidencia-10-error-validacion.png` | Error uniforme por DTO invalido. |
| `evidencia-11-swagger-basic-auth.png` | Acceso Basic Auth de Swagger. |
| `evidencia-13-reporte-pdf.png` | Descarga de reporte PDF. |
| `evidencia-13-reporte-excel.png` | Descarga de reporte Excel. |
| `evidencia-13-certificado-confirmado.png` | Certificado de inscripcion confirmada. |
| `evidencia-13-estadisticas.png` | Estadisticas con rango de fechas. |
| `evidencia-14-gradle-build-success.png` | Gradle mostrando `BUILD SUCCESSFUL`. |
| `evidencia-15-render-dashboard.png` | Servicios activos en Render. |
| `evidencia-16-env-render.png` | Variables de entorno en Render con secretos ocultos. |
| `evidencia-17-fechas-iso8601.png` | Fechas en formato ISO 8601. |
| `evidencia-git-commits-mateo.png` | Commits funcionales de Mateo. |
| `evidencia-git-commits-sebastian.png` | Commits funcionales de Sebastian. |

No se deben subir capturas con tokens JWT, contrasenas reales ni secretos visibles.

---

## 6. Diagrama Entidad-Relacion

![Diagrama Entidad-Relacion](assets/Diagrama.png)

---

## 7. Enlaces de Produccion

| Recurso | URL |
|---------|-----|
| API base | `https://backend-academic-events.onrender.com` |
| Swagger UI | `https://backend-academic-events.onrender.com/swagger-ui/index.html` |
| Actuator Health | `https://backend-academic-events.onrender.com/actuator/health` |

Swagger esta protegido con:

- Usuario: `evaluator`
- Contrasena: `evaluator123`

---

## 8. Usuarios de Prueba

La base de datos se inicializa automaticamente mediante Flyway con usuarios de prueba.

| Rol | Usuario |
|-----|---------|
| Administrador | `admin@academic.test` |
| Organizador | `maria.cordero@academic.test` |
| Participante | `carlos.velez@academic.test` |

Contrasena comun:

```text
Password123*
```

---

## 9. Ejecucion Local

### Requisitos

- Java 17 o superior.
- Docker y Docker Compose.
- Git.

### Configurar variables

```powershell
copy .env.example .env
```

### Levantar PostgreSQL y Redis

```powershell
docker compose up -d
```

Servicios locales:

| Servicio | Puerto |
|----------|--------|
| PostgreSQL | `5433` |
| Redis | `6379` |

### Ejecutar aplicacion

```powershell
.\gradlew.bat bootRun
```

### Ejecutar pruebas

```powershell
.\gradlew.bat clean test --console=plain
```

---

## 10. Despliegue en Render

El despliegue se realiza con Blueprint usando `render.yaml`.

Pasos generales:

1. Subir el repositorio a GitHub.
2. Crear un Blueprint en Render.
3. Conectar el repositorio.
4. Verificar que Render cree PostgreSQL, Redis y el servicio web.
5. Confirmar que `JWT_SECRET` se genere de forma segura.
6. Confirmar que `ALLOWED_ORIGINS` no use `*` en produccion.
7. Probar Swagger, API y Actuator Health desde la URL publica.

---

## 11. Evidencias de Despliegue en Produccion

### Actuator Health

![Health Check](assets/actuator-health.png)

### Swagger UI

![Swagger cargado 1](assets/swagger-1.png)

![Swagger cargado 2](assets/swagger-2.png)

### Login en Swagger

![Swagger Login](assets/swagger-login.png)

### Endpoint protegido exitoso

![Endpoint OK](assets/endpoint-exitoso.png)

---


## 12. Conclusiones

El proyecto integra los principales contenidos de la asignatura en una API REST completa: arquitectura modular, seguridad JWT, autorizacion por roles, persistencia relacional, Redis, rate limiting, validaciones, reportes descargables, observabilidad y despliegue en produccion.

La solucion permite demostrar no solo ejecucion local, sino tambien funcionamiento desde una URL publica con Swagger protegido, health check y evidencias de consumo de endpoints reales.
