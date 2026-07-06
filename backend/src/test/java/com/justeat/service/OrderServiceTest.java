package com.justeat.service;

import com.justeat.dto.OrderItemRequest;
import com.justeat.dto.OrderRequest;
import com.justeat.exception.InvalidOrderException;
import com.justeat.exception.InvalidStatusTransitionException;
import com.justeat.model.*;
import com.justeat.repository.MenuItemRepository;
import com.justeat.repository.OrderRepository;
import com.justeat.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private User owner;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        customer = new User("cust1", "cust1@example.com", "hash", Role.CUSTOMER);
        customer.setId(1L);

        owner = new User("owner1", "owner1@example.com", "hash", Role.RESTAURANT_OWNER);
        owner.setId(2L);

        restaurant = new Restaurant();
        restaurant.setId(5L);
        restaurant.setOwner(owner);

        menuItem = new MenuItem();
        menuItem.setId(20L);
        menuItem.setRestaurant(restaurant);
        menuItem.setName("Burger");
        menuItem.setPrice(new BigDecimal("5.00"));
    }

    // Test 8: Place order with valid cart -> Order persisted with status PENDING
    @Test
    void placeOrder_withValidCart_persistsOrderWithPendingStatus() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuItemId(20L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setRestaurantId(5L);
        request.setItems(List.of(itemRequest));

        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(20L)).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(customer, request);

        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("10.00"), result.getTotalPrice());
    }

    // Test 9: Place order with empty cart -> InvalidOrderException thrown
    @Test
    void placeOrder_withEmptyCart_throwsInvalidOrderException() {
        OrderRequest request = new OrderRequest();
        request.setRestaurantId(5L);
        request.setItems(List.of());

        assertThrows(InvalidOrderException.class, () -> orderService.placeOrder(customer, request));
    }

    // Test 10: Update status from PENDING to PREPARING -> status updated and saved
    @Test
    void updateStatus_fromPendingToPreparing_updatesStatus() {
        Order order = new Order();
        order.setId(30L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithDetails(30L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(owner, 30L, OrderStatus.PREPARING);

        assertEquals(OrderStatus.PREPARING, result.getStatus());
    }

    // Test 11: Move status backwards (READY -> PREPARING) -> InvalidStatusTransitionException thrown
    @Test
    void updateStatus_movingBackwards_throwsInvalidStatusTransitionException() {
        Order order = new Order();
        order.setId(31L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.READY);

        when(orderRepository.findByIdWithDetails(31L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.updateStatus(owner, 31L, OrderStatus.PREPARING));
    }
}
