# Quick Commerce

A simplified quick‑commerce (q‑commerce) platform: customers browse a catalogue and place
orders delivered from a single local warehouse; administrators manage the catalogue, stock
and order fulfilment. Built as a layered Spring Boot application with a focus on OOP/SOLID,
polymorphism and classic design patterns.

---

## Highlights

- **Polymorphism** on a `Product` hierarchy (`RegularProduct` / `PerishableProduct` /
  `AgeRestrictedProduct`) — checkout validates every line item polymorphically, never
  branching on type.
- **State pattern** for the order lifecycle (`NEW → PAID → PICKING → PACKED → IN_DELIVERY →
  DELIVERED`, with `CANCELLED`); each state object owns its allowed transitions.
- **Strategy pattern** for delivery cost (`StandardDeliveryStrategy`,
  `FreeDeliveryAboveThresholdStrategy`) — swappable via configuration.
- **RBAC** with two roles (`USER`, `ADMIN`) enforced by stateless JWT authentication.
- **Price snapshot**: a cart shows live catalogue prices, but checkout freezes each unit
  price so later catalogue changes never alter a placed order.
- **≥ 80 % test coverage** enforced by JaCoCo (currently ~93 % line), with unit, slice and
  Testcontainers‑backed integration tests.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1 (Spring Web MVC, Security, Data JPA) |
| Build | Maven (wrapper included) |
| Database | PostgreSQL 17 |
| Migrations | Flyway |
| Auth | Spring Security 7 + JWT (JJWT) |
| API docs | springdoc‑openapi (Swagger UI) |
| Tests | JUnit 5, Mockito, Testcontainers, JaCoCo |
| Packaging | Docker (multi‑stage) + docker‑compose |

---

## Running

### With Docker (recommended)

```bash
docker compose up --build
```

This starts PostgreSQL and the application. Flyway applies the schema on startup.

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs` (a snapshot is in [`docs/openapi.json`](docs/openapi.json))

### Locally (Maven wrapper)

Requires a PostgreSQL reachable at `jdbc:postgresql://localhost:5432/qcommerce`
(user/password `qcommerce`/`qcommerce`), or override via environment variables
(`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

```bash
./mvnw spring-boot:run
```

### Default admin

On first start the application seeds an admin account (configurable via `APP_ADMIN_EMAIL` /
`APP_ADMIN_PASSWORD`):

```
email:    admin1@mail.com
password: admin123
```

### Authenticating

1. `POST /auth/register` (creates a `USER`) or use the seeded admin.
2. `POST /auth/login` → returns a JWT.
3. Send `Authorization: Bearer <token>` on subsequent requests. In Swagger UI use the
   **Authorize** button.

---

## API overview

| Endpoint | Method | Access |
|---|---|---|
| `/auth/register`, `/auth/login` | POST | public |
| `/products`, `/products/{id}` | GET | public |
| `/products`, `/products/{id}` | POST / PUT / DELETE | ADMIN |
| `/categories` | GET | public |
| `/categories`, `/categories/{id}` | POST / PUT / DELETE | ADMIN |
| `/cart`, `/cart/items`, `/cart/items/{id}` | GET / POST / DELETE | USER |
| `/orders` (checkout) | POST | USER |
| `/orders`, `/orders/{id}` | GET | USER (own) / ADMIN (all) |
| `/orders/{id}/status` | PATCH | ADMIN |

Full, live documentation is available in Swagger UI.

---

## Architecture

```
com.enjot.quickcommerce
├── config            // SecurityConfig, OpenApiConfig, DeliveryConfig, DataInitializer
├── domain            // entities: Product (+ subtypes), Category, User, Cart, LineItem, Order...
│   └── order.state   // State pattern: OrderState + concrete states + factory
├── repository        // Spring Data JPA repositories
├── security          // JWT service, filter, UserDetails
├── service           // business logic (catalog, cart, order, auth)
│   └── delivery      // Strategy pattern: delivery cost strategies
├── web               // REST controllers
│   └── dto           // request/response records
└── exception         // domain exceptions + @RestControllerAdvice
```

### Domain model (ERD)

See [`docs/ERD.md`](docs/ERD.md) for the entity‑relationship diagram.

### Design patterns

- **State** (`domain/order/state`): the order delegates transition validation to its current
  state object; an illegal transition throws `IllegalOrderTransitionException` (HTTP 409).
- **Strategy** (`service/delivery`): the active `DeliveryCostStrategy` is wired in
  `DeliveryConfig` from `app.delivery.*` properties.
- **Polymorphism** (`domain/Product`): `validateForOrder(...)` is overridden per subtype and
  invoked uniformly for every cart line at checkout.

---

## Testing

```bash
./mvnw verify
```

Runs all tests (unit + Spring slice + Testcontainers integration) and enforces the JaCoCo
line‑coverage threshold (build fails below 80 %). The HTML coverage report is written to
`target/site/jacoco/index.html`. Integration tests require Docker (Testcontainers starts a
PostgreSQL container automatically).

---

## Requirements mapping

| Requirement | Where |
|---|---|
| OOP + SOLID, layered architecture | package structure, constructor DI |
| Two roles (RBAC) | `Role`, `SecurityConfig`, JWT |
| Polymorphism | `Product` hierarchy, `validateForOrder` |
| Design pattern | **State** (orders) + **Strategy** (delivery) |
| Hibernate + SQL + ERD | JPA entities, Flyway schema, `docs/ERD.md` |
| Migrations | Flyway (`src/main/resources/db/migration`) |
| Swagger UI | springdoc‑openapi |
| Docker | `Dockerfile` (multi‑stage) + `docker-compose.yml` |
| Tests ≥ 80 % | JUnit + Testcontainers + JaCoCo gate |
| Documentation | this README, `docs/` |
