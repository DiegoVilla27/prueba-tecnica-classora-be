package com.diegovilla.pos.features.sale.infrastructure.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO to replace the quantity of a sale line.
 *
 * @param quantity new units.
 * @since 1.0.0
 */
@Schema(description = "Request body to change the quantity of a sale line")
public record SaleChangeQuantityRequestDTO(
        @Schema(
                        description = "New units of the line",
                        example = "3",
                        minimum = "1",
                        maximum = "999",
                        requiredMode = REQUIRED)
                @NotNull(message = "Quantity is required")
                @Min(value = 1, message = "Quantity must be at least 1")
                @Max(value = 999, message = "Quantity must be at most 999")
                Integer quantity) {}
