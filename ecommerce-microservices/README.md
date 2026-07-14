# E-Commerce Backend with Microservices Architecture

## Services

| Service | Port | Responsibility |
|---|---:|---|
| Discovery Server | 8761 | Eureka service registry |
| API Gateway | 8080 | Single public endpoint and JWT validation |
| Auth Service | 8081 | Registration, login, BCrypt and JWT |
| Product Service | 8082 | Product CRUD and stock reservation |
| Order Service | 8083 | Order creation and order history |

Each business service owns a separate MySQL database:

- `ecommerce_auth`
- `ecommerce_product`
- `ecommerce_order`

## Architecture

```text
Client / Postman
       |
       v
API Gateway :8080
  |       |       |
  v       v       v
Auth     Product   Order
:8081    :8082     :8083
  |        |          |
Auth DB  Product DB  Order DB

Order Service --OpenFeign--> Product Service
All services -----------> Eureka :8761
```

## Requirements

- Java 21 or newer
- Maven 3.9+
- MySQL 8, or Docker Desktop
- Ports 8080, 8081, 8082, 8083, 8761 and 3306 available

## 1. Start MySQL

Using Docker:

```bash
docker compose up -d
```

For a locally installed MySQL, the development configuration expects:

```text
username: root
password: root
```

To use different credentials, set `DB_USERNAME` and `DB_PASSWORD`.

## 2. Build everything

Open Command Prompt in the folder containing the parent `pom.xml`:

```bash
mvn clean package -DskipTests
```

## 3. Start services in order

Open five Command Prompt windows in the root project folder:

```bash
mvn -pl discovery-server spring-boot:run
```

```bash
mvn -pl auth-service spring-boot:run
```

```bash
mvn -pl product-service spring-boot:run
```

```bash
mvn -pl order-service spring-boot:run
```

```bash
mvn -pl api-gateway spring-boot:run
```

Eureka dashboard:

```text
http://localhost:8761
```

All client requests should go through:

```text
http://localhost:8080
```

## 4. Test the APIs

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Vikaasni",
  "email": "vikaasni@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "vikaasni@example.com",
  "password": "password123"
}
```

Copy the returned JWT token.

### Give one account the ADMIN role

Registration creates a `CUSTOMER`. Run this once in MySQL:

```sql
USE ecommerce_auth;
UPDATE users
SET role = 'ADMIN'
WHERE email = 'vikaasni@example.com';
```

Log in again so the new JWT contains `ADMIN`.

### Create a product

```http
POST /api/products
Authorization: Bearer YOUR_ADMIN_TOKEN
Content-Type: application/json

{
  "name": "Wireless Mouse",
  "description": "Bluetooth rechargeable mouse",
  "price": 799.00,
  "stock": 25
}
```

### List products

```http
GET /api/products
```

### Place an order

```http
POST /api/orders
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

### View the current user's orders

```http
GET /api/orders/my
Authorization: Bearer YOUR_TOKEN
```

## Concepts demonstrated

- Independent databases for independent services
- API Gateway as one public entry point
- JWT authentication and BCrypt password hashing
- Eureka service registration and discovery
- OpenFeign inter-service communication
- Resilience4j circuit-breaker fallback
- Product stock validation and reservation
- User-specific order history
- Validation and consistent JSON errors

## Engineering limitations of this MVP

This is a complete learning-project baseline, but production evolution should include:

1. Saga and transactional-outbox patterns to restore stock when a later step fails.
2. RabbitMQ or Kafka for order events.
3. Payment and Notification services.
4. Refresh tokens and secure external secret storage.
5. Flyway database migrations instead of `ddl-auto: update`.
6. Distributed tracing and centralized logging.
7. Unit, integration and contract tests.
