import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Navigaciona traka — vidljiva na svim stranicama.
 *
 * Prikazuje različite linkove zavisno od toga ko je ulogovan:
 * - Nije ulogovan: Prijava, Registracija
 * - CUSTOMER: Korpa, Porudžbine, Odjavi se
 * - ADMIN: sve + Admin Dashboard
 */
export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <div style={styles.container}>
        {/* Logo / Naziv aplikacije */}
        <Link to="/" style={styles.brand}>🛒 EShop</Link>

        {/* Navigacioni linkovi */}
        <div style={styles.links}>
          <Link to="/products" style={styles.link}>Proizvodi</Link>

          {user ? (
            // Ulogovan korisnik
            <>
              <Link to="/cart" style={styles.link}>🛒 Korpa</Link>
              <Link to="/orders" style={styles.link}>Porudžbine</Link>
              {isAdmin() && (
                <Link to="/admin" style={styles.adminLink}>⚙ Admin</Link>
              )}
              {/* Prikazujemo email i dugme za odjavu */}
              <span style={styles.email}>{user.email}</span>
              <button onClick={handleLogout} style={styles.logoutBtn}>
                Odjavi se
              </button>
            </>
          ) : (
            // Nije ulogovan
            <>
              <Link to="/login" style={styles.link}>Prijava</Link>
              <Link to="/register" style={styles.registerBtn}>Registracija</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

const styles = {
  nav: { background: '#1a1a2e', padding: '0.75rem 0', boxShadow: '0 2px 8px rgba(0,0,0,0.3)' },
  container: { maxWidth: '1200px', margin: '0 auto', padding: '0 1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  brand: { color: 'white', textDecoration: 'none', fontSize: '1.3rem', fontWeight: 'bold' },
  links: { display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' },
  link: { color: '#ccc', textDecoration: 'none', fontSize: '0.95rem', transition: 'color 0.2s' },
  adminLink: { color: '#ffd700', textDecoration: 'none', fontSize: '0.95rem', fontWeight: 'bold' },
  registerBtn: { color: 'white', textDecoration: 'none', background: '#007bff', padding: '0.4rem 0.9rem', borderRadius: '4px', fontSize: '0.9rem' },
  email: { color: '#aaa', fontSize: '0.85rem' },
  logoutBtn: { background: 'none', border: '1px solid #666', color: '#ccc', padding: '0.3rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem' },
};
