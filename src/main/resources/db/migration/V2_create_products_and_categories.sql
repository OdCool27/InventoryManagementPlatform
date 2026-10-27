CREATE TABLE products(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(255) UNIQUE,
    item_name VARCHAR(120) NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
        CHECK(unit_price >= 0),
    reserved_quantity INT NOT NULL
        CHECK(reserved_quantity >= 0),
    on_hand_quantity INT NOT NULL
        CHECK(on_hand_quantity >= 0),
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE car_parts(
    part_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    model VARCHAR(40) NOT NULL,
    make VARCHAR(40) NOT NULL,
    year VARCHAR(5) NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id)
);