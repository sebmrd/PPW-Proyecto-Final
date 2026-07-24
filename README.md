# Proyecto Final

## Integrantes:

**Nombre:** Orellana Mateo  
**Correo:** morellana1@est.ups.edu.ec  
**GitHUB:** MateOrellana

**Nombre:** Alvarado Sebastián  
**Correo:** salvaradom1@est.ups.edu.ec  
**GitHUB:** sebmrd

---

## API REST Segura para la Gestión de Eventos Académicos

Esta es una API REST desarrollada con **Spring Boot** y **PostgreSQL** para la gestión de usuarios, eventos académicos, sesiones e inscripciones. La aplicación implementa autenticación mediante JWT, autorización por roles, limitación de solicitudes (Rate Limiting) con Redis, observabilidad mediante Spring Boot Actuator y generación de reportes en PDF y Excel.

---

## Tecnologías Utilizadas

- **Backend:** Java 17, Spring Boot 4.x
- **Persistencia:** Spring Data JPA
- **Seguridad:** Spring Security, JWT, BCrypt
- **Base de datos:** PostgreSQL 15
- **Rate Limiting:** Redis 7
- **Migraciones:** Flyway
- **Documentación:** Springdoc OpenAPI (Swagger UI)
- **Reportes:** Apache POI (Excel), OpenPDF (PDF)
- **Monitoreo:** Spring Boot Actuator
- **Contenedores:** Docker y Docker Compose
- **Despliegue:** Render

---

## Requisitos Previos

Antes de ejecutar el proyecto asegúrate de tener instalado:

- Java 17 o superior
- Docker y Docker Compose
- Git

---

## Instalación

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd proyectofinal
```

### 2. Configurar las variables de entorno

Copiar el archivo de ejemplo:

```bash
cp .env.example .env
```

En Windows:

```powershell
copy .env.example .env
```

Modificar las variables según sea necesario para el entorno local.

---

## Ejecución Local

### Levantar PostgreSQL y Redis

```bash
docker compose up -d
```

Servicios utilizados:

- PostgreSQL: puerto **5433**
- Redis: puerto **6379**

Redis debe estar levantado para probar rate limiting y bloqueo temporal de login.

### Ejecutar la aplicación

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

---

## Usuarios de Prueba

La base de datos se inicializa automáticamente mediante Flyway con los siguientes usuarios:

| Rol | Usuario |
|------|----------|
| Administrador | admin@academic.test |
| Organizador | maria.cordero@academic.test |
| Participante | carlos.velez@academic.test |

**Contraseña para todos los usuarios:**

```
Password123*
```

---

## Documentación de la API (Swagger)

La documentación está disponible en:

```
http://localhost:8080/swagger-ui/index.html
```

### Credenciales de acceso

- Usuario: `evaluator`
- Contraseña: `evaluator123`

### Autenticación con JWT

Para acceder a los endpoints protegidos:

1. Ejecutar el endpoint `POST /api/auth/login`.
2. Iniciar sesión con uno de los usuarios de prueba.
3. Copiar el `accessToken` obtenido.
4. Presionar el botón **Authorize** en Swagger.
5. Pegar el token para autenticar las solicitudes.

---

## Endpoints de Monitoreo

Estado de la aplicación:

```
http://localhost:8080/actuator/health
```

---

## Despliegue en Render

El proyecto incluye:

- `Dockerfile`
- `render.yaml`

Para desplegar la aplicación:

1. Crear un servicio tipo **Blueprint** en Render.
2. Conectar el repositorio del proyecto.
3. Render creará automáticamente:
   - Base de datos PostgreSQL.
   - Servicio Redis.
   - Aplicación Spring Boot.
4. Configurar la variable de entorno `JWT_SECRET` con un valor seguro antes de publicar la aplicación.
5. Ajustar `ALLOWED_ORIGINS` al dominio público real del servicio o del frontend; no usar `*` en producción.

---

## Características Implementadas

- Autenticación con JWT.
- Autorización basada en roles.
- Gestión de usuarios.
- Gestión de eventos académicos.
- Gestión de sesiones.
- Inscripción de participantes.
- Generación de reportes en PDF y Excel.
- Rate Limiting utilizando Redis.
- Migraciones automáticas con Flyway.
- Documentación interactiva mediante Swagger.
- Monitoreo mediante Spring Boot Actuator.
- Despliegue utilizando Docker y Render.

---

## Diagrama Entidad-Relación (ERD)

![Diagrama Entidad-Relación](assets/Diagrama.png)

---

## Enlaces de Producción
* **URL Base de la API:** `https://backend-academic-events.onrender.com`
* **Documentación Swagger UI:** `https://backend-academic-events.onrender.com/swagger-ui/index.html`
* **Health Check (Actuator):** `https://backend-academic-events.onrender.com/actuator/health`

*(Nota: Swagger está protegido. Credenciales: evaluator / evaluator123)*

