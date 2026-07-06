package com.justeat.repository;

import com.justeat.model.Restaurant;
import com.justeat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByIsActiveTrueOrderByRatingDesc();
    List<Restaurant> findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndCuisineTypeContainingIgnoreCase(
            String name, String cuisineType);
    Optional<Restaurant> findByOwner(User owner);
    boolean existsByOwner(User owner);
}
