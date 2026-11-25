package com.rbac.model;

import java.sql.Timestamp;

/**
 * User — POJO model class matching the 'users' table.
 *
 * Columns (Architecture Doc §7 / PRD §7):
 *   user_id       INT, PK, AUTO_INCREMENT
 *   username      VARCHAR(50), UNIQUE
 *   email         VARCHAR(100), UNIQUE
 *   password_hash VARCHAR(255)
 *   created_date  TIMESTAMP, DEFAULT CURRENT_TIMESTAMP
 *   status        ENUM('active', 'suspended')
 *
 * Note: The 'role' field is not part of the users table — it comes from
 * the user_roles/roles join. It is included here as a transient convenience
 * field for display purposes (e.g., admin dashboard user list).
 */
public class User {

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private Timestamp createdDate;
    private String status;

    // Feature 5: Multi-Factor Authentication
    private String mfaSecret;
    private boolean mfaEnabled;

    // Account security fields (Feature 3)
    private int failedLoginAttempts;
    private Timestamp lastFailedLogin;
    private Timestamp passwordChangedAt;
    private Timestamp accountLockedUntil;

    // Transient: populated from JOIN with roles table, not a column in 'users'
    private String roleName;

    // Transient: populated from JOIN with user_profiles table
    private String firstName;
    private String lastName;

    public User() {
    }

    public User(int userId, String username, String email, String passwordHash,
                Timestamp createdDate, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdDate = createdDate;
        this.status = status;
    }

    // --- Getters and Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Timestamp getLastFailedLogin() {
        return lastFailedLogin;
    }

    public void setLastFailedLogin(Timestamp lastFailedLogin) {
        this.lastFailedLogin = lastFailedLogin;
    }

    public Timestamp getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Timestamp passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Timestamp getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(Timestamp accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

