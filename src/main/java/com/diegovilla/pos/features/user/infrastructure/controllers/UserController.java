package com.diegovilla.pos.features.user.infrastructure.controllers;

import com.diegovilla.pos.features.user.application.services.UserService;
import com.diegovilla.pos.features.user.infrastructure.docs.GetUserMeDocumentation;
import com.diegovilla.pos.features.user.infrastructure.dto.response.UserMeResponseDTO;
import com.diegovilla.pos.features.user.infrastructure.mappers.UserDtoMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing operations about the authenticated cashier.
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Exposes the profile of the authenticated cashier.")
public class UserController {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    /**
     * Retrieves the profile of the authenticated cashier.
     *
     * @return HTTP 200 with the {@link UserMeResponseDTO}.
     */
    @GetMapping("/me")
    @GetUserMeDocumentation
    public ResponseEntity<UserMeResponseDTO> getMe() {
        return ResponseEntity.ok(userDtoMapper.modelToMeResponseDTO(userService.getMe()));
    }
}
