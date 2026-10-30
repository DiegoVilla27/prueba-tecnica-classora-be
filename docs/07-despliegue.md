# 07 · Entornos, Docker y despliegue

Perfiles de Spring por entorno (como task-manager), Docker autosuficiente en local, Render + Neon en la nube y GitHub Actions.

## 1. Entornos

| Entorno | Perfil | Base de datos | URL API | Front que lo consume |
|---|---|---|---|---|
| **local** | `local` | PostgreSQL en `localhost:5432`, BD `pos_db` | `http://localhost:8080/api/v1` | `pnpm local` (`:3000`) |
| **docker** | `docker` | Postgres propio del compose (`pos-db:5432/pos_db`) | `http://localhost:8080/api/v1` | contenedor del front |
| **dev** | `dev` | Neon, rama `dev` | `https://pos-api-dev-k0y9.onrender.com/api/v1` | https://classora-tpv-dev.vercel.app (rama `dev` del front) |
| **prod** | `prod` | Neon, rama `prod` | `https://pos-api-prod.onrender.com/api/v1` | https://classora-tpv-prod.vercel.app (rama `main` del front) |

## 2. Local con Docker

El `docker-compose.yml` es **autosuficiente**: quien clone el repo solo necesita Docker.

```bash
docker compose up --build -d     # API (perfil docker) + PostgreSQL propio
docker compose down -v           # parar y borrar los datos
```

```yaml
services:
  pos-db:                        # postgres:15-alpine, crea pos_db; healthcheck pg_isready; volumen pos_db_data
  pos-api:
    build: .
    depends_on: { pos-db: { condition: service_healthy } }
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://pos-db:5432/pos_db
    ports: ["8080:8080"]
```

- El Postgres **no se publica** en el host: no choca con otro en el 5432.
- El front tiene su propio compose. No comparten red porque es el navegador quien llama a `localhost:8080`, así que con los dos levantados el sistema completo corre en local.
- Para el perfil `local` (`./mvnw spring-boot:run`) basta con cualquier PostgreSQL en `localhost:5432` con la BD `pos_db` (`user` / `password`).

`Dockerfile`: multi-stage (Maven 3.9 + Temurin 21 → `eclipse-temurin:21-jre-alpine`), idéntico al de task-manager.

## 3. Render (dev y prod)
- Dos servicios **Web Service (Docker)**: `pos-api-dev` y `pos-api-prod`, con deploy automático desactivado (lo dispara CI con *deploy hook*).
- Variables por servicio (las que `application-dev|prod.properties` referencian como `${...}`): `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `CORS_ALLOWED_ORIGINS`, `SEED_CASHIER_PASSWORD`.
- `CORS_ALLOWED_ORIGINS` (patrones separados por coma): dev `http://localhost:[*],https://classora-tpv-dev.vercel.app` · prod `https://classora-tpv-prod.vercel.app`. Verificado con preflight: esos orígenes reciben `Access-Control-Allow-Origin` y cualquier otro, 403. Al cambiar una variable hay que elegir **Save, rebuild, and deploy** (el auto-deploy está desactivado).
- Health check: `/api/v1/actuator/health`.
- Render inyecta `PORT`: la app escucha en `server.port=${PORT:8080}` y confía en las cabeceras `X-Forwarded-*` del proxy (`server.forward-headers-strategy=framework`).
- **Arranque en frío** (plan gratuito, unos 50 s): el front lo gestiona con un estado "Conectando con el servidor…" y reintento (ver docs del front).

## 4. Neon
- Un proyecto (región Frankfurt) con dos ramas: `dev` y `prod`. Flyway aplica las migraciones al arrancar cada servicio.
- Neon da la cadena como `postgresql://usuario:clave@host/neondb?...`; Spring necesita el formato JDBC y el usuario/clave aparte:
  - `DATABASE_URL` = `jdbc:postgresql://<host>/neondb?sslmode=require`
  - `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- Credenciales **solo** en Render. Si `DATABASE_URL` no empieza por `jdbc:`, el arranque falla con `'url' must start with "jdbc"`.

## 5. Ramas y CI/CD (GitHub Actions · `.github/workflows/ci-cd.yml`)

```
feature/* ──PR──▶ dev ──PR──▶ main
                  │            │
                  ▼            ▼
             deploy dev    deploy prod
```

| Job | Cuándo | Pasos |
|---|---|---|
| `static-checks` | PR y push a `dev`/`main` | Solo PR a `main` desde `dev` · `spotless:check` · `compile` |
| `unit-tests` | tras `static-checks` | `./mvnw test jacoco:check@check-coverage` (≥ 80 % en domain/application) · artefacto JaCoCo |
| `integration-tests` | tras `static-checks`, en paralelo | `failsafe` sobre los `*IT` (Testcontainers) |
| `build` | tras `static-checks`, en paralelo | `docker build` con el mismo `Dockerfile` que Render |
| `quality-gate` | siempre | Falla si cualquiera de los anteriores falla |
| `deploy` | push a `dev`/`main` con gate OK | `curl $RENDER_DEPLOY_HOOK_DEV` o `_PROD` |

Secrets: `RENDER_DEPLOY_HOOK_DEV`, `RENDER_DEPLOY_HOOK_PROD`. Mismo esquema que task-manager, sin detección de cambios por carpeta (repo de un solo proyecto), Sonar ni Discord.
Protección de ramas: `main` y `dev` solo por PR con CI en verde.

## 6. Checklist de puesta en marcha
- [x] Crear BD `pos_db` en `global_postgres`.
- [x] Proyecto Neon con ramas `dev` y `prod`.
- [x] Servicios Render `pos-api-dev` y `pos-api-prod` con variables y deploy hooks.
- [x] Secrets en GitHub.
- [x] Rama `dev` creada desde `main` y protegida.
- [x] URLs reales en este doc y en `04-api.md` (`OpenApiConfig` usa la URL relativa `/api/v1`, válida en todos los entornos).
- [ ] URLs en el front.
