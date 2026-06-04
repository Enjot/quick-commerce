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

class CatalogControllerTest extends MockMvcAuthSupport {

    private long createCategory(String admin, String name) throws Exception {
        String body = mockMvc.perform(post("/categories").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Map<String, Object> regularProduct(String sku, String name, long categoryId) {
        return Map.of("type", "REGULAR", "sku", sku, "name", name,
                "price", 4.50, "categoryId", categoryId, "stockQuantity", 15, "active", true);
    }

    @Test
    void adminCreatesCatalogAndPublicCanBrowse() throws Exception {
        String admin = adminBearer();
        long categoryId = createCategory(admin, "Bakery");

        String created = mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(regularProduct("CAT-BREAD", "Bread", categoryId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("REGULAR"))
                .andReturn().getResponse().getContentAsString();
        long productId = objectMapper.readTree(created).get("id").asLong();

        // Public (no token) browsing and lookup.
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CAT-BREAD"));
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotCreateProduct() throws Exception {
        String admin = adminBearer();
        long categoryId = createCategory(admin, "Snacks");
        String user = registerAndBearer("catalog-user@example.com", "1999-03-03");

        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, user)
                        .contentType(MediaType.APPLICATION_JSON).content(json(regularProduct("CAT-CHIPS", "Chips", categoryId))))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateSkuReturnsConflict() throws Exception {
        String admin = adminBearer();
        long categoryId = createCategory(admin, "Dairy");
        String payload = json(regularProduct("CAT-MILK", "Milk", categoryId));

        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated());
        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isConflict());
    }

    @Test
    void invalidProductPayloadReturnsBadRequest() throws Exception {
        String admin = adminBearer();
        long categoryId = createCategory(admin, "Produce");
        // Missing required price.
        mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("type", "REGULAR", "sku", "CAT-X", "name", "X",
                                "categoryId", categoryId, "stockQuantity", 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdatesAndDeletesProduct() throws Exception {
        String admin = adminBearer();
        long categoryId = createCategory(admin, "Frozen");
        String created = mockMvc.perform(post("/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(regularProduct("CAT-PEAS", "Peas", categoryId))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        Map<String, Object> update = Map.of("type", "REGULAR", "sku", "CAT-PEAS", "name", "Garden Peas",
                "price", 5.00, "categoryId", categoryId, "stockQuantity", 30, "active", true);
        mockMvc.perform(put("/products/{id}", id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Garden Peas"));

        mockMvc.perform(delete("/products/{id}", id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}
