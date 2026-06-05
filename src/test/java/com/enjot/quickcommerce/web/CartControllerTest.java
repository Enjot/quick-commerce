package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.support.MockMvcAuthSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends MockMvcAuthSupport {

    private long createProduct(String admin, String sku, double price) throws Exception {
        String category = mockMvc.perform(post("/categories").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", "Cat-" + sku))))
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(category).get("id").asLong();
        String product = mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("type", "REGULAR", "sku", sku, "name", sku,
                                "price", price, "categoryId", categoryId, "stockQuantity", 50, "active", true))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(product).get("id").asLong();
    }

    @Test
    void userManagesCartAndTotalTracksLivePrice() throws Exception {
        String admin = adminBearer();
        long productId = createProduct(admin, "CART-WIDGET", 4.00);
        String user = registerAndBearer("cart-flow@example.com", "1998-07-07");

        // Add two units -> total 8.00
        String afterAdd = mockMvc.perform(post("/cart/items").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("productId", productId, "quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(8.00))
                .andReturn().getResponse().getContentAsString();
        long lineItemId = objectMapper.readTree(afterAdd).get("items").get(0).get("id").asLong();

        // Admin raises the price; the cart total reflects it live.
        mockMvc.perform(put("/products/{id}", productId).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REGULAR", "sku", "CART-WIDGET", "name", "CART-WIDGET",
                        "price", 6.00, "categoryId",
                        objectMapper.readTree(mockMvc.perform(get("/products/{id}", productId))
                                .andReturn().getResponse().getContentAsString()).get("categoryId").asLong(),
                        "stockQuantity", 50, "active", true))));

        mockMvc.perform(get("/cart").header(HttpHeaders.AUTHORIZATION, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12.00));

        mockMvc.perform(delete("/cart/items/{id}", lineItemId).header(HttpHeaders.AUTHORIZATION, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void addingInactiveProductIsRejected() throws Exception {
        String admin = adminBearer();
        long productId = createProduct(admin, "CART-INACTIVE", 9.99);
        // Deactivate it.
        long categoryId = objectMapper.readTree(mockMvc.perform(get("/products/{id}", productId))
                .andReturn().getResponse().getContentAsString()).get("categoryId").asLong();
        mockMvc.perform(put("/products/{id}", productId).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("type", "REGULAR", "sku", "CART-INACTIVE", "name", "CART-INACTIVE",
                        "price", 9.99, "categoryId", categoryId, "stockQuantity", 5, "active", false))));

        String user = registerAndBearer("cart-inactive@example.com", "1998-07-07");
        mockMvc.perform(post("/cart/items").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("productId", productId, "quantity", 1))))
                .andExpect(status().isBadRequest());
    }
}
