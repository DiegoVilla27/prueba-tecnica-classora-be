package com.diegovilla.pos.features.sale.application.commands;

/**
 * Command to replace the quantity of a sale line.
 *
 * @param quantity new units.
 * @since 1.0.0
 */
public record SaleChangeQuantityCommand(int quantity) {}
