# 07 · Entornos, Docker y despliegue

Mismo modelo que task-manager: perfiles de Spring por entorno, red Docker compartida en local, Render + Neon en la nube y GitHub Actions.

## 1. Entornos

| Entorno | Perfil | Base de datos | URL API | Front que lo consume |
|---|---|---|---|---|
| **local** | `local` | `global_postgres` expuesto en `localhost:5432`, BD `pos_db` | `http://localhost:8080/api/v1` | `pnpm local` (`:3000`) |
| **docker** | `docker` | `global_postgres:5432/pos_db` (red `shared-network`) | `http://localhost:8080/api/v1` | contenedor del front |
| **dev** | `dev` | Neon, rama `dev` | `https://pos-api-dev-k0y9.onrender.com/api/v1` | https://classora-tpv-dev.vercel.app (rama `dev` del front) |
| **prod** | `prod` | Neon, rama `prod` | `https://pos-api-prod.onrender.com/api/v1` | https://classora-tpv-prod.vercel.app (rama `main` del front) |

## 2. Local con Docker

Se reutiliza la infraestructura global del autor (`~/Desktop/shared-network/docker-compose.yml` → `global_postgres` en la red externa `shared-network`):

```bash
# 1. Infra compartida (una vez)
docker compose -f ~/Desktop/shared-network/docker-compose.yml up -d postgres
docker exec -it global_postgres psql -U user -c "CREATE DATABASE pos_db;"

# 2a. API en la máquina
./mvnw spring-boot:run                       # perfil local (application-local.properties)

# 2b. o API en contenedor
docker compose up --build -d                 # perfil docker
```

`docker-compose.yml` de este repo (igual que task-manager):
```yaml
name: classora-pos
networks:
  shared-network:
    external: true
services:
  pos-api:
    build: .
    container_name: pos_api
    restart: always
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - "8080:8080"
    networks:
      - shared-network
```
El front tiene su propio `docker-compose.yml` en la misma red, así que con ambos levantados el sistema completo corre en local.

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
