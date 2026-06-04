package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.AgeRestrictedProduct;
import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.domain.PerishableProduct;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.RegularProduct;
import com.enjot.quickcommerce.web.dto.ProductRequest;
import org.springframework.stereotype.Component;

/**
 * Builds and updates concrete {@link Product} subtypes from {@link ProductRequest}
 * payloads, enforcing the subtype-specific field requirements.
 */
@Component
public class ProductMapper {

    public Product toNewEntity(ProductRequest request, Category category) {
        Product product = switch (request.type()) {
            case REGULAR -> new RegularProduct();
            case PERISHABLE -> new PerishableProduct();
            case AGE_RESTRICTED -> new AgeRestrictedProduct();
        };
        applyCommon(product, request, category);
        applySubtype(product, request);
        return product;
    }

    public void applyUpdate(Product product, ProductRequest request, Category category) {
        if (product.getType() != request.type()) {
            throw new IllegalArgumentException("Product type cannot be changed after creation");
        }
        applyCommon(product, request, category);
        applySubtype(product, request);
    }

    private void applyCommon(Product product, ProductRequest request, Category category) {
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setStockQuantity(request.stockQuantity());
        product.setActive(request.active() == null || request.active());
    }

    private void applySubtype(Product product, ProductRequest request) {
        switch (product) {
            case PerishableProduct perishable -> {
                if (request.expiryDate() == null) {
                    throw new IllegalArgumentException("expiryDate is required for a perishable product");
                }
                perishable.setExpiryDate(request.expiryDate());
                perishable.setShelfLifeDays(request.shelfLifeDays());
            }
            case AgeRestrictedProduct restricted -> {
                if (request.minimumAge() == null || request.minimumAge() <= 0) {
                    throw new IllegalArgumentException("minimumAge must be positive for an age-restricted product");
                }
                restricted.setMinimumAge(request.minimumAge());
            }
            default -> {
                // RegularProduct has no extra fields.
            }
        }
    }
}
