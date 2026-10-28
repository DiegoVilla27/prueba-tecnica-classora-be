-- =============================================================================
-- V1 · Initial schema: cashiers (users), product catalog, sales and sale lines.
-- See docs/03-arquitectura.md §5. Never edit an applied migration: add V<n>.
-- =============================================================================

-- Cashiers (the only kind of user; no roles)
CREATE TABLE users (
    id          UUID         PRIMARY KEY,
    first_name  VARCHAR(60)  NOT NULL,
    last_name   VARCHAR(60)  NOT NULL,
    email       VARCHAR(120) NOT NULL,
    password    VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Product catalog (read-only for the checkout)
CREATE TABLE products (
    id          UUID          PRIMARY KEY,
    code        VARCHAR(14)   NOT NULL,
    name        VARCHAR(120)  NOT NULL,
    price       NUMERIC(12,2) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uk_products_code UNIQUE (code),
    CONSTRAINT ck_products_price_non_negative CHECK (price >= 0),
    CONSTRAINT ck_products_code_digits CHECK (code ~ '^[0-9]{4,14}$')
);

-- Sales (aggregate root)
CREATE TABLE sales (
    id          UUID        PRIMARY KEY,
    cashier_id  UUID        NOT NULL,
    status      VARCHAR(20) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    closed_at   TIMESTAMPTZ,
    CONSTRAINT fk_sales_cashier FOREIGN KEY (cashier_id) REFERENCES users (id),
    CONSTRAINT ck_sales_status CHECK (status IN ('OPEN', 'COMPLETED', 'CANCELLED'))
);

-- RN-08: a cashier has at most one OPEN sale
CREATE UNIQUE INDEX ux_sales_one_open_per_cashier ON sales (cashier_id) WHERE status = 'OPEN';

-- Sale lines (internal entity of the Sale aggregate, with price snapshot)
CREATE TABLE sale_lines (
    id            UUID          PRIMARY KEY,
    sale_id       UUID          NOT NULL,
    product_id    UUID          NOT NULL,
    product_code  VARCHAR(14)   NOT NULL,
    product_name  VARCHAR(120)  NOT NULL,
    unit_price    NUMERIC(12,2) NOT NULL,
    quantity      INT           NOT NULL,
    position      INT           NOT NULL,
    CONSTRAINT fk_sale_lines_sale FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_lines_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_sale_lines_sale_product UNIQUE (sale_id, product_id),
    CONSTRAINT ck_sale_lines_quantity CHECK (quantity BETWEEN 1 AND 999)
);

CREATE INDEX ix_sale_lines_sale ON sale_lines (sale_id);
