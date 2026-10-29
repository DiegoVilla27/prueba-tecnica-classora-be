package com.diegovilla.pos.features.sale.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO of one page of the sales history, newest first.
 *
 * @param content sales of this page.
 * @param page zero-based page number.
 * @param size page size.
 * @param totalElements total number of sales of the cashier.
 * @param totalPages total number of pages.
 * @since 1.0.0
 */
@Schema(description = "Page of the sales history of the authenticated cashier, newest first")
public record SalePageResponseDTO(
        @Schema(description = "Sales of this page") List<SaleSummaryResponseDTO> content,
        @Schema(description = "Zero-based page number", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of sales", example = "42") long totalElements,
        @Schema(description = "Total number of pages", example = "3") int totalPages) {}
