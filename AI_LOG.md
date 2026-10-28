# 🤖 AI_LOG — Backend

Registro del uso de inteligencia artificial en el backend. Complementa la **Parte 4** de la prueba y se actualiza al terminar cada petición (skill `update-ai-log`).

## Resumen

### Herramientas
| Herramienta | Uso |
|---|---|
| Claude Code (Claude Opus 5.5) | Análisis del enunciado, diseño DDD, documentación y skills; implementación y tests (próximos hitos) |

### Decisiones y correcciones humanas principales
| Qué propuso la IA | Qué decidió el autor | Por qué |
|---|---|---|
| Arquitectura en capas genérica | Hexagonal por features + DDD, copiando el estilo de `task-manager` | Forma de trabajo propia |
| Backend "completo" (JWT, roles, rate limit, caché, Flyway, `@Version`, Testcontainers, CI/CD…) | Recortar a lo que la prueba necesita: producto por código + venta | La prueba no exige backend; evitar sobreingeniería |

---

## Registro

### #01 · 2026-10-28 ~14:40 · Analizar la prueba y proponer un plan
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Leer el PDF de la prueba y armar un plan del sistema completo: requisitos, casos de uso, endpoints, DDD y arquitecturas.
- **Qué hizo la IA:** Propuso para la Parte 3 un backend Spring Boot hexagonal, endpoints REST (producto por código y venta), formato de error y estrategia de integración con el front.
- **Decisiones / correcciones humanas:** Ninguna en este paso.
- **Resultado:** Plan entregado en el chat (sin archivos). Hora aproximada: anterior a la creación de las carpetas (14:48).

### #02 · 2026-10-28 15:20 · Documentar el backend con el estilo de task-manager
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Crear los `.md` del backend con arquitectura hexagonal por features y DDD, copiando la forma de trabajo del repo `task-manager` (security, testing, OpenAPI, etc.).
- **Qué hizo la IA:** Analizó `task-manager` (estructura, modelos con `create`/`reconstruct`, services, controllers, anotaciones de documentación OpenAPI, errores, seguridad JWT/Bucket4j/CORS, tests). Creó `README.md`, `CLAUDE.md`, `docs/01…08` y las skills `new-feature`, `new-endpoint` y `write-tests`.
- **Decisiones / correcciones humanas:** Ninguna en este paso. Se corrigió en la petición #03.
- **Resultado:** Docs creados; sin código.

### #03 · 2026-10-28 15:40 · Recortar la sobreingeniería del backend
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Mucho de lo documentado en el back no se usará y es sobreingeniería. Ajustar los `.md`, extraer de task-manager reglas y skills generales y crear un log de IA con fecha y hora.
- **Qué hizo la IA:**
  - Redujo el alcance a 7 casos de uso: `GET /products/{code}` y abrir, consultar, añadir línea, cambiar cantidad, eliminar línea y finalizar venta.
  - Eliminó del alcance:
    - las features `auth` y `user`, JWT, roles y rate limiting;
    - Caffeine, Flyway, `@Version`, Testcontainers, perfiles `dev`/`prod`, CI/CD y despliegue;
    - CRUD de productos, cancelar venta y "una venta abierta por cajero";
    - los VOs `Money` y `ProductCode`.
  - Reescribió `CLAUDE.md` con una regla explícita de anti-sobreingeniería, y los docs `01`–`04`.
  - Consolidó persistencia en `03`, testing en `05` y roadmap en `06`, con una tabla "Fuera de alcance" que explica cómo se haría cada cosa en la entrevista.
  - Recortó las skills del proyecto.
  - Copió las skills agnósticas del autor (`clean-code`, `conventional-commits`) y añadió `update-ai-log`.
- **Decisiones / correcciones humanas:** El autor pidió quitar la sobreingeniería. La IA mantuvo Spotless y JaCoCo (baratos y parte del estándar del autor) y el feature `sale` (necesario para explicar la integración completa). Ambos quedan sujetos a revisión.
- **Resultado:** Docs y skills actualizados; sin código.

### #04 · 2026-10-28 15:48 · Commit y push de la documentación
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer commit y push en cada proyecto, siempre con Conventional Commits.
- **Qué hizo la IA:** Primer commit en `main` (`docs: add project documentation, agent rules and skills`) y push a `origin/main`; después este commit con la entrada del log.
- **Decisiones / correcciones humanas:** El autor fija Conventional Commits como norma para todos los commits.
- **Resultado:** Hecho; rama `main` publicada en GitHub.

