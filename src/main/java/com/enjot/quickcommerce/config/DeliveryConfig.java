package com.enjot.quickcommerce.config;

import com.enjot.quickcommerce.service.delivery.DeliveryCostStrategy;
import com.enjot.quickcommerce.service.delivery.FreeDeliveryAboveThresholdStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Selects the active {@link DeliveryCostStrategy}. The chosen policy is wired
 * here from configuration, so swapping strategies needs no code change in the
 * order service.
 */
@Configuration
public class DeliveryConfig {

    @Bean
    public DeliveryCostStrategy deliveryCostStrategy(
            @Value("${app.delivery.free-threshold}") BigDecimal freeThreshold,
            @Value("${app.delivery.standard-fee}") BigDecimal standardFee) {
        return new FreeDeliveryAboveThresholdStrategy(freeThreshold, standardFee);
    }
}
