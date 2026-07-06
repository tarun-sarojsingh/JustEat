package com.justeat.dto;

import com.justeat.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long restaurantId;
    private String restaurantName;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.status = order.getStatus().name();
        r.totalPrice = order.getTotalPrice();
        r.createdAt = order.getCreatedAt();
        r.updatedAt = order.getUpdatedAt();
        if (order.getRestaurant() != null) {
            r.restaurantId = order.getRestaurant().getId();
            r.restaurantName = order.getRestaurant().getName();
        }
        r.items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        return r;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public List<OrderItemResponse> getItems() { return items; }
}
