package com.diegovilla.pos.features.user.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.diegovilla.pos.features.user.application.services.UserService;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.diegovilla.pos.features.user.infrastructure.dto.response.UserMeResponseDTO;
import com.diegovilla.pos.features.user.infrastructure.mappers.UserDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private UserDtoMapper userDtoMapper;

    @InjectMocks private UserController userController;

    @Test
    @DisplayName("Should return the authenticated cashier profile")
    void shouldGetMe() {
        UserModel cashier = UserModel.create("Ana", "López", "ana@pos.local", "hash");
        UserMeResponseDTO dto =
                new UserMeResponseDTO(cashier.getId(), "Ana", "López", "ana@pos.local");
        when(userService.getMe()).thenReturn(cashier);
        when(userDtoMapper.modelToMeResponseDTO(cashier)).thenReturn(dto);

        ResponseEntity<UserMeResponseDTO> response = userController.getMe();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }
}
