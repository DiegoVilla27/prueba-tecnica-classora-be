package com.diegovilla.pos.features.sale.domain.model;

import com.diegovilla.pos.features.product.domain.model.ProductModel;
import com.diegovilla.pos.features.sale.domain.exceptions.EmptySaleException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleLineNotFoundException;
import com.diegovilla.pos.features.sale.domain.exceptions.SaleNotOpenException;
import com.diegovilla.pos.features.sale.domain.valueobjects.Quantity;
import com.diegovilla.pos.features.sale.domain.valueobjects.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate root representing a checkout sale handled by one cashier.
 *
 * <p>Owns its {@link SaleLine}s and enforces every business rule of the checkout: one line per
 * product (RN-01), quantity range (RN-02), price snapshot (RN-04), no empty completion (RN-05) and
 * modifications only while open (RN-06). Total and item count are derived, never stored.
 *
 * @since 1.0.0
 */
@Getter
public class SaleModel {

    private final UUID id;
    private final UUID cashierId;
    private SaleStatus status;
    private final List<SaleLine> lines;
    private final Long version;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    /** Internal constructor — use the factory methods instead. */
    private SaleModel(
            UUID id,
            UUID cashierId,
            SaleStatus status,
            List<SaleLine> lines,
            Long version,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt) {
        this.id = Objects.requireNonNull(id, "Sale ID cannot be null");
        this.cashierId = Objects.requireNonNull(cashierId, "Sale cashierId cannot be null");
        this.status = Objects.requireNonNull(status, "Sale status cannot be null");
        this.lines = new ArrayList<>(Objects.requireNonNull(lines, "Sale lines cannot be null"));
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt, "Sale createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Sale updatedAt cannot be null");
        this.closedAt = closedAt;
    }

    /**
     * Factory method opening a new, empty sale for a cashier.
     *
     * @param cashierId identifier of the cashier who opens the sale.
     * @return a new {@code OPEN} {@link SaleModel} without version (not persisted yet).
     */
    public static SaleModel create(UUID cashierId) {
        Instant now = Instant.now();
        return new SaleModel(
                UUID.randomUUID(), cashierId, SaleStatus.OPEN, List.of(), null, now, now, null);
    }

    /**
     * Reconstructs a {@link SaleModel} from persisted data.
     *
     * @param id unique identifier.
     * @param cashierId owner cashier.
     * @param status current status.
     * @param lines lines in scan order.
     * @param version optimistic-locking version.
     * @param createdAt creation timestamp.
     * @param updatedAt last-update timestamp.
     * @param closedAt completion or cancellation timestamp, {@code null} while open.
     * @return a fully hydrated aggregate.
     */
    public static SaleModel reconstruct(
            UUID id,
            UUID cashierId,
            SaleStatus status,
            List<SaleLine> lines,
            Long version,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt) {
        return new SaleModel(id, cashierId, status, lines, version, createdAt, updatedAt, closedAt);
    }

    /**
     * Returns the lines in scan order.
     *
     * @return an unmodifiable copy of the lines.
     */
    public List<SaleLine> getLines() {
        return List.copyOf(lines);
    }

    /**
     * Adds a scanned product. If the product already has a line, its quantity is increased (RN-01);
     * otherwise a new line is created with the current price frozen (RN-04).
     *
     * @param product scanned product.
     * @param quantity units scanned (1–999).
     * @throws SaleNotOpenException if the sale is not open.
     * @throws com.diegovilla.pos.features.sale.domain.exceptions.InvalidQuantityException if the
     *     quantity, or the resulting one, is outside 1–999.
     */
    public void addProduct(ProductModel product, int quantity) {
        ensureOpen();
        Quantity units = new Quantity(quantity);
        lines.stream()
                .filter(line -> line.getProductId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        line -> line.increase(units),
                        () -> lines.add(SaleLine.create(product, units)));
        touch();
    }

    /**
     * Replaces the quantity of a line.
     *
     * @param lineId identifier of the line.
     * @param quantity new units (1–999).
     * @throws SaleNotOpenException if the sale is not open.
     * @throws SaleLineNotFoundException if the line does not belong to the sale.
     */
    public void changeQuantity(UUID lineId, int quantity) {
        ensureOpen();
        Quantity units = new Quantity(quantity);
        findLine(lineId).changeQuantity(units);
        touch();
    }

    /**
     * Removes a line from the sale.
     *
     * @param lineId identifier of the line.
     * @throws SaleNotOpenException if the sale is not open.
     * @throws SaleLineNotFoundException if the line does not belong to the sale.
     */
    public void removeLine(UUID lineId) {
        ensureOpen();
        lines.remove(findLine(lineId));
        touch();
    }

    /**
     * Completes (charges) the sale.
     *
     * @throws SaleNotOpenException if the sale is not open.
     * @throws EmptySaleException if the sale has no lines (RN-05).
     */
    public void complete() {
        ensureOpen();
        if (lines.isEmpty()) {
            throw new EmptySaleException();
        }
        close(SaleStatus.COMPLETED);
    }

    /**
     * Cancels (empties) the sale.
     *
     * @throws SaleNotOpenException if the sale is not open.
     */
    public void cancel() {
        ensureOpen();
        close(SaleStatus.CANCELLED);
    }

    /**
     * Calculates the sale total (RN-03).
     *
     * @return the sum of the line subtotals, with scale 2.
     */
    public BigDecimal total() {
        return lines.stream()
                .map(SaleLine::subtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    /**
     * Counts the units in the sale.
     *
     * @return the sum of the line quantities.
     */
    public int itemCount() {
        return lines.stream().mapToInt(line -> line.getQuantity().value()).sum();
    }

    /** Throws if the sale can no longer be changed (RN-06). */
    private void ensureOpen() {
        if (status != SaleStatus.OPEN) {
            throw new SaleNotOpenException();
        }
    }

    /**
     * Finds a line of this sale.
     *
     * @param lineId identifier of the line.
     * @return the matching line.
     * @throws SaleLineNotFoundException if no line has that identifier.
     */
    private SaleLine findLine(UUID lineId) {
        return lines.stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(SaleLineNotFoundException::new);
    }

    /**
     * Moves the sale to a final status.
     *
     * @param finalStatus {@code COMPLETED} or {@code CANCELLED}.
     */
    private void close(SaleStatus finalStatus) {
        this.status = finalStatus;
        touch();
        this.closedAt = updatedAt;
    }

    /** Refreshes the last-update timestamp (it also bumps the persisted version). */
    private void touch() {
        this.updatedAt = Instant.now();
    }
}
