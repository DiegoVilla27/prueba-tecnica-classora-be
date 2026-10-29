package com.diegovilla.pos.features.sale.infrastructure.dto.response;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO of one sale in the sales history (without lines; {@code GET /sales/{id}} has them).
 *
 * @param id unique identifier of the sale.
 * @param status lifecycle status.
 * @param itemCount total units.
 * @param lineCount distinct products.
 * @param total sale total.
 * @param createdAt opening timestamp.
 * @param closedAt completion or cancellation timestamp, {@code null} while open.
 * @since 1.0.0
 */
@Schema(description = "Summary of a sale in the history")
public record SaleSummaryResponseDTO(
        @Schema(description = "Sale id", example = "79dc62c0-d640-48aa-af49-c1b806796005") UUID id,
        @Schema(description = "Lifecycle status", example = "COMPLETED") SaleStatus status,
        @Schema(description = "Total units", example = "6") int itemCount,
        @Schema(description = "Distinct products", example = "4") int lineCount,
        @Schema(description = "Sale total (scale 2)", example = "14.94") BigDecimal total,
        @Schema(description = "Opening timestamp (UTC)", example = "2026-10-29T14:55:47Z")
                Instant createdAt,
        @Schema(
                        description = "Completion or cancellation timestamp (UTC), null while open",
                        example = "2026-10-29T14:58:02Z")
                Instant closedAt) {}
