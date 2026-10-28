package com.diegovilla.pos.features.sale.infrastructure.dto.response;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO with the full state of a sale; the checkout renders it after every operation.
 *
 * @param id unique identifier of the sale.
 * @param status lifecycle status.
 * @param cashierId owner cashier.
 * @param lines lines in scan order.
 * @param itemCount total units.
 * @param total sum of the line subtotals.
 * @param version optimistic-locking version.
 * @param createdAt creation timestamp.
 * @param closedAt completion or cancellation timestamp, {@code null} while open.
 * @since 1.0.0
 */
@Schema(description = "Sale with its lines and totals")
public record SaleResponseDTO(
        @Schema(
                        description = "Unique identifier of the sale",
                        example = "0d9f3c2a-5b8e-4f1a-9c7d-2e6b4a8f1c3d")
                UUID id,
        @Schema(description = "Lifecycle status", example = "OPEN") SaleStatus status,
        @Schema(
                        description = "Cashier who owns the sale",
                        example = "123e4567-e89b-12d3-a456-426655440000")
                UUID cashierId,
        @Schema(description = "Lines in scan order") List<SaleLineResponseDTO> lines,
        @Schema(description = "Total units in the sale", example = "2") int itemCount,
        @Schema(description = "Sum of the line subtotals", example = "1.90") BigDecimal total,
        @Schema(description = "Optimistic-locking version", example = "3") Long version,
        @Schema(description = "Creation timestamp (UTC)", example = "2026-10-28T14:30:00Z")
                Instant createdAt,
        @Schema(description = "Completion or cancellation timestamp (UTC), null while open")
                Instant closedAt) {}
