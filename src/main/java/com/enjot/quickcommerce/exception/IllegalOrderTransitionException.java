package com.enjot.quickcommerce.exception;

import com.enjot.quickcommerce.domain.OrderStatus;

/**
 * Raised when an order is asked to move to a status that the current state
 * does not permit.
 */
public class IllegalOrderTransitionException extends DomainException {

    public IllegalOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Illegal order status transition: %s -> %s".formatted(from, to));
    }
}
