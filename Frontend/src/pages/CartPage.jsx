import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

/**
 * Stranica sa korpom korisnika.
 *
 * Prikazuje sve stavke korpe, ukupan iznos i dugme za porudžbinu.
 * Server automatski zna čija je korpa — čita iz JWT tokena.
 */
export default function CartPage() {
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [shippingAddress, setShippingAddress] = useState('');
  const [ordering, setOrdering] = useState(false);
  const [message, setMessage] = useState('');

  // Učitavamo korpu kada se stranica otvori
  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const res = await api.get('/cart');
      setCartItems(res.data);
    } catch (err) {
      console.error('Greška pri učitavanju korpe', err);
    } finally {
      setLoading(false);
    }
  };

  // Ažuriranje količine stavke
  const updateQuantity = async (cartItemId, newQuantity) => {
    if (newQuantity < 1) return;
    try {
      await api.put(`/cart/${cartItemId}`, { quantity: newQuantity });
      fetchCart(); // osvežavamo korpu
    } catch (err) {
      console.error('Greška pri ažuriranju količine', err);
    }
  };

  // Brisanje stavke iz korpe
  const removeItem = async (cartItemId) => {
    try {
      await api.delete(`/cart/${cartItemId}`);
      fetchCart();
    } catch (err) {
      console.error('Greška pri brisanju stavke', err);
    }
  };

  // Kreiranje porudžbine
  const placeOrder = async () => {
    if (!shippingAddress.trim()) {
      setMessage('Unesite adresu dostave!');
      return;
    }
    setOrdering(true);
    try {
      await api.post('/orders', { shippingAddress });
      setMessage('Porudžbina uspešno kreirana! 🎉');
      setTimeout(() => navigate('/orders'), 1500);
    } catch (err) {
      if (err.response?.data?.message) {
        setMessage(err.response.data.message);
      } else {
        setMessage('Greška pri kreiranju porudžbine.');
      }
    } finally {
      setOrdering(false);
    }
  };

  // Računamo ukupan iznos na frontendu
  const total = cartItems.reduce((sum, item) => sum + (item.subtotal || 0), 0);

  if (loading) return <p style={styles.loading}>Učitavanje korpe...</p>;

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Moja korpa</h1>

      {message && (
        <div style={message.includes('greška') || message.includes('Unesite') ? styles.error : styles.success}>
          {message}
        </div>
      )}

      {cartItems.length === 0 ? (
        <div style={styles.empty}>
          <p>Korpa je prazna.</p>
          <button onClick={() => navigate('/products')} style={styles.shopBtn}>
            Idi na proizvode
          </button>
        </div>
      ) : (
        <div style={styles.layout}>
          {/* Lista stavki */}
          <div style={styles.items}>
            {cartItems.map(item => (
              <div key={item.id} style={styles.item}>
                <div style={styles.itemInfo}>
                  <h3 style={styles.itemName}>{item.productName}</h3>
                  <p style={styles.itemPrice}>{item.price} RSD / kom</p>
                </div>
                <div style={styles.itemControls}>
                  {/* Dugmad za promenu količine */}
                  <button onClick={() => updateQuantity(item.id, item.quantity - 1)} style={styles.qtyBtn}>−</button>
                  <span style={styles.qty}>{item.quantity}</span>
                  <button onClick={() => updateQuantity(item.id, item.quantity + 1)} style={styles.qtyBtn}>+</button>
                </div>
                <span style={styles.subtotal}>{item.subtotal?.toFixed(2)} RSD</span>
                <button onClick={() => removeItem(item.id)} style={styles.removeBtn}>✕</button>
              </div>
            ))}
          </div>

          {/* Rezime i porudžbina */}
          <div style={styles.summary}>
            <h2 style={styles.summaryTitle}>Rezime porudžbine</h2>
            <div style={styles.totalRow}>
              <span>Ukupno:</span>
              <strong>{total.toFixed(2)} RSD</strong>
            </div>

            <div style={styles.field}>
              <label style={styles.label}>Adresa dostave</label>
              <textarea
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                style={styles.textarea}
                placeholder="Unesite vašu adresu dostave..."
                rows={3}
              />
            </div>

            <button onClick={placeOrder} style={styles.orderBtn} disabled={ordering}>
              {ordering ? 'Obrada...' : 'Naruči'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '1rem' },
  title: { marginBottom: '1rem', color: '#333' },
  layout: { display: 'grid', gridTemplateColumns: '1fr 320px', gap: '2rem' },
  items: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  item: { display: 'flex', alignItems: 'center', gap: '1rem', background: 'white', padding: '1rem', borderRadius: '8px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' },
  itemInfo: { flex: 1 },
  itemName: { margin: 0, fontSize: '1rem', color: '#333' },
  itemPrice: { margin: '0.2rem 0 0', color: '#666', fontSize: '0.85rem' },
  itemControls: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
  qtyBtn: { width: '28px', height: '28px', border: '1px solid #ddd', background: 'white', borderRadius: '4px', cursor: 'pointer', fontSize: '1rem' },
  qty: { minWidth: '24px', textAlign: 'center', fontWeight: 'bold' },
  subtotal: { fontWeight: 'bold', color: '#007bff', minWidth: '100px', textAlign: 'right' },
  removeBtn: { background: 'none', border: 'none', color: '#dc3545', cursor: 'pointer', fontSize: '1.1rem' },
  summary: { background: 'white', padding: '1.5rem', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', height: 'fit-content' },
  summaryTitle: { margin: '0 0 1rem', color: '#333' },
  totalRow: { display: 'flex', justifyContent: 'space-between', fontSize: '1.2rem', marginBottom: '1.5rem', paddingBottom: '1rem', borderBottom: '1px solid #eee' },
  field: { marginBottom: '1rem' },
  label: { display: 'block', marginBottom: '0.3rem', fontWeight: '500', color: '#555' },
  textarea: { width: '100%', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '0.9rem', boxSizing: 'border-box', resize: 'vertical' },
  orderBtn: { width: '100%', padding: '0.8rem', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1rem', cursor: 'pointer' },
  loading: { textAlign: 'center', padding: '2rem', color: '#666' },
  empty: { textAlign: 'center', padding: '3rem' },
  shopBtn: { padding: '0.75rem 1.5rem', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '1rem' },
  success: { background: '#d4edda', color: '#155724', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
  error: { background: '#ffe0e0', color: '#c00', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
};
