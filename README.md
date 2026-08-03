# 🛒 EShop — Full Stack E-Commerce Application

A full-stack e-commerce web application built with **Spring Boot** (backend) and **React** (frontend).
Features JWT authentication, role-based access control, product catalog, shopping cart, and order management.

---

## ✨ Features

### Customer
- Browse and search products with pagination
- Register / Login with JWT authentication
- Add products to cart, update quantities, remove items
- Place orders with shipping address
- View personal order history and status

### Admin
- Full CRUD for **products** and **categories**
- View all orders from all customers
- Update order status (PENDING → CONFIRMED → SHIPPED → DELIVERED → CANCELLED)
- Protected routes — accessible only with ADMIN role

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.5, Spring Security 6 |
| Authentication | JWT (JJWT 0.12.6), BCrypt password hashing |
| Database | PostgreSQL, Spring Data JPA, Hibernate |
| Frontend | React 19, Vite 7, React Router v7, Axios |
| Testing | JUnit 5, Mockito 5, AssertJ, Spring Security Test |
| API Docs | springdoc-openapi (Swagger UI) |
| Build | Maven, npm, Docker Compose |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│          (Vite dev server — localhost:5173)              │
│                                                         │
│  Pages: Products, Cart, Orders, Login, Register         │
│  Admin: Dashboard (Products, Categories, Orders)        │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP + JWT (Authorization: Bearer)
                       │ Axios → /api/**
┌──────────────────────▼──────────────────────────────────┐
│                  Spring Boot Backend                     │
│                  (localhost:8080)                        │
│                                                         │
│  Controllers → Services → Repositories                  │
│  Spring Security Filter Chain + JWT Filter              │
│  GlobalExceptionHandler (@RestControllerAdvice)         │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA / Hibernate
┌──────────────────────▼──────────────────────────────────┐
│               PostgreSQL Database                        │
│               (localhost:5433)                           │
│                                                         │
│  Tables: users, products, categories,                   │
│          cart_items, orders, order_items                 │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 1. Database Setup

The quickest way — start PostgreSQL with Docker Compose from the repo root:

```bash
docker compose up -d db
```

This runs PostgreSQL 16 on port **5433** with the `ecommerce` database already created.

<details>
<summary>Alternative: use a local PostgreSQL install</summary>

```sql
CREATE DATABASE ecommerce;
```

Then point the app at it via environment variables (defaults are in
`Backend/src/main/resources/application.yaml`):

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
```
</details>

### 2. Run the Backend

```bash
cd Backend
./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run
```

Backend starts at: `http://localhost:8080`
Hibernate creates and updates tables automatically (`ddl-auto: update`) — existing data is kept.

Interactive API docs: **`http://localhost:8080/swagger-ui.html`**
(log in via `/api/auth/login`, then click **Authorize** and paste the token).

### 3. Run the Frontend

```bash
cd Frontend
cp .env.example .env        # sets VITE_API_URL
npm install
npm run dev
```

Frontend starts at: `http://localhost:5173`

### 4. Create an Admin User

Register a user via the app, then manually set the role in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Products
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/products` | Public | Get all (paginated, searchable) |
| GET | `/api/products/{id}` | Public | Get one |
| POST | `/api/products` | Admin | Create |
| PUT | `/api/products/{id}` | Admin | Update |
| DELETE | `/api/products/{id}` | Admin | Delete |

### Categories
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/categories` | Public | Get all |
| POST | `/api/categories` | Admin | Create |
| PUT | `/api/categories/{id}` | Admin | Update |
| DELETE | `/api/categories/{id}` | Admin | Delete (blocked if has products) |

### Cart
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/cart` | Authenticated | Get my cart |
| POST | `/api/cart` | Authenticated | Add item |
| PUT | `/api/cart/{id}` | Authenticated | Update quantity |
| DELETE | `/api/cart/{id}` | Authenticated | Remove item |
| DELETE | `/api/cart` | Authenticated | Clear entire cart |

### Orders
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/orders` | Authenticated | Place order from cart |
| GET | `/api/orders` | Authenticated | Get my orders |
| GET | `/api/orders/{id}` | Authenticated | Get one of my orders |
| GET | `/api/admin/orders` | Admin | Get all orders |
| PUT | `/api/admin/orders/{id}/status` | Admin | Update status |

> **Access column key.** *Public* = no token needed. *Authenticated* = any valid JWT;
> these endpoints are scoped to the calling user in the query itself
> (`findByIdAndUserId`), so one customer cannot read another's cart or orders.
> *Admin* = requires `ROLE_ADMIN`, enforced by `@PreAuthorize("hasRole('ADMIN')")`
> on the method, or by the `/api/admin/**` rule in `SecurityConfig`.

---

## 🧪 Running Tests

```bash
cd Backend
./mvnw test
```

**18 tests** covering:
- `JwtServiceTest` — token generation, email extraction, token validation
- `AuthServiceTest` — register (success + duplicate email), login (success + bad credentials)
- `ProductServiceTest` — getById, create, delete (success + not found)
- `ProductControllerSecurityTest` — role-based access: a `CUSTOMER` gets **403** on
  product writes, an `ADMIN` succeeds, and public `GET` endpoints stay open

Service tests use **Mockito**; the security test uses `@WebMvcTest` +
`spring-security-test`. Neither needs a database.

`EcommerceApplicationTests.contextLoads()` is the one test that *does* require a
running PostgreSQL — start it with `docker compose up -d db` first.

---

## 📁 Project Structure

```
Ecommerce full stack/
├── Backend/
│   └── src/
│       ├── main/java/com/andrija/ecommerce/
│       │   ├── config/          # SecurityConfig, CorsConfig
│       │   ├── controller/      # REST controllers
│       │   ├── dto/             # Request/Response DTOs (Java records)
│       │   ├── entity/          # JPA entities
│       │   ├── enums/           # Role, OrderStatus
│       │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│       │   ├── repository/      # Spring Data JPA repositories
│       │   ├── security/        # JwtService, JwtFilter, UserDetailsService
│       │   └── service/         # Business logic
│       └── test/                # JUnit 5 + Mockito unit tests
│
└── Frontend/
    └── src/
        ├── components/          # Navbar, ProtectedRoute
        ├── context/             # AuthContext (JWT state management)
        ├── pages/               # LoginPage, ProductsPage, CartPage, etc.
        └── services/            # Axios API client
```

---

## 🔐 Security

- Passwords hashed with **BCrypt** (one-way, salted, never stored as plain text)
- **JWT** tokens — stateless, server never stores sessions (`SessionCreationPolicy.STATELESS`)
- Role-based access on two levels: URL rules in `SecurityConfig` **and**
  `@PreAuthorize("hasRole('ADMIN')")` on individual controller methods
- Custom `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`
- Authorities are re-read from the database on every request, so a role change
  takes effect immediately without reissuing the token
- Ownership checks live in the query (`findByIdAndUserId`), so one user cannot
  read another's cart or orders even by guessing IDs
- Secrets (`JWT_SECRET`, DB password) are read from environment variables,
  with local-development defaults
- CORS configured for React dev server origins

### Known limitations

Deliberately out of scope for this project, and the first things to address next:
tokens cannot be revoked before expiry (no refresh token or denylist), the JWT is
stored in `localStorage` rather than an httpOnly cookie, stock decrement in
checkout has no optimistic lock (`@Version`) so concurrent orders could oversell,
`getAllOrders()` triggers N+1 queries and has no pagination, and there are no
Flyway migrations (schema is managed by `ddl-auto: update`).

---

## 📝 License

This project is for educational/portfolio purposes.
