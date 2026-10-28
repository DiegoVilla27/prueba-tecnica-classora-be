# 08 · Roadmap (Backend)

Se trabaja primero el backend completo y desplegado; después el front lo consume. Cada hito: `./mvnw verify` en verde, entrada en `AI_LOG.md` y commit con Conventional Commits (cuando el usuario lo pida).

## Hito 0 · Documentación ✅

## Hito 1 · Setup y core ✅
- [x] Spring Initializr (Java 21, Maven): web, data-jpa, validation, security, actuator, postgresql, flyway, lombok.
- [x] mapstruct, springdoc-openapi, jjwt, bucket4j, testcontainers; spotless, jacoco, failsafe.
- [x] Perfiles `local`, `docker`, `dev`, `prod` en `application-<perfil>.properties` (dev/prod con placeholders para Render).
- [x] `core/` desde task-manager: errors, openapi, security (JWT sin roles, CORS por entorno, rate limit con bucket de login). El seeder se hace en el hito 2.
- [x] `V1__init.sql` (esquema completo).
- [x] `Dockerfile` + `docker-compose.yml` (red `shared-network`).

## Hito 2 · `user` + `auth` — ~2 h
- [ ] `UserModel` (cajero, sin roles), puertos, adapters, `/users/me`.
- [ ] `AuthService` (login, refresh), `AuthController` + docs.
- [ ] `DatabaseSeeder`: un cajero desde `seed.cashier.*`.
- [ ] Tests + `AuthIT`.

## Hito 3 · `product` ⭐ — ~2 h
- [ ] `ProductModel`, puerto, service, adapter, mappers.
- [ ] `GET /products/{code}` + docs.
- [ ] `V2__seed_products.sql` (= fixtures del front).
- [ ] Tests + `ProductLookupIT`.

## Hito 4 · `sale` — ~4 h
- [ ] `SaleModel`, `SaleLine`, `SaleStatus`, `Quantity`, excepciones + `SaleModelTest`.
- [ ] Service con permisos, puerto, adapter (`@EntityGraph`, `@Version`), resolver de constraints.
- [ ] 8 endpoints + docs.
- [ ] Tests + `CheckoutFlowIT`.

## Hito 5 · CI/CD y despliegue — ~2 h
- [ ] Workflow `ci-cd.yml` (quality → test → deploy).
- [ ] Neon (ramas `dev`/`main`), Render (`pos-api-dev`/`pos-api-prod`), secrets.
- [ ] Rama `dev` + protección de ramas.
- [ ] Smoke test en dev: login → escanear → finalizar desde Swagger.

## Hito 6 · Entrega
- [ ] README estilo task-manager: badges, arquitectura, entornos con URLs, credenciales demo, comandos, Docker, CI/CD.
- [ ] `AI_LOG.md` revisado.

---

## Fuera de alcance (se explica en la entrevista)

| Tema | Cómo se haría | Por qué no ahora |
|---|---|---|
| Roles (supervisor, admin) | `UserRole` + `@PreAuthorize`, como task-manager. Caso típico: anulaciones que requieren autorización de un encargado | El enunciado solo tiene cajeros |
| Gestión de usuarios (CRUD) | Feature `user` completa como task-manager | No hay backoffice; un cajero sembrado |
| Gestión del catálogo | CRUD de productos + caché con invalidación | Catálogo fijo por migración |
| Caché de productos | Caffeine `@Cacheable` / Redis | El índice único basta a esta escala |
| Listados e informes de ventas | `GET /sales` paginado con Specifications | Ningún caso de uso lo usa |
| Pagos, tickets, IVA, promociones, inventario, devoluciones | Nuevos bounded contexts, eventos de dominio y outbox | Fuera del enunciado |
| Multi-tienda / multi-caja | `storeId`, `terminalId` en la venta | Una caja |
| Observabilidad avanzada | Micrometer + Prometheus + OpenTelemetry | Actuator `health` basta |
