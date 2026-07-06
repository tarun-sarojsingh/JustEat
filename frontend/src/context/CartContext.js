import React, { createContext, useContext, useState } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [restaurantId, setRestaurantId] = useState(null);
  const [restaurantName, setRestaurantName] = useState(null);
  const [items, setItems] = useState([]); // [{ menuItemId, name, price, quantity }]

  const addItem = (restaurant, menuItem) => {
    // Cart supports items from one restaurant at a time.
    if (restaurantId && restaurantId !== restaurant.id) {
      const confirmSwitch = window.confirm(
        `Your cart has items from ${restaurantName}. Start a new cart for ${restaurant.name}?`
      );
      if (!confirmSwitch) return;
      setItems([]);
    }
    setRestaurantId(restaurant.id);
    setRestaurantName(restaurant.name);

    setItems((prev) => {
      const existing = prev.find((i) => i.menuItemId === menuItem.id);
      if (existing) {
        return prev.map((i) =>
          i.menuItemId === menuItem.id ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [...prev, { menuItemId: menuItem.id, name: menuItem.name, price: menuItem.price, quantity: 1 }];
    });
  };

  const updateQuantity = (menuItemId, quantity) => {
    if (quantity <= 0) {
      setItems((prev) => prev.filter((i) => i.menuItemId !== menuItemId));
      return;
    }
    setItems((prev) => prev.map((i) => (i.menuItemId === menuItemId ? { ...i, quantity } : i)));
  };

  const clearCart = () => {
    setItems([]);
    setRestaurantId(null);
    setRestaurantName(null);
  };

  const loadCart = (restaurant, cartItems) => {
    setRestaurantId(restaurant.id);
    setRestaurantName(restaurant.name);
    setItems(cartItems);
  };

  const subtotal = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  return (
    <CartContext.Provider
      value={{ restaurantId, restaurantName, items, addItem, updateQuantity, clearCart, loadCart, subtotal }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
