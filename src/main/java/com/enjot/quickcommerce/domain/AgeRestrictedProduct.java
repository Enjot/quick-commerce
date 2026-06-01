package com.enjot.quickcommerce.domain;

import com.enjot.quickcommerce.exception.ProductNotPurchasableException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

/**
 * Product subject to a minimum-age requirement (e.g. alcohol, 18+).
 */
@Entity
@DiscriminatorValue("AGE_RESTRICTED")
@Getter
@Setter
public class AgeRestrictedProduct extends Product {

    @Column(name = "minimum_age")
    private int minimumAge;

    @Override
    public ProductType getType() {
        return ProductType.AGE_RESTRICTED;
    }

    @Override
    public void validateForOrder(LocalDate customerDateOfBirth) {
        if (customerDateOfBirth == null) {
            throw new ProductNotPurchasableException(
                    "Product '%s' requires a verified age of at least %d".formatted(getSku(), minimumAge));
        }
        int age = Period.between(customerDateOfBirth, LocalDate.now()).getYears();
        if (age < minimumAge) {
            throw new ProductNotPurchasableException(
                    "Product '%s' requires a minimum age of %d, customer is %d".formatted(getSku(), minimumAge, age));
        }
    }
}
