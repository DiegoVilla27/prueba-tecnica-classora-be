package com.diegovilla.pos.features.sale.domain.model;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.sale.domain.valueobjects.Quantity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Internal entity of the {@link SaleModel} aggregate: one line per product.
 *
 * <p>Keeps a snapshot of the product code, name and unit price taken when the product was first
 * scanned (RN-04), so later catalog changes do not alter an ongoing sale. Only the aggregate root
 * may change its quantity.
 *
 * @since 1.0.0
 */
@Getter
public class SaleLine {

    private final UUID id;
    private final UUID productId;
    private final String productCode;
    private final String productName;
    private final BigDecimal unitPrice;
    private Quantity quantity;

    /** Internal constructor — use the factory methods instead. */
    private SaleLine(
            UUID id,
            UUID productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Quantity quantity) {
        this.id = Objects.requireNonNull(id, "Sale line ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Sale line productId cannot be null");
        this.productCode =
                Objects.requireNonNull(productCode, "Sale line productCode cannot be null");
        this.productName =
                Objects.requireNonNull(productName, "Sale line productName cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Sale line unitPrice cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Sale line quantity cannot be null");
    }

    /**
     * Factory method creating a line from a scanned product, freezing its current price.
     *
     * @param product scanned product.
     * @param quantity units scanned.
     * @return a new {@link SaleLine} with a generated UUID.
     */
    static SaleLine create(ProductModel product, Quantity quantity) {
        return new SaleLine(
                UUID.randomUUID(),
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getPrice(),
                quantity);
    }

    /**
     * Reconstructs a {@link SaleLine} from persisted data.
     *
     * @param id unique identifier.
     * @param productId identifier of the product.
     * @param productCode product code snapshot.
     * @param productName product name snapshot.
     * @param unitPrice unit price snapshot.
     * @param quantity units (1–999).
     * @return a fully hydrated line.
     */
    public static SaleLine reconstruct(
            UUID id,
            UUID productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            int quantity) {
        return new SaleLine(
                id, productId, productCode, productName, unitPrice, new Quantity(quantity));
    }

    /**
     * Calculates the line amount (RN-03).
     *
     * @return unit price × quantity, with scale 2 and {@link RoundingMode#HALF_EVEN}.
     */
    public BigDecimal subtotal() {
        return unitPrice
                .multiply(BigDecimal.valueOf(quantity.value()))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Adds units to the line (RN-01).
     *
     * @param units units to add.
     */
    void increase(Quantity units) {
        this.quantity = quantity.plus(units);
    }

    /**
     * Replaces the units of the line.
     *
     * @param quantity new units.
     */
    void changeQuantity(Quantity quantity) {
        this.quantity = quantity;
    }
}
