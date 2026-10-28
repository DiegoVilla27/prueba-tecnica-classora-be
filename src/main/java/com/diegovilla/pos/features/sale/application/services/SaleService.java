package com.diegovilla.pos.features.sale.application.services;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import com.diegovilla.pos.core.security.jwt.utils.PermissionValidator;
import com.diegovilla.pos.features.product.application.ports.ProductRepositoryPort;
import com.diegovilla.pos.features.product.domain.exceptions.ProductNotFoundException;
import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.sale.application.commands.SaleAddLineCommand;
import com.diegovilla.pos.features.sale.application.commands.SaleChangeQuantityCommand;
import com.diegovilla.pos.features.sale.application.ports.SaleRepositoryPort;
import com.diegovilla.pos.features.sale.domain.exceptions.OpenSaleAlreadyExistsException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleNotFoundException;
import com.diegovilla.pos.features.sale.domain.model.SaleModel;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service encapsulating the checkout use cases of a sale.
 *
 * <p>Every operation on an existing sale checks that it belongs to the authenticated cashier
 * (RN-09); the business rules themselves live in the {@link SaleModel} aggregate.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService {

    private final SaleRepositoryPort saleRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final PermissionValidator permissionValidator;

    /**
     * Opens a new sale for the authenticated cashier.
     *
     * @return the new open {@link SaleModel}.
     * @throws OpenSaleAlreadyExistsException if the cashier already has an open sale (RN-08).
     */
    @Transactional
    public SaleModel open() {
        UUID cashierId = authenticatedUserProvider.getCurrentUserId();
        if (saleRepositoryPort.existsOpenByCashierId(cashierId)) {
            throw new OpenSaleAlreadyExistsException();
        }

        SaleModel sale = saleRepositoryPort.save(SaleModel.create(cashierId));

        log.info("Sale opened successfully. id={}", sale.getId());

        return sale;
    }

    /**
     * Retrieves the open sale of the authenticated cashier (used after reloading the checkout).
     *
     * @return the open {@link SaleModel}.
     * @throws SaleNotFoundException if the cashier has no open sale.
     */
    public SaleModel getCurrent() {
        SaleModel sale =
                saleRepositoryPort
                        .getOpenByCashierId(authenticatedUserProvider.getCurrentUserId())
                        .orElseThrow(SaleNotFoundException::new);

        log.info("Current sale retrieved successfully. id={}", sale.getId());

        return sale;
    }

    /**
     * Retrieves a sale of the authenticated cashier.
     *
     * @param id identifier of the sale.
     * @return the {@link SaleModel}.
     * @throws SaleNotFoundException if the sale does not exist.
     * @throws org.springframework.security.access.AccessDeniedException if it belongs to another
     *     cashier.
     */
    public SaleModel getById(UUID id) {
        SaleModel sale = getOwnedSale(id);

        log.info("Sale retrieved successfully. id={}", sale.getId());

        return sale;
    }

    /**
     * Adds a scanned product to a sale; scanning the same code again increases its quantity.
     *
     * @param id identifier of the sale.
     * @param command product code and units.
     * @return the updated {@link SaleModel}.
     * @throws ProductNotFoundException if no product has the code.
     */
    @Transactional
    public SaleModel addLine(UUID id, SaleAddLineCommand command) {
        SaleModel sale = getOwnedSale(id);
        ProductModel product =
                productRepositoryPort
                        .getByCode(command.code())
                        .orElseThrow(ProductNotFoundException::new);

        sale.addProduct(product, command.quantity());
        SaleModel saved = saleRepositoryPort.save(sale);

        log.info("Product added to sale successfully. id={}, productId={}", id, product.getId());

        return saved;
    }

    /**
     * Replaces the quantity of a sale line.
     *
     * @param id identifier of the sale.
     * @param lineId identifier of the line.
     * @param command new units.
     * @return the updated {@link SaleModel}.
     */
    @Transactional
    public SaleModel changeLineQuantity(UUID id, UUID lineId, SaleChangeQuantityCommand command) {
        SaleModel sale = getOwnedSale(id);

        sale.changeQuantity(lineId, command.quantity());
        SaleModel saved = saleRepositoryPort.save(sale);

        log.info("Sale line quantity changed successfully. id={}, lineId={}", id, lineId);

        return saved;
    }

    /**
     * Removes a line from a sale.
     *
     * @param id identifier of the sale.
     * @param lineId identifier of the line.
     * @return the updated {@link SaleModel} (the checkout needs the new total).
     */
    @Transactional
    public SaleModel removeLine(UUID id, UUID lineId) {
        SaleModel sale = getOwnedSale(id);

        sale.removeLine(lineId);
        SaleModel saved = saleRepositoryPort.save(sale);

        log.info("Sale line removed successfully. id={}, lineId={}", id, lineId);

        return saved;
    }

    /**
     * Completes (charges) a sale.
     *
     * @param id identifier of the sale.
     * @return the completed {@link SaleModel}.
     */
    @Transactional
    public SaleModel complete(UUID id) {
        SaleModel sale = getOwnedSale(id);

        sale.complete();
        SaleModel saved = saleRepositoryPort.save(sale);

        log.info("Sale completed successfully. id={}", id);

        return saved;
    }

    /**
     * Cancels (empties) a sale.
     *
     * @param id identifier of the sale.
     */
    @Transactional
    public void cancel(UUID id) {
        SaleModel sale = getOwnedSale(id);

        sale.cancel();
        saleRepositoryPort.save(sale);

        log.info("Sale cancelled successfully. id={}", id);
    }

    /**
     * Loads a sale and checks that it belongs to the authenticated cashier (RN-09).
     *
     * @param id identifier of the sale.
     * @return the owned {@link SaleModel}.
     * @throws SaleNotFoundException if the sale does not exist.
     */
    private SaleModel getOwnedSale(UUID id) {
        SaleModel sale = saleRepositoryPort.getById(id).orElseThrow(SaleNotFoundException::new);
        permissionValidator.validateHasPermissions(sale.getCashierId());
        return sale;
    }
}
