package com.diegovilla.pos.features.sale.domain.valueobjects;

import com.diegovilla.pos.features.sale.domain.exceptions.InvalidQuantityException;

/**
 * Value object representing the units of a sale line (RN-02: between {@value #MIN} and {@value
 * #MAX}).
 *
 * @param value number of units.
 * @since 1.0.0
 */
public record Quantity(int value) {

    /** Minimum units per line. */
    public static final int MIN = 1;

    /** Maximum units per line. */
    public static final int MAX = 999;

    /**
     * Validates the units.
     *
     * @throws InvalidQuantityException if the value is outside {@value #MIN}–{@value #MAX}.
     */
    public Quantity {
        if (value < MIN || value > MAX) {
            throw new InvalidQuantityException();
        }
    }

    /**
     * Adds units to this quantity.
     *
     * @param other units to add.
     * @return a new {@link Quantity} with the sum.
     * @throws InvalidQuantityException if the sum exceeds {@value #MAX}.
     */
    public Quantity plus(Quantity other) {
        return new Quantity(value + other.value);
    }
}
