# 06 · Testing (Backend)

Igual que task-manager: **unit por capa** (JUnit 5 + Mockito + AssertJ) + **integración con Testcontainers** para los flujos críticos, ambos en CI.

## 1. Niveles

| Nivel | Cómo | Ubicación (espejo de `main`) | Plugin |
|---|---|---|---|
| Dominio | JUnit + AssertJ, sin Spring ni mocks | `features/<f>/domain/<X>ModelTest` | Surefire |
| Aplicación | `@ExtendWith(MockitoExtension.class)`, `@Mock` puertos, `@InjectMocks` | `features/<f>/application/<X>ServiceTest` | Surefire |
| Controller | Mockito sobre service y mapper | `features/<f>/infrastructure/controllers/<X>ControllerTest` | Surefire |
| Adapter | Mockito sobre `JpaRepository` y mappers | `features/<f>/infrastructure/adapters/<X>RepositoryAdapterTest` | Surefire |
| Core | Errores, JWT, rate limit, `PermissionValidator` | `core/**` | Surefire |
| **Integración** | `@SpringBootTest` + `MockMvc` + **PostgreSQL en Testcontainers** + Flyway real | `integration/<Flujo>IT` | Failsafe |

## 2. Casos mínimos

### `SaleModelTest` (el más importante)
- should create open sale for cashier without lines
- should add new line with price snapshot
- should increase quantity when the same product is added again
- should reject quantity above 999, also when adding to an existing line
- should change quantity / remove line
- should complete a sale with lines and set closedAt
- should reject completing an empty sale
- should cancel an open sale
- should reject any change when the sale is not open
- should calculate total with BigDecimal precision (`0.10 × 3 = 0.30`)

### Services
- `AuthServiceTest`: login OK · credenciales inválidas → 401 · refresh OK · refresh inválido.
- `ProductServiceTest`: encontrado · no encontrado.
- `SaleServiceTest`: abrir · abrir con una ya abierta → 409 · current 200/404 · añadir línea · producto inexistente · venta de otro cajero → 403 · completar · cancelar.

### Integración (`*IT`)
| IT | Flujo |
|---|---|
| `AuthIT` | login cajero → `/users/me` · contraseña mala → 401 · sin token → 401 |
| `ProductLookupIT` | 400 código inválido · 404 · 200 con JSON exacto |
| `CheckoutFlowIT` | abrir → añadir 2× mismo producto → cambiar cantidad → eliminar → completar → 409 al modificar · abrir de nuevo → cancelar |

## 3. Convenciones
- `@DisplayName("Should …")` + método `should…`.
- Datos con `create(...)`/`reconstruct(...)`; dinero con `new BigDecimal("0.95")` + `isEqualByComparingTo`.
- `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)`.
- IT: un contenedor PostgreSQL compartido (`@Testcontainers` + `@ServiceConnection`), datos creados en cada test.

## 4. Configuración
- `./mvnw test` (unit) · `./mvnw verify` (unit + IT + Spotless check + JaCoCo check).
- JaCoCo: ≥ 80 % líneas en `domain` y `application`.
- CI: los dos niveles en cada PR (ver `07-despliegue.md`).
