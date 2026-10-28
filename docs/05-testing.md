# 05 · Testing (Backend)

Mismo enfoque que task-manager: **JUnit 5 + Mockito + AssertJ**, tests unitarios por capa, `@DisplayName("Should …")`. Sin tests de integración con contenedores (fuera de alcance).

## 1. Niveles

| Nivel | Cómo | Ubicación (espejo de `main`) |
|---|---|---|
| Dominio | JUnit + AssertJ, sin Spring ni mocks | `features/<f>/domain/<X>ModelTest` |
| Aplicación | `@ExtendWith(MockitoExtension.class)`, `@Mock` puertos, `@InjectMocks` service | `features/<f>/application/<X>ServiceTest` |
| Controller | Mockito sobre service y mapper; status y body | `features/<f>/infrastructure/controllers/<X>ControllerTest` |
| Adapter | Mockito sobre `JpaRepository` y mappers | `features/<f>/infrastructure/adapters/<X>RepositoryAdapterTest` |
| Core | Handlers y factories de errores | `core/errors/**` |

## 2. Casos mínimos

### `SaleModelTest` (el más importante)
- should create open sale without lines
- should add new line with price snapshot
- should increase quantity when the same product is added again
- should reject quantity above 999, also when adding to an existing line
- should change quantity / remove line
- should complete a sale with lines and set completedAt
- should reject completing an empty sale
- should reject any change when the sale is not open
- should calculate total with BigDecimal precision (`0.10 × 3 = 0.30`)

### `ProductServiceTest`
- should return product by code
- should throw ProductNotFoundException when code does not exist

### `SaleServiceTest`
- should open a sale
- should add a line fetching the product by code
- should throw 404 when sale or product does not exist
- should update / remove a line
- should complete a sale

### Controllers y adapters
Cada endpoint: status (200/201) y DTO mapeado. Adapters: `findByCode`/`findById` mapeados a modelo y `save` devuelve el modelo.

## 3. Ejemplo (estilo task-manager)

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepositoryPort productRepositoryPort;
    @InjectMocks private ProductService productService;

    @Test
    @DisplayName("Should retrieve a product by its code successfully")
    void shouldGetProductByCode() {
        ProductModel product = ProductModel.create("8410001000011", "Leche entera 1L", new BigDecimal("0.95"));
        when(productRepositoryPort.getByCode("8410001000011")).thenReturn(Optional.of(product));

        ProductModel found = productService.getByCode("8410001000011");

        assertThat(found).isSameAs(product);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when code does not exist")
    void shouldRejectUnknownCode() {
        when(productRepositoryPort.getByCode("0000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getByCode("0000"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found.");
    }
}
```

## 4. Configuración
- `./mvnw test` · `./mvnw verify` (Spotless check + JaCoCo).
- JaCoCo: regla ≥ 80 % de líneas en paquetes `domain` y `application`.
- Datos de prueba con `create(...)`/`reconstruct(...)`, nunca con entidades JPA.
- Dinero con `new BigDecimal("0.95")`.
