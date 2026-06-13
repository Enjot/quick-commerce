package com.enjot.quickcommerce.config;

import com.enjot.quickcommerce.service.delivery.DeliveryCostStrategy;
import com.enjot.quickcommerce.service.delivery.FreeDeliveryAboveThresholdStrategy;
import com.enjot.quickcommerce.service.delivery.StandardDeliveryStrategy;
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
            @Value("${app.delivery.strategy:free-above-threshold}") String strategy,
            @Value("${app.delivery.free-threshold}") BigDecimal freeThreshold,
            @Value("${app.delivery.standard-fee}") BigDecimal standardFee) {
        return switch (strategy) {
            case "standard" -> new StandardDeliveryStrategy(standardFee);
            case "free-above-threshold" -> new FreeDeliveryAboveThresholdStrategy(freeThreshold, standardFee);
            default -> throw new IllegalArgumentException(
                    "Unknown delivery strategy '" + strategy + "'; expected 'standard' or 'free-above-threshold'");
        };
    }
}
