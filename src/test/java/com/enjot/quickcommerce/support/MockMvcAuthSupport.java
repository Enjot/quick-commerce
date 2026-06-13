package com.enjot.quickcommerce.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base for full-stack MockMvc tests: boots the application against the shared
 * PostgreSQL container and provides helpers to obtain real JWT bearer tokens.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class MockMvcAuthSupport extends PostgresContainerSupport {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Keep delivery free across full-stack tests so order totals equal the item
     * subtotal; the delivery strategy itself is covered by dedicated unit tests.
     */
    @DynamicPropertySource
    static void freeDelivery(DynamicPropertyRegistry registry) {
        registry.add("app.delivery.free-threshold", () -> "0.00");
        registry.add("app.delivery.standard-fee", () -> "0.00");
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected String bearer(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return "Bearer " + node.get("token").asText();
    }

    protected String adminBearer() throws Exception {
        return bearer("admin1@mail.com", "admin123");
    }

    protected String registerAndBearer(String email, String dateOfBirth) throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", "password123", "dateOfBirth", dateOfBirth))));
        return bearer(email, "password123");
    }
}
