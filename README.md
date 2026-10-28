# 🧾 Classora TPV — Backend (Spring Boot)

API REST de la caja de supermercado (prueba técnica Classora): **cajeros autenticados con JWT**, **consulta de producto por código** y **gestión de la venta**, desplegada en `dev` y `prod`.
**Java 17 · Spring Boot 4.1.0 · Hexagonal por features · DDD · Spring Security + JWT · Flyway · PostgreSQL (Neon) · OpenAPI · Docker · Render · GitHub Actions**.
Estilo heredado de [task-manager](https://github.com/DiegoVilla27/task-manager-system/tree/main/apps/task-manager).

> Frontend: `prueba-tecnica-classora-fe` (React · Vercel).

## 📚 Documentación

| Doc | Contenido |
|---|---|
| [`CLAUDE.md`](CLAUDE.md) | **Reglas obligatorias**: alcance, stack, arquitectura, convenciones, git |
| [`AI_LOG.md`](AI_LOG.md) | Registro cronológico del uso de IA |
| [`docs/01-negocio-y-dominio.md`](docs/01-negocio-y-dominio.md) | Requisitos, bounded contexts, agregado `Sale`, reglas de negocio |
| [`docs/02-casos-de-uso.md`](docs/02-casos-de-uso.md) | Casos de uso (el alcance completo) |
| [`docs/03-arquitectura.md`](docs/03-arquitectura.md) | Árbol de paquetes, agregado de ejemplo, Flyway, ADRs |
| [`docs/04-api.md`](docs/04-api.md) | Endpoints, contratos, errores, OpenAPI, ⭐ respuesta de entrevista |
| [`docs/05-seguridad.md`](docs/05-seguridad.md) | JWT, propiedad de la venta, rate limiting, CORS, secretos |
| [`docs/06-testing.md`](docs/06-testing.md) | Unit por capa + integración con Testcontainers |
| [`docs/07-despliegue.md`](docs/07-despliegue.md) | Entornos, Docker, Render, Neon, CI/CD |
| [`docs/08-roadmap.md`](docs/08-roadmap.md) | Hitos y lo que queda fuera de alcance |

## 🌐 Entornos *(URLs al desplegar)*
| Entorno | API | Swagger |
|---|---|---|
| local | `http://localhost:8080/api/v1` | `/api/v1/api-docs/swagger-ui.html` |
| dev | _pendiente_ | _pendiente_ |
| prod | _pendiente_ | _pendiente_ |

## ▶️ Ejecutar en local

Requisitos: JDK 17 o superior (el proyecto compila a Java 17, como task-manager; la imagen Docker usa Temurin 21), Docker y la infraestructura compartida (`~/Desktop/shared-network`, contenedor `global_postgres`). La configuración de cada entorno está en `src/main/resources/application-<perfil>.properties`.

```bash
# 1. Base de datos (una vez)
docker compose -f ~/Desktop/shared-network/docker-compose.yml up -d postgres
docker exec global_postgres psql -U user -d postgres -c "CREATE DATABASE pos_db;"

# 2a. API en la máquina (perfil local) → http://localhost:8080/api/v1
./mvnw spring-boot:run

# 2b. o API en contenedor (perfil docker, red shared-network)
docker compose up --build -d

# Calidad
./mvnw test                             # unit
./mvnw verify                           # unit + integración (Testcontainers) + Spotless + JaCoCo
./mvnw spotless:apply                   # formatear
```

- Swagger UI: `http://localhost:8080/api/v1/api-docs/swagger-ui.html` → `POST /auth/login` y pulsa **Authorize** con el `accessToken`.
- Cajero demo (local/docker): `cajero@pos.local` / `cajero1234`.
- Health: `http://localhost:8080/api/v1/actuator/health`
