# 03 · Arquitectura y persistencia

> Reglas obligatorias en [`CLAUDE.md`](../CLAUDE.md). Aquí el **porqué** y el árbol completo.

## 1. ¿Por qué hexagonal + DDD (y por qué no más)?
- El valor está en las **reglas de la venta**: deben estar aisladas y testearse sin Spring ni BD → dominio puro + puertos.
- **Misma forma que task-manager**: consistencia con el estilo propio, nada que aprender.
- **Y nada más**: sin CQRS, sin eventos, sin capas extra. Dos features, un agregado, un puerto de lectura entre ellas.

## 2. Flujo de una petición

```
Front ─▶ CORS ─▶ Controller ─(DTO→Command)─▶ Service ─▶ Port ─▶ Adapter ─▶ JPA ─▶ PostgreSQL
                    ▲                          │
                    └──(Model→ResponseDTO)─────┘    errores → @RestControllerAdvice → ErrorResponseDTO
```

## 3. Árbol completo

```
src/main/java/com/diegovilla/pos/
├── PosApplication.java
├── core/
│   ├── config/CorsConfig.java
│   ├── errors/
│   │   ├── dtos/{ErrorResponseDTO, FieldErrorDTO}.java
│   │   ├── exceptions/{ApiException, DomainException, ResourceNotFoundException}.java
│   │   ├── factories/{ErrorResponseFactory, FieldErrorFactory}.java
│   │   └── handlers/{ApiExceptionHandler, SpringExceptionHandler}.java
│   ├── openapi/
│   │   ├── OpenApiConfig.java
│   │   └── common/{BadRequest, NotFound, Conflict, UnprocessableEntity, InternalServerError}ApiResponse.java
│   └── seeders/DatabaseSeeder.java
└── features/
    ├── product/
    │   ├── domain/
    │   │   ├── model/ProductModel.java
    │   │   └── exceptions/ProductNotFoundException.java
    │   ├── application/
    │   │   ├── ports/ProductRepositoryPort.java          # Optional<ProductModel> getByCode(String) · save(...) (seeder)
    │   │   └── services/ProductService.java              # getByCode
    │   └── infrastructure/
    │       ├── controllers/ProductController.java
    │       ├── docs/GetProductByCodeDocumentation.java
    │       ├── dto/response/ProductResponseDTO.java
    │       ├── mappers/{ProductDtoMapper, ProductEntityMapper}.java
    │       ├── entity/ProductEntity.java
    │       ├── repository/ProductJpaRepository.java      # Optional<ProductEntity> findByCode(String)
    │       └── adapters/ProductRepositoryAdapter.java
    └── sale/
        ├── domain/
        │   ├── model/{SaleModel, SaleLine}.java
        │   ├── valueobjects/{SaleStatus, Quantity}.java
        │   └── exceptions/{SaleNotFoundException, SaleLineNotFoundException, SaleNotOpenException,
        │                   EmptySaleException, InvalidQuantityException}.java
        ├── application/
        │   ├── commands/{SaleAddLineCommand, SaleUpdateLineCommand}.java
        │   ├── ports/SaleRepositoryPort.java             # getById · save
        │   └── services/SaleService.java                 # open, getById, addLine, updateLine, removeLine, complete
        └── infrastructure/
            ├── controllers/SaleController.java
            ├── docs/{OpenSale, GetSale, AddSaleLine, UpdateSaleLine, RemoveSaleLine, CompleteSale}Documentation.java
            ├── dto/request/{SaleAddLineRequestDTO, SaleUpdateLineRequestDTO}.java
            ├── dto/response/{SaleResponseDTO, SaleLineResponseDTO}.java
            ├── mappers/{SaleDtoMapper, SaleEntityMapper}.java
            ├── entity/{SaleEntity, SaleLineEntity}.java
            ├── repository/SaleJpaRepository.java         # findById con @EntityGraph("lines")
            └── adapters/SaleRepositoryAdapter.java

src/main/resources/
├── application.properties          # context-path, swagger, jackson, perfil por defecto local
├── application-local.properties    # localhost:5432
└── application-docker.properties   # host del contenedor
```

## 4. Agregado de ejemplo (guía de estilo)

```java
@Getter
public class SaleModel {
    private final UUID id;
    private SaleStatus status;
    private final List<SaleLine> lines;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private SaleModel(...) { /* Objects.requireNonNull(...) */ }

    public static SaleModel create() {
        Instant now = Instant.now();
        return new SaleModel(UUID.randomUUID(), SaleStatus.OPEN, new ArrayList<>(), now, now, null);
    }

    public static SaleModel reconstruct(...) { ... }

    public void addProduct(ProductModel product, int quantity) {
        ensureOpen();
        findLineByProduct(product.getId()).ifPresentOrElse(
                line -> line.increase(quantity),                 // Quantity valida ≤ 999
                () -> lines.add(SaleLine.snapshotOf(product, quantity)));
        touch();
    }

    public void complete() {
        ensureOpen();
        if (lines.isEmpty()) throw new EmptySaleException();
        status = SaleStatus.COMPLETED;
        completedAt = Instant.now();
        touch();
    }

    public BigDecimal total() {
        return lines.stream().map(SaleLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<SaleLine> getLines() { return List.copyOf(lines); }

    private void ensureOpen() {
        if (status != SaleStatus.OPEN) throw new SaleNotOpenException();
    }
}
```

## 5. Persistencia

```
products
  id UUID PK · code VARCHAR(14) NOT NULL UNIQUE (uk_products_code) · name VARCHAR(120) NOT NULL
  price NUMERIC(12,2) NOT NULL · created_at · updated_at

sales
  id UUID PK · status VARCHAR(20) NOT NULL · created_at · updated_at · completed_at

sale_lines
  id UUID PK · sale_id UUID FK → sales ON DELETE CASCADE · product_id UUID FK → products
  product_code VARCHAR(14) · product_name VARCHAR(120) · unit_price NUMERIC(12,2)   -- snapshot
  quantity INT NOT NULL · position INT NOT NULL
```
- `SaleEntity` `@OneToMany(mappedBy = "sale", cascade = ALL, orphanRemoval = true) @OrderBy("position")`.
- `ddl-auto=update` (como task-manager en local/docker). Sin Flyway: no hay entornos que migrar.
- `DatabaseSeeder` inserta ~20 productos **idénticos a los fixtures MSW del front** si la tabla está vacía.
- `docker-compose.yml` con un único servicio `postgres`.

## 6. Decisiones (ADRs)

| # | Decisión | Descartado | Motivo |
|---|---|---|---|
| ADR-01 | Hexagonal por features, estilo task-manager | Capas clásicas | Aislar dominio; consistencia |
| ADR-02 | `SaleLine` dentro del agregado, sin repositorio | Repositorio de líneas | Las reglas abarcan toda la venta |
| ADR-03 | Endpoints de comando por línea | `PUT` del carrito entero | Intención explícita, el servidor fija el precio |
| ADR-04 | El cliente envía **código + cantidad**, nunca precio | Enviar precio | Seguridad del precio |
| ADR-05 | `ErrorResponseDTO` de task-manager | `ProblemDetail` | Consistencia con el front y con el estilo propio |
| ADR-06 | `PATCH /{id}/complete` | `POST` | Mismo estilo que `PATCH /tasks/{id}/complete` |
| ADR-07 | **Sin** auth, usuarios, rate limit, caché, Flyway, `@Version`, CI/CD | Copiarlo todo de task-manager | La prueba no lo pide; se explica en entrevista (`06-roadmap.md`) |
