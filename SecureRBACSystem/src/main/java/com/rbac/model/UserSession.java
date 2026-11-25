package com.rbac.model;

import java.sql.Timestamp;

/**
 * UserSession — POJO model class matching the 'user_sessions' table.
 *
 * Columns:
 *   session_id  VARCHAR(128), PK
 *   user_id     INT, FK → users
 *   ip_address  VARCHAR(45)
 *   user_agent  VARCHAR(512)
 *   login_time  TIMESTAMP
 *   last_active TIMESTAMP
 *   is_active   BOOLEAN
 */
public class UserSession {

    private String sessionId;
    private int userId;
    private String ipAddress;
    private String userAgent;
    private Timestamp loginTime;
    private Timestamp lastActive;
    private boolean active;

    public UserSession() {
    }

    // --- Getters and Setters ---

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public Timestamp getLastActive() {
        return lastActive;
    }

    public void setLastActive(Timestamp lastActive) {
        this.lastActive = lastActive;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
