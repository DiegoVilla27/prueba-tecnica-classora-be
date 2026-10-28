package com.diegovilla.pos.features.auth.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.security.jwt.JwtModel;
import com.diegovilla.pos.features.auth.application.commands.AuthLoginCommand;
import com.diegovilla.pos.features.auth.application.commands.AuthRefreshCommand;
import com.diegovilla.pos.features.auth.application.services.AuthService;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthLoginRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthRefreshRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.response.AuthResponseDTO;
import com.diegovilla.pos.features.auth.infrastructure.mappers.AuthDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private AuthDtoMapper authDtoMapper;

    @InjectMocks private AuthController authController;

    private final JwtModel tokens = new JwtModel("access", "refresh", 3600L);
    private final AuthResponseDTO response =
            new AuthResponseDTO("access", "refresh", "Bearer", 3600L);

    @Test
    @DisplayName("Should return tokens on login")
    void shouldLogin() {
        AuthLoginRequestDTO dto = new AuthLoginRequestDTO("ana@pos.local", "cajero1234");
        AuthLoginCommand command = new AuthLoginCommand("ana@pos.local", "cajero1234");
        when(authDtoMapper.loginRequestDTOToCommand(dto)).thenReturn(command);
        when(authService.login(command)).thenReturn(tokens);
        when(authDtoMapper.jwtModelToResponseDTO(tokens)).thenReturn(response);

        ResponseEntity<AuthResponseDTO> result = authController.login(dto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Should return renewed tokens on refresh")
    void shouldRefresh() {
        AuthRefreshRequestDTO dto = new AuthRefreshRequestDTO("refresh");
        AuthRefreshCommand command = new AuthRefreshCommand("refresh");
        when(authDtoMapper.refreshRequestDTOToCommand(dto)).thenReturn(command);
        when(authService.refresh(command)).thenReturn(tokens);
        when(authDtoMapper.jwtModelToResponseDTO(tokens)).thenReturn(response);

        ResponseEntity<AuthResponseDTO> result = authController.refresh(dto);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }
}
