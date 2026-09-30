# Banking API

REST API for digital banking client and account management. Allows registering clients, opening accounts for them, and closing accounts when clients return them. Secured with JWT-based authentication and role-based access control.

## Tech Stack

- **Java 23**
- **Spring Boot 4.1.1** (Web, Data JPA, Validation, Security)
- **Hibernate 7.4.5**
- **PostgreSQL 18**
- **Spring Security** with **JWT** authentication
- **Lombok**
- **springdoc-openapi 3.1.1** (Swagger UI)
- **Maven**

## Features

- CRUD for clients (Client)
- CRUD for accounts (Account)
- Assign an account to a client (`assign`)
- Close (release) an account (`release`)
- List free accounts (`/api/accounts/free`)
- Account number uniqueness check
- JWT-based authentication and authorization
- Role-based access control (`USER`, `ADMIN`)
- BCrypt password hashing
- Automatic ID generation
- Input validation via Jakarta Bean Validation
- Centralized error handling with proper HTTP status codes (401, 403, 404, 409)
- One-to-Many relationship between Client and Account
- When a client is deleted, their accounts are automatically released (`ON DELETE SET NULL`)
- **Money stored as `BigDecimal`** (never `double`/`float`) — precise decimal arithmetic
- **`AccountType` enum** stored as `VARCHAR` (not ordinal) — safe for refactoring
- **Balance cannot be modified via PUT** — only through dedicated operations (future)

## Requirements

- JDK 23+
- PostgreSQL 14+
- Maven 3.9+

## Getting Started

### 1. Create the database

```sql
CREATE DATABASE bank_project;

CREATE TABLE clients (
    client_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    birth_year INT NOT NULL CHECK (birth_year BETWEEN 1900 AND EXTRACT(YEAR FROM CURRENT_DATE)),
    CONSTRAINT uq_clients_name_birth_year UNIQUE (first_name, last_name, birth_year)
);

CREATE TABLE accounts (
    account_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id      BIGINT NULL REFERENCES clients(client_id) ON DELETE SET NULL,
    account_number VARCHAR(23) NOT NULL UNIQUE,
    account_type   VARCHAR(20) NOT NULL CHECK (account_type IN ('DEBIT', 'CREDIT', 'SAVINGS')),
    balance        NUMERIC(19, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE users (
    user_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);
```

### 2. Set environment variables

The database password and JWT secret are provided via environment variables:

- **IntelliJ IDEA:** `Run → Edit Configurations → Environment variables → DB_PASSWORD=your_password;JWT_SECRET=your_secret`
- **Linux/macOS:** `export DB_PASSWORD=your_password && export JWT_SECRET=your_secret`
- **Windows (PowerShell):** `$env:DB_PASSWORD="your_password"; $env:JWT_SECRET="your_secret"`

Generate a JWT secret (at least 32 bytes):

```bash
openssl rand -base64 32
```

### 3. Configure `application.properties`

```properties
spring.application.name=banking-api

spring.datasource.url=jdbc:postgresql://localhost:5432/bank_project
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`.

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

To authorize in Swagger UI:
1. Call `POST /api/auth/login` and copy the `token` from the response.
2. Click **Authorize** (top right).
3. Paste **only the token** (without the `Bearer ` prefix — Swagger adds it automatically).
4. Click **Authorize** → **Close**.

## Security

The API is secured with **Spring Security** and **JWT** (JSON Web Tokens).

### Authentication flow

1. `POST /api/auth/register` — create a new user (default role: `USER`).
2. `POST /api/auth/login` — authenticate and receive a JWT.
3. Include the token in subsequent requests:

```
Authorization: Bearer <token>
```

### Roles

- `USER` — can read and create clients and accounts
- `ADMIN` — full access, including deletion

`DELETE /api/**` requires the `ADMIN` role.

### Creating an ADMIN

Public registration creates `USER` accounts only. To create an `ADMIN`, insert directly into the database:

```sql
INSERT INTO users (username, password_hash, role)
VALUES ('admin', '<bcrypt_hash>', 'ADMIN');
```

You can reuse the `password_hash` from an existing user created through `/register`.

## Endpoints

### Authentication

| Method | URL                   | Access |
|--------|-----------------------|--------|
| POST   | `/api/auth/register`  | Public |
| POST   | `/api/auth/login`     | Public |

### Clients

| Method | URL                 | Access        |
|--------|---------------------|---------------|
| GET    | `/api/clients`      | Authenticated |
| GET    | `/api/clients/{id}` | Authenticated |
| POST   | `/api/clients`      | Authenticated |
| PUT    | `/api/clients/{id}` | Authenticated |
| DELETE | `/api/clients/{id}` | ADMIN only    |

### Accounts

