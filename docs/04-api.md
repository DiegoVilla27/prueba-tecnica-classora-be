# 04 · API REST, contratos, errores y OpenAPI

Base: `http://localhost:8080/api/v1` · Swagger UI: `/api/v1/api-docs/swagger-ui.html` · Spec JSON: `/api/v1/api-docs`
Sin autenticación (fuera de alcance; ver `06-roadmap.md`). CORS: `http://localhost:[*]`.

## 1. Endpoints

| Método | Ruta | Body | Respuestas |
|---|---|---|---|
| GET | `/products/{code}` ⭐ | — | 200 `ProductResponseDTO` · 400 · 404 · 500 |
| POST | `/sales` | — | 201 `SaleResponseDTO` + `Location` |
| GET | `/sales/{id}` | — | 200 · 404 |
| POST | `/sales/{id}/lines` | `{ code, quantity }` | 200 venta · 400 · 404 · 409 · 422 |
| PATCH | `/sales/{id}/lines/{lineId}` | `{ quantity }` | 200 venta · 400 · 404 · 409 · 422 |
| DELETE | `/sales/{id}/lines/{lineId}` | — | 200 venta · 404 · 409 |
| PATCH | `/sales/{id}/complete` | — | 200 venta · 404 · 409 · 422 |

## 2. Contratos

### `ProductResponseDTO`
```json
{ "id": "6f1c1a7e-0b7a-4c55-9a0e-2b3f6c1d9e10", "code": "8410001000011", "name": "Leche entera 1L", "price": 0.95 }
```
`price` como número decimal (`spring.jackson.generator.write-bigdecimal-as-plain=true`). El front lo pasa a céntimos en su mapper.

### `SaleAddLineRequestDTO`
```java
@Schema(description = "Request body to add a product to a sale by its code")
public record SaleAddLineRequestDTO(
        @Schema(description = "Scannable product code", example = "8410001000011", requiredMode = REQUIRED)
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^\\d{4,14}$", message = "Code must contain 4 to 14 digits")
        String code,
        @Schema(description = "Units to add", example = "1", minimum = "1", maximum = "999", requiredMode = REQUIRED)
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity must be at most 999")
        Integer quantity) {}
```

### `SaleUpdateLineRequestDTO`
`{ "quantity": 3 }` con las mismas validaciones de cantidad.

### `SaleResponseDTO`
```json
{
  "id": "0d9f…",
  "status": "OPEN",
  "lines": [
    { "id": "…", "productId": "…", "code": "8410001000011", "name": "Leche entera 1L",
      "unitPrice": 0.95, "quantity": 2, "subtotal": 1.90 }
  ],
  "itemCount": 2,
  "total": 1.90,
  "createdAt": "2026-10-28T14:30:00Z",
  "completedAt": null
}
```

## 3. Errores (formato único de task-manager)

```json
{
  "timestamp": "2026-10-28T14:32:15Z",
  "status": 400,
  "error": "Bad Request",
  "message": "The request contains validation errors.",
  "errors": [ { "field": "code", "message": "Code must contain 4 to 14 digits" } ]
}
```

| Origen | Handler | HTTP |
|---|---|---|
| Bean Validation (`@Valid`, `@Pattern` en `@PathVariable`), JSON malformado, UUID inválido | `SpringExceptionHandler` | 400 (con `errors[]` si aplica) |
| `ResourceNotFoundException` y subclases (`ProductNotFoundException`, `SaleNotFoundException`, `SaleLineNotFoundException`) | `ApiExceptionHandler` | 404 |
| `SaleNotOpenException` | `ApiExceptionHandler` | 409 |
| `DomainException` y subclases (`EmptySaleException`, `InvalidQuantityException`) | `ApiExceptionHandler` | 422 |
| Cualquier otra | `SpringExceptionHandler` (fallback, sin stacktrace, log `error`) | 500 |

## 4. ⭐ Respuesta para la entrevista: "endpoint para consultar un producto por código"
1. **Ruta**: `GET /api/v1/products/{code}`. Recurso identificado por su clave natural de negocio; GET es idempotente y cacheable por HTTP.
2. **Validación**: `@Pattern("^\\d{4,14}$")` en el path → 400 con el detalle del campo, sin tocar la BD.
3. **200**: DTO mínimo para la caja (`id`, `code`, `name`, `price`). Nunca la entidad JPA (no se filtra el modelo de persistencia).
4. **404** con `ErrorResponseDTO` si no existe → el front muestra "No existe ningún producto con el código…".
5. **500** genérico ante fallos técnicos, sin detalles internos; se registra con traza.
6. **Rendimiento**: índice único en `code` (búsqueda O(log n)).
7. **Capas**: `ProductController` → `ProductService.getByCode` → `ProductRepositoryPort` → `ProductRepositoryAdapter` (JPA). El dominio no sabe de HTTP ni de JPA.
8. **Documentación**: `@GetProductByCodeDocumentation` con ejemplos 200/400/404/500.
9. **Tests**: `ProductServiceTest` (encontrado / no encontrado), `ProductControllerTest`, `ProductRepositoryAdapterTest`.
10. **Si escalara** (se menciona, no se implementa): JWT y roles, rate limiting, caché (Caffeine/Redis) e invalidación al editar precios. Todo ya resuelto en task-manager.

## 5. OpenAPI (estilo task-manager)
- `OpenApiConfig`: info, contacto, licencia, servidor local. Sin esquema de seguridad.
- `core/openapi/common/*ApiResponse`: meta-anotaciones con ejemplo de `ErrorResponseDTO` por status.
- Una anotación por endpoint en `infrastructure/docs`:
```java
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @Documented
@Operation(summary = "Get product by code",
           description = "Retrieves a product by its scannable code. Used by the checkout on every scan.")
@ApiResponse(responseCode = "200", description = "Product found.",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
@BadRequestApiResponse @NotFoundApiResponse @InternalServerErrorApiResponse
public @interface GetProductByCodeDocumentation {}
```
- DTOs con `@Schema` en cada campo; `springdoc.swagger-ui.operationsSorter=method`.
