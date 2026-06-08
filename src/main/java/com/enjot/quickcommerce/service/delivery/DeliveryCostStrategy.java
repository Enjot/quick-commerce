package com.enjot.quickcommerce.service.delivery;

import java.math.BigDecimal;

/**
 * Strategy pattern: encapsulates how the delivery fee is derived from an order
 * subtotal, so the policy can be swapped without touching checkout logic.
 */
public interface DeliveryCostStrategy {

    BigDecimal calculate(BigDecimal orderSubtotal);
}
