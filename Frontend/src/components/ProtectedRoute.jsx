import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute — štiti stranice od neautorizovanog pristupa.
 *
 * Upotreba u App.jsx:
 *   <Route path="/cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
 *   <Route path="/admin" element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>} />
 *
 * Logika:
 * - Nije ulogovan → preusmeriti na /login
 * - adminOnly=true, ali nije ADMIN → preusmeriti na /
 * - Sve OK → prikazati children komponentu
 */
export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, isAdmin } = useAuth();

  // Nije ulogovan — šaljemo na login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Treba ADMIN, ali korisnik je CUSTOMER
  if (adminOnly && !isAdmin()) {
    return <Navigate to="/" replace />;
  }

  // Sve prošlo — prikazujemo stranicu
  return children;
}
