---
name: new-feature
description: Crea una feature backend con arquitectura hexagonal + DDD (domain/application/infrastructure) en el estilo de task-manager, creando solo las carpetas que el caso de uso necesita. Usar al empezar un bounded context nuevo.
---

# new-feature (backend)

## 0. Antes
- ¿Está en `docs/02-casos-de-uso.md`? Si no → **no se hace** (YAGNI); preguntar.
- Nombre singular en minúsculas (`product`, `sale`).

## 1. Orden (de dentro hacia fuera; crear solo lo necesario)
1. **domain/valueobjects**: enums y `record`s que validan en el constructor compacto (solo si encapsulan una regla).
2. **domain/model/<X>Model**: `@Getter`, constructor privado con `Objects.requireNonNull`, `create(...)`, `reconstruct(...)`, métodos de negocio que lanzan excepciones de dominio. Sin setters. Javadoc `@since 1.0.0`.
3. **domain/exceptions**: una clase por error, extendiendo `ResourceNotFoundException` (404) / `DomainException` (422) / `ApiException(HttpStatus.CONFLICT, …)` (409).
4. **`<X>ModelTest`** (skill `write-tests`).
5. **application/commands**: `record`s, solo si el caso de uso recibe datos.
6. **application/ports/<X>RepositoryPort**: solo los métodos que usan los casos de uso.
7. **application/services/<X>Service**: `@Service @Slf4j @RequiredArgsConstructor @Transactional(readOnly = true)`; `@Transactional` en escrituras; `log.info(...)` por operación.
8. **infrastructure/entity**, **repository** (`JpaRepository`), **mappers** (`<X>EntityMapper` con `default` + `reconstruct`; `<X>DtoMapper` MapStruct), **adapters** (`<X>RepositoryAdapter implements <X>RepositoryPort`).
9. **infrastructure/dto** y endpoints con la skill `new-endpoint`.
10. Tests de service, controller y adapter.
11. Entrada en `AI_LOG.md` (`update-ai-log`).

## 2. No crear por defecto
`specifications/`, resolvers de excepciones de BD, `application/dto`, paginación, filtros, seguridad, caché. Solo cuando un caso de uso lo pida.

## 3. Verificación
`./mvnw spotless:apply && ./mvnw verify`
