# userapp

User management REST API with JWT authentication and role-based access control, built with Java 17 and Spring Boot 3.

Users register and log in to receive a signed JWT. Every other endpoint requires that token, and access is decided by role (`USER`, `ADMIN`) and by ownership of the account.

## Features

- **Registration and login** with validated input and BCrypt password hashing.
- **JWT authentication** (HS256, stateless sessions) through a custom Spring Security filter.
- **Role-based authorization** with method security: admins manage all users, regular users manage only their own account. Registration always creates a `USER`, so a client can never choose its own role.
- **Centralized error handling** with consistent JSON error responses.
- **Security audit logging** for sensitive events, plus structured logging with Logback and a Logstash config.
- **API documentation** with Swagger UI (OpenAPI 3).
- **Observability** through Spring Boot Actuator and a Prometheus metrics registry.
- **Test suite** with unit tests (services, JWT, security rules) and integration tests (controllers, repository, concurrency), plus JaCoCo coverage.

## Tech stack

Java 17, Spring Boot 3.5, Spring Security, Spring Data JPA, JJWT, H2 (in-memory, for development), Lombok, springdoc-openapi, Micrometer + Prometheus, JUnit 5, Maven.

## API

| Method | Path | Access | Description |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Create an account (role `USER`) |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| GET | `/api/users` | `ADMIN` | List all users |
| GET | `/api/users/{id}` | `ADMIN` or the user themselves | Get one user |
| PUT | `/api/users/{id}` | `ADMIN` or the user themselves | Update a user |
| DELETE | `/api/users/{id}` | `ADMIN` | Delete a user |

Send the token as `Authorization: Bearer <token>`.

No admin account is seeded: registration always creates a `USER`. To try the admin endpoints locally, promote a user in the H2 console (`UPDATE users SET role = 'ADMIN' WHERE username = 'jane';`, adjust the table name to your schema) and log in again.

Register request:

```json
{ "username": "jane", "email": "jane@example.com", "password": "a-strong-password" }
```

## Getting started

**Requirements:** JDK 17+ (Maven is included through the wrapper).

```bash
git clone https://github.com/ClaudioPaulo/userapp.git
cd userapp

# Signing key for the JWTs. Use any long random Base64 string.
export JWT_SECRET=$(openssl rand -base64 48)

./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`.

- Swagger UI (dev profile): `http://localhost:8080/swagger-ui.html`
- H2 console (dev profile): `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:userdb`

Quick check with curl:

```bash
curl -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"jane","email":"jane@example.com","password":"a-strong-password"}'

curl -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"jane","password":"a-strong-password"}'
```

## Profiles

`dev` is the default: Swagger UI, the H2 console and verbose logging are enabled. With `SPRING_PROFILES_ACTIVE=prod` they are all off. The app refuses to start without `JWT_SECRET`, so no signing key lives in the repository.

## Running tests

```bash
./mvnw verify
```

## Project structure

```
src/main/java/com/claudiopaulo/userapp/
├── config/       # security configuration, JWT filter, OpenAPI
├── controller/   # AuthController, UserController
├── dto/          # request and response objects
├── entity/       # User, Role
├── exception/    # global exception handler
├── repository/   # Spring Data repositories
├── security/     # security audit logger
└── service/      # user service, JWT service, UserDetailsService
```

## Roadmap

- PostgreSQL support with Flyway migrations
- Refresh tokens
- Docker image and Compose file

## Author

[Claudio Paulo](https://github.com/ClaudioPaulo), software engineer based in Lisbon.
# spring-boot-auth-template
