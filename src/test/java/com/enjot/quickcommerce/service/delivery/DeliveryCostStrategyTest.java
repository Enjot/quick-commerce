package com.enjot.quickcommerce.service.delivery;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryCostStrategyTest {

    @Test
    void standardStrategyAlwaysChargesFlatFee() {
        DeliveryCostStrategy strategy = new StandardDeliveryStrategy(new BigDecimal("9.99"));

        assertThat(strategy.calculate(new BigDecimal("5.00"))).isEqualByComparingTo("9.99");
        assertThat(strategy.calculate(new BigDecimal("500.00"))).isEqualByComparingTo("9.99");
    }

    @Test
    void freeAboveThresholdWaivesFeeAtOrAboveThreshold() {
        DeliveryCostStrategy strategy =
                new FreeDeliveryAboveThresholdStrategy(new BigDecimal("100.00"), new BigDecimal("9.99"));

        assertThat(strategy.calculate(new BigDecimal("99.99"))).isEqualByComparingTo("9.99");
        assertThat(strategy.calculate(new BigDecimal("100.00"))).isEqualByComparingTo("0.00");
        assertThat(strategy.calculate(new BigDecimal("150.00"))).isEqualByComparingTo("0.00");
    }
}