### #05 · 2026-10-28 16:02 · Ampliar el alcance: backend completo, con JWT y desplegado
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Entregar todo funcional y realista: front y back, en local con Docker y en dev/prod (Render para el back), con autenticación JWT. Empezar por el back, ajustando primero los `.md`.
- **Qué hizo la IA:**
  - Recuperó del alcance lo que el despliegue real justifica:
    - features `auth` (login, refresh) y `user` (`/users/me`), roles `CASHIER`/`SUPERVISOR`;
    - perfiles `local`/`docker`/`dev`/`prod`, Flyway con `validate`, `@Version`;
    - Bucket4j, CORS por entorno, Actuator, secretos solo por entorno;
    - Testcontainers, Docker con la red `shared-network`, Render + Neon y GitHub Actions.
  - Añadió los casos de uso `GET /sales/current` y `PATCH /sales/{id}/cancel`, y la regla "una venta abierta por cajero".
  - Reescribió `CLAUDE.md`, `01`–`04` y `README`.
  - Creó `05-seguridad.md` y `07-despliegue.md`, y renumeró `06-testing.md` y `08-roadmap.md`.
  - Actualizó las skills (`@PreAuthorize`, Flyway, `*IT`).
- **Decisiones / correcciones humanas:** El autor elige auth con JWT y despliegue real; el back va primero.
- **Decisiones de la IA, sujetas a revisión:**
  - Usuarios sembrados por variables de entorno, sin registro.
  - Catálogo por migración Flyway.
  - Siguen fuera de alcance: CRUD de usuarios y catálogo, caché e informes.
  - Los secretos no se commitean (en task-manager hay credenciales de Neon en `application-prod.properties`).
- **Resultado:** Docs y skills actualizados; sin código; sin commit.

### #06 · 2026-10-28 16:06 · Quitar roles: un único usuario, el cajero
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El autor preguntó por qué había un supervisor si el PDF no lo menciona, y decidió mantener la autenticación pero sin roles: solo existe el cajero, con un seed de un cajero con sus datos.
- **Qué hizo la IA:**
  - Reconoció que el supervisor era un añadido propio sin respaldo en el enunciado ni uso en el front.
  - Eliminó `UserRole`, la columna `role`, el claim `role`, `@PreAuthorize`/`@EnableMethodSecurity`, el seed del supervisor y `PermissionsIT`.
  - Mantuvo la regla de propiedad: un cajero solo opera sus ventas (403).
  - El `DatabaseSeeder` crea un cajero desde `SEED_CASHIER_FIRST_NAME`, `SEED_CASHIER_LAST_NAME`, `SEED_CASHIER_EMAIL` y `SEED_CASHIER_PASSWORD`.
  - Movió "Roles" a "Fuera de alcance" del roadmap.
- **Decisiones / correcciones humanas:** Corrección del autor: sin roles; solo cajero; un cajero sembrado.
- **Resultado:** Docs y skills actualizados (`CLAUDE.md`, `01`–`08`, `README`, `new-endpoint`); sin código; sin commit.

### #07 · 2026-10-28 16:10 · Commit y push del nuevo alcance
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer commit y push en ambos repos.
- **Qué hizo la IA:** Commit `docs: expand scope to deployed backend with jwt auth` con los cambios de las peticiones #05 y #06, push a `origin/main` y este commit del log.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hecho.

