package com.enjot.quickcommerce.domain;

/**
 * Lifecycle states of an {@link Order}. Allowed transitions between these
 * states are governed by the State pattern in {@code domain.order.state}.
 */
public enum OrderStatus {
    NEW,
    PAID,
    PICKING,
    PACKED,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED
}
