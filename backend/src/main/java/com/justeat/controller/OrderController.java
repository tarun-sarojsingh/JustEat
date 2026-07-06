package com.justeat.controller;

import com.justeat.dto.OrderRequest;
import com.justeat.dto.OrderResponse;
import com.justeat.dto.OrderStatusUpdateRequest;
import com.justeat.model.Order;
import com.justeat.model.OrderStatus;
import com.justeat.model.User;
import com.justeat.security.CurrentUserProvider;
import com.justeat.service.OrderService;
import com.justeat.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final CurrentUserProvider currentUserProvider;

    public OrderController(OrderService orderService, RestaurantService restaurantService,
                            CurrentUserProvider currentUserProvider) {
        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.currentUserProvider = currentUserProvider;
    }

    // ---- Customer (US 2.3, 2.4, 2.5) ----

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request,
                                                     Authentication authentication) {
        User customer = currentUserProvider.resolve(authentication);
        Order order = orderService.placeOrder(customer, request);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> history(Authentication authentication) {
        User customer = currentUserProvider.resolve(authentication);
        List<OrderResponse> result = orderService.getHistoryForCustomer(customer.getId())
                .stream().map(OrderResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** IDOR fix: resolves the current customer and verifies they own the order. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, Authentication authentication) {
        User customer = currentUserProvider.resolve(authentication);
        Order order = orderService.getByIdForCustomer(id, customer.getId());
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // ---- Restaurant owner (US 3.5) ----

    @GetMapping("/incoming")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<OrderResponse>> incoming(Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        Long restaurantId = restaurantService.getByOwner(owner).getId();
        List<OrderResponse> result = orderService.getIncomingForRestaurant(restaurantId)
                .stream().map(OrderResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody OrderStatusUpdateRequest request,
                                                       Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        Order order = orderService.updateStatus(owner, id, newStatus);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
