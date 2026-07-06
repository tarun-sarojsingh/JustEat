import React, { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/api';

const STAGES = ['PENDING', 'PREPARING', 'READY', 'COMPLETED'];

export default function OrderTracking() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const lastStatus = useRef(null);

  useEffect(() => {
    const fetchOrder = async () => {
      const { data } = await api.get(`/orders/${id}`);
      if (lastStatus.current && lastStatus.current !== data.status) {
        toast.info(`Order status updated: ${data.status}`);
      }
      lastStatus.current = data.status;
      setOrder(data);
    };

    fetchOrder();
    const interval = setInterval(fetchOrder, 30000); // poll every 30 seconds per US 2.4
    return () => clearInterval(interval);
  }, [id]);

  if (!order) return <p className="page">Loading order…</p>;

  const currentStageIndex = STAGES.indexOf(order.status);

  return (
    <div className="page">
      <h1>Order #{order.id}</h1>
      <p className="order-total">Total: £{Number(order.totalPrice).toFixed(2)}</p>

      <div className="status-tracker">
        {STAGES.map((stage, index) => (
          <div key={stage} className={`status-step ${index <= currentStageIndex ? 'complete' : ''}`}>
            <div className="status-dot" />
            <span>{stage.charAt(0) + stage.slice(1).toLowerCase()}</span>
          </div>
        ))}
      </div>

      <ul className="order-items-list">
        {order.items?.map((item) => (
          <li key={item.id}>
            {item.quantity} × {item.menuItem?.name} — £{(item.unitPrice * item.quantity).toFixed(2)}
          </li>
        ))}
      </ul>
    </div>
  );
}
