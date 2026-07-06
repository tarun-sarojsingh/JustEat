import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';

export default function Restaurants() {
  const [restaurants, setRestaurants] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchRestaurants = async (searchQuery) => {
    setLoading(true);
    try {
      const { data } = await api.get('/restaurants', { params: { query: searchQuery || undefined } });
      setRestaurants(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRestaurants('');
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchRestaurants(query);
  };

  return (
    <div className="page">
      <h1>Restaurants near you</h1>
      <form className="search-bar" onSubmit={handleSearch}>
        <input
          placeholder="Search by name or cuisine…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="submit" className="primary-button">
          Search
        </button>
      </form>

      {loading ? (
        <p>Loading restaurants…</p>
      ) : restaurants.length === 0 ? (
        <p className="empty-state">No restaurants found. Try a different search.</p>
      ) : (
        <div className="restaurant-grid">
          {restaurants.map((r) => (
            <Link to={`/restaurants/${r.id}`} key={r.id} className="restaurant-card">
              <h3>{r.name}</h3>
              <p className="cuisine">{r.cuisineType || 'Various cuisine'}</p>
              <p className="rating">⭐ {r.rating?.toFixed ? r.rating.toFixed(1) : r.rating}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