### #08 · 2026-10-28 16:20 · Hito 1: setup del proyecto y core
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Empezar con el back (trabajando directamente en `main`).
- **Qué hizo la IA:**
  - Proyecto Maven con Spring Boot 4.1.1 y Java 21:
    - starters webmvc, data-jpa, validation, security, actuator y flyway;
    - MapStruct, springdoc, jjwt, Bucket4j, dotenv-java y Testcontainers 2;
    - plugins Spotless, JaCoCo (≥ 80 % en domain/application) y Failsafe;
    - Maven wrapper.
  - Portó el `core` de task-manager (errors, openapi, security) y lo adaptó:
    - JWT sin roles, con claim `type` (access/refresh); `JwtModel` en camelCase;
    - `PermissionValidator` solo comprueba la propiedad;
    - CORS por entorno (`CORS_ALLOWED_ORIGINS`);
    - rate limiting con un bucket estricto para `/auth/login` y cabecera `Retry-After`;
    - OpenAPI con servidor relativo y nuevas respuestas 422 y 429;
    - `/actuator/health` público.
  - Descartó `AtLeastOneField` (YAGNI).
  - Configuración:
    - perfiles `local`/`docker`/`dev`/`prod` con secretos por entorno;
    - `PosApplication` carga `.env`; `.env.example`;
    - `server.port=${PORT:8080}` y cabeceras de proxy para Render.
  - Flyway `V1__init.sql` con el esquema completo y sus constraints.
  - `Dockerfile` multi-stage (usuario no root) y `docker-compose.yml` en `shared-network`.
  - Adaptó los tests del core (34) y añadió `PosApplicationIT` con PostgreSQL en Testcontainers (3).
- **Verificación:**
  - `./mvnw verify` en verde: 34 unit + 3 IT, Spotless y JaCoCo.
  - App arrancada contra `global_postgres` (BD `pos_db`): health UP, Swagger 200, 401 con `ErrorResponseDTO`, preflight CORS 200 y Flyway aplicado.
  - Imagen Docker levantada con perfil `docker` y health UP.
- **Decisiones / correcciones humanas:** El autor eligió trabajar en `main` en lugar de una rama `feature/*`.
- **Notas:** El puerto 8080 de la máquina lo ocupa el contenedor `pokedex_api` del autor (no se tocó). Las pruebas usaron el 8081 y `docker-compose` permite `API_PORT`.
- **Resultado:** Hito 1 completado; sin commit.

### #09 · 2026-10-28 16:23 · Quitar dotenv: configuración en application-<perfil>.properties
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El autor corrigió el uso de `dotenv-java`: en task-manager la configuración vive en `application-xxx.properties`, no en `.env`. También preguntó por el contenedor `pokedex` mencionado en #08.
- **Qué hizo la IA:**
  - Eliminó la dependencia `dotenv-java`, el cargador de `.env` en `PosApplication`, `.env` y `.env.example`, el `env_file` de `docker-compose.yml` y la variable `API_PORT` (puerto fijo `8080:8080` como task-manager).
  - Reorganizó la configuración:
    - `application.properties`: valores comunes.
    - `application-local|docker.properties`: valores de desarrollo directos (BD, secretos JWT de desarrollo, CORS, cajero demo en `seed.cashier.*`).
    - `application-dev|prod.properties`: valores sensibles como placeholders `${VARIABLE}` que Spring resuelve con las variables de entorno de Render, para no commitear credenciales reales.
  - Actualizó `CLAUDE.md`, `README` y los docs `01`, `03`, `05`, `07` y `08`.
- **Sobre "pokedex":** `docker ps` mostró que los contenedores `pokedex_api` (puerto 8080) y `pokedex_backoffice` (4200) siguen corriendo desde hace 6 días. Pertenecen al proyecto compose `/Users/diegovilla/Desktop/pokedex-system`, cuya carpeta ya no existe. Por eso el 8080 está ocupado; no se tocaron.
- **Decisiones / correcciones humanas:** Corrección del autor: configuración por perfiles en `.properties`, sin `.env`.
- **Verificación:** `./mvnw verify` en verde (34 unit + 3 IT). Arranque con perfil `local` (en el 8081, por el puerto ocupado): health UP y 401 correcto.
- **Resultado:** Hecho; sin commit.

### #10 · 2026-10-28 16:26 · Verificación en el puerto 8080
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El autor eliminó los contenedores Docker que no usa.
- **Qué hizo la IA:** Comprobó que solo queda la infraestructura global (`global_postgres`, `global_redis`, `global_mongodb`, `global_rabbitmq`) y que el 8080 está libre. Arrancó la API en el 8080 con el perfil `local` (health UP) y con `docker compose` y el perfil `docker` (health UP, Swagger 200).
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 1 verificado en el puerto por defecto; sin commit.

