import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

/**
 * Stranica za prijavljivanje korisnika.
 *
 * Tok:
 * 1. Korisnik unosi email i lozinku
 * 2. Klikne "Prijavi se" → šaljemo POST /api/auth/login
 * 3. Server vraća { token, email, role }
 * 4. Čuvamo u AuthContext (login funkcija) i localStorage
 * 5. Preusmeravamo na početnu stranicu
 */
export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  // State za form polja
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Ažurira state kada korisnik kuca u input polja
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); // sprečava reload stranice
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', formData);
      login(response.data); // čuvamo token u Context + localStorage
      navigate('/');        // idemo na početnu stranicu
    } catch (err) {
      // Server vraća 401 za pogrešne kredencijale
      setError('Pogrešan email ili lozinka.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Prijava</h2>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              style={styles.input}
              placeholder="andrija@example.com"
              required
            />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Lozinka</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              style={styles.input}
              placeholder="••••••"
              required
            />
          </div>

          <button type="submit" style={styles.button} disabled={loading}>
            {loading ? 'Učitavanje...' : 'Prijavi se'}
          </button>
        </form>

        <p style={styles.link}>
          Nemaš nalog? <Link to="/register">Registruj se</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' },
  card: { background: 'white', padding: '2rem', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', width: '100%', maxWidth: '400px' },
  title: { textAlign: 'center', marginBottom: '1.5rem', color: '#333' },
  field: { marginBottom: '1rem' },
  label: { display: 'block', marginBottom: '0.3rem', fontWeight: '500', color: '#555' },
  input: { width: '100%', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '1rem', boxSizing: 'border-box' },
  button: { width: '100%', padding: '0.75rem', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1rem', cursor: 'pointer', marginTop: '0.5rem' },
  error: { background: '#ffe0e0', color: '#c00', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
  link: { textAlign: 'center', marginTop: '1rem', color: '#666' },
};
