package com.rbac.model;

import java.sql.Timestamp;

/**
 * AuditLog — POJO model class matching the 'audit_logs' table.
 *
 * Columns:
 *   log_id        INT, PK, AUTO_INCREMENT
 *   user_id       INT, FK → users (nullable for failed logins by unknown users)
 *   action_type   VARCHAR(50)
 *   target_entity VARCHAR(50)
 *   target_id     INT
 *   ip_address    VARCHAR(45)
 *   details       TEXT
 *   created_at    TIMESTAMP
 *
 * Note: 'username' is a transient field populated from a JOIN with users table.
 */
public class AuditLog {

    private int logId;
    private Integer userId;
    private String actionType;
    private String targetEntity;
    private Integer targetId;
    private String ipAddress;
    private String details;
    private Timestamp createdAt;

    // Transient: populated from JOIN with users table, not a column in 'audit_logs'
    private String username;

    public AuditLog() {
    }

    // --- Getters and Setters ---

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
