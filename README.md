# Banking API

REST API for digital banking client and account management. Allows registering clients, opening accounts for them, and closing accounts when clients return them.

## Tech Stack

- **Java 23**
- **Spring Boot 4.1.1** (Web, Data JPA, Validation)
- **Hibernate 7.4.5**
- **PostgreSQL 18**
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
- Automatic ID generation
- Input validation via Jakarta Bean Validation
- Centralized error handling with proper HTTP status codes (404, 409)
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
```

### 2. Set environment variables

The database password is provided via the `DB_PASSWORD` environment variable:

- **IntelliJ IDEA:** `Run → Edit Configurations → Environment variables → DB_PASSWORD=your_password`
- **Linux/macOS:** `export DB_PASSWORD=your_password`
- **Windows (PowerShell):** `$env:DB_PASSWORD="your_password"`

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
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`.

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

### Clients

| Method | URL                 | Description                                       |
|--------|---------------------|---------------------------------------------------|
| GET    | `/api/clients`      | List all clients                                  |
| GET    | `/api/clients/{id}` | Get client by ID with their accounts              |
| POST   | `/api/clients`      | Create a new client                               |
| PUT    | `/api/clients/{id}` | Update client data                                |
| DELETE | `/api/clients/{id}` | Delete a client (accounts are automatically released) |

### Accounts

| Method | URL                        | Description                        |
|--------|----------------------------|------------------------------------|
| GET    | `/api/accounts`            | List all accounts                  |
| GET    | `/api/accounts/free`       | List free accounts (no holder)     |
| GET    | `/api/accounts/{id}`       | Get account by ID with current holder |
| POST   | `/api/accounts`            | Create a new account               |
| PUT    | `/api/accounts/{id}`       | Update account type                |
| DELETE | `/api/accounts/{id}`       | Delete an account                  |
| POST   | `/api/accounts/{id}/assign`| Assign an account to a client      |
| POST   | `/api/accounts/{id}/release`| Close (release) an account        |

## Request Examples

### Create a client

```http
POST /api/clients
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
```

**200 OK** — `holder` is `null` again.

## Response Codes

| Code | When                                                                              |
|------|-----------------------------------------------------------------------------------|
| 200  | Successful GET / PUT / assign / release                                           |
| 201  | Successful creation (POST)                                                        |
| 204  | Successful deletion (DELETE)                                                      |
| 400  | Input validation error                                                            |
| 404  | Resource not found (`ClientNotFoundException`, `AccountNotFoundException`)        |
| 409  | State conflict (`AccountAlreadyAssignedException`, `AccountAlreadyFreeException`, `AccountNumberDuplicateException`) |
| 500  | Internal server error                                                             |

## Design Decisions

### Why `BigDecimal` for money?

`double` and `float` cannot represent decimal numbers precisely — `0.1 + 0.2` gives `0.30000000000000004`. In a banking system this means lost cents over time. `BigDecimal` stores numbers as exact decimals, mapped to `NUMERIC(19, 2)` in PostgreSQL.

### Why `@Enumerated(EnumType.STRING)` for `AccountType`?

The default `EnumType.ORDINAL` stores enum positions (`0, 1, 2`). Reordering constants silently corrupts existing rows. `EnumType.STRING` stores names (`'DEBIT'`, `'CREDIT'`, `'SAVINGS'`) — safe for refactoring and readable in the database.

### Why can't `balance` be changed via PUT?

Allowing balance changes through `PUT /api/accounts/{id}` would let anyone set an arbitrary balance, bypassing business rules. Balance should only change through dedicated operations (deposit, withdraw, transfer) with proper validation — those are out of scope for this project.

## Project Structure

```
src/main/java/com/example/bankingapi/
├── controllers/        REST controllers
├── dto/                Data Transfer Objects
│   ├── client/
│   └── account/
├── enums/              AccountType
├── exceptions/         Custom exceptions + GlobalExceptionHandler
├── mappers/            Entity ↔ DTO mapping
├── models/             JPA entities (Client, Account)
├── repositories/       Spring Data JPA repositories
└── services/           Business logic, @Transactional
```

## Author

**Dmitrii Ektov** — educational project for a Java development course.