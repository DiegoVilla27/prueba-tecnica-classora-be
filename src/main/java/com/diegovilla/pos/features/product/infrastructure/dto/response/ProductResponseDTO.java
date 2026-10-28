package com.diegovilla.pos.features.product.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO with the data the checkout needs from a scanned product.
 *
 * @param id unique identifier of the product.
 * @param code scannable code.
 * @param name display name.
 * @param price unit price (2 decimals).
 * @since 1.0.0
 */
@Schema(description = "Catalog product")
public record ProductResponseDTO(
        @Schema(
                        description = "Unique identifier of the product",
                        example = "0b6e4c4e-1a2b-4c3d-8e9f-100000000001")
                UUID id,
        @Schema(description = "Scannable product code", example = "8412345000010") String code,
        @Schema(description = "Display name of the product", example = "Leche entera 1L")
                String name,
        @Schema(description = "Unit price with 2 decimals", example = "0.95") BigDecimal price) {}
