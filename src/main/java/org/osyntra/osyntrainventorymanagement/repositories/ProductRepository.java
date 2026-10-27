package org.osyntra.osyntrainventorymanagement.repositories;

import org.osyntra.osyntrainventorymanagement.dtos.product.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class ProductRepository {
    private final JdbcClient jdbcClient;
    private static final String SELECT = """
            SELECT p.*, c.name AS category, cp.make, cp.model, cp.year
            FROM products p LEFT JOIN categories c ON c.id = p.category_id
            LEFT JOIN car_parts cp ON cp.product_id = p.id
            """;
    public static final RowMapper<ProductResponse> MAPPER = (rs, row) -> new ProductResponse(
            rs.getObject("id", UUID.class), rs.getString("sku"), rs.getString("barcode"),
            rs.getString("item_name"), rs.getString("item_description"), rs.getBigDecimal("unit_price"),
            rs.getObject("category_id", Integer.class), rs.getString("category"), rs.getBoolean("active"),
            rs.getString("make"), rs.getString("model"), rs.getString("year"),
            rs.getTimestamp("added_at").toInstant(), rs.getTimestamp("modified_at").toInstant());

    public ProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<ProductResponse> findById(UUID id) {
        return jdbcClient.sql(SELECT + " WHERE p.id = :id").param("id", id).query(MAPPER).optional();
    }

    public void lock(UUID id) {
        jdbcClient.sql("SELECT id FROM products WHERE id = :id FOR UPDATE")
                .param("id", id).query(UUID.class).optional();
    }

    public List<ProductResponse> findAll(String search, String category, String make, String model,
                                         String year, boolean includeInactive, int limit, int offset) {
        StringBuilder sql = new StringBuilder(SELECT + " WHERE (:includeInactive OR p.active)");
        Map<String, Object> params = new HashMap<>();
        params.put("includeInactive", includeInactive);
        if (search != null && !search.isBlank()) {
            sql.append(" AND (p.item_name ILIKE :search OR p.sku ILIKE :search OR p.item_description ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }
        addFilter(sql, params, "category", "c.name", category);
        addFilter(sql, params, "make", "cp.make", make);
        addFilter(sql, params, "model", "cp.model", model);
        addFilter(sql, params, "year", "cp.year", year);
        sql.append(" ORDER BY p.item_name, p.id LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);
        return jdbcClient.sql(sql.toString()).params(params).query(MAPPER).list();
    }

    private void addFilter(StringBuilder sql, Map<String, Object> params, String name, String column, String value) {
        if (value != null && !value.isBlank()) {
            sql.append(" AND lower(").append(column).append(") = lower(:").append(name).append(")");
            params.put(name, value.trim());
        }
    }

    public UUID create(ProductRequest request) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("""
                INSERT INTO products(id, sku, barcode, item_name, item_description, unit_price, category_id)
                VALUES (:id, :sku, :barcode, :name, :description, :price, :category)
                """).params(parameters(id, request)).update();
        saveCarPart(id, request);
        return id;
    }

    public void update(UUID id, ProductRequest request) {
        jdbcClient.sql("""
                UPDATE products SET sku = :sku, barcode = :barcode, item_name = :name,
                item_description = :description, unit_price = :price, category_id = :category,
                modified_at = clock_timestamp() WHERE id = :id
                """).params(parameters(id, request)).update();
        saveCarPart(id, request);
    }

    private Map<String, Object> parameters(UUID id, ProductRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("sku", request.sku().trim());
        params.put("barcode", request.barcode() == null || request.barcode().isBlank() ? null : request.barcode().trim());
        params.put("name", request.itemName().trim());
        params.put("description", request.itemDescription().trim());
        params.put("price", request.unitPrice());
        params.put("category", request.categoryId());
        return params;
    }

    private void saveCarPart(UUID id, ProductRequest request) {
        if (request.make() == null) {
            jdbcClient.sql("DELETE FROM car_parts WHERE product_id = :id").param("id", id).update();
        } else {
            jdbcClient.sql("""
                    INSERT INTO car_parts(product_id, make, model, year) VALUES (:id, :make, :model, :year)
                    ON CONFLICT(product_id) DO UPDATE SET make = EXCLUDED.make, model = EXCLUDED.model, year = EXCLUDED.year
                    """).param("id", id).param("make", request.make().trim())
                    .param("model", request.model().trim()).param("year", request.year()).update();
        }
    }

    public void deactivate(UUID id) {
        jdbcClient.sql("UPDATE products SET active = FALSE, modified_at = clock_timestamp() WHERE id = :id")
                .param("id", id).update();
    }

    public List<CategoryResponse> categories() {
        return jdbcClient.sql("SELECT id, name FROM categories ORDER BY name").query(CategoryResponse.class).list();
    }

    public CategoryResponse createCategory(String name) {
        Integer id = jdbcClient.sql("INSERT INTO categories(name) VALUES (:name) RETURNING id")
                .param("name", name.trim()).query(Integer.class).single();
        return new CategoryResponse(id, name.trim());
    }

    public boolean categoryExists(int id) {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM categories WHERE id = :id)")
                .param("id", id).query(Boolean.class).single();
    }
}
