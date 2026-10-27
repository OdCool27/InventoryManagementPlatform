package org.osyntra.osyntrainventorymanagement.repositories;

import org.osyntra.osyntrainventorymanagement.models.Inventory;
import org.osyntra.osyntrainventorymanagement.dtos.inventory.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class InventoryRepository {
    private final JdbcClient jdbcClient;
    private static final RowMapper<Inventory> MAPPER = (rs, row) -> new Inventory(
            rs.getObject("product_id", UUID.class), rs.getInt("quantity_on_hand"),
            rs.getInt("quantity_reserved"), rs.getInt("reorder_level"), rs.getTimestamp("updated_at").toInstant());

    public InventoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Inventory> findByProductId(UUID id, boolean lock) {
        return jdbcClient.sql("SELECT * FROM inventory WHERE product_id = :id" + (lock ? " FOR UPDATE" : ""))
                .param("id", id).query(MAPPER).optional();
    }

    public void create(UUID id, InventoryRequest request) {
        jdbcClient.sql("""
                INSERT INTO inventory(product_id, quantity_on_hand, reorder_level)
                VALUES (:id, :quantity, :reorder)
                """).param("id", id).param("quantity", request.quantityOnHand())
                .param("reorder", request.reorderLevel()).update();
    }

    public void adjust(UUID id, int amount, boolean set) {
        jdbcClient.sql("SELECT adjust_inventory(:id, :amount, :set)")
                .param("id", id).param("amount", amount).param("set", set).query(Integer.class).single();
    }

    public void setReorderLevel(UUID id, int level) {
        jdbcClient.sql("UPDATE inventory SET reorder_level = :level, updated_at = clock_timestamp() WHERE product_id = :id")
                .param("id", id).param("level", level).update();
    }

    public List<InventoryResponse> findAll(String search, String category, String make, String model,
                                           String year, boolean lowStock, boolean outOfStock, int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, c.name AS category, cp.make, cp.model, cp.year,
                i.quantity_on_hand, i.quantity_reserved, i.reorder_level, i.updated_at
                FROM
                """);
        sql.append(lowStock ? " low_stock_inventory() i " : " inventory i ");
        sql.append("""
                JOIN products p ON p.id = i.product_id
                LEFT JOIN categories c ON c.id = p.category_id
                LEFT JOIN car_parts cp ON cp.product_id = p.id WHERE p.active
                """);
        Map<String, Object> params = new HashMap<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (p.item_name ILIKE :search OR p.sku ILIKE :search OR p.item_description ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }
        String[] names = {"category", "make", "model", "year"};
        String[] columns = {"c.name", "cp.make", "cp.model", "cp.year"};
        String[] values = {category, make, model, year};
        for (int i = 0; i < names.length; i++) {
            if (values[i] != null && !values[i].isBlank()) {
                sql.append(" AND lower(").append(columns[i]).append(") = lower(:").append(names[i]).append(")");
                params.put(names[i], values[i].trim());
            }
        }
        if (outOfStock) {
            sql.append(" AND i.quantity_on_hand - i.quantity_reserved = 0");
        }
        sql.append(" ORDER BY p.item_name, p.id LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);
        return jdbcClient.sql(sql.toString()).params(params).query((rs, row) -> {
            int quantity = rs.getInt("quantity_on_hand");
            int reserved = rs.getInt("quantity_reserved");
            int reorder = rs.getInt("reorder_level");
            return new InventoryResponse(ProductRepository.MAPPER.mapRow(rs, row), quantity, reserved,
                    quantity - reserved, reorder, quantity - reserved <= reorder, rs.getTimestamp("updated_at").toInstant());
        }).list();
    }

    public InventorySummary summary() {
        return jdbcClient.sql("""
                SELECT (SELECT count(*) FROM products WHERE active) AS total_products,
                count(*) AS stocked_products, coalesce(sum(i.quantity_on_hand), 0) AS total_units,
                count(*) FILTER (WHERE i.quantity_on_hand - i.quantity_reserved <= i.reorder_level) AS low_stock_products,
                count(*) FILTER (WHERE i.quantity_on_hand - i.quantity_reserved = 0) AS out_of_stock_products
                FROM inventory i JOIN products p ON p.id = i.product_id WHERE p.active
                """).query(InventorySummary.class).single();
    }
}
