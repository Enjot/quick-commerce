package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;
import com.enjot.quickcommerce.exception.IllegalOrderTransitionException;

import java.util.Set;

/**
 * State pattern: each order lifecycle state is its own object that knows which
 * subsequent states are reachable from it. The order delegates transition
 * validation to its current state, so the rules live with the state, not in a
 * sprawling conditional.
 */
public interface OrderState {

    OrderStatus status();

    Set<OrderStatus> allowedTransitions();

    default boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }

    /**
     * @throws IllegalOrderTransitionException if {@code target} is not reachable from this state.
     */
    default void checkTransitionTo(OrderStatus target) {
        if (!allowedTransitions().contains(target)) {
            throw new IllegalOrderTransitionException(status(), target);
        }
    }
}
