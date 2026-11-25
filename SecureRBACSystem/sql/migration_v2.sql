-- ============================================================
-- Secure Role-Based Access Management System
-- Database Migration v2 — New Features
-- Run this AFTER the original schema.sql has been applied.
-- ============================================================

USE rbac_system;

-- ============================================================
-- Feature 1: Fine-Grained Permissions
-- ============================================================

-- Table: permissions
CREATE TABLE IF NOT EXISTS permissions (
    permission_id   INT            PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50)    NOT NULL UNIQUE,
    description     VARCHAR(255)   DEFAULT NULL
) ENGINE=InnoDB;

-- Table: role_permissions (mapping table)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (permission_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Seed permissions
INSERT INTO permissions (permission_name, description) VALUES
    ('VIEW_USERS',       'View the list of all users'),
    ('CREATE_USERS',     'Create new user accounts'),
    ('UPDATE_USERS',     'Update existing user accounts'),
    ('DELETE_USERS',     'Delete user accounts'),
    ('VIEW_ROLES',       'View available roles'),
    ('MANAGE_ROLES',     'Create, update, or delete roles'),
    ('VIEW_DASHBOARD',   'Access the admin dashboard'),
    ('VIEW_AUDIT_LOGS',  'View system audit logs'),
    ('MANAGE_SESSIONS',  'View and revoke user sessions');

-- Seed role → permission mappings
-- Admin (role_id=1) gets ALL permissions
INSERT INTO role_permissions (role_id, permission_id)
    SELECT 1, permission_id FROM permissions;

-- Manager (role_id=2) gets: VIEW_USERS, VIEW_ROLES, VIEW_DASHBOARD, UPDATE_USERS, MANAGE_SESSIONS
INSERT INTO role_permissions (role_id, permission_id)
    SELECT 2, permission_id FROM permissions
    WHERE permission_name IN ('VIEW_USERS', 'VIEW_ROLES', 'VIEW_DASHBOARD', 'UPDATE_USERS', 'MANAGE_SESSIONS');

-- User (role_id=3) gets: VIEW_ROLES only
INSERT INTO role_permissions (role_id, permission_id)
    SELECT 3, permission_id FROM permissions
    WHERE permission_name IN ('VIEW_ROLES');


-- ============================================================
-- Feature 2: Audit Logging
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    log_id        INT            PRIMARY KEY AUTO_INCREMENT,
    user_id       INT            DEFAULT NULL,
    action_type   VARCHAR(50)    NOT NULL,
    target_entity VARCHAR(50)    DEFAULT NULL,
    target_id     INT            DEFAULT NULL,
    ip_address    VARCHAR(45)    DEFAULT NULL,
    details       TEXT           DEFAULT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Index for querying logs by time (most recent first)
CREATE INDEX idx_audit_created_at ON audit_logs (created_at DESC);
-- Index for querying logs by user
CREATE INDEX idx_audit_user_id ON audit_logs (user_id);


-- ============================================================
-- Feature 3: Account Security & Recovery
-- ============================================================

-- Add lockout and password expiration columns to users table
ALTER TABLE users
    ADD COLUMN failed_login_attempts INT          NOT NULL DEFAULT 0,
    ADD COLUMN last_failed_login     TIMESTAMP    NULL DEFAULT NULL,
    ADD COLUMN password_changed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN account_locked_until  TIMESTAMP    NULL DEFAULT NULL;

-- Table: password_reset_tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id   INT            PRIMARY KEY AUTO_INCREMENT,
    user_id    INT            NOT NULL,
    token      VARCHAR(64)    NOT NULL UNIQUE,
    expires_at TIMESTAMP      NOT NULL,
    used       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index for token lookup
CREATE INDEX idx_prt_token ON password_reset_tokens (token);


-- ============================================================
-- Feature 4: Session Management
-- ============================================================

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id  VARCHAR(128)   PRIMARY KEY,
    user_id     INT            NOT NULL,
    ip_address  VARCHAR(45)    DEFAULT NULL,
    user_agent  VARCHAR(512)   DEFAULT NULL,
    login_time  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index for finding active sessions per user
CREATE INDEX idx_session_user_active ON user_sessions (user_id, is_active);


-- ============================================================
-- Feature 6: Enhanced User Profiles
-- ============================================================

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id             INT            PRIMARY KEY,
    first_name          VARCHAR(50)    DEFAULT NULL,
    last_name           VARCHAR(50)    DEFAULT NULL,
    phone_number        VARCHAR(20)    DEFAULT NULL,
    profile_picture_url VARCHAR(500)   DEFAULT NULL,
    last_login_date     TIMESTAMP      NULL DEFAULT NULL,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Create a default profile for the existing admin user
INSERT INTO user_profiles (user_id, first_name, last_name)
    VALUES (1, 'System', 'Admin');
