# CLAUDE.md — Reglas del agente (Backend · TPV Classora)

> Reglas obligatorias para cualquier agente o persona. Basadas en la forma de trabajo de
> [`task-manager`](https://github.com/DiegoVilla27/task-manager-system/tree/main/apps/task-manager): **copiar su estilo**, pero **solo lo que esta prueba necesita**.
> Documentación: [`docs/`](docs/). Skills: [`.claude/skills/`](.claude/skills/). Log de IA: [`AI_LOG.md`](AI_LOG.md).

---

## 0. Contexto y alcance

API REST mínima para la caja de supermercado: **consultar un producto por código** (lo que pide la prueba) y **gestionar una venta** (para poder explicar la integración completa con el front).
La prueba dice explícitamente que **no** hace falta un backend completo. Por eso:

> 🚫 **Anti-sobreingeniería.** No se añade nada que no esté en [`docs/02-casos-de-uso.md`](docs/02-casos-de-uso.md). Autenticación, usuarios, roles, rate limiting, caché, migraciones versionadas, bloqueo optimista, CI/CD y despliegue **no están en el alcance**: se explican en la entrevista (ver `docs/06-roadmap.md` → "Fuera de alcance") y, si llegan, se copian de task-manager.

---

## 1. Stack

| Ámbito | Elección |
|---|---|
| Lenguaje / framework | Java 21 · Spring Boot (misma versión que task-manager) · Web MVC · Data JPA · Validation |
| Persistencia | PostgreSQL en Docker (`docker-compose.yml`) · `ddl-auto=update` · seeder del catálogo |
| Mapeo | MapStruct (+ `lombok-mapstruct-binding`) |
| Boilerplate | Lombok (`@Getter`, `@RequiredArgsConstructor`, `@Slf4j`); **nunca** `@Data` ni setters en dominio |
| Docs API | springdoc-openapi · Swagger en `/api/v1/api-docs/swagger-ui.html` · una anotación de documentación por endpoint |
| Config | `server.servlet.context-path=/api/v1` · perfiles `local` y `docker` |
| Calidad | Spotless (Google Java Format AOSP) · JaCoCo |
| Tests | JUnit 5 · Mockito · AssertJ |

---

## 2. Arquitectura: Hexagonal por features + DDD

Paquete raíz: `com.diegovilla.pos`

```
com.diegovilla.pos
├── PosApplication.java
├── core/
│   ├── config/CorsConfig.java          # WebMvcConfigurer: permite http://localhost:[*]
│   ├── errors/
│   │   ├── dtos/                       # ErrorResponseDTO, FieldErrorDTO
│   │   ├── exceptions/                 # ApiException (abstracta), DomainException (422), ResourceNotFoundException (404)
│   │   ├── factories/                  # ErrorResponseFactory, FieldErrorFactory
│   │   └── handlers/                   # ApiExceptionHandler, SpringExceptionHandler
│   ├── openapi/
│   │   ├── OpenApiConfig.java
│   │   └── common/                     # @BadRequestApiResponse, @NotFoundApiResponse, @ConflictApiResponse,
│   │                                   # @UnprocessableEntityApiResponse, @InternalServerErrorApiResponse
│   └── seeders/DatabaseSeeder.java     # catálogo ficticio (mismo que los mocks del front)
└── features/
    ├── product/
    └── sale/
```

### Estructura de cada feature (la de task-manager, sin carpetas vacías)
```
features/<feature>/
├── domain/
│   ├── model/              # <X>Model: constructor privado + create(...) + reconstruct(...) + métodos de negocio
│   ├── valueobjects/       # enums y VOs
│   └── exceptions/         # una clase por error de negocio
├── application/
│   ├── commands/           # records inmutables (solo si el caso de uso recibe datos)
│   ├── ports/              # <X>RepositoryPort
│   └── services/           # <X>Service (casos de uso)
└── infrastructure/
    ├── controllers/        # @RestController delgado
    ├── docs/               # @<Op><X>Documentation por endpoint
    ├── dto/request|response/
    ├── mappers/            # <X>DtoMapper, <X>EntityMapper (MapStruct)
    ├── entity/             # @Entity (nunca sale de infrastructure)
    ├── repository/         # <X>JpaRepository
    └── adapters/           # <X>RepositoryAdapter implements <X>RepositoryPort
```
No se crean `specifications/`, `exceptions/` de BD, `dto/response` de aplicación, etc. **hasta que un caso de uso los necesite**.

### Reglas de dependencia
```
infrastructure ──▶ application ──▶ domain
```
- `domain` no depende de Spring, JPA ni Jackson (excepción heredada de task-manager: las excepciones extienden `ApiException`, que lleva `HttpStatus`).
- `application` depende de `domain` y de **puertos**, nunca de `JpaRepository` ni de entidades.
- `infrastructure` implementa puertos y expone HTTP.
- `sale` usa `product` **solo a través de `ProductRepositoryPort`**.

---

## 3. Convenciones (copiadas de task-manager)

- **Modelo**: `@Getter`, campos `private`, constructor privado con `Objects.requireNonNull`, `create(...)` (valida, genera `UUID` e `Instant`), `reconstruct(...)` (rehidrata sin revalidar). Cambios de estado = métodos con nombre de negocio que lanzan `DomainException`. Colecciones expuestas como `List.copyOf(...)`.
- **Commands**: `record`.
- **Services**: `@Service @Slf4j @RequiredArgsConstructor @Transactional(readOnly = true)` en la clase; `@Transactional` en escrituras; `log.info("Sale completed successfully. id={}", id)`.
- **Controllers**: `@Validated @RestController @RequestMapping("/<recurso>") @RequiredArgsConstructor @Tag(...)`; `ResponseEntity`; 201 al crear, 200 resto; cada método con su `@<Op><X>Documentation`. Sin lógica.
- **DTOs**: `record` con `@Schema(description, example, requiredMode)` y Bean Validation con mensaje.
- **Mappers**: MapStruct `componentModel = "spring"`; Entity → Model con `default` + `reconstruct`.
- **Errores**: se **lanzan** excepciones; los handlers producen `ErrorResponseDTO { timestamp, status, error, message, errors[] }`. Nunca `ResponseEntity` de error en un controller.
- **Javadoc** en clases y métodos públicos (`@since 1.0.0`).
- **Dinero**: `BigDecimal` escala 2, `RoundingMode.HALF_EVEN`; en BD `NUMERIC(12,2)`. Nunca `double`.
- **Tiempo**: `Instant` UTC. **IDs**: `UUID`.
- `./mvnw spotless:apply` antes de terminar.

## 4. Principios
- **SOLID**, **DRY** (errores y respuestas OpenAPI comunes en `core`), **KISS**, **YAGNI** (la regla más importante en este repo).
- Patrones usados (y solo estos): Hexagonal (Ports & Adapters), Aggregate Root, Value Object, Factory Method (`create`/`reconstruct`), Repository, Command, DTO + Mapper, Meta-annotation (docs OpenAPI).
- Skills generales: `clean-code`, `conventional-commits`.

## 5. Testing (estilo task-manager)
`<X>ModelTest` (dominio, sin mocks) · `<X>ServiceTest` (Mockito) · `<X>ControllerTest` (Mockito) · `<X>RepositoryAdapterTest` (Mockito) · tests de `core/errors`.
`@DisplayName("Should …")` + método `should…`. AssertJ. JaCoCo ≥ 80 % en `domain` y `application`. Detalle: [`docs/05-testing.md`](docs/05-testing.md).

## 6. Flujo del agente
1. Leer el doc relevante de `docs/`.
2. Skills: `new-feature`, `new-endpoint`, `write-tests`, `update-ai-log`, `clean-code`, `conventional-commits`.
3. Antes de terminar: `./mvnw spotless:apply && ./mvnw verify`.
4. No commitear sin que el usuario lo pida. Conventional Commits.
5. **Al terminar cada petición, actualizar [`AI_LOG.md`](AI_LOG.md)** (skill `update-ai-log`).
