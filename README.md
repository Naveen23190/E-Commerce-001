# E-Commerce Application

A full-stack starter project built with Java 17, Spring Boot, Spring Security, Hibernate/JPA, PostgreSQL, and React (Vite).

## Features
- Public product browsing and product image display
- Product image upload stored **inside PostgreSQL** as `bytea` (not on the filesystem)
- Customer registration and HTTP Basic authentication
- Role-based access: `ROLE_CUSTOMER` and `ROLE_ADMIN`
- Cart management and order placement
- Admin product create/update/delete
- PostgreSQL + frontend + backend via Docker Compose

> This is a learning/portfolio starter, not a production-ready commerce platform. Before production, add payment integration, email verification, password reset, rate limiting, audit logging, inventory locking, and stronger session/token management.

## Requirements
- Java 17+
- Maven 3.9+ (or use `./mvnw` after adding a Maven Wrapper)
- Node.js 20+
- Docker Desktop (optional, easiest way to start PostgreSQL)

## 1. Start PostgreSQL
From the project root:
```bash
docker compose up -d postgres
```
Database defaults are in `docker-compose.yml` and `backend/src/main/resources/application.properties`.

## 2. Start backend
```bash
cd backend
mvn spring-boot:run
```
Backend: `http://localhost:8080`

A default admin is created on startup:
- Username: `admin`
- Password: `Admin@12345`

Change this password before using the application outside local development.

## 3. Start frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend: `http://localhost:5173`

## Image storage design
`Product.imageData` is a `byte[]` mapped to PostgreSQL `bytea`; `Product.imageContentType` stores the MIME type. Admins upload multipart image files using `POST /api/admin/products` (field names: `name`, `description`, `price`, `stock`, `image`). The public image endpoint is `GET /api/products/{id}/image`.

## Main API
- `POST /api/auth/register` — register customer
- `GET /api/products` — list products
- `GET /api/products/{id}` — product details
- `GET /api/products/{id}/image` — image bytes from PostgreSQL
- `POST /api/admin/products` — create product + image (ADMIN)
- `PUT /api/admin/products/{id}` — update product (ADMIN)
- `DELETE /api/admin/products/{id}` — delete product (ADMIN)
- `GET /api/cart` — current user's cart
- `POST /api/cart/items` — add item (`productId`, `quantity`)
- `PUT /api/cart/items/{productId}` — set quantity
- `DELETE /api/cart/items/{productId}` — remove item
- `POST /api/orders` — place order from cart
- `GET /api/orders` — current user's orders

For protected API calls, use HTTP Basic auth. Example:
```bash
curl -u admin:Admin@12345 http://localhost:8080/api/cart
```

## Notes
- Hibernate creates/updates tables using `ddl-auto=update` for convenience. Use migrations (Flyway/Liquibase) in a serious deployment.
- CORS is configured for the Vite dev server at `localhost:5173`.
- No real payment processing is implemented.
