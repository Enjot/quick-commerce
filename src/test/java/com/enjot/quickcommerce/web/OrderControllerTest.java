package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.support.MockMvcAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends MockMvcAuthSupport {

    private long createProduct(String admin, String sku, double price, int stock) throws Exception {
        String category = mockMvc.perform(post("/categories").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "OrdCat-" + sku))))
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(category).get("id").asLong();
        String product = mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("type", "REGULAR", "sku", sku, "name", sku,
                                "price", price, "categoryId", categoryId, "stockQuantity", stock, "active", true))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(product).get("id").asLong();
    }

    private void addToCart(String user, long productId, int qty) throws Exception {
        mockMvc.perform(post("/cart/items").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("productId", productId, "quantity", qty))))
                .andExpect(status().isOk());
    }

    private long checkout(String user) throws Exception {
        String order = mockMvc.perform(post("/orders").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("deliveryAddress", "1 Main St"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(order).get("id").asLong();
    }

    @Test
    void checkoutFreezesPriceAndAdminDrivesLifecycle() throws Exception {
        String admin = adminBearer();
        long productId = createProduct(admin, "ORD-A", 4.00, 10);
        String user = registerAndBearer("order-flow@example.com", "1990-01-01");
        addToCart(user, productId, 2);

        long orderId = checkout(user);

        // Catalogue price changes after checkout; the order keeps its snapshot.
        long categoryId = objectMapper.readTree(mockMvc.perform(get("/products/{id}", productId))
                .andReturn().getResponse().getContentAsString()).get("categoryId").asLong();
        mockMvc.perform(put("/products/{id}", productId).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REGULAR", "sku", "ORD-A", "name", "ORD-A",
                        "price", 99.00, "categoryId", categoryId, "stockQuantity", 10, "active", true))));

        mockMvc.perform(get("/orders/{id}", orderId).header(HttpHeaders.AUTHORIZATION, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(8.00));

        // ADMIN advances the order through legal transitions.
        mockMvc.perform(patch("/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PAID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // Illegal jump PAID -> DELIVERED is rejected.
        mockMvc.perform(patch("/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "DELIVERED"))))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PICKING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKING"));
    }

    @Test
    void userCannotTransitionStatusAndCannotSeeOthersOrders() throws Exception {
        String admin = adminBearer();
        long productId = createProduct(admin, "ORD-B", 5.00, 10);
        String owner = registerAndBearer("order-owner@example.com", "1990-01-01");
        addToCart(owner, productId, 1);
        long orderId = checkout(owner);

        // USER may not change status.
        mockMvc.perform(patch("/orders/{id}/status", orderId).header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PAID"))))
                .andExpect(status().isForbidden());

        // A different user cannot read the order.
        String other = registerAndBearer("order-other@example.com", "1990-01-01");
        mockMvc.perform(get("/orders/{id}", orderId).header(HttpHeaders.AUTHORIZATION, other))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutWithEmptyCartReturnsBadRequest() throws Exception {
        String user = registerAndBearer("order-empty@example.com", "1990-01-01");
        mockMvc.perform(post("/orders").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("deliveryAddress", "1 Main St"))))
                .andExpect(status().isBadRequest());
    }
}
