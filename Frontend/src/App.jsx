import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

// Stranice
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import AdminDashboard from './pages/AdminDashboard';

/**
 * Korenska komponenta aplikacije.
 *
 * Struktura:
 * BrowserRouter     — omogućava React Router navigaciju
 *   AuthProvider    — pruža user state svim komponentama
 *     Navbar        — uvek vidljiv na vrhu
 *     Routes        — definiše koje stranice postoje
 *
 * ProtectedRoute — čuva stranice koje zahtevaju login.
 * adminOnly prop — čuva Admin stranicu od CUSTOMER korisnika.
 */
function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main style={{ minHeight: '100vh', background: '#f5f6fa', padding: '1rem 0' }}>
          <Routes>
            {/* Javne stranice */}
            <Route path="/" element={<Navigate to="/products" replace />} />
            <Route path="/products" element={<ProductsPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Zaštićene stranice — mora biti ulogovan */}
            <Route path="/cart" element={
              <ProtectedRoute><CartPage /></ProtectedRoute>
            } />
            <Route path="/orders" element={
              <ProtectedRoute><OrdersPage /></ProtectedRoute>
            } />

            {/* Admin stranica — mora biti ADMIN role */}
            <Route path="/admin" element={
              <ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>
            } />

            {/* Nepostojeće stranice — vraćamo na početnu */}
            <Route path="*" element={<Navigate to="/products" replace />} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
