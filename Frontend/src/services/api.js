import axios from 'axios';

/**
 * Centralna Axios instanca za sve API pozive.
 *
 * Zašto ne koristimo direktno axios.get/post?
 * Ovde definišemo baseURL jednom — ne moramo ga ponavljati u svakom pozivu.
 * Interceptor automatski dodaje JWT token na svaki zahtev.
 *
 * baseURL se čita iz environment varijable (fajl .env, vidi .env.example).
 * Vite ubacuje samo varijable koje počinju sa VITE_ i to u BUILD vremenu —
 * zato ovo nije tajna i ne sme se koristiti za lozinke ili API ključeve.
 * Bez ovoga aplikacija bi mogla da se builduje samo za localhost.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor — izvršava se PRE svakog zahteva.
 *
 * Tok: React komponenta poziva api.get('/cart')
 *   → interceptor čita token iz localStorage
 *   → dodaje header: Authorization: Bearer eyJhbG...
 *   → zahtev odlazi na server
 *
 * Ako nema tokena (korisnik nije ulogovan) — zahtev odlazi bez headera.
 * Server će vratiti 401 za zaštićene endpointe.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor — izvršava se nakon svakog odgovora.
 *
 * Ako server vrati 401 (token istekao ili nevažeći):
 * → brišemo token iz localStorage
 * → preusmeravamo korisnika na /login
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
