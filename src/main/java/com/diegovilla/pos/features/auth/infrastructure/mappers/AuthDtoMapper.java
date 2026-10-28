package com.diegovilla.pos.features.auth.infrastructure.mappers;

import com.diegovilla.pos.core.security.jwt.JwtModel;
import com.diegovilla.pos.features.auth.application.commands.AuthLoginCommand;
import com.diegovilla.pos.features.auth.application.commands.AuthRefreshCommand;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthLoginRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.request.AuthRefreshRequestDTO;
import com.diegovilla.pos.features.auth.infrastructure.dto.response.AuthResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between authentication DTOs, commands and token models.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AuthDtoMapper {

    /**
     * Converts a login request into a command.
     *
     * @param dto incoming credentials.
     * @return the {@link AuthLoginCommand}.
     */
    AuthLoginCommand loginRequestDTOToCommand(AuthLoginRequestDTO dto);

    /**
     * Converts a refresh request into a command.
     *
     * @param dto incoming refresh token.
     * @return the {@link AuthRefreshCommand}.
     */
    AuthRefreshCommand refreshRequestDTOToCommand(AuthRefreshRequestDTO dto);

    /**
     * Converts issued tokens into the HTTP response.
     *
     * @param jwtModel issued tokens.
     * @return the {@link AuthResponseDTO}.
     */
    @Mapping(target = "tokenType", constant = "Bearer")
    AuthResponseDTO jwtModelToResponseDTO(JwtModel jwtModel);
}
