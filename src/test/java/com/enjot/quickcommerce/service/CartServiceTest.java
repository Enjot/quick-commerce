package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Cart;
import com.enjot.quickcommerce.domain.LineItem;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.RegularProduct;
import com.enjot.quickcommerce.domain.User;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.repository.CartRepository;
import com.enjot.quickcommerce.repository.LineItemRepository;
import com.enjot.quickcommerce.repository.ProductRepository;
import com.enjot.quickcommerce.repository.UserRepository;
import com.enjot.quickcommerce.web.dto.AddCartItemRequest;
import com.enjot.quickcommerce.web.dto.CartResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository carts;
    @Mock
    private UserRepository users;
    @Mock
    private ProductRepository products;
    @Mock
    private LineItemRepository lineItems;

    private CartService service;

    @BeforeEach
    void setUp() {
        service = new CartService(carts, users, products, lineItems);
    }

    private Product product(long id, String price, boolean active) {
        RegularProduct product = new RegularProduct();
        product.setId(id);
        product.setSku("SKU-" + id);
        product.setName("Product " + id);
        product.setPrice(new BigDecimal(price));
        product.setActive(active);
        return product;
    }

    private Cart cartFor(long userId) {
        User user = new User();
        user.setId(userId);
        Cart cart = new Cart(user);
        cart.setId(100L);
        return cart;
    }

    @Test
    void addItemCreatesNewLineItemAndComputesTotal() {
        Cart cart = cartFor(1L);
        given(carts.findByUserId(1L)).willReturn(Optional.of(cart));
        given(products.findById(5L)).willReturn(Optional.of(product(5L, "4.00", true)));
        given(carts.save(any(Cart.class))).willAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.addItem(1L, new AddCartItemRequest(5L, 3));

        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo("12.00");
    }

    @Test
    void addItemMergesQuantityForExistingProduct() {
        Cart cart = cartFor(1L);
        cart.addLineItem(new LineItem(product(5L, "4.00", true), 2, new BigDecimal("4.00")));
        given(carts.findByUserId(1L)).willReturn(Optional.of(cart));
        given(products.findById(5L)).willReturn(Optional.of(product(5L, "4.00", true)));
        given(carts.save(any(Cart.class))).willAnswer(inv -> inv.getArgument(0));

        CartResponse response = service.addItem(1L, new AddCartItemRequest(5L, 3));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(5);
        assertThat(response.total()).isEqualByComparingTo("20.00");
    }

    @Test
    void addInactiveProductIsRejected() {
        given(carts.findByUserId(1L)).willReturn(Optional.of(cartFor(1L)));
        given(products.findById(5L)).willReturn(Optional.of(product(5L, "4.00", false)));

        assertThatThrownBy(() -> service.addItem(1L, new AddCartItemRequest(5L, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addMissingProductThrows() {
        given(carts.findByUserId(1L)).willReturn(Optional.of(cartFor(1L)));
        given(products.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(1L, new AddCartItemRequest(9L, 1)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeMissingItemThrows() {
        Cart cart = cartFor(1L);
        given(carts.findByUserId(1L)).willReturn(Optional.of(cart));
        given(lineItems.findByIdAndCartId(7L, 100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeItem(1L, 7L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getCartTotalReflectsCurrentProductPrice() {
        Cart cart = cartFor(1L);
        Product product = product(5L, "4.00", true);
        cart.addLineItem(new LineItem(product, 2, new BigDecimal("4.00")));
        given(carts.findByUserId(1L)).willReturn(Optional.of(cart));

        // Catalogue price rises after the item was added; the cart reflects it live.
        product.setPrice(new BigDecimal("6.00"));

        assertThat(service.getCart(1L).total()).isEqualByComparingTo("12.00");
    }
}
