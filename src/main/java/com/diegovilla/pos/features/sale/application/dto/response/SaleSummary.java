package com.diegovilla.pos.features.sale.application.dto.response;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-only summary of a sale for the sales history: no lines, only the derived figures.
 *
 * @param id identifier of the sale.
 * @param status lifecycle status.
 * @param itemCount number of units.
 * @param lineCount number of distinct products.
 * @param total total amount (scale 2).
 * @param createdAt when the sale was opened.
 * @param closedAt when it was completed or cancelled; {@code null} while open.
 * @since 1.0.0
 */
public record SaleSummary(
        UUID id,
        SaleStatus status,
        int itemCount,
        int lineCount,
        BigDecimal total,
        Instant createdAt,
        Instant closedAt) {}
