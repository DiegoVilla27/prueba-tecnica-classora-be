package com.diegovilla.pos.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
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
 * Sales history ({@code GET /sales}) against PostgreSQL (Testcontainers): every test uses a new
 * cashier, so the history only contains the sales it creates.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SalesHistoryIT {

    private static final String MILK = "8412345000010";
    private static final String BAG = "8412345000119";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepositoryPort userRepositoryPort;
    @Autowired private PasswordHasherPort passwordHasherPort;

    private String token;

    @BeforeEach
    void newCashier() throws Exception {
        String email = "historial-" + UUID.randomUUID() + "@pos.test";
        userRepositoryPort.save(
                UserModel.create("Ana", "Historial", email, passwordHasherPort.hash("pw")));
        token = login(email, "pw");
    }

    private String login(String email, String password) throws Exception {
        String body =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\":\"%s\",\"password\":\"%s\"}"
                                                        .formatted(email, password)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    private String openSale() throws Exception {
        String body =
                mockMvc.perform(post("/sales").header("Authorization", token))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private void addLine(String saleId, String code, int quantity) throws Exception {
        mockMvc.perform(
                        post("/sales/" + saleId + "/lines")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"code\":\"%s\",\"quantity\":%d}"
                                                .formatted(code, quantity)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list completed, cancelled and open sales newest first with their totals")
    void shouldListHistory() throws Exception {
        String completed = openSale();
        addLine(completed, MILK, 2);
        addLine(completed, BAG, 3);
        mockMvc.perform(patch("/sales/" + completed + "/complete").header("Authorization", token))
                .andExpect(status().isOk());
        String cancelled = openSale();
        addLine(cancelled, MILK, 1);
        mockMvc.perform(patch("/sales/" + cancelled + "/cancel").header("Authorization", token))
                .andExpect(status().isNoContent());
        String open = openSale();

        mockMvc.perform(get("/sales").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].id").value(open))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.content[0].itemCount").value(0))
                .andExpect(jsonPath("$.content[0].total").value(0.00))
                .andExpect(jsonPath("$.content[1].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[2].id").value(completed))
                .andExpect(jsonPath("$.content[2].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[2].itemCount").value(5))
                .andExpect(jsonPath("$.content[2].lineCount").value(2))
                .andExpect(jsonPath("$.content[2].total").value(2.20))
                .andExpect(jsonPath("$.content[2].closedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Should paginate and only show the sales of the authenticated cashier")
    void shouldPaginateOwnSales() throws Exception {
        for (int i = 0; i < 3; i++) {
            String sale = openSale();
            mockMvc.perform(patch("/sales/" + sale + "/cancel").header("Authorization", token))
                    .andExpect(status().isNoContent());
        }
        String otherEmail = "otro-" + UUID.randomUUID() + "@pos.test";
        userRepositoryPort.save(
                UserModel.create("Otro", "Cajero", otherEmail, passwordHasherPort.hash("pw")));
        String otherToken = login(otherEmail, "pw");

        mockMvc.perform(get("/sales?page=1&size=2").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/sales").header("Authorization", otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should reject an invalid page size and require authentication")
    void shouldValidateAndAuthenticate() throws Exception {
        mockMvc.perform(get("/sales?size=0").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/sales")).andExpect(status().isUnauthorized());
    }
}
