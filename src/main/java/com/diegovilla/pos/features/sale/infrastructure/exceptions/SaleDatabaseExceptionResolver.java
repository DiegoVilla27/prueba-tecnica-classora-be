package com.diegovilla.pos.features.sale.infrastructure.exceptions;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import com.diegovilla.pos.core.errors.translators.DatabaseExceptionResolver;
import com.diegovilla.pos.features.sale.domain.exceptions.ConcurrentSaleModificationException;
import com.diegovilla.pos.features.sale.domain.exceptions.OpenSaleAlreadyExistsException;
import org.springframework.stereotype.Component;

/**
 * Translates the database constraints of the sale tables into domain exceptions.
 *
 * <p>Both constraints are the last guard when concurrent requests of the same cashier race each
 * other:
 *
 * <ul>
 *   <li>{@code ux_sales_one_open_per_cashier}: two "open sale" requests (RN-08).
 *   <li>{@code uk_sale_lines_sale_product}: two simultaneous scans of a product that is not in the
 *       sale yet; the line insert fails before the version check (RN-10).
 * </ul>
 *
 * @since 1.0.0
 */
@Component
public class SaleDatabaseExceptionResolver implements DatabaseExceptionResolver {

    static final String ONE_OPEN_SALE_PER_CASHIER = "ux_sales_one_open_per_cashier";
    static final String ONE_LINE_PER_PRODUCT = "uk_sale_lines_sale_product";

    /** {@inheritDoc} */
    @Override
    public ApiException resolve(String constraintName) {
        if (ONE_OPEN_SALE_PER_CASHIER.equalsIgnoreCase(constraintName)) {
            return new OpenSaleAlreadyExistsException();
        }
        if (ONE_LINE_PER_PRODUCT.equalsIgnoreCase(constraintName)) {
            return new ConcurrentSaleModificationException();
        }
        return null;
    }
}
