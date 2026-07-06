package com.justeat.repository;

import com.justeat.model.Order;
import com.justeat.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByRestaurantIdAndStatusNotOrderByCreatedAtDesc(Long restaurantId, OrderStatus status);
    List<Order> findByRestaurantId(Long restaurantId);
}
