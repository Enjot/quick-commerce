-- Core schema for the quick-commerce domain.

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    product_type    VARCHAR(31)   NOT NULL,
    sku             VARCHAR(255)  NOT NULL UNIQUE,
    name            VARCHAR(255)  NOT NULL,
    description     TEXT,
    price           NUMERIC(12, 2) NOT NULL,
    category_id     BIGINT        NOT NULL REFERENCES categories (id),
    stock_quantity  INTEGER       NOT NULL,
    active          BOOLEAN       NOT NULL,
    -- perishable
    expiry_date     DATE,
    shelf_life_days INTEGER,
    -- age restricted
    minimum_age     INTEGER
);
CREATE INDEX idx_products_category ON products (category_id);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL UNIQUE REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES users (id),
    status           VARCHAR(255)   NOT NULL,
    total_amount     NUMERIC(12, 2) NOT NULL,
    delivery_fee     NUMERIC(12, 2) NOT NULL,
    delivery_address VARCHAR(255),
    created_at       TIMESTAMPTZ    NOT NULL
);
CREATE INDEX idx_orders_user ON orders (user_id);

CREATE TABLE line_items (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT         NOT NULL REFERENCES products (id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    cart_id    BIGINT         REFERENCES carts (id),
    order_id   BIGINT         REFERENCES orders (id)
);
CREATE INDEX idx_line_items_cart ON line_items (cart_id);
CREATE INDEX idx_line_items_order ON line_items (order_id);

CREATE TABLE order_status_history (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT       NOT NULL REFERENCES orders (id),
    status     VARCHAR(255) NOT NULL,
    changed_at TIMESTAMPTZ  NOT NULL,
    changed_by VARCHAR(255)
);
CREATE INDEX idx_osh_order ON order_status_history (order_id);
