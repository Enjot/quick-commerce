# Swagger UI screenshots

Add screenshots of the running Swagger UI here and reference them from the project README.

To capture them:

1. Start the stack: `docker compose up --build`
2. Open `http://localhost:8080/swagger-ui.html`
3. Suggested shots:
   - the full endpoint list grouped by tag (Authentication, Products, Categories, Cart, Orders),
   - the **Authorize** dialog with a pasted Bearer token,
   - a successful `POST /auth/login` response,
   - an order response showing `status` and `statusHistory`.
4. Save the images in this folder (e.g. `swagger-overview.png`) and link them in `README.md`.

A machine‑readable snapshot of the API is already committed at [`../openapi.json`](../openapi.json).
