# 07 · Entornos, Docker y despliegue

Mismo modelo que task-manager: perfiles de Spring por entorno, red Docker compartida en local, Render + Neon en la nube y GitHub Actions.

## 1. Entornos

| Entorno | Perfil | Base de datos | URL API | Front que lo consume |
|---|---|---|---|---|
| **local** | `local` | `global_postgres` expuesto en `localhost:5432`, BD `pos_db` | `http://localhost:8080/api/v1` | `pnpm local` (`:3000`) |
| **docker** | `docker` | `global_postgres:5432/pos_db` (red `shared-network`) | `http://localhost:8080/api/v1` | contenedor del front |
| **dev** | `dev` | Neon, rama `dev` | `https://<pos-api-dev>.onrender.com/api/v1` | Vercel preview (rama `dev`) |
| **prod** | `prod` | Neon, rama `main` | `https://<pos-api-prod>.onrender.com/api/v1` | Vercel production (rama `main`) |

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
- Health check: `/api/v1/actuator/health`.
- Render inyecta `PORT`: la app escucha en `server.port=${PORT:8080}` y confía en las cabeceras `X-Forwarded-*` del proxy (`server.forward-headers-strategy=framework`).
- **Arranque en frío** (plan gratuito, unos 50 s): el front lo gestiona con un estado "Conectando con el servidor…" y reintento (ver docs del front).

## 4. Neon
- Un proyecto con dos ramas: `dev` y `main`. Flyway aplica las migraciones al arrancar cada servicio.
- Cadena JDBC con `sslmode=require`. Credenciales **solo** en Render y GitHub Secrets.

## 5. Ramas y CI/CD (GitHub Actions · `.github/workflows/ci-cd.yml`)

```
feature/* ──PR──▶ dev ──PR──▶ main
                  │            │
                  ▼            ▼
             deploy dev    deploy prod
```

| Job | Cuándo | Pasos |
|---|---|---|
| `quality` | PR y push a `dev`/`main` | `spotless:check` · `compile` |
| `test` | tras `quality` | `./mvnw verify` (unit + IT con Testcontainers + JaCoCo) · artefacto con informe |
| `deploy-dev` | push a `dev` y `test` OK | `curl $RENDER_DEPLOY_HOOK_DEV` |
| `deploy-prod` | push a `main` y `test` OK | `curl $RENDER_DEPLOY_HOOK_PROD` |

Secrets: `RENDER_DEPLOY_HOOK_DEV`, `RENDER_DEPLOY_HOOK_PROD` (y `SONAR_TOKEN` si se activa Sonar).
Protección de ramas: `main` y `dev` solo por PR con CI en verde.

## 6. Checklist de puesta en marcha
- [ ] Crear BD `pos_db` en `global_postgres`.
- [ ] Proyecto Neon con ramas `dev` y `main`.
- [ ] Servicios Render `pos-api-dev` y `pos-api-prod` con variables y deploy hooks.
- [ ] Secrets en GitHub.
- [ ] Rama `dev` creada desde `main` y protegida.
- [ ] Añadir las URLs reales a este doc, a `OpenApiConfig` y al front.
