package org.osyntra.osyntrainventorymanagement.models;

import java.time.Instant;

public class AuditLog {
    private Long id;
    private Integer userId;
    private String action;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private Instant createdAt;

    //CONSTRUCTORS
    public AuditLog(Long id, Integer userId, String action, String entityType, String entityId,
                    String oldValue, String newValue, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    //ACCESSORS
    public Long getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
