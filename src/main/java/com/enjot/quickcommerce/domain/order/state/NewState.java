package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Freshly created order, awaiting payment. */
public class NewState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.NEW;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of(OrderStatus.PAID, OrderStatus.CANCELLED);
    }
}
