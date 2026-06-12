# Swagger UI screenshots

Captured from the running application at `http://localhost:8080/swagger-ui.html`
(seeded with sample categories, products, a customer and a delivered order).

## Endpoint overview

The full API grouped by tag (Authentication, Products, Categories, Cart, Orders) plus schemas.

![Swagger overview](swagger-overview.png)

## Authorize dialog

Pasting a Bearer JWT into the **Authorize** dialog (`bearerAuth`, HTTP Bearer).

![Authorize dialog](swagger-authorize.png)

## `POST /auth/login` response

A successful login returning the JWT, token type and role.

![Login response](swagger-login-response.png)

## `GET /orders/{id}` response

An order showing `status`, line items with frozen `unitPrice`, and the full `statusHistory`.

![Order response](swagger-order-response.png)

---

To re-capture: start the app, run the seed flow, then drive Swagger UI with a headless
Chromium/Edge browser. A machine-readable snapshot of the API is committed at
[`../openapi.json`](../openapi.json).
