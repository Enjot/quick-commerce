package com.enjot.quickcommerce.domain;

import com.enjot.quickcommerce.exception.ProductNotPurchasableException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Perishable product that must not be sold past its expiry date.
 */
@Entity
@DiscriminatorValue("PERISHABLE")
@Getter
@Setter
public class PerishableProduct extends Product {

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Override
    public ProductType getType() {
        return ProductType.PERISHABLE;
    }

    @Override
    public void validateForOrder(LocalDate customerDateOfBirth) {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new ProductNotPurchasableException(
                    "Product '%s' expired on %s and cannot be ordered".formatted(getSku(), expiryDate));
        }
    }
}
