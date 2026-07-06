package com.justeat.repository;

import com.justeat.model.Order;
import com.justeat.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Load a single order with all associations eagerly to avoid lazy-init and ByteBuddy proxy issues. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.restaurant r " +
           "LEFT JOIN FETCH r.owner " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.menuItem " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    /** Customer order history — all associations eagerly loaded. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.restaurant " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.menuItem " +
           "WHERE o.customer.id = :customerId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    /** Restaurant owner incoming orders — excludes completed. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.restaurant " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.menuItem " +
           "WHERE o.restaurant.id = :restaurantId " +
           "AND o.status <> :status " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByRestaurantIdWithDetailsExcludeStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") OrderStatus status);

    List<Order> findByRestaurantId(Long restaurantId);
}
