package com.diegovilla.pos.features.user.infrastructure.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHasherAdapterTest {

    private final BCryptPasswordHasherAdapter adapter =
            new BCryptPasswordHasherAdapter(new BCryptPasswordEncoder());

    @Test
    @DisplayName("Should hash a password and verify it")
    void shouldHashAndMatch() {
        String hash = adapter.hash("cajero1234");

        assertThat(hash).isNotEqualTo("cajero1234");
        assertThat(adapter.matches("cajero1234", hash)).isTrue();
        assertThat(adapter.matches("wrong", hash)).isFalse();
    }
}
