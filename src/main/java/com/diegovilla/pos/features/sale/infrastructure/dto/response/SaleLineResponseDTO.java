package com.diegovilla.pos.features.sale.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for one line of a sale.
 *
 * @param id unique identifier of the line.
 * @param productId identifier of the product.
 * @param code product code snapshot.
 * @param name product name snapshot.
 * @param unitPrice unit price snapshot.
 * @param quantity units.
 * @param subtotal unit price × quantity.
 * @since 1.0.0
 */
@Schema(description = "Sale line")
public record SaleLineResponseDTO(
        @Schema(
                        description = "Unique identifier of the line",
                        example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
                UUID id,
        @Schema(
                        description = "Identifier of the product",
                        example = "0b6e4c4e-1a2b-4c3d-8e9f-000000000001")
                UUID productId,
        @Schema(description = "Product code", example = "8410001000011") String code,
        @Schema(description = "Product name", example = "Leche entera 1L") String name,
        @Schema(description = "Unit price frozen when the product was scanned", example = "0.95")
                BigDecimal unitPrice,
        @Schema(description = "Units", example = "2") int quantity,
        @Schema(description = "Unit price × quantity", example = "1.90") BigDecimal subtotal) {}
