---
name: write-tests
description: Escribe tests backend al estilo task-manager (dominio sin mocks, service/controller/adapter con Mockito, integración con Testcontainers, AssertJ, @DisplayName "Should …"). Usar al añadir o cambiar comportamiento.
---

# write-tests (backend)

## Qué escribir
| Cambio | Test |
|---|---|
| Modelo / VO | `<X>ModelTest`: reglas y transiciones, sin mocks |
| Service | `<X>ServiceTest`: `@ExtendWith(MockitoExtension.class)`, `@Mock` puertos, `@InjectMocks` service; happy path + cada excepción |
| Controller | `<X>ControllerTest`: mocks de service y mapper; status y body |
| Adapter | `<X>RepositoryAdapterTest`: mapeo entity ⇄ model |
| Core (errores, JWT, rate limit, permisos) | test del componente tocado |
| Flujo crítico (auth, lookup de producto, checkout, permisos) | `*IT`: `@SpringBootTest` + MockMvc + PostgreSQL en Testcontainers + Flyway real |

## Plantilla
```java
@Test
@DisplayName("Should increase quantity when the same product is added again")
void shouldIncreaseQuantityWhenSameProductAdded() {
    ProductModel milk = ProductModel.create("8410001000011", "Leche entera 1L", new BigDecimal("0.95"));
    SaleModel sale = SaleModel.create(UUID.randomUUID());

    sale.addProduct(milk, 1);
    sale.addProduct(milk, 2);

    assertThat(sale.getLines()).hasSize(1);
    assertThat(sale.getLines().getFirst().getQuantity()).isEqualTo(3);
    assertThat(sale.total()).isEqualByComparingTo("2.85");
}
```

## Reglas
- Datos con `create(...)`/`reconstruct(...)` del dominio.
- AssertJ (`assertThat`, `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)`).
- Dinero con `new BigDecimal("0.95")` y `isEqualByComparingTo`.
- `verify(port).save(...)` solo cuando la interacción **es** el comportamiento.
- `./mvnw verify` en verde; JaCoCo ≥ 80 % en `domain`/`application`.
