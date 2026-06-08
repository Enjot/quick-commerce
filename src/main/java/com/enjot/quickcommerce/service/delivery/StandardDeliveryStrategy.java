package com.enjot.quickcommerce.service.delivery;

import java.math.BigDecimal;

/**
 * Charges a flat delivery fee regardless of order value.
 */
public class StandardDeliveryStrategy implements DeliveryCostStrategy {

    private final BigDecimal flatFee;

    public StandardDeliveryStrategy(BigDecimal flatFee) {
        this.flatFee = flatFee;
    }

    @Override
    public BigDecimal calculate(BigDecimal orderSubtotal) {
        return flatFee;
    }
}
