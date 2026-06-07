package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Items being picked from the warehouse. */
public class PickingState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.PICKING;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of(OrderStatus.PACKED);
    }
}
