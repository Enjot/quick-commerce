package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.AgeRestrictedProduct;
import com.enjot.quickcommerce.domain.Cart;
import com.enjot.quickcommerce.domain.LineItem;
import com.enjot.quickcommerce.domain.Order;
import com.enjot.quickcommerce.domain.OrderStatus;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.RegularProduct;
import com.enjot.quickcommerce.domain.User;
import com.enjot.quickcommerce.exception.IllegalOrderTransitionException;
import com.enjot.quickcommerce.exception.ProductNotPurchasableException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.CartRepository;
import com.enjot.quickcommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orders;
    @Mock
    private CartRepository carts;
    @Mock
    private CartService cartService;

    private OrderService service;

    @BeforeEach
    void setUp() {
        // Free delivery keeps totals equal to the item subtotal for these cases.
        service = new OrderService(orders, carts, cartService, subtotal -> BigDecimal.ZERO);
    }

    private User adult() {
        User user = new User();
        user.setId(1L);
        user.setEmail("buyer@example.com");
        user.setDateOfBirth(LocalDate.now().minusYears(30));
        return user;
    }

    private RegularProduct regular(String price, int stock) {
        RegularProduct product = new RegularProduct();
        product.setId(5L);
        product.setSku("REG");
        product.setName("Bread");
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stock);
        product.setActive(true);
        return product;
    }

    private Cart cartWith(User user, Product product, int quantity) {
        Cart cart = new Cart(user);
        cart.setId(10L);
        cart.addLineItem(new LineItem(product, quantity, product.getPrice()));
        return cart;
    }

    @Test
    void checkoutConvertsCartFreezesPriceDecrementsStockAndClearsCart() {
        User user = adult();
        Product product = regular("4.00", 10);
        Cart cart = cartWith(user, product, 3);
        given(cartService.getOrCreateCart(1L)).willReturn(cart);
        given(orders.save(any(Order.class))).willAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(77L);
            return o;
        });

        var response = service.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("1 Main St"));

        assertThat(response.id()).isEqualTo(77L);
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        assertThat(response.totalAmount()).isEqualByComparingTo("12.00");
        assertThat(response.items()).singleElement()
                .satisfies(item -> assertThat(item.unitPrice()).isEqualByComparingTo("4.00"));
        assertThat(product.getStockQuantity()).isEqualTo(7);
        assertThat(cart.getLineItems()).isEmpty();
    }

    @Test
    void priceSnapshotIsIndependentOfLaterCatalogueChange() {
        User user = adult();
        Product product = regular("4.00", 10);
        Cart cart = cartWith(user, product, 2);
        given(cartService.getOrCreateCart(1L)).willReturn(cart);
        given(orders.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        var response = service.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("addr"));
        product.setPrice(new BigDecimal("99.00")); // catalogue price changes afterwards

        assertThat(response.totalAmount()).isEqualByComparingTo("8.00");
        assertThat(response.items().getFirst().unitPrice()).isEqualByComparingTo("4.00");
    }

    @Test
    void checkoutAddsDeliveryFeeFromStrategy() {
        OrderService withFee = new OrderService(orders, carts, cartService, subtotal -> new BigDecimal("5.00"));
        User user = adult();
        Product product = regular("4.00", 10);
        given(cartService.getOrCreateCart(1L)).willReturn(cartWith(user, product, 2));
        given(orders.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        var response = withFee.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("addr"));

        assertThat(response.deliveryFee()).isEqualByComparingTo("5.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("13.00"); // 8.00 items + 5.00 delivery
    }

    @Test
    void checkoutEmptyCartIsRejected() {
        Cart cart = new Cart(adult());
        given(cartService.getOrCreateCart(1L)).willReturn(cart);

        assertThatThrownBy(() -> service.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("addr")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkoutRejectsUnderageCustomerForRestrictedProduct() {
        User minor = adult();
        minor.setDateOfBirth(LocalDate.now().minusYears(15));
        AgeRestrictedProduct wine = new AgeRestrictedProduct();
        wine.setId(8L);
        wine.setSku("WINE");
        wine.setName("Wine");
        wine.setPrice(new BigDecimal("20.00"));
        wine.setStockQuantity(5);
        wine.setActive(true);
        wine.setMinimumAge(18);
        given(cartService.getOrCreateCart(1L)).willReturn(cartWith(minor, wine, 1));

        assertThatThrownBy(() -> service.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("addr")))
                .isInstanceOf(ProductNotPurchasableException.class);
    }

    @Test
    void checkoutRejectsInsufficientStock() {
        User user = adult();
        Product product = regular("4.00", 1);
        given(cartService.getOrCreateCart(1L)).willReturn(cartWith(user, product, 5));

        assertThatThrownBy(() -> service.checkout(1L, new com.enjot.quickcommerce.web.dto.CheckoutRequest("addr")))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void transitionStatusRecordsHistoryOnLegalMove() {
        Order order = new Order();
        order.setId(3L);
        order.setStatus(OrderStatus.NEW);
        given(orders.findById(3L)).willReturn(Optional.of(order));

        var response = service.transitionStatus(3L, OrderStatus.PAID, "admin1@mail.com");

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getStatusHistory()).extracting("status").contains(OrderStatus.PAID);
    }

    @Test
    void transitionStatusRejectsIllegalMove() {
        Order order = new Order();
        order.setId(3L);
        order.setStatus(OrderStatus.NEW);
        given(orders.findById(3L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> service.transitionStatus(3L, OrderStatus.DELIVERED, "admin"))
                .isInstanceOf(IllegalOrderTransitionException.class);
    }
}
