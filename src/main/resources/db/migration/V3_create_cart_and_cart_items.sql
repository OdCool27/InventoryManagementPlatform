CREATE TABLE carts(
    cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id INT REFERENCES customers(customer_id),

);

CREATE TABLE cart_items(
    cart_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(cart_id),
    sku VARCHAR(50) NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL
);