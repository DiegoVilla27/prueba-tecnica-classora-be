# 01 · Negocio y dominio (DDD)

## 1. Contexto y objetivo

La prueba (Parte 3) pide explicar **cómo integrarías el front con un backend Java**, por ejemplo "un endpoint que permita consultar un producto mediante su código, qué devolvería y cómo gestionarías errores". **No exige backend completo.**

Objetivo de este repo, en orden de prioridad:
1. **`GET /products/{code}`** bien hecho: validación, respuesta, errores, documentación y tests. Es lo que preguntarán.
2. **La venta en servidor** (abrir, añadir, cambiar cantidad, eliminar, finalizar) para demostrar DDD con un agregado real y para que el front pueda pasar a "fase 2".
3. Nada más. Lo que no aporte a 1 o 2 es sobreingeniería para esta prueba.

## 2. Requisitos

### Funcionales
| ID | Requisito | Prioridad |
|---|---|---|
| RF-B01 | Consultar un producto por **código** | **Must** |
| RF-B02 | Abrir una venta | Should |
| RF-B03 | Añadir producto por código (suma si ya existe) | Should |
| RF-B04 | Cambiar la cantidad de una línea | Should |
| RF-B05 | Eliminar una línea | Should |
| RF-B06 | Finalizar la venta (inmutable a partir de ahí) | Should |
| RF-B07 | Consultar una venta | Should |

### No funcionales
| ID | Requisito | Cómo |
|---|---|---|
| RNF-B01 | Búsqueda por código rápida | Índice único en `products.code` |
| RNF-B02 | Exactitud monetaria | `BigDecimal` escala 2, `NUMERIC(12,2)` |
| RNF-B03 | Errores consistentes | `ErrorResponseDTO` único (task-manager) |
| RNF-B04 | Contrato documentado | OpenAPI con ejemplos y errores por endpoint |
| RNF-B05 | Entrada validada | Bean Validation + regex en path variables |
| RNF-B06 | El cliente no fija precios | Las líneas se crean con **código + cantidad**; el precio sale del catálogo |
| RNF-B07 | Calidad | Spotless, JaCoCo ≥ 80 % en dominio/aplicación |
| RNF-B08 | Arranque sencillo | `docker compose up` + `./mvnw spring-boot:run` |

## 3. Bounded contexts → features

```
┌──────────────┐  ProductRepositoryPort  ┌──────────────┐
│     sale     │ ──────────────────────▶ │   product    │
│  (core)      │                         │  (catálogo)  │
└──────────────┘                         └──────────────┘
```
- **`sale`** es el *core domain*: ahí están las reglas.
- **`product`** es *supporting*: el catálogo, solo lectura para la caja.

## 4. Modelo de dominio

### `ProductModel` (feature `product`)
| Campo | Tipo | Regla |
|---|---|---|
| id | UUID | — |
| code | String | Solo dígitos, 4–14, **único** |
| name | String | No vacío |
| price | BigDecimal | ≥ 0, escala 2 |
| createdAt / updatedAt | Instant | — |

Solo `create(...)` (seeder) y `reconstruct(...)`. Sin edición: no hay casos de uso de gestión del catálogo.

### `SaleModel` (agregado raíz, feature `sale`)
| Campo | Tipo | Regla |
|---|---|---|
| id | UUID | — |
| status | `SaleStatus` | `OPEN → COMPLETED` |
| lines | `List<SaleLine>` | Una por producto, en orden de escaneo |
| createdAt / updatedAt / completedAt | Instant | — |
| total | — | **Derivado**: Σ subtotales (no se guarda) |

`SaleLine` (entidad interna, sin repositorio propio): `id`, `productId`, `productCode`, `productName`, `unitPrice` (**snapshot** al añadir), `quantity` (1–999), `subtotal()` derivado.

```java
sale.addProduct(ProductModel product, int quantity)  // RN-01, RN-02, RN-04
sale.changeQuantity(UUID lineId, int quantity)        // RN-02
sale.removeLine(UUID lineId)
sale.complete()                                       // RN-05, RN-06
sale.total(); sale.itemCount();
```

### Value objects
- `SaleStatus` (enum: `OPEN`, `COMPLETED`).
- `Quantity` (`record Quantity(int value)` que valida 1–999).
- Sin VO `Money` ni `ProductCode`: `BigDecimal` + validación en DTO bastan aquí (KISS). Si el dominio creciera (monedas, descuentos), se extraen.

## 5. Reglas de negocio

| ID | Regla | Dónde | Error |
|---|---|---|---|
| RN-01 | Un producto = una línea; añadirlo otra vez suma cantidad | `SaleModel.addProduct` | — |
| RN-02 | Cantidad por línea 1–999 (también al sumar) | `Quantity` | `InvalidQuantityException` 422 |
| RN-03 | Subtotal = precio × cantidad; total = Σ subtotales | `SaleLine`, `SaleModel` | — |
| RN-04 | El precio se **congela** en la línea al añadirla | `addProduct` | — |
| RN-05 | No se finaliza una venta vacía | `complete()` | `EmptySaleException` 422 |
| RN-06 | Solo una venta `OPEN` se puede modificar | todos los métodos | `SaleNotOpenException` 409 |
| RN-07 | Código: solo dígitos, 4–14 | `@Pattern` en el DTO / path | 400 |

## 6. Glosario (compartido con el front)
Venta (Sale) · Línea (SaleLine) · Producto (Product) · Código · Escanear · Finalizar (complete) · Subtotal/Importe · Total.
