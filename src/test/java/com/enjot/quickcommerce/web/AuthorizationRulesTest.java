package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.support.PostgresContainerSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the role-based access rules independent of business handlers:
 * the security chain authorizes (or rejects) before dispatch.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationRulesTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String bearer(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return "Bearer " + node.get("token").asText();
    }

    private String userToken() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "rbac-user@example.com",
                        "password", "password123", "dateOfBirth", "2000-05-01"))));
        return bearer("rbac-user@example.com", "password123");
    }

    private String adminToken() throws Exception {
        return bearer("admin@qcommerce.local", "admin123");
    }

    @Test
    void anonymousRequestToProtectedEndpointIsUnauthorized() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userIsForbiddenOnAdminEndpoint() throws Exception {
        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, userToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminIsAuthorizedOnAdminEndpoint() throws Exception {
        // Authorized by role; no handler is mapped yet, so the request passes
        // authorization and resolves to 404 rather than 401/403.
        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminIsForbiddenOnUserOnlyCart() throws Exception {
        mockMvc.perform(get("/cart").header(HttpHeaders.AUTHORIZATION, adminToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
