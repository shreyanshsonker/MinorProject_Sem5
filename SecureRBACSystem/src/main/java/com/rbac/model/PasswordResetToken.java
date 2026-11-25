package com.rbac.model;

import java.sql.Timestamp;

/**
 * PasswordResetToken — POJO model class matching the 'password_reset_tokens' table.
 *
 * Columns:
 *   token_id   INT, PK, AUTO_INCREMENT
 *   user_id    INT, FK → users
 *   token      VARCHAR(64), UNIQUE
 *   expires_at TIMESTAMP
 *   used       BOOLEAN
 *   created_at TIMESTAMP
 */
public class PasswordResetToken {

    private int tokenId;
    private int userId;
    private String token;
    private Timestamp expiresAt;
    private boolean used;
    private Timestamp createdAt;

    public PasswordResetToken() {
    }

    // --- Getters and Setters ---

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
