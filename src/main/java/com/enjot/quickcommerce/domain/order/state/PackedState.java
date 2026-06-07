package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Set;

/** Order packed and ready to dispatch. */
public class PackedState implements OrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.PACKED;
    }

    @Override
    public Set<OrderStatus> allowedTransitions() {
        return Set.of(OrderStatus.IN_DELIVERY);
    }
}
