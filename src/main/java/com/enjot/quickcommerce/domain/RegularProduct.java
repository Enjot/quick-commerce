package com.enjot.quickcommerce.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Ordinary product with no purchase restrictions.
 */
@Entity
@DiscriminatorValue("REGULAR")
public class RegularProduct extends Product {

    @Override
    public ProductType getType() {
        return ProductType.REGULAR;
    }

    @Override
    public void validateForOrder(LocalDate customerDateOfBirth) {
        // No restrictions.
    }
}
