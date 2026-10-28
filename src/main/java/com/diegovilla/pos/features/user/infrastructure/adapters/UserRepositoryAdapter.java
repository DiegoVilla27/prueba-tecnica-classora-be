package com.diegovilla.pos.features.user.infrastructure.adapters;

import com.diegovilla.pos.core.errors.translators.DatabaseExceptionTranslator;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.diegovilla.pos.features.user.infrastructure.mappers.UserEntityMapper;
import com.diegovilla.pos.features.user.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementing {@link UserRepositoryPort} with Spring Data JPA.
 *
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;
    private final DatabaseExceptionTranslator databaseExceptionTranslator;

    /** {@inheritDoc} */
    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<UserModel> getById(UUID id) {
        return userJpaRepository.findById(id).map(userEntityMapper::entityToModel);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<UserModel> getByEmail(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email).map(userEntityMapper::entityToModel);
    }

    /** {@inheritDoc} */
    @Override
    public UserModel save(UserModel userModel) {
        try {
            return userEntityMapper.entityToModel(
                    userJpaRepository.saveAndFlush(userEntityMapper.modelToEntity(userModel)));
        } catch (DataIntegrityViolationException ex) {
            throw databaseExceptionTranslator.translate(ex);
        }
    }
}
