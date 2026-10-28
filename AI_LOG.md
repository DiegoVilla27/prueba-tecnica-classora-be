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
