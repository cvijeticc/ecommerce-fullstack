import { useState, useEffect } from 'react';
import api from '../services/api';

/**
 * Admin kontrolna tabla.
 *
 * Tabovi:
 * 1. Proizvodi — CRUD (dodavanje, izmena, brisanje)
 * 2. Kategorije — CRUD
 * 3. Porudžbine — pregled svih + promena statusa
 */

const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('products');

  // ─── Stanje za proizvode ──────────────────────────────────────────────────
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [productForm, setProductForm] = useState({
    name: '', description: '', price: '', stockQuantity: '', imageUrl: '', categoryId: ''
  });
  const [editingProductId, setEditingProductId] = useState(null);

  // ─── Stanje za kategorije ─────────────────────────────────────────────────
  const [categoryForm, setCategoryForm] = useState({ name: '', description: '' });
  const [editingCategoryId, setEditingCategoryId] = useState(null);

  // ─── Stanje za porudžbine ─────────────────────────────────────────────────
  const [orders, setOrders] = useState([]);

  const [message, setMessage] = useState('');

  // Učitavamo podatke pri otvaranju stranice
  useEffect(() => {
    fetchProducts();
    fetchCategories();
    fetchOrders();
  }, []);

  const fetchProducts = () => api.get('/products?size=100').then(r => setProducts(r.data.content));
  const fetchCategories = () => api.get('/categories').then(r => setCategories(r.data));
  const fetchOrders = () => api.get('/admin/orders').then(r => setOrders(r.data));

  const showMessage = (msg) => { setMessage(msg); setTimeout(() => setMessage(''), 3000); };

  // ─── Operacije sa proizvodima ─────────────────────────────────────────────

  const saveProduct = async (e) => {
    e.preventDefault();
    try {
      const data = { ...productForm, price: parseFloat(productForm.price), stockQuantity: parseInt(productForm.stockQuantity), categoryId: productForm.categoryId || null };
      if (editingProductId) {
        await api.put(`/products/${editingProductId}`, data);
        showMessage('Proizvod ažuriran ✓');
      } else {
        await api.post('/products', data);
        showMessage('Proizvod dodat ✓');
      }
      setProductForm({ name: '', description: '', price: '', stockQuantity: '', imageUrl: '', categoryId: '' });
      setEditingProductId(null);
      fetchProducts();
    } catch (err) {
      showMessage('Greška: ' + (err.response?.data?.message || 'nepoznata greška'));
    }
  };

  const editProduct = (p) => {
    setProductForm({ name: p.name, description: p.description || '', price: p.price, stockQuantity: p.stockQuantity, imageUrl: p.imageUrl || '', categoryId: p.categoryId || '' });
    setEditingProductId(p.id);
    setActiveTab('products');
    window.scrollTo(0, 0);
  };

  const deleteProduct = async (id) => {
    if (!window.confirm('Obriši proizvod?')) return;
    await api.delete(`/products/${id}`);
    showMessage('Proizvod obrisan ✓');
    fetchProducts();
  };

  // ─── Operacije sa kategorijama ────────────────────────────────────────────

  const saveCategory = async (e) => {
    e.preventDefault();
    try {
      if (editingCategoryId) {
        await api.put(`/categories/${editingCategoryId}`, categoryForm);
        showMessage('Kategorija ažurirana ✓');
      } else {
        await api.post('/categories', categoryForm);
        showMessage('Kategorija dodata ✓');
      }
      setCategoryForm({ name: '', description: '' });
      setEditingCategoryId(null);
      fetchCategories();
    } catch (err) {
      showMessage('Greška: ' + (err.response?.data?.message || 'nepoznata greška'));
    }
  };

  const deleteCategory = async (id) => {
    if (!window.confirm('Obriši kategoriju?')) return;
    await api.delete(`/categories/${id}`);
    showMessage('Kategorija obrisana ✓');
    fetchCategories();
  };

  // ─── Promena statusa porudžbine ───────────────────────────────────────────

  const updateOrderStatus = async (orderId, status) => {
    try {
      await api.put(`/admin/orders/${orderId}/status`, { status });
      showMessage('Status ažuriran ✓');
      fetchOrders();
    } catch (err) {
      showMessage('Greška pri ažuriranju statusa');
    }
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Admin Dashboard</h1>

      {message && <div style={styles.message}>{message}</div>}

      {/* Tabovi */}
      <div style={styles.tabs}>
        {['products', 'categories', 'orders'].map(tab => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            style={activeTab === tab ? styles.activeTab : styles.tab}>
            {tab === 'products' ? 'Proizvodi' : tab === 'categories' ? 'Kategorije' : 'Porudžbine'}
          </button>
        ))}
      </div>

      {/* ─── TAB: PROIZVODI ──────────────────────────────────────────────── */}
      {activeTab === 'products' && (
        <div>
          <h2>{editingProductId ? 'Izmeni proizvod' : 'Dodaj proizvod'}</h2>
          <form onSubmit={saveProduct} style={styles.form}>
            <div style={styles.formRow}>
              <input placeholder="Naziv *" value={productForm.name} onChange={e => setProductForm({...productForm, name: e.target.value})} style={styles.input} required />
              <input placeholder="Cena *" type="number" step="0.01" value={productForm.price} onChange={e => setProductForm({...productForm, price: e.target.value})} style={styles.input} required />
              <input placeholder="Količina *" type="number" value={productForm.stockQuantity} onChange={e => setProductForm({...productForm, stockQuantity: e.target.value})} style={styles.input} required />
            </div>
            <div style={styles.formRow}>
              <input placeholder="URL slike" value={productForm.imageUrl} onChange={e => setProductForm({...productForm, imageUrl: e.target.value})} style={styles.input} />
              <select value={productForm.categoryId} onChange={e => setProductForm({...productForm, categoryId: e.target.value})} style={styles.input}>
                <option value="">-- Kategorija --</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <textarea placeholder="Opis" value={productForm.description} onChange={e => setProductForm({...productForm, description: e.target.value})} style={styles.textarea} rows={2} />
            <div style={styles.formBtns}>
              <button type="submit" style={styles.saveBtn}>{editingProductId ? 'Sačuvaj izmene' : 'Dodaj proizvod'}</button>
              {editingProductId && <button type="button" onClick={() => { setEditingProductId(null); setProductForm({ name: '', description: '', price: '', stockQuantity: '', imageUrl: '', categoryId: '' }); }} style={styles.cancelBtn}>Otkaži</button>}
            </div>
          </form>

          <table style={styles.table}>
            <thead><tr><th style={styles.th}>ID</th><th style={styles.th}>Naziv</th><th style={styles.th}>Cena</th><th style={styles.th}>Stanje</th><th style={styles.th}>Kategorija</th><th style={styles.th}>Akcije</th></tr></thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td style={styles.td}>{p.id}</td>
                  <td style={styles.td}>{p.name}</td>
                  <td style={styles.td}>{p.price} RSD</td>
                  <td style={styles.td}>{p.stockQuantity}</td>
                  <td style={styles.td}>{p.categoryName || '-'}</td>
                  <td style={styles.td}>
                    <button onClick={() => editProduct(p)} style={styles.editBtn}>Izmeni</button>
                    <button onClick={() => deleteProduct(p.id)} style={styles.deleteBtn}>Obriši</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ─── TAB: KATEGORIJE ─────────────────────────────────────────────── */}
      {activeTab === 'categories' && (
        <div>
          <h2>{editingCategoryId ? 'Izmeni kategoriju' : 'Dodaj kategoriju'}</h2>
          <form onSubmit={saveCategory} style={styles.form}>
            <div style={styles.formRow}>
              <input placeholder="Naziv kategorije *" value={categoryForm.name} onChange={e => setCategoryForm({...categoryForm, name: e.target.value})} style={styles.input} required />
              <input placeholder="Opis" value={categoryForm.description} onChange={e => setCategoryForm({...categoryForm, description: e.target.value})} style={styles.input} />
            </div>
            <div style={styles.formBtns}>
              <button type="submit" style={styles.saveBtn}>{editingCategoryId ? 'Sačuvaj' : 'Dodaj'}</button>
              {editingCategoryId && <button type="button" onClick={() => { setEditingCategoryId(null); setCategoryForm({ name: '', description: '' }); }} style={styles.cancelBtn}>Otkaži</button>}
            </div>
          </form>

          <table style={styles.table}>
            <thead><tr><th style={styles.th}>ID</th><th style={styles.th}>Naziv</th><th style={styles.th}>Opis</th><th style={styles.th}>Akcije</th></tr></thead>
            <tbody>
              {categories.map(c => (
                <tr key={c.id}>
                  <td style={styles.td}>{c.id}</td>
                  <td style={styles.td}>{c.name}</td>
                  <td style={styles.td}>{c.description || '-'}</td>
                  <td style={styles.td}>
                    <button onClick={() => { setEditingCategoryId(c.id); setCategoryForm({ name: c.name, description: c.description || '' }); window.scrollTo(0,0); }} style={styles.editBtn}>Izmeni</button>
                    <button onClick={() => deleteCategory(c.id)} style={styles.deleteBtn}>Obriši</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ─── TAB: PORUDŽBINE ─────────────────────────────────────────────── */}
      {activeTab === 'orders' && (
        <div>
          <h2>Sve porudžbine ({orders.length})</h2>
          <table style={styles.table}>
            <thead><tr><th style={styles.th}>ID</th><th style={styles.th}>Korisnik</th><th style={styles.th}>Iznos</th><th style={styles.th}>Adresa</th><th style={styles.th}>Datum</th><th style={styles.th}>Status</th></tr></thead>
            <tbody>
              {orders.map(o => (
                <tr key={o.id}>
                  <td style={styles.td}>{o.id}</td>
                  <td style={styles.td}>{o.userEmail}</td>
                  <td style={styles.td}>{o.totalAmount?.toFixed(2)} RSD</td>
                  <td style={styles.td}>{o.shippingAddress}</td>
                  <td style={styles.td}>{new Date(o.createdAt).toLocaleDateString('sr-RS')}</td>
                  <td style={styles.td}>
                    {/* Dropdown za promenu statusa */}
                    <select value={o.status} onChange={e => updateOrderStatus(o.id, e.target.value)} style={styles.statusSelect}>
                      {ORDER_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { maxWidth: '1200px', margin: '0 auto', padding: '1rem' },
  title: { color: '#333', marginBottom: '1rem' },
  tabs: { display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '2px solid #eee', paddingBottom: '0' },
  tab: { padding: '0.6rem 1.2rem', background: 'none', border: 'none', cursor: 'pointer', color: '#666', fontSize: '1rem' },
  activeTab: { padding: '0.6rem 1.2rem', background: 'none', border: 'none', cursor: 'pointer', color: '#007bff', fontSize: '1rem', borderBottom: '2px solid #007bff', fontWeight: 'bold' },
  form: { background: 'white', padding: '1.5rem', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', marginBottom: '1.5rem' },
  formRow: { display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' },
  input: { flex: 1, minWidth: '150px', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '0.9rem' },
  textarea: { width: '100%', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '0.9rem', boxSizing: 'border-box', marginBottom: '1rem' },
  formBtns: { display: 'flex', gap: '0.5rem' },
  saveBtn: { padding: '0.6rem 1.5rem', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  cancelBtn: { padding: '0.6rem 1.5rem', background: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  table: { width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' },
  th: { padding: '0.75rem', background: '#f8f9fa', textAlign: 'left', color: '#555', fontSize: '0.85rem', borderBottom: '1px solid #eee' },
  td: { padding: '0.75rem', borderBottom: '1px solid #f5f5f5', fontSize: '0.9rem', color: '#333' },
  editBtn: { padding: '0.3rem 0.7rem', background: '#ffc107', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '0.3rem', fontSize: '0.8rem' },
  deleteBtn: { padding: '0.3rem 0.7rem', background: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem' },
  statusSelect: { padding: '0.3rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '0.85rem' },
  message: { background: '#d4edda', color: '#155724', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
};
