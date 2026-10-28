package com.diegovilla.pos.features.sale.application.commands;

/**
 * Command to add a scanned product to a sale.
 *
 * @param code scannable product code.
 * @param quantity units to add.
 * @since 1.0.0
 */
public record SaleAddLineCommand(String code, int quantity) {}
