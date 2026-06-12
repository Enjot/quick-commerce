# Entity–Relationship Diagram

The schema is owned by Flyway (`src/main/resources/db/migration`). Products use
`SINGLE_TABLE` inheritance: all subtypes share the `products` table, discriminated by
`product_type`, with subtype‑specific nullable columns.

![Entity–relationship diagram](ERD.png)

<details>
<summary>Mermaid source</summary>

```mermaid
erDiagram
    users ||--o| carts : "has one"
    users ||--o{ orders : places
    categories ||--o{ products : groups
    carts ||--o{ line_items : contains
    orders ||--o{ line_items : contains
    products ||--o{ line_items : "referenced by"
    orders ||--o{ order_status_history : logs

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role
        date date_of_birth
        timestamptz created_at
    }
    categories {
        bigint id PK
        varchar name UK
    }
    products {
        bigint id PK
        varchar product_type "discriminator"
        varchar sku UK
        varchar name
        text description
        numeric price
        bigint category_id FK
        int stock_quantity
        boolean active
        date expiry_date "PERISHABLE"
        int shelf_life_days "PERISHABLE"
        int minimum_age "AGE_RESTRICTED"
    }
    carts {
        bigint id PK
        bigint user_id FK,UK
        timestamptz created_at
        timestamptz updated_at
    }
    orders {
        bigint id PK
        bigint user_id FK
        varchar status
        numeric total_amount
        numeric delivery_fee
        varchar delivery_address
        timestamptz created_at
    }
    line_items {
        bigint id PK
        bigint product_id FK
        int quantity
        numeric unit_price "snapshot at checkout"
        bigint cart_id FK "nullable"
        bigint order_id FK "nullable"
    }
    order_status_history {
        bigint id PK
        bigint order_id FK
        varchar status
        timestamptz changed_at
        varchar changed_by
    }
```

</details>

## Order lifecycle (State pattern)

```mermaid
stateDiagram-v2
    [*] --> NEW
    NEW --> PAID
    NEW --> CANCELLED
    PAID --> PICKING
    PAID --> CANCELLED
    PICKING --> PACKED
    PACKED --> IN_DELIVERY
    IN_DELIVERY --> DELIVERED
    DELIVERED --> [*]
    CANCELLED --> [*]
```
