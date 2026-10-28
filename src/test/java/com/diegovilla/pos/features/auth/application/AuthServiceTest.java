package com.diegovilla.pos.features.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.security.jwt.JwtModel;
import com.diegovilla.pos.core.security.jwt.JwtService;
import com.diegovilla.pos.features.auth.application.commands.AuthLoginCommand;
import com.diegovilla.pos.features.auth.application.commands.AuthRefreshCommand;
import com.diegovilla.pos.features.auth.application.services.AuthService;
import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private final UserModel cashier = UserModel.create("Ana", "López", "ana@pos.local", "hash");
    private final JwtModel tokens = new JwtModel("access", "refresh", 3600L);

    @Test
    @DisplayName("Should log in with valid credentials")
    void shouldLogin() {
        when(userRepositoryPort.getByEmail("ana@pos.local")).thenReturn(Optional.of(cashier));
        when(passwordHasherPort.matches("cajero1234", "hash")).thenReturn(true);
        when(jwtService.generateToken(cashier.getId().toString())).thenReturn(tokens);

        assertThat(authService.login(new AuthLoginCommand("ana@pos.local", "cajero1234")))
                .isSameAs(tokens);
    }

    @Test
    @DisplayName("Should reject a wrong password with a generic message")
    void shouldRejectWrongPassword() {
        when(userRepositoryPort.getByEmail("ana@pos.local")).thenReturn(Optional.of(cashier));
        when(passwordHasherPort.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthLoginCommand("ana@pos.local", "wrong")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    @DisplayName("Should reject an unknown email with the same generic message")
    void shouldRejectUnknownEmail() {
        when(userRepositoryPort.getByEmail("nobody@pos.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthLoginCommand("nobody@pos.local", "x")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    @DisplayName("Should refresh the session with a valid refresh token")
    void shouldRefresh() {
        when(jwtService.isValid("refresh-token", false)).thenReturn(true);
        when(jwtService.extractSubject("refresh-token", false))
                .thenReturn(cashier.getId().toString());
        when(userRepositoryPort.getById(cashier.getId())).thenReturn(Optional.of(cashier));
        when(jwtService.generateToken(cashier.getId().toString())).thenReturn(tokens);

        assertThat(authService.refresh(new AuthRefreshCommand("refresh-token"))).isSameAs(tokens);
    }

    @Test
    @DisplayName("Should reject an invalid refresh token")
    void shouldRejectInvalidRefreshToken() {
        when(jwtService.isValid("bad", false)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new AuthRefreshCommand("bad")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired refresh token.");
        verify(userRepositoryPort, never()).getById(cashier.getId());
    }

    @Test
    @DisplayName("Should reject a refresh token whose cashier no longer exists")
    void shouldRejectRefreshForMissingCashier() {
        when(jwtService.isValid("refresh-token", false)).thenReturn(true);
        when(jwtService.extractSubject("refresh-token", false))
                .thenReturn(cashier.getId().toString());
        when(userRepositoryPort.getById(cashier.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new AuthRefreshCommand("refresh-token")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
