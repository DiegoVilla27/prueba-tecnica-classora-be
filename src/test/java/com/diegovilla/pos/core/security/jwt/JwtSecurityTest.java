package com.diegovilla.pos.core.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtSecurityTest {

    @Mock private JwtProperties jwtProperties;

    @InjectMocks private JwtService jwtService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.secret()).thenReturn("4zx8BmgguZyFKmspfzMvL084GB7550AE");
        lenient().when(jwtProperties.expSecret()).thenReturn(3600L);
        lenient().when(jwtProperties.refresh()).thenReturn("yZWg3ahaIHPLHvqFwbdLbZP58vxTcmfb");
        lenient().when(jwtProperties.expRefresh()).thenReturn(604800L);
    }

    @Test
    @DisplayName("Should generate valid access and refresh tokens")
    void shouldGenerateTokenSuccessfully() {
        JwtModel tokens = jwtService.generateToken(UUID.randomUUID().toString());

        assertThat(tokens.expiresIn()).isEqualTo(3600L);
        assertThat(jwtService.isValid(tokens.accessToken(), true)).isTrue();
        assertThat(jwtService.isValid(tokens.refreshToken(), false)).isTrue();
    }

    @Test
    @DisplayName("Should extract the subject from the access token")
    void shouldGetSubjectToToken() {
        UUID userId = UUID.randomUUID();

        JwtModel tokens = jwtService.generateToken(userId.toString());

        assertThat(UUID.fromString(jwtService.extractSubject(tokens.accessToken(), true)))
                .isEqualTo(userId);
    }

    @Test
    @DisplayName("Should reject a refresh token used as access token and vice versa")
    void shouldRejectTokenOfWrongType() {
        JwtModel tokens = jwtService.generateToken(UUID.randomUUID().toString());

        assertThat(jwtService.isValid(tokens.refreshToken(), true)).isFalse();
        assertThat(jwtService.isValid(tokens.accessToken(), false)).isFalse();
    }

    @Test
    @DisplayName("Should reject a malformed token")
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isValid("not-a-jwt", true)).isFalse();
    }
}
