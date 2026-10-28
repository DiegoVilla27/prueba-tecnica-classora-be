package com.diegovilla.pos.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Boots the whole application against PostgreSQL (Testcontainers) with Flyway migrations. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PosApplicationIT {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("Should start the application and expose a public health endpoint")
    void shouldExposeHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should reject protected endpoints without a token with 401")
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(get("/products/8412345000010"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Should serve the OpenAPI specification publicly")
    void shouldServeOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk());
    }
}
