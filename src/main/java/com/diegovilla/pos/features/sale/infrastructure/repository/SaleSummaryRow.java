package com.diegovilla.pos.features.sale.infrastructure.repository;

import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Row of the sales history query: one sale with its aggregated lines.
 *
 * <p>The sums are {@code null} for a sale without lines (SQL {@code SUM} over no rows).
 *
 * @param id identifier of the sale.
 * @param status lifecycle status.
 * @param itemCount sum of the line quantities, or {@code null}.
 * @param lineCount number of lines.
 * @param total sum of unit price × quantity, or {@code null}.
 * @param createdAt opening time.
 * @param closedAt closing time, or {@code null}.
 * @since 1.0.0
 */
public record SaleSummaryRow(
        UUID id,
        SaleStatus status,
        Long itemCount,
        Long lineCount,
        BigDecimal total,
        Instant createdAt,
        Instant closedAt) {}
