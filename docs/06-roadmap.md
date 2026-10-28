# 06 · Roadmap (Backend)

> La prueba **no exige** backend implementado. Prioridad absoluta: **Hito 2** (producto por código). El resto, si hay tiempo.

## Hito 0 · Documentación ✅

## Hito 1 · Setup — ~1 h
- [ ] Spring Initializr (Java 21, Maven): web, data-jpa, validation, postgresql, lombok.
- [ ] mapstruct, springdoc-openapi, spotless, jacoco.
- [ ] `application.properties` + perfiles `local` / `docker`; `docker-compose.yml` con PostgreSQL.
- [ ] `core/` desde task-manager, **solo**: errors, openapi (sin security scheme), `CorsConfig`, seeder.

## Hito 2 · `product` ⭐ — ~2 h
- [ ] `ProductModel` + `ProductNotFoundException`.
- [ ] Puerto, service, entity, repository, adapter, mappers.
- [ ] `GET /products/{code}` + documentación OpenAPI.
- [ ] Seeder con el mismo catálogo que los mocks del front.
- [ ] Tests (service, controller, adapter).

## Hito 3 · `sale` — ~3 h
- [ ] `SaleModel`, `SaleLine`, `SaleStatus`, `Quantity` + excepciones + `SaleModelTest`.
- [ ] Service, puerto, adapter (`@EntityGraph`), entidades y mappers.
- [ ] 6 endpoints + documentación.
- [ ] Tests.

## Hito 4 · Entrega — ~30 min
- [ ] README (estilo task-manager): cómo arrancar, Swagger, decisiones.
- [ ] `AI_LOG.md` revisado.
- [ ] Probar el front con `.env.dev` contra este backend.

---

## Fuera de alcance (para explicar en la entrevista, no para implementar)

| Tema | Cómo se haría | Por qué no ahora |
|---|---|---|
| Autenticación de cajeros | JWT doble token + Spring Security + `PermissionValidator`, copiado de task-manager | La prueba no tiene login |
| Roles (cajero / supervisor) | RBAC con `@PreAuthorize` | Sin usuarios no hay roles |
| Rate limiting | Bucket4j por IP, como task-manager | API local de demo |
| Caché de productos | Caffeine `@Cacheable` + `@CacheEvict` al editar | Índice único ya basta |
| Concurrencia | `@Version` (bloqueo optimista) en `SaleEntity` | Una caja, un cajero |
| Migraciones | Flyway | Solo hay entorno local |
| Cancelar venta / una venta abierta por cajero | `PATCH /sales/{id}/cancel`, índice parcial único | Requiere usuarios |
| Gestión del catálogo | CRUD admin de productos | El catálogo es fijo (seeder) |
| Integración con contenedores | Testcontainers + `@SpringBootTest` | Los unit tests por capa cubren la prueba |
| CI/CD y despliegue | GitHub Actions + Sonar + Render, como task-manager | No se pide despliegue |
| Más dominio | Pagos, tickets, IVA, promociones, inventario por eventos, devoluciones | Fuera del enunciado |
