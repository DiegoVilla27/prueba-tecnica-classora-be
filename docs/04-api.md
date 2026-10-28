# 04 · API REST, contratos, errores y OpenAPI

| Entorno | Base URL | Swagger |
|---|---|---|
| local / docker | `http://localhost:8080/api/v1` | `/api/v1/api-docs/swagger-ui.html` |
| dev | `https://pos-api-dev-k0y9.onrender.com/api/v1` | ídem |
| prod | `https://pos-api-prod.onrender.com/api/v1` | ídem |

Auth: `Authorization: Bearer <accessToken>` salvo `/auth/**`, `/api-docs/**` y `/actuator/health`.

## 1. Endpoints

| Método | Ruta | Auth | Body | Respuestas |
|---|---|---|---|---|
| POST | `/auth/login` | público | `{ email, password }` | 200 · 400 · 401 · 429 |
| POST | `/auth/refresh` | público | `{ refreshToken }` | 200 · 400 · 401 |
| GET | `/users/me` | ✅ | — | 200 · 401 |
| GET | `/products/{code}` ⭐ | ✅ | — | 200 · 400 · 401 · 404 · 429 |
| POST | `/sales` | ✅ | — | 201 + `Location` · 401 · 409 |
| GET | `/sales/current` | ✅ | — | 200 · 401 · 404 |
| GET | `/sales/{id}` | ✅ (suya) | — | 200 · 401 · 403 · 404 |
| POST | `/sales/{id}/lines` | ✅ (suya) | `{ code, quantity }` | 200 · 400 · 401 · 403 · 404 · 409 · 422 |
| PATCH | `/sales/{id}/lines/{lineId}` | ✅ (suya) | `{ quantity }` | 200 · 400 · 401 · 403 · 404 · 409 · 422 |
| DELETE | `/sales/{id}/lines/{lineId}` | ✅ (suya) | — | 200 · 401 · 403 · 404 · 409 |
| PATCH | `/sales/{id}/complete` | ✅ (suya) | — | 200 · 401 · 403 · 404 · 409 · 422 |
| PATCH | `/sales/{id}/cancel` | ✅ (suya) | — | 204 · 401 · 403 · 404 · 409 |
| GET | `/actuator/health` | público | — | 200 |

✅ = requiere `Bearer`; (suya) = la venta debe pertenecer al cajero autenticado, si no 403.

## 2. Contratos

### `AuthResponseDTO`
```json
{ "accessToken": "eyJ…", "refreshToken": "eyJ…", "tokenType": "Bearer", "expiresIn": 3600 }
```

### `UserMeResponseDTO`
```json
{ "id": "…", "firstName": "Ana", "lastName": "López", "email": "cajero@pos.demo" }
```

### `ProductResponseDTO`
```json
{ "id": "6f1c1a7e-…", "code": "8412345000010", "name": "Leche entera 1L", "price": 0.95 }
```
`price` es un número JSON con 2 decimales (`NUMERIC(12,2)` → `BigDecimal` escala 2, p. ej. `0.10`); el front lo pasa a céntimos.

### `SaleAddLineRequestDTO`
```java
@Schema(description = "Request body to add a product to a sale by its code")
public record SaleAddLineRequestDTO(
        @Schema(description = "Scannable product code", example = "8412345000010", requiredMode = REQUIRED)
        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^\\d{4,14}$", message = "Code must contain 4 to 14 digits")
        String code,
        @Schema(description = "Units to add", example = "1", minimum = "1", maximum = "999", requiredMode = REQUIRED)
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity must be at most 999")
        Integer quantity) {}
```

### `SaleResponseDTO`
```json
{
  "id": "0d9f…",
  "status": "OPEN",
  "cashierId": "…",
  "lines": [
    { "id": "…", "productId": "…", "code": "8412345000010", "name": "Leche entera 1L",
      "unitPrice": 0.95, "quantity": 2, "subtotal": 1.90 }
  ],
  "itemCount": 2,
  "total": 1.90,
  "version": 3,
  "createdAt": "2026-10-28T14:30:00Z",
  "closedAt": null
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
| Bean Validation, JSON malformado, UUID inválido | `SpringExceptionHandler` | 400 |
| Token ausente/inválido/expirado, credenciales incorrectas | `SpringExceptionHandler` (vía `HandlerExceptionResolver` de `SecurityConfig`) | 401 |
| Venta de otro cajero | `PermissionValidator` | 403 |
| `ResourceNotFoundException` y subclases | `ApiExceptionHandler` | 404 |
| `SaleNotOpenException`, `OpenSaleAlreadyExistsException`, `ObjectOptimisticLockingFailureException`, constraints (`ux_sales_one_open_per_cashier`, `uk_sale_lines_sale_product` → `SaleDatabaseExceptionResolver`) | `ApiExceptionHandler` / `SpringExceptionHandler` / `DatabaseExceptionTranslator` | 409 |
| `DomainException` y subclases | `ApiExceptionHandler` | 422 |
| Bucket agotado | `RateLimitingFilter` | 429 + `Retry-After` |
| Cualquier otra | `SpringExceptionHandler` (sin stacktrace, log `error`) | 500 |

## 4. ⭐ Respuesta para la entrevista: "endpoint para consultar un producto por código"
1. **Ruta**: `GET /api/v1/products/{code}`: recurso por su clave natural de negocio; GET idempotente.
2. **Seguridad**: requiere JWT de cajero; rate limiting; CORS solo para el front.
3. **Validación**: `@Pattern("^\\d{4,14}$")` en el path → 400 con detalle del campo, sin tocar BD.
4. **200**: DTO mínimo (`id`, `code`, `name`, `price`), nunca la entidad JPA.
5. **404** con `ErrorResponseDTO` si no existe → el front lo muestra inline junto al input.
6. **500** genérico sin detalles internos; log con traza.
7. **Rendimiento**: índice único en `code`.
8. **Capas**: `ProductController` → `ProductService.getByCode` → `ProductRepositoryPort` → `ProductRepositoryAdapter` (JPA).
9. **Documentación**: `@GetProductByCodeDocumentation` con ejemplos 200/400/401/404/429/500.
10. **Tests**: service, controller, adapter + `ProductLookupIT` (Testcontainers).
11. **Siguiente paso si escalara**: caché (Caffeine/Redis) con invalidación al cambiar precios.

## 5. OpenAPI (estilo task-manager)
- `OpenApiConfig`: info, contacto, licencia, servidores (local, dev, prod) y esquema `Bearer Authentication`.
- `core/openapi/common/*ApiResponse`: meta-anotaciones con ejemplo de `ErrorResponseDTO`.
- Una anotación por endpoint en `infrastructure/docs`, por ejemplo:
```java
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @Documented
@Operation(summary = "Get product by code",
           description = "Retrieves a product by its scannable code. Used by the checkout on every scan.")
@ApiResponse(responseCode = "200", description = "Product found.",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
@BadRequestApiResponse @UnauthorizedApiResponse @NotFoundApiResponse
@TooManyRequestsApiResponse @InternalServerErrorApiResponse
public @interface GetProductByCodeDocumentation {}
```
