package com.enjot.quickcommerce.web.dto;

import com.enjot.quickcommerce.domain.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create/update payload for a catalogue product. The {@code type} discriminator
 * selects which optional subtype fields apply.
 */
public record ProductRequest(
        @NotNull ProductType type,
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @NotNull Long categoryId,
        @PositiveOrZero int stockQuantity,
        Boolean active,
        // PERISHABLE
        LocalDate expiryDate,
        Integer shelfLifeDays,
        // AGE_RESTRICTED
        Integer minimumAge) {
}
