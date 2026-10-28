# 01 · Negocio y dominio (DDD)

## 1. Contexto y objetivo

La prueba (Parte 3) pregunta cómo integrarías el front con un backend Java. En lugar de solo explicarlo, se entrega **un backend real, funcional y desplegado** que el front consume en todos los entornos:

1. **`GET /products/{code}`** impecable (lo que preguntarán): validación, respuesta, errores, documentación y tests.
2. **Venta en servidor** con un agregado DDD real: el servidor es la fuente de verdad de precios y totales.
3. **Cajero autenticado** (JWT): cada venta pertenece al cajero que la abre. No hay roles: el único usuario del sistema es el cajero.
4. **Despliegue realista**: local con Docker, `dev` y `prod` en Render + Neon, CI/CD.

## 2. Requisitos

### Funcionales
| ID | Requisito | Prioridad |
|---|---|---|
| RF-B01 | Iniciar sesión (email + contraseña) y renovar sesión (refresh token) | Must |
| RF-B02 | Consultar el usuario autenticado | Must |
| RF-B03 | Consultar un producto por **código** | Must |
| RF-B04 | Abrir una venta (un cajero tiene como máximo una abierta) | Must |
| RF-B05 | Recuperar la venta abierta del cajero (tras recargar la página) | Must |
| RF-B06 | Añadir producto por código (suma si ya existe) | Must |
| RF-B07 | Cambiar la cantidad de una línea | Must |
| RF-B08 | Eliminar una línea | Must |
| RF-B09 | Finalizar la venta | Must |
| RF-B10 | Cancelar la venta ("vaciar venta" en la caja) | Must |
| RF-B11 | Consultar una venta por id | Should |

### No funcionales
| ID | Requisito | Cómo |
|---|---|---|
| RNF-B01 | Búsqueda por código rápida | Índice único en `products.code` |
| RNF-B02 | Exactitud monetaria | `BigDecimal` escala 2, `NUMERIC(12,2)` |
| RNF-B03 | Seguridad | JWT, BCrypt, propiedad de la venta, rate limiting, CORS, secretos por entorno |
| RNF-B04 | El cliente no fija precios | Líneas por **código + cantidad**; precio del catálogo |
| RNF-B05 | Concurrencia | Bloqueo optimista (`@Version`) en la venta |
| RNF-B06 | Errores consistentes | `ErrorResponseDTO` único (task-manager) |
| RNF-B07 | Contrato documentado | OpenAPI con ejemplos y errores por endpoint |
| RNF-B08 | Esquema versionado | Flyway en todos los entornos |
| RNF-B09 | Calidad | Spotless, JaCoCo ≥ 80 % dominio/aplicación, CI en cada PR |
| RNF-B10 | Operable | Actuator `/health` (Render), logs por operación, perfiles por entorno |

## 3. Bounded contexts → features

```
                    ┌──────────────┐
                    │     auth     │  login / refresh
                    └──────┬───────┘
                           │ UserRepositoryPort
┌──────────────┐    ┌──────▼───────┐
│     sale     │───▶│     user     │  cajeros
│   (core)     │    └──────────────┘
│              │    ┌──────────────┐
│              │───▶│   product    │  catálogo (solo lectura)
└──────────────┘    └──────────────┘
```
- **Core domain:** `sale`. **Supporting:** `product`. **Generic:** `user`, `auth`.

## 4. Modelo de dominio

### `UserModel` (feature `user`, como task-manager)
`id`, `firstName`, `lastName`, `email` (único), `passwordHash`, timestamps. **Sin roles**: todo usuario es un cajero (lo que el enunciado describe).
Los usuarios **no** se registran: el `DatabaseSeeder` crea **un cajero** por entorno con sus datos (`SEED_CASHIER_FIRST_NAME`, `SEED_CASHIER_LAST_NAME`, `SEED_CASHIER_EMAIL`, `SEED_CASHIER_PASSWORD`).

### `ProductModel` (feature `product`)
`id`, `code` (solo dígitos, 4–14, único), `name`, `price` (`BigDecimal` ≥ 0), timestamps. Catálogo cargado por migración Flyway (`V2__seed_products.sql`), idéntico a los fixtures MSW del front.

### `SaleModel` (agregado raíz, feature `sale`)
| Campo | Regla |
|---|---|
| id | UUID |
| cashierId | Usuario que la abrió |
| status | `SaleStatus`: `OPEN → COMPLETED` · `OPEN → CANCELLED` |
| lines | `List<SaleLine>`, una por producto, en orden de escaneo |
| version | Bloqueo optimista |
| createdAt / updatedAt / closedAt | Instant |
| total / itemCount | **Derivados** (no se guardan) |

`SaleLine` (entidad interna): `id`, `productId`, `productCode`, `productName`, `unitPrice` (**snapshot**), `quantity` (1–999), `subtotal()`.

```java
SaleModel.create(cashierId)
sale.addProduct(ProductModel product, int quantity)   // RN-01, RN-02, RN-04
sale.changeQuantity(UUID lineId, int quantity)         // RN-02
sale.removeLine(UUID lineId)
sale.complete()                                        // RN-05, RN-06
sale.cancel()                                          // RN-06
sale.total(); sale.itemCount();
```

### Value objects
`SaleStatus` (enum) y `Quantity` (`record` que valida 1–999). Sin VO `Money`/`ProductCode` (KISS: `BigDecimal` y validación en DTO bastan).

## 5. Reglas de negocio

| ID | Regla | Dónde | Error |
|---|---|---|---|
| RN-01 | Un producto = una línea; añadirlo otra vez suma cantidad | `addProduct` | — |
| RN-02 | Cantidad por línea 1–999 (también al sumar) | `Quantity` | `InvalidQuantityException` 422 |
| RN-03 | Subtotal = precio × cantidad; total = Σ subtotales | `SaleLine`, `SaleModel` | — |
| RN-04 | El precio se **congela** al añadir la línea | `addProduct` | — |
| RN-05 | No se finaliza una venta vacía | `complete()` | `EmptySaleException` 422 |
| RN-06 | Solo una venta `OPEN` se modifica, finaliza o cancela | todos los métodos | `SaleNotOpenException` 409 |
| RN-07 | Código: solo dígitos, 4–14 | `@Pattern` | 400 |
| RN-08 | Un cajero tiene como máximo **una** venta `OPEN` | `SaleService.open` + índice parcial único | `OpenSaleAlreadyExistsException` 409 |
| RN-09 | Un cajero solo opera **sus** ventas | `PermissionValidator` | 403 |
| RN-10 | Edición concurrente de la misma venta | `@Version` | 409 |

## 6. Glosario (compartido con el front)
Venta (Sale) · Línea (SaleLine) · Producto (Product) · Código · Escanear · Finalizar (complete) · Cancelar/Vaciar (cancel) · Subtotal/Importe · Total · Cajero (usuario del sistema).
