package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.security.AppUserDetails;
import com.enjot.quickcommerce.service.CartService;
import com.enjot.quickcommerce.web.dto.AddCartItemRequest;
import com.enjot.quickcommerce.web.dto.CartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "The authenticated user's shopping cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "View the current user's cart with a live total")
    public CartResponse getCart(@AuthenticationPrincipal AppUserDetails user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to the cart (merges quantity if already present)")
    public CartResponse addItem(@AuthenticationPrincipal AppUserDetails user,
                                @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(user.getId(), request);
    }

    @DeleteMapping("/items/{lineItemId}")
    @Operation(summary = "Remove a line item from the cart")
    public CartResponse removeItem(@AuthenticationPrincipal AppUserDetails user,
                                   @PathVariable Long lineItemId) {
        return cartService.removeItem(user.getId(), lineItemId);
    }
}
