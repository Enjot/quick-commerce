package com.enjot.quickcommerce.config;

import com.enjot.quickcommerce.service.delivery.DeliveryCostStrategy;
import com.enjot.quickcommerce.service.delivery.FreeDeliveryAboveThresholdStrategy;
import com.enjot.quickcommerce.service.delivery.StandardDeliveryStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryConfigTest {

    private static final BigDecimal THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal FEE = new BigDecimal("9.99");

    private final DeliveryConfig config = new DeliveryConfig();

    @Test
    void selectsFreeAboveThresholdStrategy() {
        DeliveryCostStrategy strategy =
                config.deliveryCostStrategy("free-above-threshold", THRESHOLD, FEE);

        assertThat(strategy).isInstanceOf(FreeDeliveryAboveThresholdStrategy.class);
        assertThat(strategy.calculate(new BigDecimal("50.00"))).isEqualByComparingTo("9.99");
        assertThat(strategy.calculate(new BigDecimal("100.00"))).isEqualByComparingTo("0.00");
    }

    @Test
    void selectsStandardStrategy() {
        DeliveryCostStrategy strategy =
                config.deliveryCostStrategy("standard", THRESHOLD, FEE);

        assertThat(strategy).isInstanceOf(StandardDeliveryStrategy.class);
        assertThat(strategy.calculate(new BigDecimal("50.00"))).isEqualByComparingTo("9.99");
        assertThat(strategy.calculate(new BigDecimal("500.00"))).isEqualByComparingTo("9.99");
    }

    @Test
    void rejectsUnknownStrategy() {
        assertThatThrownBy(() -> config.deliveryCostStrategy("express", THRESHOLD, FEE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("express");
    }
}
