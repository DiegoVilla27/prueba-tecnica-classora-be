# CLAUDE.md — Reglas del agente (Backend · TPV Classora)

> Reglas obligatorias para cualquier agente o persona. Basadas en la forma de trabajo de
> [`task-manager`](https://github.com/DiegoVilla27/task-manager-system/tree/main/apps/task-manager): **copiar su estilo**, solo con lo que este producto necesita.
> Documentación: [`docs/`](docs/). Skills: [`.claude/skills/`](.claude/skills/). Log de IA: [`AI_LOG.md`](AI_LOG.md).

---

## 0. Contexto y alcance

API REST **realista y desplegada** para la caja de un supermercado:
- **Cajeros autenticados** (JWT) que abren, operan, finalizan y cancelan **sus** ventas. Un único tipo de usuario: el cajero (sin roles). Un cajero sembrado por entorno.
- **Catálogo** consultado por código en cada escaneo.
- Entornos `local`, `docker`, `dev` y `prod` (Render + Neon), con CI/CD.

> 🚫 **Anti-sobreingeniería.** El alcance es **exactamente** [`docs/02-casos-de-uso.md`](docs/02-casos-de-uso.md). Todo lo que hay aquí existe porque un caso de uso o el despliegue real lo exige. Lo demás está en `docs/08-roadmap.md` → "Fuera de alcance".

---

## 1. Stack

| Ámbito | Elección |
|---|---|
| Lenguaje / framework | Java 17 · Spring Boot 4.1.0 (mismas versiones que task-manager; imagen Docker Temurin 21) · Web MVC · Data JPA · Validation · Security · Actuator |
| Persistencia | PostgreSQL · **Flyway** (`db/migration`) · `ddl-auto=validate` |
| Mapeo | MapStruct (+ `lombok-mapstruct-binding`) |
| Boilerplate | Lombok (`@Getter`, `@RequiredArgsConstructor`, `@Slf4j`); **nunca** `@Data` ni setters en dominio |
| Seguridad | Spring Security 6 · JWT doble token (jjwt) · **sin roles** (todo usuario es cajero) · Bucket4j · CORS |
| Docs API | springdoc-openapi · Swagger en `/api/v1/api-docs/swagger-ui.html` · una anotación de documentación por endpoint |
| Config | `server.servlet.context-path=/api/v1` · perfiles `local`, `docker`, `dev`, `prod` en `application-<perfil>.properties` (como task-manager) |
| Calidad | Spotless (Google Java Format AOSP) · JaCoCo · SonarCloud (opcional) |
| Tests | JUnit 5 · Mockito · AssertJ · Testcontainers (integración) |
| Entrega | Dockerfile multi-stage (Temurin 21 JRE) · Render · Neon (ramas `dev` / `main`) · GitHub Actions |

---

## 2. Arquitectura: Hexagonal por features + DDD

Paquete raíz: `com.diegovilla.pos`

```
com.diegovilla.pos
├── PosApplication.java
├── core/
│   ├── errors/          # dtos, exceptions, factories, handlers, translators (igual que task-manager)
│   ├── openapi/         # OpenApiConfig + common/*ApiResponse
│   ├── security/        # SecurityConfig, cors/, jwt/ (filter, service, ports, adapters, utils/PermissionValidator), ratelimit/
│   └── seeders/         # DatabaseSeeder (usuarios demo desde variables de entorno)
├── features/
│   ├── auth/            # login, refresh (sin modelo propio)
│   ├── user/            # UserModel (cajero), /users/me
│   ├── product/         # catálogo
│   └── sale/            # agregado principal
└── utils/
```

### Estructura de cada feature (la de task-manager, sin carpetas vacías)
```
features/<feature>/
├── domain/          model/ · valueobjects/ · exceptions/
├── application/     commands/ · ports/ · services/ (· dto/response/ si hay proyecciones compuestas)
└── infrastructure/  controllers/ · docs/ · dto/request|response/ · mappers/ · entity/ · repository/ · adapters/ (· exceptions/ si hay constraints de BD que traducir)
```
Solo se crea una carpeta cuando un caso de uso la necesita.

### Reglas de dependencia
```
infrastructure ──▶ application ──▶ domain
```
- `domain` no depende de Spring, JPA ni Jackson (excepción heredada: las excepciones extienden `ApiException`).
- `application` depende de `domain` y de **puertos**; el usuario autenticado llega por el puerto `AuthenticatedUserProvider`, nunca por `SecurityContextHolder`.
- `infrastructure` implementa puertos y expone HTTP.
- Entre features solo se usan **puertos** (`sale` → `ProductRepositoryPort`; `user` → `SaleRepositoryPort` si hiciera falta).

---

## 3. Convenciones (copiadas de task-manager)

- **Modelo**: `@Getter`, campos `private`, constructor privado con `Objects.requireNonNull`, `create(...)`, `reconstruct(...)`, métodos de negocio que lanzan excepciones de dominio. Colecciones expuestas con `List.copyOf`.
- **Commands**: `record`.
- **Services**: `@Service @Slf4j @RequiredArgsConstructor @Transactional(readOnly = true)`; `@Transactional` en escrituras; permisos con `PermissionValidator`; `log.info("Sale completed successfully. id={}", id)`.
- **Controllers**: `@Validated @RestController @RequestMapping @RequiredArgsConstructor @Tag`; endpoints protegidos con `authenticated()` en `SecurityConfig` (sin `@PreAuthorize`: no hay roles); `ResponseEntity`; 201 al crear, 204 en transiciones sin cuerpo, 200 resto; `@<Op><X>Documentation` en cada método.
- **DTOs**: `record` + `@Schema` + Bean Validation con mensaje.
- **Mappers**: MapStruct `componentModel = "spring"`; Entity → Model con `default` + `reconstruct`.
- **Errores**: se lanzan excepciones; los handlers devuelven `ErrorResponseDTO { timestamp, status, error, message, errors[] }` (también 401/403/429).
- **Persistencia**: el esquema lo define **Flyway** (`V1__init.sql`, `V2__seed_products.sql`…); nunca `ddl-auto=update`. Constraints con nombre (`uk_products_code`) y su `DatabaseExceptionResolver`.
- **Configuración**: toda en `application.properties` (común) + `application-<perfil>.properties`, como task-manager. `local` y `docker` llevan sus valores directamente (son de desarrollo). En `dev` y `prod` los valores sensibles se escriben como `${VARIABLE}` y los resuelve Spring con las variables de entorno de Render: nunca se commitean credenciales reales.
- **Javadoc** en clases y métodos públicos (`@since 1.0.0`).
- **`.properties` y caracteres no ASCII**: Spring los lee como ISO-8859-1; escribir tildes como `\u00f3` (p. ej. `L\u00f3pez`).
- **Dinero**: `BigDecimal` escala 2, `HALF_EVEN`; `NUMERIC(12,2)`. **Tiempo**: `Instant` UTC. **IDs**: `UUID`.
- `./mvnw spotless:apply` antes de terminar.

## 4. Principios
- **SOLID**, **DRY** (errores, respuestas OpenAPI y seguridad en `core`), **KISS**, **YAGNI**.
- Patrones usados: Hexagonal (Ports & Adapters), Aggregate Root, Value Object, Factory Method, Repository, Command, DTO + Mapper, Strategy (`DatabaseExceptionResolver`), Chain of Responsibility (filtros de seguridad), Meta-annotation (docs OpenAPI).
- Skills generales: `clean-code`, `conventional-commits`.

## 5. Testing (estilo task-manager)
Unit por capa (`ModelTest`, `ServiceTest`, `ControllerTest`, `RepositoryAdapterTest`, `core`) + **integración con Testcontainers** para los flujos críticos (`*IT`). `@DisplayName("Should …")`, AssertJ, JaCoCo ≥ 80 % en `domain`/`application`. Detalle: [`docs/06-testing.md`](docs/06-testing.md).

## 6. Git y entrega
- Ramas: `feature/*` → PR a `dev` → PR a `main`. `dev` despliega a **dev**, `main` a **prod** (ver [`docs/07-despliegue.md`](docs/07-despliegue.md)).
- **Conventional Commits siempre** (`feat(sale): …`, `fix(auth): …`, `docs: …`). No commitear ni pushear sin que el usuario lo pida.

## 7. Flujo del agente
1. Leer el doc relevante de `docs/`.
2. Skills: `new-feature`, `new-endpoint`, `write-tests`, `update-ai-log`, `clean-code`, `conventional-commits`.
3. Antes de terminar: `./mvnw spotless:apply && ./mvnw verify`.
4. **Al terminar cada petición, actualizar [`AI_LOG.md`](AI_LOG.md)** (skill `update-ai-log`).
