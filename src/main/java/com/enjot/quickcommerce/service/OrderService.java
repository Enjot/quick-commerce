package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Cart;
import com.enjot.quickcommerce.domain.LineItem;
import com.enjot.quickcommerce.domain.Order;
import com.enjot.quickcommerce.domain.OrderStatus;
import com.enjot.quickcommerce.domain.OrderStatusHistory;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.User;
import com.enjot.quickcommerce.domain.order.state.OrderState;
import com.enjot.quickcommerce.domain.order.state.OrderStateFactory;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.CartRepository;
import com.enjot.quickcommerce.repository.OrderRepository;
import com.enjot.quickcommerce.web.dto.CheckoutRequest;
import com.enjot.quickcommerce.web.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final CartRepository carts;
    private final CartService cartService;

    public OrderService(OrderRepository orders, CartRepository carts, CartService cartService) {
        this.orders = orders;
        this.carts = carts;
        this.cartService = cartService;
    }

    /**
     * Converts the user's cart into an order, freezing a unit-price snapshot on
     * each line and validating every product polymorphically before purchase.
     */
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart");
        }
        User user = cart.getUser();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.NEW);
        order.setDeliveryAddress(request.deliveryAddress());
        order.setDeliveryFee(BigDecimal.ZERO);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineItem cartItem : cart.getLineItems()) {
            Product product = cartItem.getProduct();
            product.validateForOrder(user.getDateOfBirth());
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ResourceConflictException("Insufficient stock for product " + product.getSku());
            }
            BigDecimal snapshotPrice = product.getPrice();
            order.addLineItem(new LineItem(product, cartItem.getQuantity(), snapshotPrice));
            subtotal = subtotal.add(snapshotPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }

        order.setTotalAmount(subtotal.add(order.getDeliveryFee()));
        order.addStatusHistory(new OrderStatusHistory(OrderStatus.NEW, user.getEmail()));
        Order saved = orders.save(order);

        cart.getLineItems().clear();
        carts.save(cart);

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(Long userId, boolean admin) {
        List<Order> result = admin ? orders.findAll() : orders.findByUserId(userId);
        return result.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long orderId, Long userId, boolean admin) {
        Order order = admin
                ? orders.findById(orderId).orElseThrow(() -> EntityNotFoundException.of("Order", orderId))
                : orders.findByIdAndUserId(orderId, userId).orElseThrow(() -> EntityNotFoundException.of("Order", orderId));
        return OrderResponse.from(order);
    }

    /**
     * Applies a status transition, delegating the legality check to the current
     * {@link OrderState} and recording the change in the order's history.
     */
    @Transactional
    public OrderResponse transitionStatus(Long orderId, OrderStatus target, String changedBy) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> EntityNotFoundException.of("Order", orderId));
        OrderState current = OrderStateFactory.forStatus(order.getStatus());
        current.checkTransitionTo(target);
        order.setStatus(target);
        order.addStatusHistory(new OrderStatusHistory(target, changedBy));
        return OrderResponse.from(order);
    }
}
