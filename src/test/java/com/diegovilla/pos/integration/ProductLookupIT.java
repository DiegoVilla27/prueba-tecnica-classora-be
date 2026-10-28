package com.diegovilla.pos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Product lookup by code against PostgreSQL (Testcontainers) with the catalog loaded by the Flyway
 * {@code V2} migration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ProductLookupIT {

    @Autowired private MockMvc mockMvc;

    private String accessToken;

    @BeforeEach
    void login() throws Exception {
        String body =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"email": "cajero@pos.test", "password": "test-password"}
                                                """))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        accessToken = JsonPath.read(body, "$.accessToken");
    }

    @Test
    @DisplayName("Should return the seeded product with its exact JSON contract")
    void shouldReturnProduct() throws Exception {
        String body =
                mockMvc.perform(
                                get("/products/8410001000011")
                                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        assertThat(body)
                .isEqualTo(
                        "{\"id\":\"0b6e4c4e-1a2b-4c3d-8e9f-000000000001\","
                                + "\"code\":\"8410001000011\","
                                + "\"name\":\"Leche entera 1L\","
                                + "\"price\":0.95}");
    }

    @Test
    @DisplayName("Should keep two decimals in the price")
    void shouldKeepPriceScale() throws Exception {
        String body =
                mockMvc.perform(
                                get("/products/8410001000110")
                                        .header("Authorization", "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        assertThat(body).contains("\"price\":0.10");
    }

    @Test
    @DisplayName("Should return 404 when no product has the code")
    void shouldReturnNotFound() throws Exception {
        mockMvc.perform(
                        get("/products/9999999999999")
                                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found."));
    }

    @Test
    @DisplayName("Should return 400 with the field error when the code format is invalid")
    void shouldRejectInvalidCode() throws Exception {
        mockMvc.perform(get("/products/12ab").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("code"))
                .andExpect(
                        jsonPath("$.errors[0].message").value("Code must contain 4 to 14 digits"));
    }

    @Test
    @DisplayName("Should return 401 without an access token")
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(get("/products/8410001000011")).andExpect(status().isUnauthorized());
    }
}
