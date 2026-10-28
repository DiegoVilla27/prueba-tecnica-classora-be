package com.diegovilla.pos.features.auth.application.commands;

/**
 * Command carrying the refresh token used to renew the session.
 *
 * @param refreshToken signed JWT refresh token.
 * @since 1.0.0
 */
public record AuthRefreshCommand(String refreshToken) {}
