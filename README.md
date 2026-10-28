# 🧾 Classora TPV — Backend (Spring Boot)

API REST de la caja de supermercado (prueba técnica Classora): **cajeros autenticados con JWT**, **consulta de producto por código** y **gestión de la venta**, desplegada en `dev` y `prod`.
**Java 21 · Spring Boot · Hexagonal por features · DDD · Spring Security + JWT · Flyway · PostgreSQL (Neon) · OpenAPI · Docker · Render · GitHub Actions**.
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

## ▶️ Ejecutar *(se completará al implementar)*
```bash
cp .env.example .env
./mvnw spring-boot:run          # perfil local (requiere global_postgres)
./mvnw verify                   # unit + integración + spotless + jacoco
docker compose up --build -d    # perfil docker
```
