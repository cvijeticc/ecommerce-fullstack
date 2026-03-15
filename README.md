# EShop — Full Stack E-Commerce Application

A full-stack e-commerce web application built with **Spring Boot** (backend) and **React** (frontend).
Features JWT authentication, role-based access control, product catalog, shopping cart, and order management.

---

## Features

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

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.5, Spring Security 6 |
| Authentication | JWT (JJWT 0.12.6), BCrypt password hashing |
| Database | PostgreSQL, Spring Data JPA, Hibernate |
| Frontend | React 18, Vite, React Router v6, Axios |
| Testing | JUnit 5, Mockito 5, AssertJ |
| Build | Maven, npm |

---

## Architecture

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

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE ecommerce;
```

Configure connection in `Backend/src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ecommerce
    username: postgres
    password: your_password
```

### 2. Run the Backend

```bash
cd Backend
./mvnw spring-boot:run
```

Backend starts at: `http://localhost:8080`
On first run, Hibernate automatically creates all tables (`ddl-auto: create`).

### 3. Run the Frontend

```bash
cd Frontend
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

## API Endpoints

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
| GET | `/api/cart` | Customer | Get my cart |
| POST | `/api/cart` | Customer | Add item |
| PUT | `/api/cart/{id}` | Customer | Update quantity |
| DELETE | `/api/cart/{id}` | Customer | Remove item |

### Orders
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/orders` | Customer | Place order from cart |
| GET | `/api/orders` | Customer | Get my orders |
| GET | `/api/admin/orders` | Admin | Get all orders |
| PUT | `/api/admin/orders/{id}/status` | Admin | Update status |

---

## Running Tests

```bash
cd Backend
./mvnw test
```

**13 unit tests** covering:
- `JwtServiceTest` — token generation, email extraction, token validation
- `AuthServiceTest` — register (success + duplicate email), login (success + bad credentials)
- `ProductServiceTest` — getById, create, delete (success + not found)

Tests use **Mockito** to mock all dependencies — no database required.

---

## Project Structure

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

## Security

- Passwords hashed with **BCrypt** (one-way, never stored as plain text)
- **JWT** tokens — stateless, server never stores sessions
- Role-based access: `CUSTOMER` vs `ADMIN`
- Spring Security filter chain intercepts every request
- CORS configured for React dev server origins

---

## License

This project is for educational/portfolio purposes.
