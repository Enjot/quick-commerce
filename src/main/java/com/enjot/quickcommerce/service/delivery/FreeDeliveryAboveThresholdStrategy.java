package com.enjot.quickcommerce.service.delivery;

import java.math.BigDecimal;

/**
 * Waives the delivery fee once the order subtotal reaches a threshold,
 * otherwise charges a flat base fee.
 */
public class FreeDeliveryAboveThresholdStrategy implements DeliveryCostStrategy {

    private final BigDecimal threshold;
    private final BigDecimal baseFee;

    public FreeDeliveryAboveThresholdStrategy(BigDecimal threshold, BigDecimal baseFee) {
        this.threshold = threshold;
        this.baseFee = baseFee;
    }

    @Override
    public BigDecimal calculate(BigDecimal orderSubtotal) {
        return orderSubtotal.compareTo(threshold) >= 0 ? BigDecimal.ZERO : baseFee;
    }
}
