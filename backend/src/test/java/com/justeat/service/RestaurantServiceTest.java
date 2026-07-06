package com.justeat.service;

import com.justeat.exception.ResourceNotFoundException;
import com.justeat.model.Restaurant;
import com.justeat.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    // Test 4: Get all active restaurants -> returns non-empty list of active restaurants
    @Test
    void search_withNoQuery_returnsActiveRestaurantsSortedByRating() {
        Restaurant r1 = new Restaurant();
        r1.setName("Pizza Place");
        r1.setActive(true);
        Restaurant r2 = new Restaurant();
        r2.setName("Sushi Spot");
        r2.setActive(true);

        when(restaurantRepository.findByIsActiveTrueOrderByRatingDesc()).thenReturn(List.of(r1, r2));

        List<Restaurant> results = restaurantService.search(null);

        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
    }

    // Test 5: Get restaurant by non-existent ID -> ResourceNotFoundException thrown
    @Test
    void getById_withNonExistentId_throwsResourceNotFoundException() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> restaurantService.getById(999L));
    }
}
