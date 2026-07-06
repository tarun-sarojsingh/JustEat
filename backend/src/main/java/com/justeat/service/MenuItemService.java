package com.justeat.service;

import com.justeat.dto.MenuItemRequest;
import com.justeat.exception.InvalidOrderException;
import com.justeat.exception.ResourceNotFoundException;
import com.justeat.model.MenuItem;
import com.justeat.model.Restaurant;
import com.justeat.model.User;
import com.justeat.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantService restaurantService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantService = restaurantService;
    }

    /** US 2.2 — menu items shown to customers, excluding soft-deleted items. */
    public List<MenuItem> getMenuForRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsDeletedFalse(restaurantId);
    }

    /** US 3.2 — add a menu item to the owner's own restaurant. */
    public MenuItem addItem(User owner, MenuItemRequest request) {
        Restaurant restaurant = restaurantService.getByOwner(owner);
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        applyRequest(item, request);
        return menuItemRepository.save(item);
    }

    public MenuItem updateItem(User owner, Long itemId, MenuItemRequest request) {
        MenuItem item = getOwnedItem(owner, itemId);
        applyRequest(item, request);
        return menuItemRepository.save(item);
    }

    /** US 3.2 — soft delete so historical order_items are preserved. */
    public void softDeleteItem(User owner, Long itemId) {
        MenuItem item = getOwnedItem(owner, itemId);
        item.setDeleted(true);
        menuItemRepository.save(item);
    }

    private MenuItem getOwnedItem(User owner, Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id " + itemId));
        if (!item.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new InvalidOrderException("You do not own the restaurant for this menu item");
        }
        return item;
    }

    private void applyRequest(MenuItem item, MenuItemRequest request) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        if (request.getIsTodaysSpecial() != null) item.setTodaysSpecial(request.getIsTodaysSpecial());
        if (request.getIsDealOfDay() != null) item.setDealOfDay(request.getIsDealOfDay());
        if (request.getIsAvailable() != null) item.setAvailable(request.getIsAvailable());
    }
}
