package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Terminal state: order delivered. */
public class DeliveredState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.DELIVERED;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of();
    }
}
