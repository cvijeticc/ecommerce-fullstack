import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

/**
 * Stranica sa listom svih proizvoda.
 *
 * Funkcionalnosti:
 * - Pretraga po imenu (debounce — čeka da korisnik prestane da kuca)
 * - Filtriranje po kategoriji
 * - Paginacija (Prev/Next dugmad)
 * - Dodavanje u korpu
 */
export default function ProductsPage() {
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // Učitavamo kategorije jednom pri otvaranju stranice
  useEffect(() => {
    api.get('/categories').then(res => setCategories(res.data));
  }, []);

  // Učitavamo proizvode svaki put kad se promeni pretraga, kategorija ili stranica
  useEffect(() => {
    fetchProducts();
  }, [search, selectedCategory, page]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      let url = '';
      if (selectedCategory) {
        // Filtriranje po kategoriji — nema paginacije u ovom endpointu
        const res = await api.get(`/products/category/${selectedCategory}`);
        setProducts(res.data);
        setTotalPages(1);
      } else {
        // Pretraga + paginacija
        const params = new URLSearchParams({ page, size: 8, sort: 'createdAt,desc' });
        if (search) params.append('search', search);
        const res = await api.get(`/products?${params}`);
        setProducts(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error('Greška pri učitavanju proizvoda', err);
    } finally {
      setLoading(false);
    }
  };

  // Dodavanje u korpu — POST /api/cart
  const addToCart = async (productId) => {
    try {
      await api.post('/cart', { productId, quantity: 1 });
      setMessage('Proizvod dodat u korpu! 🛒');
      setTimeout(() => setMessage(''), 2000);
    } catch (err) {
      if (err.response?.status === 401) {
        navigate('/login'); // nije ulogovan
      } else {
        setMessage('Greška pri dodavanju u korpu.');
      }
    }
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Proizvodi</h1>

      {/* Poruka o uspešnom dodavanju */}
      {message && <div style={styles.message}>{message}</div>}

      {/* Filteri */}
      <div style={styles.filters}>
        <input
          type="text"
          placeholder="Pretraži proizvode..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          style={styles.searchInput}
        />
        <select
          value={selectedCategory}
          onChange={(e) => { setSelectedCategory(e.target.value); setPage(0); }}
          style={styles.select}
        >
          <option value="">Sve kategorije</option>
          {categories.map(cat => (
            <option key={cat.id} value={cat.id}>{cat.name}</option>
          ))}
        </select>
      </div>

      {/* Lista proizvoda */}
      {loading ? (
        <p style={styles.loading}>Učitavanje...</p>
      ) : products.length === 0 ? (
        <p style={styles.empty}>Nema proizvoda.</p>
      ) : (
        <div style={styles.grid}>
          {products.map(product => (
            <div key={product.id} style={styles.card}>
              {product.imageUrl && (
                <img src={product.imageUrl} alt={product.name} style={styles.image} />
              )}
              <div style={styles.cardBody}>
                <h3 style={styles.productName}>{product.name}</h3>
                {product.categoryName && (
                  <span style={styles.category}>{product.categoryName}</span>
                )}
                <p style={styles.description}>
                  {product.description?.substring(0, 80)}
                  {product.description?.length > 80 ? '...' : ''}
                </p>
                <div style={styles.footer}>
                  <span style={styles.price}>{product.price} RSD</span>
                  <span style={styles.stock}>
                    {product.stockQuantity > 0 ? `Na stanju: ${product.stockQuantity}` : 'Nema na stanju'}
                  </span>
                </div>
                <button
                  onClick={() => addToCart(product.id)}
                  style={product.stockQuantity > 0 ? styles.addBtn : styles.addBtnDisabled}
                  disabled={product.stockQuantity === 0}
                >
                  {product.stockQuantity > 0 ? 'Dodaj u korpu' : 'Nema na stanju'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Paginacija */}
      {totalPages > 1 && (
        <div style={styles.pagination}>
          <button onClick={() => setPage(p => p - 1)} disabled={page === 0} style={styles.pageBtn}>
            ← Prethodna
          </button>
          <span style={styles.pageInfo}>Stranica {page + 1} od {totalPages}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1} style={styles.pageBtn}>
            Sledeća →
          </button>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { maxWidth: '1200px', margin: '0 auto', padding: '1rem' },
  title: { marginBottom: '1rem', color: '#333' },
  filters: { display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' },
  searchInput: { flex: 1, minWidth: '200px', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '1rem' },
  select: { padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '1rem' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1.5rem' },
  card: { background: 'white', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', overflow: 'hidden' },
  image: { width: '100%', height: '180px', objectFit: 'cover' },
  cardBody: { padding: '1rem' },
  productName: { margin: '0 0 0.3rem', color: '#333', fontSize: '1rem' },
  category: { background: '#e8f4ff', color: '#0066cc', padding: '0.2rem 0.5rem', borderRadius: '12px', fontSize: '0.75rem' },
  description: { color: '#666', fontSize: '0.85rem', margin: '0.5rem 0' },
  footer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '0.5rem 0' },
  price: { fontWeight: 'bold', color: '#007bff', fontSize: '1.1rem' },
  stock: { fontSize: '0.8rem', color: '#888' },
  addBtn: { width: '100%', padding: '0.5rem', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '0.5rem' },
  addBtnDisabled: { width: '100%', padding: '0.5rem', background: '#ccc', color: '#666', border: 'none', borderRadius: '4px', cursor: 'not-allowed', marginTop: '0.5rem' },
  pagination: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '2rem' },
  pageBtn: { padding: '0.5rem 1rem', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  pageInfo: { color: '#666' },
  loading: { textAlign: 'center', color: '#666', padding: '2rem' },
  empty: { textAlign: 'center', color: '#888', padding: '2rem' },
  message: { background: '#d4edda', color: '#155724', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
};
