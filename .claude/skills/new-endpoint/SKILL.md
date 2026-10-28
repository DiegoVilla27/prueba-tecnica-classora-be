---
name: new-endpoint
description: Añade un endpoint REST a una feature con DTO validado, command, mapper, caso de uso, anotación de documentación OpenAPI propia y tests, al estilo task-manager. Usar para cualquier endpoint nuevo o modificado.
---

# new-endpoint

## 1. Contrato primero
Debe existir en `docs/02-casos-de-uso.md`. Actualizar la tabla y el JSON en `docs/04-api.md` (el front depende de ello).

## 2. Pasos
1. **Request DTO** (`infrastructure/dto/request/<X><Op>RequestDTO`): `record` con `@Schema(description, example, requiredMode)` y validaciones con mensaje.
2. **Command** (`application/commands`): `record`.
3. **Mapper**: métodos en `<X>DtoMapper` (`…RequestDTOToCommand`, `modelToResponseDTO`).
4. **Caso de uso**: método en `<X>Service`. La regla de negocio va en el **modelo**, no en el service.
5. **Documentación** (`infrastructure/docs/<Op><X>Documentation`):
   ```java
   @Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME) @Documented
   @Operation(summary = "...", description = "...")
   @ApiResponse(responseCode = "200", description = "...",
       content = @Content(mediaType = "application/json", schema = @Schema(implementation = XResponseDTO.class)))
   @BadRequestApiResponse @UnauthorizedApiResponse @ForbiddenApiResponse @NotFoundApiResponse
   @TooManyRequestsApiResponse @InternalServerErrorApiResponse
   public @interface OpXDocumentation {}
   ```
   Solo las respuestas que el endpoint puede devolver de verdad.
6. **Controller** (delgado):
   ```java
   @PostMapping("/{id}/lines")
   @AddSaleLineDocumentation
   public ResponseEntity<SaleResponseDTO> addLine(@PathVariable UUID id, @Valid @RequestBody SaleAddLineRequestDTO dto) {
       SaleModel sale = saleService.addLine(id, saleDtoMapper.addLineRequestDTOToCommand(dto));
       return ResponseEntity.ok(saleDtoMapper.modelToResponseDTO(sale));
   }
   ```
   201 al crear (+ `Location`), 204 en transiciones sin cuerpo, 200 en el resto. Errores **siempre** por excepción.
7. **Seguridad**: sin roles ni `@PreAuthorize`; todo endpoint requiere sesión por defecto. Si el recurso tiene dueño, `PermissionValidator` en el service. Si es público, añadirlo a `permitAll` en `SecurityConfig` y justificarlo.
8. **Tests**: `ServiceTest` (happy + cada excepción, incluido 403), `ControllerTest` (status + body) y el `*IT` del flujo si es crítico.

## 3. Verificación
Swagger muestra el endpoint con ejemplos; `./mvnw verify` en verde; entrada en `AI_LOG.md`.
