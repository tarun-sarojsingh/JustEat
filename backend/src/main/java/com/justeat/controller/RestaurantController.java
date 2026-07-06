package com.justeat.controller;

import com.justeat.dto.RestaurantRequest;
import com.justeat.model.MenuItem;
import com.justeat.model.Restaurant;
import com.justeat.model.User;
import com.justeat.security.CurrentUserProvider;
import com.justeat.service.MenuItemService;
import com.justeat.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final CurrentUserProvider currentUserProvider;

    public RestaurantController(RestaurantService restaurantService, MenuItemService menuItemService,
                                 CurrentUserProvider currentUserProvider) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.currentUserProvider = currentUserProvider;
    }

    // ---- Customer-facing (US 2.1, 2.2) ----

    @GetMapping("/api/restaurants")
    public ResponseEntity<List<Restaurant>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(restaurantService.search(query));
    }

    @GetMapping("/api/restaurants/{id}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuForRestaurant(id));
    }

    // ---- Restaurant owner (US 3.1) ----

    @GetMapping("/api/restaurant/mine")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Restaurant> getMyRestaurant(Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        Restaurant restaurant = restaurantService.findByOwnerOrNull(owner);
        if (restaurant == null) {
            return ResponseEntity.noContent().build(); // 204 = not registered yet
        }
        return ResponseEntity.ok(restaurant);
    }

    @PostMapping("/api/restaurant")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Restaurant> registerRestaurant(@Valid @RequestBody RestaurantRequest request,
                                                           Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(restaurantService.register(owner, request));
    }

    @PutMapping("/api/restaurant/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id,
                                                         @Valid @RequestBody RestaurantRequest request,
                                                         Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(restaurantService.update(owner, id, request));
    }
}
