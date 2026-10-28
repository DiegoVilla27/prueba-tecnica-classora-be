package com.diegovilla.pos.features.sale.infrastructure.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO to add a product to a sale by its code. The price is never sent: it comes from the
 * catalog.
 *
 * @param code scannable product code.
 * @param quantity units to add.
 * @since 1.0.0
 */
@Schema(description = "Request body to add a product to a sale by its code")
public record SaleAddLineRequestDTO(
        @Schema(
                        description = "Scannable product code",
                        example = "8412345000010",
                        requiredMode = REQUIRED)
                @NotBlank(message = "Code is required")
                @Pattern(regexp = "^\\d{4,14}$", message = "Code must contain 4 to 14 digits")
                String code,
        @Schema(
                        description = "Units to add",
                        example = "1",
                        minimum = "1",
                        maximum = "999",
                        requiredMode = REQUIRED)
                @NotNull(message = "Quantity is required")
                @Min(value = 1, message = "Quantity must be at least 1")
                @Max(value = 999, message = "Quantity must be at most 999")
                Integer quantity) {}
