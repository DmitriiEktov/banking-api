# Banking API

REST API for managing clients and bank accounts.

## Technologies
- Java 21
- Spring Boot 4.0
- Spring Data JPA (Hibernate)
- PostgreSQL / H2
- Maven

## Features
- CRUD operations for clients
- CRUD operations for accounts
- Assign account to client
- Release account
- Input data validation

## How to Run
1. Clone the repository
2. Configure `application.properties`
3. Run `mvn spring-boot:run`

## API Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/clients | All clients |
| GET | /api/clients/{id} | Client by ID |
| POST | /api/clients | Create a client |
| PUT | /api/clients/{id} | Update a client |
| DELETE | /api/clients/{id} | Delete a client |
| GET | /api/accounts | All accounts |
| GET | /api/accounts/{id} | Account by ID |
| POST | /api/accounts | Create an account |
| PUT | /api/accounts/{accountId}/assign/{clientId} | Assign account to client |
| PUT | /api/accounts/{id}/release | Release account |
| DELETE | /api/accounts/{id} | Delete an account |
| GET | /api/accounts/free | Free accounts |
| GET | /api/accounts/client/{clientId} | Accounts of a client |