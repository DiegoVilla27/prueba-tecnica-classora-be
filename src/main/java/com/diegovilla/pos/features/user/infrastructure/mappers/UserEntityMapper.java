package com.diegovilla.pos.features.user.infrastructure.mappers;

import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.diegovilla.pos.features.user.infrastructure.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between {@link UserEntity} and {@link UserModel}.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    /**
     * Converts a persistence entity into a domain model.
     *
     * @param userEntity persistence entity to convert.
     * @return a reconstructed {@link UserModel}, or {@code null} if input is null.
     */
    default UserModel entityToModel(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return UserModel.reconstruct(
                userEntity.getId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt());
    }

    /**
     * Converts a domain model into a JPA entity.
     *
     * @param userModel domain model to convert.
     * @return a JPA {@link UserEntity} ready for persistence.
     */
    UserEntity modelToEntity(UserModel userModel);
}
