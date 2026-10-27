CREATE TABLE audit_logs(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    action VARCHAR(40) NOT NULL,
    entity_type VARCHAR(30) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX audit_logs_entity_idx ON audit_logs(entity_type, entity_id, created_at);
CREATE FUNCTION adjust_inventory(p_product_id UUID, p_amount INT, p_set BOOLEAN DEFAULT FALSE)
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    current_quantity INT;
    reserved INT;
    next_quantity BIGINT;
BEGIN
    SELECT quantity_on_hand, quantity_reserved INTO current_quantity, reserved
    FROM inventory WHERE product_id = p_product_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Inventory not found' USING ERRCODE = 'P0002';
    END IF;
    next_quantity := CASE WHEN p_set THEN p_amount::BIGINT ELSE current_quantity::BIGINT + p_amount END;
    IF next_quantity < reserved OR next_quantity > 2147483647 THEN
        RAISE EXCEPTION 'Insufficient available stock or quantity overflow' USING ERRCODE = '23514';
    END IF;
    UPDATE inventory SET quantity_on_hand = next_quantity, updated_at = clock_timestamp()
    WHERE product_id = p_product_id;
    RETURN next_quantity::INT;
END;
$$;
CREATE FUNCTION low_stock_inventory()
RETURNS SETOF inventory LANGUAGE sql STABLE AS $$
    SELECT i.* FROM inventory i JOIN products p ON p.id = i.product_id
    WHERE p.active AND i.quantity_on_hand - i.quantity_reserved <= i.reorder_level;
$$;
