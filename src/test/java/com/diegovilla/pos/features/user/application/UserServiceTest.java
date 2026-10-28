package com.diegovilla.pos.features.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.application.services.UserService;
import com.diegovilla.pos.features.user.domain.exceptions.UserNotFoundException;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks private UserService userService;

    private final UserModel cashier = UserModel.create("Ana", "López", "ana@pos.local", "hash");

    @Test
    @DisplayName("Should retrieve the authenticated cashier")
    void shouldGetMe() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashier.getId());
        when(userRepositoryPort.getById(cashier.getId())).thenReturn(Optional.of(cashier));

        assertThat(userService.getMe()).isSameAs(cashier);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the cashier no longer exists")
    void shouldRejectMissingCashier() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashier.getId());
        when(userRepositoryPort.getById(cashier.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found.");
    }
}
