package com.enjot.quickcommerce.domain.order.state;

import com.enjot.quickcommerce.domain.OrderStatus;
import com.enjot.quickcommerce.exception.IllegalOrderTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class OrderStateTest {

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void factoryResolvesAStateForEveryStatus(OrderStatus status) {
        assertThat(OrderStateFactory.forStatus(status).status()).isEqualTo(status);
    }

    @Test
    void newStateAllowsPaymentAndCancellation() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.NEW);

        assertThat(state.allowedTransitions())
                .containsExactlyInAnyOrder(OrderStatus.PAID, OrderStatus.CANCELLED);
        assertThatCode(() -> state.checkTransitionTo(OrderStatus.PAID)).doesNotThrowAnyException();
    }

    @Test
    void illegalTransitionIsRejected() {
        OrderState state = OrderStateFactory.forStatus(OrderStatus.NEW);

        assertThatThrownBy(() -> state.checkTransitionTo(OrderStatus.DELIVERED))
                .isInstanceOf(IllegalOrderTransitionException.class);
    }

    @Test
    void deliveredAndCancelledAreTerminal() {
        assertThat(OrderStateFactory.forStatus(OrderStatus.DELIVERED).isTerminal()).isTrue();
        assertThat(OrderStateFactory.forStatus(OrderStatus.CANCELLED).isTerminal()).isTrue();
    }

    @Test
    void happyPathChainIsFullyConnected() {
        OrderStatus[] chain = {OrderStatus.NEW, OrderStatus.PAID, OrderStatus.PICKING,
                OrderStatus.PACKED, OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED};
        for (int i = 0; i < chain.length - 1; i++) {
            OrderState state = OrderStateFactory.forStatus(chain[i]);
            int next = i + 1;
            assertThatCode(() -> state.checkTransitionTo(chain[next])).doesNotThrowAnyException();
        }
    }
}