### #11 · 2026-10-28 16:30 · Explicar Flyway y commitear el hito 1
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El autor preguntó qué es `V1__init.sql` y Flyway frente a crear las tablas desde las entities (`ddl-auto=update` en task-manager). Después pidió commit y push.
- **Qué hizo la IA:**
  - Explicó la diferencia: las entities mapean los datos y Flyway define el esquema; Hibernate solo valida.
  - Explicó por qué conviene con BD de producción: historial versionado, migraciones de datos, mismo esquema en todos los entornos, y el índice parcial y los `CHECK` que JPA no expresa.
  - Commit y push del hito 1 en `main`.
- **Decisiones / correcciones humanas:** El autor acepta mantener Flyway.
- **Resultado:** Hito 1 publicado en `origin/main`.

### #12 · 2026-10-28 16:36 · Hito 2: features user y auth
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Empezar el hito 2 (user + auth).
- **Qué hizo la IA:**
  - Feature `user`, portada de task-manager y simplificada:
    - `UserModel` sin roles, con `create` validado y `reconstruct`;
    - `UserNotFoundException`;
    - puertos `UserRepositoryPort` y `PasswordHasherPort`, `UserService.getMe`;
    - entity, repository y mappers MapStruct;
    - adaptadores JPA y BCrypt;
    - `GET /users/me` con `@GetUserMeDocumentation`.
  - `utils/data/ValidateDataUtils`, solo con los métodos usados.
  - `core/seeders`: `DatabaseSeeder` idempotente + `SeedCashierProperties` (`seed.cashier.*`).
  - Feature `auth`:
    - `POST /auth/login` (401 genérico "Invalid credentials.");
    - `POST /auth/refresh` (valida el tipo de token y que el cajero exista);
    - DTOs en camelCase con `tokenType`;
    - docs OpenAPI públicas (`@SecurityRequirements` vacío).
  - Tests: `UserModelTest`, `ValidateDataUtilsTest`, `UserServiceTest`, `UserControllerTest`, `UserRepositoryAdapterTest`, `BCryptPasswordHasherAdapterTest`, `AuthServiceTest`, `AuthControllerTest` y `AuthIT` (login, `/users/me`, 401, 400 y refresh).
- **Corrección detectada en la prueba manual:** Spring lee los `.properties` como ISO-8859-1, así que "López" llegaba como "LÃ³pez". Se escribió como `López`, se borró y re-sembró el cajero local, y se añadió la regla a `CLAUDE.md`.
- **Verificación:**
  - `./mvnw verify`: 56 unit + 8 IT en verde; JaCoCo cumple.
  - Manual contra `global_postgres`: login del cajero demo, `/users/me` con datos correctos y 401 con contraseña errónea.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 2 completado; sin commit.

### #13 · 2026-10-28 19:49 · Alineación de versiones de Java y Spring Boot con task-manager
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El autor pregunta si el proyecto usa el mismo JDK y SDK que task-manager.
- **Qué hizo la IA:**
  - Comparó el `pom.xml`, el Maven wrapper y el `Dockerfile` de task-manager con los de este repo.
  - Encontró dos diferencias introducidas en el hito 1: `java.version` 21 (task-manager: 17) y Spring Boot 4.1.1 (task-manager: 4.1.0).
  - Las alineó en el `pom.xml` y actualizó `README.md`, `CLAUDE.md` y `docs/08-roadmap.md`.
  - Coincidían ya: Maven 3.9.16 (wrapper 3.3.4), imágenes Docker Temurin 21 y el resto de librerías (Lombok, MapStruct, springdoc, jjwt, Bucket4j, Spotless, JaCoCo).
- **Verificación:** `./mvnw verify` en verde (56 unit + 8 IT, JaCoCo y Spotless OK); el bytecode generado es Java 17 (major 61).
- **Decisiones / correcciones humanas:** El autor detectó la discrepancia de versiones.
- **Resultado:** Versiones iguales a task-manager; sin commit.

### #14 · 2026-10-28 19:59 · Commit y push del hito 2
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer commit y push y decir con qué se sigue.
- **Qué hizo la IA:** Dos commits en `main` con Conventional Commits:
  - `build: align java and spring boot versions with task-manager`;
  - `feat(auth): add cashier login, token refresh and /users/me`.
  Después hizo push a `origin/main` y propuso el hito 3 (feature `product`).
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 2 publicado en `origin/main`.

