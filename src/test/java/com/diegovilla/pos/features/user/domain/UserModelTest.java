package com.diegovilla.pos.features.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.diegovilla.pos.core.errors.exceptions.DomainException;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserModelTest {

    @Test
    @DisplayName("Should create a cashier with trimmed names and lower-cased email")
    void shouldCreateCashier() {
        UserModel user = UserModel.create(" Ana ", " López ", " Cajero@POS.local ", "hash");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("López");
        assertThat(user.getEmail()).isEqualTo("cajero@pos.local");
        assertThat(user.getPassword()).isEqualTo("hash");
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should reject an invalid email")
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> UserModel.create("Ana", "López", "not-an-email", "hash"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Email has an invalid format");
    }

    @Test
    @DisplayName("Should reject a too short first name")
    void shouldRejectShortFirstName() {
        assertThatThrownBy(() -> UserModel.create("A", "López", "a@pos.local", "hash"))
                .isInstanceOf(DomainException.class)
                .hasMessage("First name must be between 2 and 60 characters");
    }

    @Test
    @DisplayName("Should reject a blank password hash")
    void shouldRejectBlankPassword() {
        assertThatThrownBy(() -> UserModel.create("Ana", "López", "a@pos.local", " "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Password is required");
    }
}
