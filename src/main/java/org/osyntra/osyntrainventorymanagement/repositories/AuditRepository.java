package org.osyntra.osyntrainventorymanagement.repositories;

import org.osyntra.osyntrainventorymanagement.models.AuditLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class AuditRepository {
    private final JdbcClient jdbcClient;

    public AuditRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void create(int userId, String action, String entityType, String entityId, String oldValue, String newValue) {
        jdbcClient.sql("""
                INSERT INTO audit_logs(user_id, action, entity_type, entity_id, old_value, new_value)
                VALUES (:user, :action, :type, :id, :old, :new)
                """).param("user", userId).param("action", action).param("type", entityType)
                .param("id", entityId).param("old", oldValue).param("new", newValue).update();
    }

    public List<AuditLog> findAll(String entityType, String entityId, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (entityType != null) {
            sql.append(" AND entity_type = :type");
            params.put("type", entityType);
        }
        if (entityId != null) {
            sql.append(" AND entity_id = :id");
            params.put("id", entityId);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);
        return jdbcClient.sql(sql.toString()).params(params).query((rs, row) -> new AuditLog(
                rs.getLong("id"), rs.getInt("user_id"), rs.getString("action"), rs.getString("entity_type"),
                rs.getString("entity_id"), rs.getString("old_value"), rs.getString("new_value"),
                rs.getTimestamp("created_at").toInstant())).list();
    }
}
