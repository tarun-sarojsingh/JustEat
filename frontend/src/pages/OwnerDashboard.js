import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../api/api';

const NEXT_STATUS = { PENDING: 'PREPARING', PREPARING: 'READY', READY: 'COMPLETED' };

export default function OwnerDashboard() {
  const [restaurant, setRestaurant] = useState(null);
  const [restaurantForm, setRestaurantForm] = useState({
    name: '', cuisineType: '', address: '', phone: '', description: '', isActive: false,
  });
  const [menu, setMenu] = useState([]);
  const [orders, setOrders] = useState([]);
  const [itemForm, setItemForm] = useState({ name: '', description: '', price: '', category: '' });
  const [loading, setLoading] = useState(true);

  const loadDashboard = async () => {
    setLoading(true);
    try {
      const mineRes = await api.get('/restaurant/mine');
      if (mineRes.status === 204 || !mineRes.data) {
        // Owner hasn't registered a restaurant yet — show the registration form.
        setRestaurant(null);
        return;
      }
      const myRestaurant = mineRes.data;
      const [menuRes, ordersRes] = await Promise.all([
        api.get(`/restaurants/${myRestaurant.id}/menu`),
        api.get('/orders/incoming'),
      ]);
      setRestaurant(myRestaurant);
      setMenu(menuRes.data);
      setOrders(ordersRes.data);
    } catch (err) {
      toast.error('Could not load your restaurant dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRegisterRestaurant = async (e) => {
    e.preventDefault();
    try {
      const { data } = await api.post('/restaurant', restaurantForm);
      setRestaurant(data);
      toast.success('Restaurant registered!');
      loadDashboard();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Could not register restaurant');
    }
  };

  const handleAddItem = async (e) => {
    e.preventDefault();
    try {
      await api.post('/menu-items', { ...itemForm, price: Number(itemForm.price), isAvailable: true });
      toast.success('Menu item added');
      setItemForm({ name: '', description: '', price: '', category: '' });
      const { data } = await api.get(`/restaurants/${restaurant.id}/menu`);
      setMenu(data);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Could not add menu item');
    }
  };

  const handleDeleteItem = async (itemId) => {
    try {
      await api.delete(`/menu-items/${itemId}`);
      setMenu((prev) => prev.filter((m) => m.id !== itemId));
      toast.success('Item removed from menu');
    } catch {
      toast.error('Could not remove item');
    }
  };

  const handleAdvanceStatus = async (order) => {
    const next = NEXT_STATUS[order.status];
    if (!next) return;
    try {
      await api.put(`/orders/${order.id}/status`, { status: next });
      setOrders((prev) => prev.map((o) => (o.id === order.id ? { ...o, status: next } : o)));
      toast.success(`Order #${order.id} moved to ${next}`);
    } catch {
      toast.error('Could not update order status');
    }
  };

  if (loading) return <p className="page">Loading dashboard…</p>;

  if (!restaurant) {
    return (
      <div className="page">
        <h1>Register your restaurant</h1>
        <form className="restaurant-form" onSubmit={handleRegisterRestaurant}>
          <label>
            Restaurant name
            <input
              value={restaurantForm.name}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, name: e.target.value })}
              required
            />
          </label>
          <label>
            Cuisine type
            <input
              value={restaurantForm.cuisineType}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, cuisineType: e.target.value })}
            />
          </label>
          <label>
            Address
            <input
              value={restaurantForm.address}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, address: e.target.value })}
            />
          </label>
          <label>
            Phone
            <input
              value={restaurantForm.phone}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, phone: e.target.value })}
            />
          </label>
          <label>
            Description
            <textarea
              value={restaurantForm.description}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, description: e.target.value })}
            />
          </label>
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={restaurantForm.isActive}
              onChange={(e) => setRestaurantForm({ ...restaurantForm, isActive: e.target.checked })}
            />
            Make visible to customers immediately
          </label>
          <button type="submit" className="primary-button">
            Register restaurant
          </button>
        </form>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Restaurant dashboard</h1>

      <section className="dashboard-section">
        <h2>Incoming orders</h2>
        {orders.length === 0 ? (
          <p className="empty-state">No active orders right now.</p>
        ) : (
          <div className="order-list">
            {orders.map((order) => (
              <div key={order.id} className="owner-order-card">
                <div>
                  <strong>Order #{order.id}</strong>
                  <p>{order.items?.length || 0} item(s) · £{Number(order.totalPrice).toFixed(2)}</p>
                </div>
                <span className={`status-pill status-${order.status.toLowerCase()}`}>{order.status}</span>
                {NEXT_STATUS[order.status] && (
                  <button className="primary-button" onClick={() => handleAdvanceStatus(order)}>
                    Move to {NEXT_STATUS[order.status]}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="dashboard-section">
        <h2>Add a menu item</h2>
        <form className="menu-item-form" onSubmit={handleAddItem}>
          <input
            placeholder="Name"
            value={itemForm.name}
            onChange={(e) => setItemForm({ ...itemForm, name: e.target.value })}
            required
          />
          <input
            placeholder="Category (e.g. Mains)"
            value={itemForm.category}
            onChange={(e) => setItemForm({ ...itemForm, category: e.target.value })}
          />
          <input
            type="number"
            step="0.01"
            placeholder="Price"
            value={itemForm.price}
            onChange={(e) => setItemForm({ ...itemForm, price: e.target.value })}
            required
          />
          <input
            placeholder="Description"
            value={itemForm.description}
            onChange={(e) => setItemForm({ ...itemForm, description: e.target.value })}
          />
          <button type="submit" className="primary-button">
            Add item
          </button>
        </form>
      </section>

      <section className="dashboard-section">
        <h2>Your menu</h2>
        {menu.length === 0 ? (
          <p className="empty-state">No menu items yet — add your first one above.</p>
        ) : (
          <div className="menu-items">
            {menu.map((item) => (
              <div key={item.id} className="menu-item-card">
                <div className="menu-item-info">
                  <strong>{item.name}</strong>
                  <p className="menu-item-price">£{Number(item.price).toFixed(2)}</p>
                </div>
                <button className="link-button danger" onClick={() => handleDeleteItem(item.id)}>
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
