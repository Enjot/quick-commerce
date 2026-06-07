package com.enjot.quickcommerce.web;

import com.enjot.quickcommerce.security.AppUserDetails;
import com.enjot.quickcommerce.service.OrderService;
import com.enjot.quickcommerce.web.dto.CheckoutRequest;
import com.enjot.quickcommerce.web.dto.OrderResponse;
import com.enjot.quickcommerce.web.dto.OrderStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Checkout and order lifecycle")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check out the current cart into a new order (USER)")
    public OrderResponse checkout(@AuthenticationPrincipal AppUserDetails user,
                                  @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(user.getId(), request);
    }

    @GetMapping
    @Operation(summary = "List orders — own for USER, all for ADMIN")
    public List<OrderResponse> list(@AuthenticationPrincipal AppUserDetails user) {
        return orderService.list(user.getId(), isAdmin(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order (own for USER, any for ADMIN)")
    public OrderResponse get(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id) {
        return orderService.get(id, user.getId(), isAdmin(user));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition an order to a new status (ADMIN)")
    public OrderResponse updateStatus(@AuthenticationPrincipal AppUserDetails user,
                                      @PathVariable Long id,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.transitionStatus(id, request.status(), user.getUsername());
    }

    private boolean isAdmin(AppUserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
