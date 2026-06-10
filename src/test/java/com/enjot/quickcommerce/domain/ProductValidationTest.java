package com.enjot.quickcommerce.domain;

import com.enjot.quickcommerce.exception.ProductNotPurchasableException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the polymorphic {@code validateForOrder} contract across the
 * product hierarchy.
 */
class ProductValidationTest {

    @Test
    void regularProductHasNoRestrictions() {
        assertThatCode(() -> new RegularProduct().validateForOrder(null)).doesNotThrowAnyException();
        assertThatCode(() -> new RegularProduct().validateForOrder(LocalDate.now())).doesNotThrowAnyException();
    }

    @Test
    void perishableRejectsExpiredAndAllowsFresh() {
        PerishableProduct expired = new PerishableProduct();
        expired.setSku("MILK");
        expired.setExpiryDate(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> expired.validateForOrder(null))
                .isInstanceOf(ProductNotPurchasableException.class);

        PerishableProduct fresh = new PerishableProduct();
        fresh.setExpiryDate(LocalDate.now().plusDays(3));
        assertThatCode(() -> fresh.validateForOrder(null)).doesNotThrowAnyException();

        PerishableProduct undated = new PerishableProduct();
        assertThatCode(() -> undated.validateForOrder(null)).doesNotThrowAnyException();
    }

    @Test
    void ageRestrictedEnforcesMinimumAge() {
        AgeRestrictedProduct wine = new AgeRestrictedProduct();
        wine.setSku("WINE");
        wine.setMinimumAge(18);

        assertThatThrownBy(() -> wine.validateForOrder(LocalDate.now().minusYears(16)))
                .isInstanceOf(ProductNotPurchasableException.class);
        assertThatThrownBy(() -> wine.validateForOrder(null))
                .isInstanceOf(ProductNotPurchasableException.class);
        assertThatCode(() -> wine.validateForOrder(LocalDate.now().minusYears(21)))
                .doesNotThrowAnyException();
    }
}
