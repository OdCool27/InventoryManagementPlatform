CREATE TABLE orders(
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(cart_id),
    customer_id INT NOT NULL REFERENCES customers(customer_id),
    confirmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

);

CREATE TABLE order_items();