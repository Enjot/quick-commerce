package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Cart;
import com.enjot.quickcommerce.domain.LineItem;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.User;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.repository.CartRepository;
import com.enjot.quickcommerce.repository.LineItemRepository;
import com.enjot.quickcommerce.repository.ProductRepository;
import com.enjot.quickcommerce.repository.UserRepository;
import com.enjot.quickcommerce.web.dto.AddCartItemRequest;
import com.enjot.quickcommerce.web.dto.CartResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CartService {

    private final CartRepository carts;
    private final UserRepository users;
    private final ProductRepository products;
    private final LineItemRepository lineItems;

    public CartService(CartRepository carts, UserRepository users,
                       ProductRepository products, LineItemRepository lineItems) {
        this.carts = carts;
        this.users = users;
        this.products = products;
        this.lineItems = lineItems;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return CartResponse.from(requireCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = products.findById(request.productId())
                .orElseThrow(() -> EntityNotFoundException.of("Product", request.productId()));
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is not available: " + product.getSku());
        }

        cart.getLineItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setQuantity(existing.getQuantity() + request.quantity());
                            existing.setUnitPrice(product.getPrice());
                        },
                        () -> cart.addLineItem(new LineItem(product, request.quantity(), product.getPrice())));

        cart.setUpdatedAt(Instant.now());
        return CartResponse.from(carts.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long lineItemId) {
        Cart cart = requireCart(userId);
        LineItem item = lineItems.findByIdAndCartId(lineItemId, cart.getId())
                .orElseThrow(() -> EntityNotFoundException.of("Cart item", lineItemId));
        cart.removeLineItem(item);
        cart.setUpdatedAt(Instant.now());
        return CartResponse.from(carts.save(cart));
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return carts.findByUserId(userId).orElseGet(() -> {
            User user = users.findById(userId)
                    .orElseThrow(() -> EntityNotFoundException.of("User", userId));
            return carts.save(new Cart(user));
        });
    }

    private Cart requireCart(Long userId) {
        return carts.findByUserId(userId)
                .orElseThrow(() -> EntityNotFoundException.of("Cart for user", userId));
    }
}
