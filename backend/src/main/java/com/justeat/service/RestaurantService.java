package com.justeat.service;

import com.justeat.dto.RestaurantRequest;
import com.justeat.exception.InvalidOrderException;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.model.Restaurant;
import com.justeat.model.User;
import com.justeat.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /** US 2.1 — empty search returns all active restaurants sorted by rating. */
    public List<Restaurant> search(String query) {
        if (query == null || query.isBlank()) {
            return restaurantRepository.findByIsActiveTrueOrderByRatingDesc();
        }
        return restaurantRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndCuisineTypeContainingIgnoreCase(query, query);
    }

    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + id));
    }

    /** US 3.1 — an owner may only register one restaurant. */
    public Restaurant register(User owner, RestaurantRequest request) {
        if (restaurantRepository.existsByOwner(owner)) {
            throw new InvalidOrderException("This account has already registered a restaurant");
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setOwner(owner);
        applyRequest(restaurant, request);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant update(User owner, Long restaurantId, RestaurantRequest request) {
        Restaurant restaurant = getById(restaurantId);
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new InvalidOrderException("You do not own this restaurant");
        }
        applyRequest(restaurant, request);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant getByOwner(User owner) {
        return restaurantRepository.findByOwner(owner)
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for this account"));
    }

    /** Returns null (rather than throwing) so callers like GET /api/restaurant/mine
     *  can distinguish "not registered yet" from a real error. */
    public Restaurant findByOwnerOrNull(User owner) {
        return restaurantRepository.findByOwner(owner).orElse(null);
    }

    private void applyRequest(Restaurant restaurant, RestaurantRequest request) {
        restaurant.setName(request.getName());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            restaurant.setActive(request.getIsActive());
        }
    }
}
