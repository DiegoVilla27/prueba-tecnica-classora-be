package com.diegovilla.pos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.diegovilla.pos.features.user.application.ports.PasswordHasherPort;
import com.diegovilla.pos.features.user.application.ports.UserRepositoryPort;
import com.diegovilla.pos.features.user.domain.model.UserModel;
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
import org.springframework.test.web.servlet.ResultActions;

/**
 * Full checkout flow against PostgreSQL (Testcontainers): open, scan, change quantities, remove,
 * complete, cancel, and the ownership and state rules.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CheckoutFlowIT {

    private static final String MILK = "8412345000010";
    private static final String BAG = "8412345000119";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepositoryPort userRepositoryPort;
    @Autowired private PasswordHasherPort passwordHasherPort;

    private String token;

    @BeforeEach
    void loginAndCloseOpenSale() throws Exception {
        token = login("cajero@pos.test", "test-password");
        String current =
                mockMvc.perform(get("/sales/current").header("Authorization", bearer(token)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        if (current.contains("\"status\":\"OPEN\"")) {
            String id = JsonPath.read(current, "$.id");
            mockMvc.perform(
                            patch("/sales/" + id + "/cancel")
                                    .header("Authorization", bearer(token)))
                    .andExpect(status().isNoContent());
        }
    }

    private String login(String email, String password) throws Exception {
        String body =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\": \"%s\", \"password\": \"%s\"}"
                                                        .formatted(email, password)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String openSale() throws Exception {
        String body =
                mockMvc.perform(post("/sales").header("Authorization", bearer(token)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private ResultActions addLine(String saleId, String code, int quantity) throws Exception {
        return mockMvc.perform(
                post("/sales/" + saleId + "/lines")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"%s\", \"quantity\": %d}".formatted(code, quantity)));
    }

    private static String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Should run a full sale: scan, merge, change, remove, complete and lock it")
    void shouldRunFullSale() throws Exception {
        mockMvc.perform(get("/sales/current").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());

        String location =
                mockMvc.perform(post("/sales").header("Authorization", bearer(token)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("OPEN"))
                        .andExpect(jsonPath("$.lines").isEmpty())
                        .andExpect(jsonPath("$.total").value(0))
                        .andReturn()
                        .getResponse()
                        .getHeader("Location");
        String saleId = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(post("/sales").header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The cashier already has an open sale."));

        addLine(saleId, MILK, 1).andExpect(status().isOk());
        addLine(saleId, MILK, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.lines[0].subtotal").value(1.90));
        String afterBag =
                body(
                        addLine(saleId, BAG, 3)
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lines[1].code").value(BAG))
                                .andExpect(jsonPath("$.itemCount").value(5)));
        assertThat(afterBag).contains("\"total\":2.20");
        String milkLineId = JsonPath.read(afterBag, "$.lines[0].id");
        String bagLineId = JsonPath.read(afterBag, "$.lines[1].id");

        String afterChange =
                body(
                        mockMvc.perform(
                                        patch("/sales/" + saleId + "/lines/" + milkLineId)
                                                .header("Authorization", bearer(token))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"quantity\": 4}"))
                                .andExpect(status().isOk()));
        assertThat(afterChange).contains("\"total\":4.10");

        addLine(saleId, MILK, 996)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Quantity must be between 1 and 999."));
        addLine(saleId, "9999999999999", 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found."));
        addLine(saleId, "12ab", 0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));

        String afterRemove =
                body(
                        mockMvc.perform(
                                        delete("/sales/" + saleId + "/lines/" + bagLineId)
                                                .header("Authorization", bearer(token)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lines.length()").value(1)));
        assertThat(afterRemove).contains("\"total\":3.80");

        mockMvc.perform(get("/sales/current").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saleId));

        mockMvc.perform(
                        patch("/sales/" + saleId + "/complete")
                                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.closedAt").isNotEmpty());

        addLine(saleId, BAG, 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The sale is not open."));
        mockMvc.perform(get("/sales/" + saleId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should reject completing an empty sale and allow cancelling it once")
    void shouldCancelEmptySale() throws Exception {
        String saleId = openSale();

        mockMvc.perform(
                        patch("/sales/" + saleId + "/complete")
                                .header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("An empty sale cannot be completed."));
        mockMvc.perform(
                        patch("/sales/" + saleId + "/cancel")
                                .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(
                        patch("/sales/" + saleId + "/cancel")
                                .header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/sales/current").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should forbid a cashier to read or change a sale of another cashier")
    void shouldForbidSaleOfAnotherCashier() throws Exception {
        String saleId = openSale();
        if (userRepositoryPort.getByEmail("otro@pos.test").isEmpty()) {
            userRepositoryPort.save(
                    UserModel.create(
                            "Otro",
                            "Cajero",
                            "otro@pos.test",
                            passwordHasherPort.hash("other-pw")));
        }
        String otherToken = login("otro@pos.test", "other-pw");

        mockMvc.perform(get("/sales/" + saleId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(
                        patch("/sales/" + saleId + "/cancel")
                                .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName(
            "Should return 404 for an unknown sale, 400 for an invalid id and 401 without token")
    void shouldRejectUnknownOrInvalidSaleId() throws Exception {
        mockMvc.perform(
                        get("/sales/00000000-0000-0000-0000-000000000000")
                                .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sale not found."));
        mockMvc.perform(get("/sales/not-a-uuid").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/sales")).andExpect(status().isUnauthorized());
    }
}
