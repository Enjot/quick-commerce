package com.enjot.quickcommerce.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(@NotBlank String deliveryAddress) {
}
