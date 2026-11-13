# Banking API

REST API for digital banking client and account management. Allows registering clients, opening accounts for them, and closing accounts when clients return them. Secured with JWT-based authentication and role-based access control. Fully containerized and equipped with a CI/CD pipeline.

## Tech Stack

- **Java 23**
- **Spring Boot 4.1.1** (Web, Data JPA, Validation, Security)
- **Hibernate 7.4.5**
- **PostgreSQL 18**
- **Liquibase** (database migrations)
- **Spring Security** with **JWT** authentication
- **Lombok**
- **springdoc-openapi 3.1.1** (Swagger UI)
- **Maven** (with Maven Wrapper)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers (PostgreSQL)
- **DevOps**: Docker (multi-stage build), Docker Compose, GitHub Actions (CI/CD), GitHub Container Registry (GHCR)

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
- Centralized error handling with proper HTTP status codes (400, 401, 403, 404, 409)
- One-to-Many relationship between Client and Account
- When a client is deleted, their accounts are automatically released (`ON DELETE SET NULL`)
- **Money stored as `BigDecimal`** (never `double`/`float`) — precise decimal arithmetic
- **`AccountType` and `Role` enums** stored as `VARCHAR` (not ordinal) — safe for refactoring
- **Balance cannot be modified via PUT** — only through dedicated operations (future)
- **Database migrations** handled automatically by Liquibase on startup
- **Comprehensive test suite**: Unit tests, Web MVC tests, Repository tests, and Integration tests (Testcontainers)
- **Ready-to-use `.http` files** for manual API testing in IntelliJ IDEA / VS Code
- **Dockerized environment** for one-command local deployment

## Requirements

- JDK 23+
- Docker & Docker Compose (for local deployment and running integration tests)
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- PostgreSQL 14+ (if running without Docker)

## Getting Started

### 1. Set environment variables

The database password and JWT secret are provided via environment variables.

Create a `.env` file in the root directory (you can copy the provided `.env.example`):

```properties
DB_PASSWORD=your_secure_password
JWT_SECRET=your_super_secret_jwt_key_at_least_32_bytes_long
```

Generate a JWT secret (at least 32 bytes) if you need a new one:
```bash
openssl rand -base64 32
```

*(Alternatively, you can set these directly in your environment or IDE's Run Configurations:)*
- **IntelliJ IDEA:** `Run → Edit Configurations → Environment variables → DB_PASSWORD=your_password;JWT_SECRET=your_secret`
- **Linux/macOS:** `export DB_PASSWORD=your_password && export JWT_SECRET=your_secret`
- **Windows (PowerShell):** `$env:DB_PASSWORD="your_password"; $env:JWT_SECRET="your_secret"`

### 2. Run with Docker Compose (Recommended)

The easiest way to start the application along with the PostgreSQL database is using Docker Compose. The database schema is automatically managed by Liquibase on startup.

```bash
docker compose up --build -d
```

- The application will be available at `http://localhost:8080`.
- PostgreSQL will be available at `localhost:5432`.

To stop the containers:
```bash
docker compose down
```

### 3. Run Locally (Without Docker)

If you prefer to run the app directly from your IDE or terminal:

**1. Create the database (Optional, if not using Liquibase manually)**
```sql
CREATE DATABASE banking_app;

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

**2. Configure `application.properties`**
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

**3. Run the application using Maven Wrapper**
```bash
./mvnw spring-boot:run
```
The application will start at `http://localhost:8080`. Note that Liquibase will automatically apply the necessary migrations to your local database upon startup.

## Testing

The project follows a multi-layered testing strategy:

- **Unit Tests** (`*ServiceTest`, `*SecurityTest`) — isolated business logic verification using Mockito.
- **Web Layer Tests** (`@WebMvcTest` + `MockMvc`) — verifying HTTP statuses, JSON contracts, and security configurations.
- **Repository Tests** (`@DataJpaTest`) — verifying custom queries and DB constraints.
- **Integration Tests** (`AbstractIntegrationTest` + Testcontainers) — full end-to-end testing over real HTTP against a real PostgreSQL database in a Docker container.

To run all tests locally:
```bash
./mvnw clean verify
```
*(Docker must be running locally for Testcontainers to spin up the PostgreSQL instance).*

## CI/CD Pipeline

The project is equipped with a GitHub Actions workflow (`.github/workflows/ci-cd.yml`):

1. **Build & Test**: Triggered on every push and pull request to `main`. It compiles the code, runs all tests (including Testcontainers), and ensures the codebase is healthy.
2. **Build & Push Docker Image**: Triggered only on pushes to `main` after tests pass. It builds a lightweight multi-stage Docker image and pushes it to the **GitHub Container Registry (GHCR)** with `latest` and commit SHA tags.

## API Documentation

### Swagger UI

Once the application is running, interactive API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

To authorize in Swagger UI:
1. Call `POST /api/auth/login` and copy the `token` from the response.
2. Click **Authorize** (top right).
3. Paste **only the token** (without the `Bearer ` prefix — Swagger adds it automatically).
4. Click **Authorize** → **Close**.

### HTTP Client (IntelliJ IDEA / VS Code)

The repository includes a `requests.http` file in the root directory.
You can open it in your IDE and execute API calls directly:
1. Run the `Register` request to get a JWT token.
2. The built-in script will automatically save the token.
3. Run the `GET /api/clients` request (the token is injected automatically via `{{auth_token}}`).

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

### Why Multi-stage Docker build?
The build stage uses JDK + Maven to compile the JAR, while the runtime stage uses a lightweight JRE Alpine image. This drastically reduces the final image size and removes build tools, source code, and the compiler from production, lowering the attack surface.

### Why Testcontainers in CI?
Integration tests should verify interaction with a real PostgreSQL database, not an in-memory H2. Testcontainers spins up the exact same DB version as in production right inside the CI runner, making the build deterministic and reliable.

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

src/main/resources/
└── db/changelog/       Liquibase migration scripts

src/test/java/...       Unit, Web, Repository, and Integration tests

.github/workflows/      GitHub Actions CI/CD pipeline
Dockerfile              Multi-stage Docker build
docker-compose.yml      Local orchestration (App + PostgreSQL)
requests.http           Ready-to-use HTTP client requests
```

## Author

**Dmitrii Ektov** — educational project for a Java development course.