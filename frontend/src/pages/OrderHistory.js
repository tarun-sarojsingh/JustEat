import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/api';

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      const { data } = await api.get('/orders/history');
      setOrders(data);
      setLoading(false);
    };
    load();
  }, []);

  if (loading) return <p className="page">Loading your orders…</p>;

  return (
    <div className="page">
      <h1>Order history</h1>
      {orders.length === 0 ? (
        <p className="empty-state">You haven't placed any orders yet.</p>
      ) : (
        <div className="order-history-list">
          {orders.map((order) => (
            <Link to={`/orders/${order.id}`} key={order.id} className="order-history-row">
              <div>
                <strong>Order #{order.id}</strong>
                <p className="order-date">{new Date(order.createdAt).toLocaleString()}</p>
              </div>
              <span className={`status-pill status-${order.status.toLowerCase()}`}>{order.status}</span>
              <span className="order-total">£{Number(order.totalPrice).toFixed(2)}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