### #15 · 2026-10-28 20:24 · Hito 3: feature product (`GET /products/{code}`)
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Empezar el hito 3.
- **Qué hizo la IA:**
  - Feature `product` hexagonal:
    - `ProductModel` solo con `reconstruct`, porque el catálogo es de solo lectura;
    - `ProductNotFoundException` (404 "Product not found.");
    - `ProductRepositoryPort.getByCode` (lo reutilizará `sale`) y `ProductService.getByCode`;
    - entity, `ProductJpaRepository.findByCode`, mappers y adapter;
    - `ProductResponseDTO(id, code, name, price)`.
  - `GET /products/{code}` con `@Pattern("^\\d{4,14}$")` en el path (400 con el detalle del campo, sin tocar la BD) y `@GetProductByCodeDocumentation`.
  - `V2__seed_products.sql`: 12 productos con UUID fijos (para que los fixtures MSW del front coincidan), incluido un código corto de 4 dígitos y la bolsa de 0,10 € para probar la precisión.
  - Tests: `ProductModelTest`, `ProductServiceTest`, `ProductControllerTest`, `ProductRepositoryAdapterTest` y `ProductLookupIT` (200 con el JSON exacto, escala 2 del precio, 404, 400 y 401).
  - Docs: hito 3 marcado en el roadmap. En `04-api.md` se quitó la propiedad `write-bigdecimal-as-plain`: no hace falta porque la escala 2 ya se serializa en plano.
- **Verificación:**
  - `./mvnw verify`: 63 unit + 13 IT en verde, JaCoCo y Spotless OK.
  - Manual contra `global_postgres`: Flyway aplicó V2; 200 (con tildes correctas y `0.10`), 404, 400 y 401.
- **Incidencia:** la primera prueba manual respondió desde un proceso antiguo del hito 2 que seguía en el puerto 8080; se paró y se repitió.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 3 completado; sin commit.

### #16 · 2026-10-28 20:29 · Commit y push del hito 3
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer commit y push.
- **Qué hizo la IA:** Commit `feat(product): add product lookup by code with seeded catalog` en `main` y push a `origin/main`.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 3 publicado en `origin/main`.

### #17 · 2026-10-28 20:42 · Hito 4: feature sale (agregado de la venta y 8 endpoints)
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Empezar el hito 4.
- **Qué hizo la IA:**
  - **Dominio:**
    - `SaleModel` (agregado raíz) con `addProduct`, `changeQuantity`, `removeLine`, `complete`, `cancel`, `total` e `itemCount` derivados.
    - `SaleLine` guarda una copia del código, nombre y precio del producto al escanearlo; solo el agregado puede cambiar su cantidad.
    - Value objects `Quantity` (1–999, con `plus`) y `SaleStatus`.
    - Excepciones: `InvalidQuantityException` y `EmptySaleException` (422); `SaleNotOpenException`, `OpenSaleAlreadyExistsException` y `ConcurrentSaleModificationException` (409); `SaleNotFoundException` y `SaleLineNotFoundException` (404).
  - **Aplicación:** `SaleService` con los 8 casos de uso, propiedad de la venta con `PermissionValidator` y uso de `ProductRepositoryPort` para resolver el código escaneado.
  - **Infraestructura:**
    - `SaleEntity`/`SaleLineEntity` con `@Version`, `cascade` + `orphanRemoval`, `position` para el orden y `@EntityGraph` en las lecturas.
    - `SaleRepositoryAdapter` y `SaleDatabaseExceptionResolver`.
    - DTOs validados, `SaleDtoMapper`, `SaleController` (201 + `Location`, 204 al cancelar) y una anotación OpenAPI por endpoint.
  - **Core:** handler 409 para `ObjectOptimisticLockingFailureException`.
  - **Tests:** `SaleModelTest`, `QuantityTest`, `SaleServiceTest`, `SaleControllerTest`, `SaleRepositoryAdapterTest`, `SaleDatabaseExceptionResolverTest`, el test del handler y `CheckoutFlowIT`. Este último cubre el flujo completo de una venta, 409/422/404/400, la venta vacía, el cancelar dos veces, el 403 con un segundo cajero y el 401.
  - **Docs:** roadmap (hito 4 ✅), RN-10 y tabla de errores.
