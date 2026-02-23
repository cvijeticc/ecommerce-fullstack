import { createContext, useContext, useState } from 'react';

/**
 * AuthContext — globalni state za autentifikaciju.
 *
 * Problem koji rešavamo:
 * Navbar treba da zna da li je korisnik ulogovan.
 * CartPage treba da zna ko je ulogovan.
 * AdminDashboard treba da proveri da li je ADMIN.
 *
 * Bez Context-a: morali bismo da props-ujemo user kroz svaku komponentu.
 * Sa Context-om: svaka komponenta može direktno da pročita user.
 *
 * localStorage — čuva token između refresh-ova stranice.
 * Kada korisnik zatvori i otvori browser, ostaje ulogovan.
 */

// Kreiramo Context objekat
const AuthContext = createContext(null);

/**
 * AuthProvider — "omotava" celu aplikaciju i pruža user state svima.
 * Postavljamo ga u main.jsx oko <App />.
 */
export function AuthProvider({ children }) {
  // Inicijalno čitamo user iz localStorage (ako je već ulogovan)
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  /**
   * login() — poziva se nakon uspešnog POST /api/auth/login ili /register
   * Čuva token i user podatke u localStorage i state-u.
   *
   * @param {Object} userData — { token, email, role } iz LoginResponse DTO-a
   */
  const login = (userData) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  /**
   * logout() — briše sve podatke i preusmerava na login stranicu.
   */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  /**
   * isAdmin() — proverava da li je ulogovan korisnik ADMIN.
   * Koristi se za prikaz Admin linka u Navbar-u i zaštitu AdminDashboard stranice.
   */
  const isAdmin = () => user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * useAuth() — custom hook za lakše korišćenje AuthContext-a.
 *
 * Umesto: const { user, login } = useContext(AuthContext);
 * Pišemo: const { user, login } = useAuth();
 */
export function useAuth() {
  return useContext(AuthContext);
}
