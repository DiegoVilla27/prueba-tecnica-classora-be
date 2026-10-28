package com.diegovilla.pos.core.security.jwt.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    @Mock private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks private PermissionValidator permissionValidator;

    @Test
    @DisplayName("Should allow access when the cashier owns the resource")
    void shouldAllowWhenOwner() {
        UUID cashierId = UUID.randomUUID();
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(cashierId);

        assertThatCode(() -> permissionValidator.validateHasPermissions(cashierId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should deny access when the resource belongs to another cashier")
    void shouldDenyWhenNotOwner() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> permissionValidator.validateHasPermissions(UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
