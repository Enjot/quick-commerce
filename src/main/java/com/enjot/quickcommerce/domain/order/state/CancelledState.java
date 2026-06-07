package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Terminal state: order cancelled. */
public class CancelledState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.CANCELLED;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of();
    }
}
