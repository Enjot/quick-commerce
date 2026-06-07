package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Out for delivery to the customer. */
public class InDeliveryState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.IN_DELIVERY;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of(OrderStatus.DELIVERED);
    }
}
