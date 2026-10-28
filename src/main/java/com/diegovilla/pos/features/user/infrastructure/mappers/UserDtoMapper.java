package com.diegovilla.pos.features.user.infrastructure.mappers;

import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.diegovilla.pos.features.user.infrastructure.dto.response.UserMeResponseDTO;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting cashier domain models into HTTP response DTOs.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    /**
     * Converts a domain model into the authenticated profile response.
     *
     * @param userModel authenticated cashier.
     * @return a {@link UserMeResponseDTO} without sensitive attributes.
     */
    UserMeResponseDTO modelToMeResponseDTO(UserModel userModel);
}
