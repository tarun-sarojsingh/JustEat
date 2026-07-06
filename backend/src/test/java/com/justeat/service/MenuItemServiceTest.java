package com.justeat.service;

import com.justeat.dto.MenuItemRequest;
import com.justeat.model.MenuItem;
import com.justeat.model.Restaurant;
import com.justeat.model.Role;
import com.justeat.model.User;
import com.justeat.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantService restaurantService;

    @InjectMocks
    private MenuItemService menuItemService;

    private User owner;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        owner = new User("owner1", "owner@example.com", "hash", Role.RESTAURANT_OWNER);
        owner.setId(10L);
        restaurant = new Restaurant();
        restaurant.setId(5L);
        restaurant.setOwner(owner);
    }

    // Test 6: Add valid menu item -> item saved and returned with generated ID
    @Test
    void addItem_withValidRequest_savesAndReturnsItemWithId() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Margherita Pizza");
        request.setPrice(new BigDecimal("9.99"));
        request.setCategory("Mains");

        when(restaurantService.getByOwner(owner)).thenReturn(restaurant);
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        MenuItem result = menuItemService.addItem(owner, request);

        assertNotNull(result.getId());
        assertEquals("Margherita Pizza", result.getName());
    }

    // Test 7: Delete menu item (soft delete) -> is_deleted set to true, item not in GET results
    @Test
    void softDeleteItem_marksItemAsDeleted() {
        MenuItem item = new MenuItem();
        item.setId(50L);
        item.setRestaurant(restaurant);
        item.setDeleted(false);

        when(menuItemRepository.findById(50L)).thenReturn(Optional.of(item));
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        when(menuItemRepository.save(captor.capture())).thenReturn(item);

        menuItemService.softDeleteItem(owner, 50L);

        assertTrue(captor.getValue().isDeleted());
    }
}
