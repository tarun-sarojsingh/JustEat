package com.justeat.controller;

import com.justeat.dto.MenuItemRequest;
import com.justeat.model.MenuItem;
import com.justeat.model.User;
import com.justeat.security.CurrentUserProvider;
import com.justeat.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu-items")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final CurrentUserProvider currentUserProvider;

    public MenuItemController(MenuItemService menuItemService, CurrentUserProvider currentUserProvider) {
        this.menuItemService = menuItemService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<MenuItem> addItem(@Valid @RequestBody MenuItemRequest request, Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(menuItemService.addItem(owner, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request,
                                                Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        return ResponseEntity.ok(menuItemService.updateItem(owner, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, Authentication authentication) {
        User owner = currentUserProvider.resolve(authentication);
        menuItemService.softDeleteItem(owner, id);
        return ResponseEntity.noContent().build();
    }
}
