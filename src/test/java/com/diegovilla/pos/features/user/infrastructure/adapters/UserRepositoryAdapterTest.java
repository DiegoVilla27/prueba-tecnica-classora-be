package com.diegovilla.pos.features.user.infrastructure.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.core.errors.exceptions.ApiException;
import com.diegovilla.pos.core.errors.translators.DatabaseExceptionTranslator;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.diegovilla.pos.features.user.infrastructure.entity.UserEntity;
import com.diegovilla.pos.features.user.infrastructure.mappers.UserEntityMapper;
import com.diegovilla.pos.features.user.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock private UserJpaRepository userJpaRepository;
    @Mock private UserEntityMapper userEntityMapper;
    @Mock private DatabaseExceptionTranslator databaseExceptionTranslator;

    @InjectMocks private UserRepositoryAdapter adapter;

    private final UserModel cashier = UserModel.create("Ana", "López", "ana@pos.local", "hash");
    private final UserEntity entity = new UserEntity();

    @Test
    @DisplayName("Should delegate existence check by email")
    void shouldCheckExistsByEmail() {
        when(userJpaRepository.existsByEmailIgnoreCase("ana@pos.local")).thenReturn(true);

        assertThat(adapter.existsByEmailIgnoreCase("ana@pos.local")).isTrue();
    }

    @Test
    @DisplayName("Should map the entity found by id and by email")
    void shouldFindByIdAndEmail() {
        when(userJpaRepository.findById(cashier.getId())).thenReturn(Optional.of(entity));
        when(userJpaRepository.findByEmailIgnoreCase("ana@pos.local"))
                .thenReturn(Optional.of(entity));
        when(userEntityMapper.entityToModel(entity)).thenReturn(cashier);

        assertThat(adapter.getById(cashier.getId())).contains(cashier);
        assertThat(adapter.getByEmail("ana@pos.local")).contains(cashier);
    }

    @Test
    @DisplayName("Should save and map back the persisted user")
    void shouldSave() {
        when(userEntityMapper.modelToEntity(cashier)).thenReturn(entity);
        when(userJpaRepository.saveAndFlush(entity)).thenReturn(entity);
        when(userEntityMapper.entityToModel(entity)).thenReturn(cashier);

        assertThat(adapter.save(cashier)).isSameAs(cashier);
    }

    @Test
    @DisplayName("Should translate integrity violations on save")
    void shouldTranslateIntegrityViolation() {
        ApiException translated = new ApiException(HttpStatus.CONFLICT, "Duplicated.") {};
        when(userEntityMapper.modelToEntity(cashier)).thenReturn(entity);
        when(userJpaRepository.saveAndFlush(entity))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(databaseExceptionTranslator.translate(any())).thenReturn(translated);

        assertThatThrownBy(() -> adapter.save(cashier)).isSameAs(translated);
    }
}
