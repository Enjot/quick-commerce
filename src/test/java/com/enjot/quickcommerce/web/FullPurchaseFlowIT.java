package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.support.MockMvcAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end happy path across all layers on a real PostgreSQL container:
 * catalogue setup, registration, browsing, cart, checkout, and the full
 * order lifecycle driven by the admin.
 */
class FullPurchaseFlowIT extends MockMvcAuthSupport {

    @Test
    void customerBuysAndAdminFulfilsOrderEndToEnd() throws Exception {
        String admin = adminBearer();

        long categoryId = objectMapper.readTree(mockMvc.perform(post("/categories")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "E2E Pantry"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();

        long productId = objectMapper.readTree(mockMvc.perform(post("/products")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("type", "REGULAR", "sku", "E2E-RICE", "name", "Rice",
                                "price", 7.00, "categoryId", categoryId, "stockQuantity", 5, "active", true))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asLong();

        String customer = registerAndBearer("e2e-customer@example.com", "1995-02-02");

        // Browse the catalogue (public).
        mockMvc.perform(get("/products?search=Rice")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("E2E-RICE"));

        // Add to cart.
        mockMvc.perform(post("/cart/items").header(HttpHeaders.AUTHORIZATION, customer)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("productId", productId, "quantity", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(21.00));

        // Checkout.
        long orderId = objectMapper.readTree(mockMvc.perform(post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, customer)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("deliveryAddress", "5 Market Rd"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(21.00))
                .andReturn().getResponse().getContentAsString()).get("id").asLong();

        // Admin drives the order through every legal transition to DELIVERED.
        for (String next : new String[]{"PAID", "PICKING", "PACKED", "IN_DELIVERY", "DELIVERED"}) {
            mockMvc.perform(patch("/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, admin)
                            .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", next))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }

        // Customer sees the delivered order with a full status history.
        mockMvc.perform(get("/orders/{id}", orderId).header(HttpHeaders.AUTHORIZATION, customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.statusHistory.length()").value(6));
    }
}