- **Corrección detectada en la prueba manual:**
  - 20 escaneos simultáneos de un producto que aún no estaba en la venta devolvían 7 × 500. El `INSERT` de la línea choca con `uk_sale_lines_sale_product` antes de que se compruebe la versión.
  - Se tradujo esa constraint a `ConcurrentSaleModificationException` (409). Tras el cambio: 3 × 200 y 17 × 409, sin 500; 10 `open` simultáneos dan 1 × 201 y 9 × 409. En ningún caso se pierden unidades.
  - `getFirst()` (Java 21) se cambió por `get(0)` en los tests, ya que el proyecto compila a Java 17.
- **Verificación:**
  - `./mvnw verify`: 107 unit + 17 IT en verde, JaCoCo y Spotless OK.
  - Manual contra `global_postgres`: los 8 endpoints, `Location` correcta, total `2.20` con la bolsa de 0,10 € y las 7 rutas en Swagger.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 4 completado; sin commit.

### #18 · 2026-10-28 21:05 · Explicación de líneas y endpoints
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Explicar qué es una línea de venta, qué hace cada endpoint y qué falta del back.
- **Qué hizo la IA:** Explicó venta/línea/total con un ticket de ejemplo, recorrió los endpoints siguiendo a un cliente en caja y la tabla de errores; resumió lo pendiente (commit del hito 4, hito 5 de despliegue, hito 6 de entrega).
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Sin cambios de código.

### #19 · 2026-10-28 21:08 · Commit y push del hito 4
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer commit y push.
- **Qué hizo la IA:** Comprobó Spotless y commit `feat(sale): add sale aggregate with checkout endpoints` en `main` con push a `origin/main`.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Hito 4 publicado en `origin/main`.

### #20 · 2026-10-28 21:15 · Requisitos del usuario para el hito 5
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Antes de empezar el hito 5, decir qué necesita el usuario hacer.
- **Qué hizo la IA:** Revisó `application-dev|prod.properties`, `docs/07-despliegue.md` y cómo se leen los secretos JWT (bytes UTF-8, mínimo 32). Listó las acciones del usuario: cuentas y proyecto Neon con ramas, dos servicios Render con sus variables y deploy hooks, secrets de GitHub y protección de ramas; separó lo que hace la IA.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Sin cambios de código.

### #21 · 2026-10-28 21:40 · Diagnóstico del primer deploy en Render
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El usuario creó Neon (ramas `prod` y `dev`), los dos servicios de Render, la rama `dev` y los secrets de GitHub; los deploys salen en rojo.
- **Qué hizo la IA:** Comprobó que las URLs de Render no responden al health check. Revisó el Dockerfile y la configuración base. Pidió el log del deploy y la lista de variables (solo nombres). Explicó cómo partir la cadena de Neon en las tres variables y quitar `channelBinding`.
- **Incidencia:** El usuario pegó en el chat las cadenas de conexión con la contraseña de Neon. La IA no la reprodujo ni la guardó en ningún fichero y recomendó rotarla.
- **Decisiones / correcciones humanas:** Las ramas de Neon se llaman `prod` y `dev` (no `main`/`dev`).
- **Resultado:** Pendiente del log de Render.

### #22 · 2026-10-28 21:50 · Causa del fallo en Render y variables obligatorias
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El usuario pegó los logs de los deploys de dev y prod y preguntó si `JWT_SECRET`, `JWT_REFRESH_SECRET`, `SEED_CASHIER_PASSWORD` y `CORS_ALLOWED_ORIGINS` son necesarias.
- **Qué hizo la IA:** Identificó la causa: `'url' must start with "jdbc"`. `DATABASE_URL` no tenía el formato JDBC, y Spring falla al crear el `DataSource` antes de llegar a resolver los secretos JWT. Explicó para qué sirve cada variable y por qué no puede ir en el repo (público).
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Sin cambios de código; el usuario corrige las variables en Render.

### #23 · 2026-10-28 21:58 · Valores simples para las variables de Render
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El usuario pide valores concretos y sencillos para `JWT_SECRET`, `JWT_REFRESH_SECRET`, `SEED_CASHIER_PASSWORD` y `CORS_ALLOWED_ORIGINS`: el entorno es efímero.
- **Qué hizo la IA:** Propuso valores legibles por entorno con el mismo patrón que `application-local.properties` (secretos JWT de al menos 32 caracteres).
- **Decisiones / correcciones humanas:** El usuario prefiere valores simples en lugar de secretos aleatorios.
- **Resultado:** Sin cambios de código.

