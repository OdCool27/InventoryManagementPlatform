CREATE TABLE categories(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE CHECK (btrim(name) <> '')
);
ALTER TABLE products ADD COLUMN category_id INT REFERENCES categories(id);
ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE car_parts ADD CONSTRAINT car_parts_product_unique UNIQUE(product_id);
CREATE TABLE inventory(
    product_id UUID PRIMARY KEY REFERENCES products(id),
    quantity_on_hand INT NOT NULL DEFAULT 0 CHECK (quantity_on_hand >= 0),
    quantity_reserved INT NOT NULL DEFAULT 0 CHECK (quantity_reserved >= 0),
    reorder_level INT NOT NULL DEFAULT 0 CHECK (reorder_level >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (quantity_reserved <= quantity_on_hand)
);
INSERT INTO inventory(product_id, quantity_on_hand, quantity_reserved)
SELECT id, on_hand_quantity, reserved_quantity FROM products;
ALTER TABLE products DROP COLUMN on_hand_quantity;
ALTER TABLE products DROP COLUMN reserved_quantity;
CREATE UNIQUE INDEX users_email_case_unique ON users(lower(email));
CREATE INDEX car_parts_vehicle_idx ON car_parts(lower(make), lower(model), year);
CREATE INDEX products_category_idx ON products(category_id);
