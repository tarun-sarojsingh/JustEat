import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/api';
import { useCart } from '../context/CartContext';

export default function Cart() {
  const { restaurantId, restaurantName, items, updateQuantity, clearCart, subtotal } = useCart();
  const [placing, setPlacing] = useState(false);
  const navigate = useNavigate();

  const handlePlaceOrder = async () => {
    setPlacing(true);
    try {
      const { data } = await api.post('/orders', {
        restaurantId,
        items: items.map((i) => ({ menuItemId: i.menuItemId, quantity: i.quantity })),
      });
      clearCart();
      toast.success('Order placed!');
      navigate(`/orders/${data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Could not place order');
    } finally {
      setPlacing(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="page">
        <h1>Your cart</h1>
        <p className="empty-state">Your cart is empty. Go find something delicious!</p>
        <button className="primary-button" onClick={() => navigate('/restaurants')}>
          Browse restaurants
        </button>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Your cart</h1>
      <p className="cart-restaurant">Ordering from <strong>{restaurantName}</strong></p>

      <div className="cart-items">
        {items.map((item) => (
          <div key={item.menuItemId} className="cart-item-row">
            <span className="cart-item-name">{item.name}</span>
            <div className="quantity-control">
              <button onClick={() => updateQuantity(item.menuItemId, item.quantity - 1)}>−</button>
              <span>{item.quantity}</span>
              <button onClick={() => updateQuantity(item.menuItemId, item.quantity + 1)}>+</button>
            </div>
            <span className="cart-item-price">£{(item.price * item.quantity).toFixed(2)}</span>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <span>Subtotal</span>
        <strong>£{subtotal.toFixed(2)}</strong>
      </div>

      <button className="primary-button full-width" onClick={handlePlaceOrder} disabled={placing}>
        {placing ? 'Placing order…' : 'Place order'}
      </button>
    </div>
  );
}
