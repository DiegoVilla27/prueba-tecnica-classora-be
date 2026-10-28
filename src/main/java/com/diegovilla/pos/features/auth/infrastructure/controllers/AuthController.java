package com.diegovilla.pos.features.auth.infrastructure.controllers;

import com.diegovilla.pos.features.auth.application.services.AuthService;
import com.diegovilla.pos.features.auth.infrastructure.docs.AuthLoginDocumentation;
import com.diegovilla.pos.features.auth.infrastructure.docs.AuthRefreshDocumentation;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthLoginRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthRefreshRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.response.AuthResponseDTO;
import com.diegovilla.pos.features.auth.infrastructure.mappers.AuthDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the public authentication endpoints.
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Cashier login and session renewal.")
public class AuthController {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    /**
     * Authenticates the cashier and returns JWT tokens.
     *
     * @param dto validated credentials.
     * @return HTTP 200 with the issued {@link AuthResponseDTO}.
     */
    @PostMapping("/login")
    @AuthLoginDocumentation
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO dto) {
        return ResponseEntity.ok(
                authDtoMapper.jwtModelToResponseDTO(
                        authService.login(authDtoMapper.loginRequestDTOToCommand(dto))));
    }

    /**
     * Renews the session with a valid refresh token.
     *
     * @param dto validated refresh token.
     * @return HTTP 200 with the renewed {@link AuthResponseDTO}.
     */
    @PostMapping("/refresh")
    @AuthRefreshDocumentation
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody AuthRefreshRequestDTO dto) {
        return ResponseEntity.ok(
                authDtoMapper.jwtModelToResponseDTO(
                        authService.refresh(authDtoMapper.refreshRequestDTOToCommand(dto))));
    }
}
