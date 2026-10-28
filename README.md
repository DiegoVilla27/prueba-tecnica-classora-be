# 🧾 Classora TPV — Backend (Spring Boot)

API REST mínima para la caja de supermercado (prueba técnica Classora, Parte 3): **consulta de producto por código** y **gestión de la venta**.
**Java 21 · Spring Boot · Hexagonal por features · DDD · OpenAPI · PostgreSQL**. Estilo heredado de [task-manager](https://github.com/DiegoVilla27/task-manager-system/tree/main/apps/task-manager), sin sobreingeniería.

> Frontend relacionado: `prueba-tecnica-classora-fe` (React).

## 📚 Documentación

| Doc | Contenido |
|---|---|
| [`CLAUDE.md`](CLAUDE.md) | **Reglas obligatorias**: alcance, stack, arquitectura, convenciones, testing |
| [`AI_LOG.md`](AI_LOG.md) | Registro cronológico del uso de IA |
| [`docs/01-negocio-y-dominio.md`](docs/01-negocio-y-dominio.md) | Requisitos, bounded contexts, agregado `Sale`, reglas de negocio |
| [`docs/02-casos-de-uso.md`](docs/02-casos-de-uso.md) | Los 7 casos de uso (el alcance completo) |
| [`docs/03-arquitectura.md`](docs/03-arquitectura.md) | Árbol de paquetes, agregado de ejemplo, persistencia, ADRs |
| [`docs/04-api.md`](docs/04-api.md) | Endpoints, contratos, errores, OpenAPI, ⭐ respuesta de entrevista |
| [`docs/05-testing.md`](docs/05-testing.md) | Tests por capa (estilo task-manager) |
| [`docs/06-roadmap.md`](docs/06-roadmap.md) | Hitos y lo que queda **fuera de alcance** (y cómo se haría) |

## ▶️ Ejecutar *(se completará al implementar)*

```bash
docker compose up -d postgres
./mvnw spring-boot:run          # perfil local
./mvnw test
./mvnw verify                   # tests + spotless + jacoco
```
Swagger: http://localhost:8080/api/v1/api-docs/swagger-ui.html
