-- ============================================================
-- Secure Role-Based Access Management System
-- Database Schema — matches Architecture Document §7 (ER Diagram)
-- MySQL syntax only (per AI Rules §3)
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS rbac_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE rbac_system;

-- ============================================================
-- Table: users
-- Columns per Architecture Doc §7:
--   user_id (PK, AUTO_INCREMENT)
--   username (VARCHAR, UNIQUE)
--   email (VARCHAR, UNIQUE)
--   password_hash (VARCHAR)
--   created_date (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
--   status (ENUM: 'active', 'suspended')
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT            PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)    NOT NULL UNIQUE,
    email         VARCHAR(100)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)   NOT NULL,
    created_date  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        ENUM('active', 'suspended') NOT NULL DEFAULT 'active'
) ENGINE=InnoDB;

-- ============================================================
-- Table: roles
-- Columns per Architecture Doc §7:
--   role_id (PK, AUTO_INCREMENT)
--   role_name (VARCHAR(30))
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    role_id   INT          PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(30)  NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ============================================================
-- Table: user_roles
-- Mapping table per Architecture Doc §7:
--   user_id (FK → users.user_id)
--   role_id (FK → roles.role_id)
-- One row per user = single-role-per-user (PRD FR-6)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Seed Data: Default Roles (PRD §2 — Admin, Manager, User)
-- ============================================================
INSERT INTO roles (role_name) VALUES ('Admin');
INSERT INTO roles (role_name) VALUES ('Manager');
INSERT INTO roles (role_name) VALUES ('User');

-- ============================================================
-- Seed Data: Default Admin Account
-- Username: admin
-- Password: admin123 (BCrypt hashed)
-- BCrypt hash for 'admin123': $2a$10$vVxx/jUBRkM.GHzzvEMKaOtZb26lZ0BviDOpy7/LYR/XN370hl8wi
-- IMPORTANT: Change this password immediately after first login!
-- ============================================================
INSERT INTO users (username, email, password_hash, status)
VALUES ('admin', 'admin@rbac.local', '$2a$10$vVxx/jUBRkM.GHzzvEMKaOtZb26lZ0BviDOpy7/LYR/XN370hl8wi', 'active');

-- Assign Admin role to the default admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
