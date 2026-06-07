package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;

import java.util.Map;

/**
 * Resolves the {@link OrderState} object for a given {@link OrderStatus}.
 * State objects are stateless and therefore shared singletons.
 */
public final class OrderStateFactory {

    private static final Map<OrderStatus, OrderState> REGISTRY = Map.of(
            OrderStatus.NEW, new NewState(),
            OrderStatus.PAID, new PaidState(),
            OrderStatus.PICKING, new PickingState(),
            OrderStatus.PACKED, new PackedState(),
            OrderStatus.IN_DELIVERY, new InDeliveryState(),
            OrderStatus.DELIVERED, new DeliveredState(),
            OrderStatus.CANCELLED, new CancelledState());

    private OrderStateFactory() {
    }

    public static OrderState forStatus(OrderStatus status) {
        OrderState state = REGISTRY.get(status);
        if (state == null) {
            throw new IllegalArgumentException("No state registered for status " + status);
        }
        return state;
    }
}
