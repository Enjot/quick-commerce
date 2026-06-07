package com.enjot.quickcommerce.web.dto;

import com.enjot.quickcommerce.domain.Order;
import com.enjot.quickcommerce.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        BigDecimal deliveryFee,
        String deliveryAddress,
        Instant createdAt,
        List<OrderItemResponse> items,
        List<StatusHistoryResponse> statusHistory) {

    public record OrderItemResponse(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {
    }

    public record StatusHistoryResponse(OrderStatus status, Instant changedAt, String changedBy) {
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getLineItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()))
                .toList();
        List<StatusHistoryResponse> history = order.getStatusHistory().stream()
                .map(entry -> new StatusHistoryResponse(entry.getStatus(), entry.getChangedAt(), entry.getChangedBy()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryFee(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                items,
                history);
    }
}
