package com.diegovilla.pos.features.auth.application.commands;

/**
 * Command carrying the cashier credentials required for authentication.
 *
 * @param email registered email address.
 * @param password raw plain-text password.
 * @since 1.0.0
 */
public record AuthLoginCommand(String email, String password) {}
