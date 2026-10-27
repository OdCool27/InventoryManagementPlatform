-- V1-V4 used a single underscore and were never discoverable by default Flyway.
-- Preserve those files; establish their intended schema without changing history.
CREATE TABLE IF NOT EXISTS users(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS employees(
    employee_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(id),
    job_title VARCHAR(100) NOT NULL,
    employee_role VARCHAR(40) NOT NULL
        CHECK (employee_role IN ('MANAGER', 'SUPERVISOR', 'ADMIN', 'STAFF'))
);
CREATE TABLE IF NOT EXISTS customers(
    customer_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(id),
    contact_number VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS products(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(255) UNIQUE,
    item_name VARCHAR(120) NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    on_hand_quantity INT NOT NULL DEFAULT 0 CHECK (on_hand_quantity >= 0),
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS car_parts(
    part_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model VARCHAR(40) NOT NULL,
    make VARCHAR(40) NOT NULL,
    year VARCHAR(5) NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id)
);
CREATE TABLE IF NOT EXISTS carts(
    cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id INT REFERENCES customers(customer_id)
);
CREATE TABLE IF NOT EXISTS cart_items(
    cart_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(cart_id),
    sku VARCHAR(50) NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL
);
CREATE TABLE IF NOT EXISTS orders(
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(cart_id),
    customer_id INT NOT NULL REFERENCES customers(customer_id),
    confirmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS order_items(
    order_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(order_id),
    sku VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0)
);
