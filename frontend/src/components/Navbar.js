import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { items } = useCart();
  const navigate = useNavigate();
  const cartCount = items.reduce((sum, i) => sum + i.quantity, 0);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        🍔 JustEat
      </Link>
      <nav className="nav-links">
        {user && user.role === 'CUSTOMER' && (
          <>
            <Link to="/restaurants">Restaurants</Link>
            <Link to="/orders/history">My orders</Link>
            <Link to="/cart" className="cart-link">
              Cart{cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
            </Link>
          </>
        )}
        {user && user.role === 'RESTAURANT_OWNER' && <Link to="/owner">Dashboard</Link>}
        {user ? (
          <button className="link-button" onClick={handleLogout}>
            Log out ({user.username})
          </button>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register" className="cta-link">
              Sign up
            </Link>
          </>
        )}
      </nav>
    </header>
  );
}
