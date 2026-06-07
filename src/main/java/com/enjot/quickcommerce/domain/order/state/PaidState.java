package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Payment received; can move to fulfilment or be cancelled. */
public class PaidState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.PAID;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of(OrderStatus.PICKING, OrderStatus.CANCELLED);
    }
}
