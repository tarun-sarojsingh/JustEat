package com.justeat.service;

import com.justeat.dto.OrderItemRequest;
import com.justeat.dto.OrderRequest;
import com.justeat.exception.InvalidOrderException;
import com.justeat.exception.InvalidStatusTransitionException;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.model.*;
import com.justeat.repository.MenuItemRepository;
import com.justeat.repository.OrderRepository;
import com.justeat.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository,
                         RestaurantRepository restaurantRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /** US 2.3 — place an order; validates restaurant consistency and item availability. */
    @Transactional
    public Order placeOrder(User customer, OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Cannot place an order with an empty cart");
        }

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Menu item not found with id " + itemRequest.getMenuItemId()));

            // Integrity: item must belong to the requested restaurant
            if (!menuItem.getRestaurant().getId().equals(request.getRestaurantId())) {
                throw new InvalidOrderException(
                        "Menu item '" + menuItem.getName() + "' does not belong to the selected restaurant");
            }
            if (!menuItem.isAvailable()) {
                throw new InvalidOrderException("Menu item '" + menuItem.getName() + "' is not available");
            }
            if (menuItem.isDeleted()) {
                throw new InvalidOrderException("Menu item '" + menuItem.getName() + "' is no longer on the menu");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(menuItem.getPrice());
            order.getItems().add(orderItem);

            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            menuItem.setOrderCount(menuItem.getOrderCount() + 1);
        }

        order.setTotalPrice(total);
        return orderRepository.save(order);
    }

    /**
     * US 2.4 — load a single order for the owning customer (IDOR-safe).
     * Uses fetch-join so all associations are initialized before the transaction closes.
     */
    @Transactional(readOnly = true)
    public Order getByIdForCustomer(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
        if (!order.getCustomer().getId().equals(customerId)) {
            // Return 404 rather than 403 to avoid leaking existence of the order
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }
        return order;
    }

    /** Internal helper — loads order with all associations (used by updateStatus). */
    @Transactional(readOnly = true)
    public Order getById(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    /** US 2.5 — customer's own order history, most recent first. */
    @Transactional(readOnly = true)
    public List<Order> getHistoryForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdWithDetails(customerId);
    }

    /** US 3.5 — owner's active (not yet completed) incoming orders. */
    @Transactional(readOnly = true)
    public List<Order> getIncomingForRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdWithDetailsExcludeStatus(restaurantId, OrderStatus.COMPLETED);
    }

    /** US 3.5 — status may only move forward: PENDING → PREPARING → READY → COMPLETED. */
    @Transactional
    public Order updateStatus(User owner, Long orderId, OrderStatus newStatus) {
        Order order = getById(orderId);

        if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new InvalidOrderException("You do not own the restaurant for this order");
        }

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot move order from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}
