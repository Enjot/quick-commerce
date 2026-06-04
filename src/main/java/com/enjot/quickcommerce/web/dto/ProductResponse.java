package com.enjot.quickcommerce.web.dto;

import com.enjot.quickcommerce.domain.AgeRestrictedProduct;
import com.enjot.quickcommerce.domain.PerishableProduct;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
        Long id,
        ProductType type,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        int stockQuantity,
        boolean active,
        LocalDate expiryDate,
        Integer shelfLifeDays,
        Integer minimumAge) {

    public static ProductResponse from(Product product) {
        LocalDate expiryDate = null;
        Integer shelfLifeDays = null;
        Integer minimumAge = null;
        if (product instanceof PerishableProduct perishable) {
            expiryDate = perishable.getExpiryDate();
            shelfLifeDays = perishable.getShelfLifeDays();
        } else if (product instanceof AgeRestrictedProduct restricted) {
            minimumAge = restricted.getMinimumAge();
        }
        return new ProductResponse(
                product.getId(),
                product.getType(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getStockQuantity(),
                product.isActive(),
                expiryDate,
                shelfLifeDays,
                minimumAge);
    }
}
