-- ============================================================
-- Secure Role-Based Access Management System
-- Database Migration v3 — MFA & Hierarchical Roles
-- Run this AFTER migration_v2.sql has been applied.
-- ============================================================

USE rbac_system;

-- ============================================================
-- Feature 7: Hierarchical Roles
-- ============================================================

-- Add parent_role_id to roles table to support hierarchy
ALTER TABLE roles
    ADD COLUMN parent_role_id INT DEFAULT NULL,
    ADD CONSTRAINT fk_parent_role FOREIGN KEY (parent_role_id) REFERENCES roles(role_id) ON DELETE SET NULL;

-- Create Super Admin role and set up basic hierarchy
-- 1 = Admin, 2 = Manager, 3 = User (from original seed)
INSERT INTO roles (role_name, parent_role_id) VALUES ('Super Admin', 1);

-- Update existing roles to form a chain:
-- Admin (1) inherits from Manager (2)
-- Manager (2) inherits from User (3)
UPDATE roles SET parent_role_id = 2 WHERE role_id = 1;
UPDATE roles SET parent_role_id = 3 WHERE role_id = 2;


-- ============================================================
-- Feature 5: Multi-Factor Authentication (MFA)
-- ============================================================

-- Add MFA columns to users table
ALTER TABLE users
    ADD COLUMN mfa_secret  VARCHAR(64)  DEFAULT NULL,
    ADD COLUMN mfa_enabled BOOLEAN      NOT NULL DEFAULT FALSE;
