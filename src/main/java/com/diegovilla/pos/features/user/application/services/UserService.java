package com.diegovilla.pos.features.user.application.services;

import com.diegovilla.pos.core.security.jwt.ports.AuthenticatedUserProvider;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.exceptions.UserNotFoundException;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service encapsulating the use cases of the cashier (user) feature.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * Retrieves the profile of the currently authenticated cashier.
     *
     * @return the authenticated {@link UserModel}.
     * @throws UserNotFoundException if the cashier in the token no longer exists.
     */
    public UserModel getMe() {
        UUID userId = authenticatedUserProvider.getCurrentUserId();
        UserModel user = userRepositoryPort.getById(userId).orElseThrow(UserNotFoundException::new);

        log.info("Authenticated user retrieved successfully. id={}", user.getId());

        return user;
    }
}
