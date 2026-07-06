import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/api';
import { useCart } from '../context/CartContext';

export default function Menu() {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menu, setMenu] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addItem } = useCart();
  const navigate = useNavigate();

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const [restaurantsRes, menuRes] = await Promise.all([
        api.get('/restaurants'),
        api.get(`/restaurants/${id}/menu`),
      ]);
      const found = restaurantsRes.data.find((r) => String(r.id) === id);
      setRestaurant(found || { id: Number(id), name: 'Restaurant' });
      setMenu(menuRes.data);
      setLoading(false);
    };
    load();
  }, [id]);

  const handleAdd = (item) => {
    addItem(restaurant, item);
    toast.success(`Added ${item.name} to cart`);
  };

  const grouped = menu.reduce((acc, item) => {
    const category = item.category || 'Menu';
    acc[category] = acc[category] || [];
    acc[category].push(item);
    return acc;
  }, {});

  if (loading) return <p className="page">Loading menu…</p>;

  return (
    <div className="page">
      <button className="link-button" onClick={() => navigate('/restaurants')}>
        ← Back to restaurants
      </button>
      <h1>{restaurant?.name}</h1>

      {menu.length === 0 && <p className="empty-state">This restaurant hasn't added any menu items yet.</p>}

      {Object.entries(grouped).map(([category, items]) => (
        <section key={category} className="menu-section">
          <h2>{category}</h2>
          <div className="menu-items">
            {items.map((item) => (
              <div key={item.id} className={`menu-item-card ${!item.available ? 'unavailable' : ''}`}>
                <div className="menu-item-info">
                  <div className="menu-item-title">
                    <strong>{item.name}</strong>
                    {item.todaysSpecial && <span className="badge special">Today's Special</span>}
                    {item.popular && <span className="badge popular">Popular</span>}
                  </div>
                  {item.description && <p className="menu-item-desc">{item.description}</p>}
                  <p className="menu-item-price">£{Number(item.price).toFixed(2)}</p>
                </div>
                <button
                  className="primary-button"
                  disabled={!item.available}
                  onClick={() => handleAdd(item)}
                >
                  {item.available ? 'Add to cart' : 'Unavailable'}
                </button>
              </div>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
