package com.diegovilla.pos.features.sale.application.ports;

import com.diegovilla.pos.features.sale.application.dto.response.SaleSummaryPage;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for the {@link SaleModel} aggregate (saved together with its lines).
 *
 * @since 1.0.0
 */
public interface SaleRepositoryPort {

    /**
     * Retrieves a sale by unique identifier.
     *
     * @param id unique identifier of the sale.
     * @return an {@link Optional} containing the sale if found, or empty otherwise.
     */
    Optional<SaleModel> getById(UUID id);

    /**
     * Retrieves the open sale of a cashier (there is at most one, RN-08).
     *
     * @param cashierId identifier of the cashier.
     * @return an {@link Optional} containing the open sale, or empty if there is none.
     */
    Optional<SaleModel> getOpenByCashierId(UUID cashierId);

    /**
     * Checks whether a cashier already has an open sale.
     *
     * @param cashierId identifier of the cashier.
     * @return {@code true} if an open sale exists; {@code false} otherwise.
     */
    boolean existsOpenByCashierId(UUID cashierId);

    /**
     * Retrieves one page of the sales of a cashier, newest first, as summaries.
     *
     * @param cashierId identifier of the cashier.
     * @param page zero-based page number.
     * @param size page size.
     * @return the {@link SaleSummaryPage}.
     */
    SaleSummaryPage findSummariesByCashierId(UUID cashierId, int page, int size);

    /**
     * Persists a new or updated sale with its lines, applying optimistic locking.
     *
     * @param saleModel aggregate to persist.
     * @return the persisted {@link SaleModel} with its updated version.
     */
    SaleModel save(SaleModel saleModel);
}
