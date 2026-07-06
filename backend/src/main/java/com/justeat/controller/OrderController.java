package com.justeat.controller;

import com.justeat.dto.OrderRequest;
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
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        User customer = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(orderService.placeOrder(customer, request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Order>> history(Authentication authentication) {
        User customer = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(orderService.getHistoryForCustomer(customer.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    // ---- Restaurant owner (US 3.5) ----

    @GetMapping("/incoming")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<Order>> incoming(Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        Long restaurantId = restaurantService.getByOwner(owner).getId();
        return ResponseEntity.ok(orderService.getIncomingForRestaurant(restaurantId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request,
                                               Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(orderService.updateStatus(owner, id, newStatus));
    }
}
