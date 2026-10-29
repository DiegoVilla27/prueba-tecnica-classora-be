package com.diegovilla.pos.features.sale.application.dto.response;

import java.util.List;

/**
 * One page of the sales history, newest first.
 *
 * @param content summaries of this page.
 * @param page zero-based page number.
 * @param size requested page size.
 * @param totalElements total number of sales of the cashier.
 * @param totalPages total number of pages.
 * @since 1.0.0
 */
public record SaleSummaryPage(
        List<SaleSummary> content, int page, int size, long totalElements, int totalPages) {

    /** Defensive copy of the content. */
    public SaleSummaryPage {
        content = List.copyOf(content);
    }
}
