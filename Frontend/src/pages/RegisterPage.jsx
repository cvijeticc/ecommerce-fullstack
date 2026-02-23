import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

/**
 * Stranica za registraciju novog korisnika.
 *
 * Nakon uspešne registracije, server odmah vraća JWT token —
 * korisnik je automatski ulogovan, ne mora ponovo da se prijavi.
 */
export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/register', formData);
      login(response.data); // automatski uloguj nakon registracije
      navigate('/');
    } catch (err) {
      // 409 Conflict — email već postoji
      if (err.response?.status === 409) {
        setError('Korisnik sa tim emailom već postoji.');
      } else if (err.response?.data?.errors) {
        // 400 — validacijske greške (kratka lozinka, pogrešan email format...)
        const errors = Object.values(err.response.data.errors).join(', ');
        setError(errors);
      } else {
        setError('Došlo je do greške. Pokušaj ponovo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Registracija</h2>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={styles.row}>
            <div style={styles.field}>
              <label style={styles.label}>Ime</label>
              <input type="text" name="firstName" value={formData.firstName}
                onChange={handleChange} style={styles.input} placeholder="Andrija" required />
            </div>
            <div style={styles.field}>
              <label style={styles.label}>Prezime</label>
              <input type="text" name="lastName" value={formData.lastName}
                onChange={handleChange} style={styles.input} placeholder="Petrović" required />
            </div>
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Email</label>
            <input type="email" name="email" value={formData.email}
              onChange={handleChange} style={styles.input} placeholder="andrija@example.com" required />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Lozinka (min. 6 karaktera)</label>
            <input type="password" name="password" value={formData.password}
              onChange={handleChange} style={styles.input} placeholder="••••••" required />
          </div>

          <button type="submit" style={styles.button} disabled={loading}>
            {loading ? 'Učitavanje...' : 'Registruj se'}
          </button>
        </form>

        <p style={styles.link}>
          Već imaš nalog? <Link to="/login">Prijavi se</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' },
  card: { background: 'white', padding: '2rem', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', width: '100%', maxWidth: '450px' },
  title: { textAlign: 'center', marginBottom: '1.5rem', color: '#333' },
  row: { display: 'flex', gap: '1rem' },
  field: { marginBottom: '1rem', flex: 1 },
  label: { display: 'block', marginBottom: '0.3rem', fontWeight: '500', color: '#555' },
  input: { width: '100%', padding: '0.6rem', border: '1px solid #ddd', borderRadius: '4px', fontSize: '1rem', boxSizing: 'border-box' },
  button: { width: '100%', padding: '0.75rem', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1rem', cursor: 'pointer', marginTop: '0.5rem' },
  error: { background: '#ffe0e0', color: '#c00', padding: '0.75rem', borderRadius: '4px', marginBottom: '1rem' },
  link: { textAlign: 'center', marginTop: '1rem', color: '#666' },
};
