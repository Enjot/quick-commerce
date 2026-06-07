package com.enjot.quickcommerce.web.dto;

import com.enjot.quickcommerce.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {
}