| Method | URL                         | Access        |
|--------|-----------------------------|---------------|
| GET    | `/api/accounts`             | Authenticated |
| GET    | `/api/accounts/free`        | Authenticated |
| GET    | `/api/accounts/{id}`        | Authenticated |
| POST   | `/api/accounts`             | Authenticated |
| PUT    | `/api/accounts/{id}`        | Authenticated |
| DELETE | `/api/accounts/{id}`        | ADMIN only    |
| POST   | `/api/accounts/{id}/assign` | Authenticated |
| POST   | `/api/accounts/{id}/release`| Authenticated |

## Request Examples

### Register a user

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "secret123"
}
```

**201 Created**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john",
  "role": "USER"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "secret123"
}
```

**200 OK**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john",
  "role": "ROLE_USER"
}
```

### Create a client

```http
POST /api/clients
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "birthYear": 1985
}
```

**201 Created**

```json
{
  "clientId": 1,
  "firstName": "John",
  "lastName": "Smith",
  "birthYear": 1985,
  "fullName": "John Smith",
  "accounts": []
}
```

### Create an account

```http
POST /api/accounts
Authorization: Bearer <token>
Content-Type: application/json

{
  "accountNumber": "40817810123456789012",
  "accountType": "DEBIT",
  "balance": 1000.00
}
```

**201 Created**

```json
{
  "accountId": 1,
  "accountNumber": "40817810123456789012",
  "accountType": "DEBIT",
  "balance": 1000.00,
  "holder": null
}
```

### Assign an account to a client

```http
POST /api/accounts/1/assign
Authorization: Bearer <token>
Content-Type: application/json

{
  "clientId": 1
}
```

**200 OK**

```json
{
  "accountId": 1,
  "accountNumber": "40817810123456789012",
  "accountType": "DEBIT",
  "balance": 1000.00,
  "holder": {
    "clientId": 1,
    "fullName": "John Smith"
  }
}
```

### Close (release) an account

```http
POST /api/accounts/1/release
Authorization: Bearer <token>
```

**200 OK** — `holder` is `null` again.

## Response Codes

| Code | When                                                                              |
|------|-----------------------------------------------------------------------------------|
| 200  | Successful GET / PUT / assign / release                                           |
| 201  | Successful creation (POST)                                                        |
| 204  | Successful deletion (DELETE)                                                      |
| 400  | Input validation error                                                            |
| 401  | Missing or invalid JWT (`BadCredentialsException`)                                |
| 403  | Insufficient permissions (e.g., `USER` trying to delete)                          |
| 404  | Resource not found (`ClientNotFoundException`, `AccountNotFoundException`)        |
| 409  | State conflict (`AccountAlreadyAssignedException`, `AccountAlreadyFreeException`, `AccountNumberDuplicateException`, `UsernameAlreadyExistsException`) |
| 500  | Internal server error                                                             |

## Design Decisions

### Why `BigDecimal` for money?

`double` and `float` cannot represent decimal numbers precisely — `0.1 + 0.2` gives `0.30000000000000004`. In a banking system this means lost cents over time. `BigDecimal` stores numbers as exact decimals, mapped to `NUMERIC(19, 2)` in PostgreSQL.

### Why `@Enumerated(EnumType.STRING)` for `AccountType` and `Role`?

The default `EnumType.ORDINAL` stores enum positions (`0, 1, 2`). Reordering constants silently corrupts existing rows. `EnumType.STRING` stores names (`'DEBIT'`, `'USER'`, `'ADMIN'`) — safe for refactoring and readable in the database.

### Why can't `balance` be changed via PUT?

Allowing balance changes through `PUT /api/accounts/{id}` would let anyone set an arbitrary balance, bypassing business rules. Balance should only change through dedicated operations (deposit, withdraw, transfer) with proper validation — those are out of scope for this project.

### Why JWT instead of sessions?

JWT is stateless — the server does not store session data. This scales horizontally: any instance can validate a token without shared session storage. The trade-off is that tokens cannot be revoked before expiration without additional infrastructure (blacklist, refresh tokens).

### Why BCrypt for passwords?

BCrypt is intentionally slow and salted. Slow hashing makes brute-force attacks expensive. Salting means identical passwords produce different hashes, defeating rainbow tables. MD5/SHA should never be used for passwords.

## Project Structure

```
src/main/java/com/example/bankingapi/
├── config/             SecurityConfig, OpenApiConfig
├── controllers/        REST controllers (Client, Account, Auth)
├── dto/                Data Transfer Objects
│   ├── auth/
│   ├── client/
│   └── account/
├── enums/              AccountType, Role
├── exceptions/         Custom exceptions + GlobalExceptionHandler
├── mappers/            Entity ↔ DTO mapping
├── models/             JPA entities (Client, Account, User)
├── repositories/       Spring Data JPA repositories
├── security/           JwtService, JwtAuthenticationFilter, CustomUserDetailsService
└── services/           Business logic, @Transactional
```

## Author

**Dmitrii Ektov** — educational project for a Java development course.