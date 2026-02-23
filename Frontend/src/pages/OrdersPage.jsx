import { useState, useEffect } from 'react';
import api from '../services/api';

/**
 * Stranica sa istorijom porudžbina korisnika.
 * Prikazuje sve porudžbine sa statusom i stavkama.
 */

// Mapa statusa na srpski + boja
const STATUS_INFO = {
  PENDING:   { label: 'Na čekanju',  color: '#ffc107', bg: '#fff8e1' },
  CONFIRMED: { label: 'Potvrđena',   color: '#17a2b8', bg: '#e3f7fb' },
  SHIPPED:   { label: 'Poslata',     color: '#007bff', bg: '#e8f4ff' },
  DELIVERED: { label: 'Dostavljena', color: '#28a745', bg: '#e8f5e9' },
  CANCELLED: { label: 'Otkazana',    color: '#dc3545', bg: '#ffe8e8' },
};

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedOrder, setExpandedOrder] = useState(null); // koji order je razvijen

  useEffect(() => {
    api.get('/orders')
      .then(res => setOrders(res.data))
      .catch(err => console.error('Greška pri učitavanju porudžbina', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p style={styles.loading}>Učitavanje porudžbina...</p>;

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Moje porudžbine</h1>

      {orders.length === 0 ? (
        <p style={styles.empty}>Još nemaš porudžbina.</p>
      ) : (
        <div style={styles.list}>
          {orders.map(order => {
            const statusInfo = STATUS_INFO[order.status] || { label: order.status, color: '#666', bg: '#f5f5f5' };
            const isExpanded = expandedOrder === order.id;

            return (
              <div key={order.id} style={styles.card}>
                {/* Header porudžbine */}
                <div style={styles.cardHeader} onClick={() => setExpandedOrder(isExpanded ? null : order.id)}>
                  <div>
                    <span style={styles.orderId}>Porudžbina #{order.id}</span>
                    <span style={styles.orderDate}>
                      {new Date(order.createdAt).toLocaleDateString('sr-RS')}
                    </span>
                  </div>
                  <div style={styles.headerRight}>
                    {/* Badge sa statusom */}
                    <span style={{ ...styles.statusBadge, color: statusInfo.color, background: statusInfo.bg }}>
                      {statusInfo.label}
                    </span>
                    <strong style={styles.total}>{order.totalAmount?.toFixed(2)} RSD</strong>
                    <span style={styles.expand}>{isExpanded ? '▲' : '▼'}</span>
                  </div>
                </div>

                {/* Detalji porudžbine (prikazuju se kada kliknemo) */}
                {isExpanded && (
                  <div style={styles.cardBody}>
                    <p style={styles.address}>📍 {order.shippingAddress}</p>
                    <table style={styles.table}>
                      <thead>
                        <tr>
                          <th style={styles.th}>Proizvod</th>
                          <th style={styles.th}>Kol.</th>
                          <th style={styles.th}>Cena</th>
                          <th style={styles.th}>Ukupno</th>
                        </tr>
                      </thead>
                      <tbody>
                        {order.items?.map(item => (
                          <tr key={item.id}>
                            <td style={styles.td}>{item.productName}</td>
                            <td style={styles.td}>{item.quantity}</td>
                            <td style={styles.td}>{item.priceAtPurchase} RSD</td>
                            <td style={styles.td}>{item.subtotal?.toFixed(2)} RSD</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { maxWidth: '900px', margin: '0 auto', padding: '1rem' },
  title: { marginBottom: '1rem', color: '#333' },
  list: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  card: { background: 'white', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', overflow: 'hidden' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.5rem', cursor: 'pointer', userSelect: 'none' },
  orderId: { fontWeight: 'bold', color: '#333', marginRight: '1rem' },
  orderDate: { color: '#888', fontSize: '0.85rem' },
  headerRight: { display: 'flex', alignItems: 'center', gap: '1rem' },
  statusBadge: { padding: '0.25rem 0.75rem', borderRadius: '12px', fontSize: '0.8rem', fontWeight: '600' },
  total: { color: '#007bff' },
  expand: { color: '#888' },
  cardBody: { padding: '1rem 1.5rem', borderTop: '1px solid #f0f0f0' },
  address: { color: '#555', marginBottom: '1rem' },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { textAlign: 'left', padding: '0.5rem', background: '#f8f9fa', color: '#555', fontSize: '0.85rem', borderBottom: '1px solid #eee' },
  td: { padding: '0.5rem', borderBottom: '1px solid #f5f5f5', fontSize: '0.9rem', color: '#333' },
  loading: { textAlign: 'center', padding: '2rem', color: '#666' },
  empty: { textAlign: 'center', padding: '3rem', color: '#888' },
};