### #24 · 2026-10-28 22:20 · Hito 5: smoke test en Render y workflow CI/CD
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Los deploys de dev y prod ya están en verde.
- **Qué hizo la IA:**
  - **Smoke test** con un script en `curl` contra las dos URLs de Render: health 200; login, `/users/me` y producto 200; 401 sin token. Escaneó leche ×2 y bolsa ×3, con 404 para un código desconocido, y completó la venta (`COMPLETED`, 5 artículos, 2.20). Swagger 200. El primer intento dio timeouts porque los servicios estaban dormidos (plan gratuito).
  - **`.github/workflows/ci-cd.yml`**, copiado del `ci-cd-backend.yml` de task-manager:
    - `static-checks`: solo PR a `main` desde `dev`, más Spotless y compilación.
    - `unit-tests` (JaCoCo ≥ 80 %), `integration-tests` (Failsafe + Testcontainers) y `build` (imagen Docker), en paralelo.
    - `quality-gate` y `deploy` con los deploy hooks de Render.
    - Sin detección de cambios por carpeta, Sonar ni Discord.
  - Verificó en local los dos comandos nuevos: 107 tests unitarios con la cobertura cumplida y 17 IT.
  - **Docs:** URLs reales en `04-api.md` y `07-despliegue.md`, ramas Neon `dev`/`prod`, cómo convertir la cadena de Neon a JDBC, tabla de jobs y roadmap del hito 5.
- **Decisiones / correcciones humanas:** El usuario configuró Neon, Render y los secrets con valores simples (entorno efímero).
- **Pendiente:** Protección de ramas en GitHub, que se hizo después con `gh` (ver #25).
- **Resultado:** dev y prod funcionando; workflow y docs sin commit.

### #25 · 2026-10-28 22:45 · Push del hito 5, CI en GitHub y protección de ramas
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** El usuario apagó el Auto-Deploy de Render, insistió en que GitHub estaba conectado e instaló y autenticó `gh`.
- **Qué hizo la IA:**
  - Comprobó los conectores de la sesión: no había ninguno de GitHub, solo git con el Llavero de macOS. No usó el token del Llavero.
  - Hizo el commit `ci: add ci-cd workflow with render deploy hooks`, el push a `main` y puso `dev` en el mismo commit.
  - Siguió los dos runs: todos los jobs en verde (static, unit + JaCoCo, IT con Testcontainers, Docker, quality gate y deploy a Render); el health de dev y prod da 200 tras el deploy.
  - Protegió `main` y `dev` con `gh api`: PR obligatorio (0 aprobaciones), check `🛡️ Backend CI Quality Gate`, aplicado también a admins, sin force-push ni borrado.
- **Decisiones / correcciones humanas:** El usuario eligió instalar `gh` para que la IA configurara la protección.
- **Resultado:** Hito 5 completado. Desde ahora todo cambio entra por PR (`feature/*` → `dev` → `main`); este cierre de docs y AI_LOG queda sin commit.

### #26 · 2026-10-28 23:05 · Hito 6: README final y cierre del backend
- **Herramienta:** Claude Code (Claude Opus 5.5)
- **Prompt (resumen):** Hacer el hito 6 (entrega del back).
- **Qué hizo la IA:**
  - **README** con el estilo del de task-manager:
    - badges (incluido el estado del CI) y tabla de entornos con URLs, Swagger, base de datos y cajero demo;
    - aviso del arranque en frío y prueba rápida en Swagger;
    - qué es una venta y una línea, endpoints y errores;
    - diagrama de secuencia de un escaneo, decisiones de arquitectura y árbol de paquetes;
    - perfiles y variables de Render, comandos, tests, CI/CD y uso de IA.
  - Comprobó el README contra el código: el diagrama tenía el rate limit antes del filtro JWT y en `SecurityConfig` va después; se corrigió.
  - **Revisión del `AI_LOG.md`:** numeración continua del #01 al #26 y `git grep` sin contraseñas ni hosts de Neon.
  - **Roadmap:** hitos 5 y 6 ✅.
  - Primer cambio por el flujo protegido: rama `feature/hito-6-entrega` → PR a `dev` → PR a `main`.
- **Decisiones / correcciones humanas:** Ninguna.
- **Resultado:** Backend terminado y documentado.
