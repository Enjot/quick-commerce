package com.enjot.quickcommerce.web.dto;

import com.enjot.quickcommerce.domain.Cart;
import com.enjot.quickcommerce.domain.LineItem;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal total) {

    public record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {
    }

    /**
     * Builds the view using the <em>current</em> catalogue price of each product,
     * so the cart total always reflects live pricing until checkout.
     */
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getLineItems().stream()
                .map(CartResponse::toItem)
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }

    private static CartItemResponse toItem(LineItem item) {
        BigDecimal livePrice = item.getProduct().getPrice();
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                livePrice,
                item.getQuantity(),
                livePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
