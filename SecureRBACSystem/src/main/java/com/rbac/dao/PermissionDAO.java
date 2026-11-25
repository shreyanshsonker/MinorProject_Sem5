package com.rbac.dao;

import com.rbac.model.Permission;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PermissionDAO — Data Access Object for 'permissions' and 'role_permissions' tables.
 *
 * Feature 1: Fine-Grained Permissions.
 * All queries use PreparedStatement exclusively (AI Rules §A3).
 */
public class PermissionDAO {

    /**
     * Gets all permissions assigned to a specific role.
     *
     * @param roleId the role's ID
     * @return list of Permission objects for that role
     */
    public List<Permission> getPermissionsForRole(int roleId) {
        String sql = "SELECT p.permission_id, p.permission_name, p.description "
                + "FROM permissions p "
                + "JOIN role_permissions rp ON p.permission_id = rp.permission_id "
                + "WHERE rp.role_id = ? "
                + "ORDER BY p.permission_name";

        List<Permission> permissions = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(mapResultSetToPermission(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * Gets all permission names for a specific user (via their role).
     * Used at login time to populate the session with permission strings.
     *
     * @param userId the user's ID
     * @return list of permission name strings (e.g., "VIEW_USERS", "DELETE_USERS")
     */
    public List<String> getPermissionsForUser(int userId) {
        String sql = "WITH RECURSIVE RoleHierarchy AS ( "
                + "    SELECT r.role_id, r.parent_role_id "
                + "    FROM roles r "
                + "    JOIN user_roles ur ON r.role_id = ur.role_id "
                + "    WHERE ur.user_id = ? "
                + "    UNION ALL "
                + "    SELECT r.role_id, r.parent_role_id "
                + "    FROM roles r "
                + "    JOIN RoleHierarchy rh ON r.role_id = rh.parent_role_id "
                + ") "
                + "SELECT DISTINCT p.permission_name "
                + "FROM permissions p "
                + "JOIN role_permissions rp ON p.permission_id = rp.permission_id "
                + "JOIN RoleHierarchy rh ON rp.role_id = rh.role_id "
                + "ORDER BY p.permission_name";

        List<String> permissions = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * Checks if a user has a specific permission.
     *
     * @param userId         the user's ID
     * @param permissionName the exact permission name (e.g., "DELETE_USERS")
     * @return true if the user has the permission
     */
    public boolean hasPermission(int userId, String permissionName) {
        String sql = "WITH RECURSIVE RoleHierarchy AS ( "
                + "    SELECT r.role_id, r.parent_role_id "
                + "    FROM roles r "
                + "    JOIN user_roles ur ON r.role_id = ur.role_id "
                + "    WHERE ur.user_id = ? "
                + "    UNION ALL "
                + "    SELECT r.role_id, r.parent_role_id "
                + "    FROM roles r "
                + "    JOIN RoleHierarchy rh ON r.role_id = rh.parent_role_id "
                + ") "
                + "SELECT COUNT(*) FROM permissions p "
                + "JOIN role_permissions rp ON p.permission_id = rp.permission_id "
                + "JOIN RoleHierarchy rh ON rp.role_id = rh.role_id "
                + "WHERE p.permission_name = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, permissionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets all available permissions in the system.
     *
     * @return list of all Permission objects
     */
    public List<Permission> getAllPermissions() {
        String sql = "SELECT * FROM permissions ORDER BY permission_id";
        List<Permission> permissions = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * Gets permission IDs currently assigned to a role.
     *
     * @param roleId the role's ID
     * @return list of permission IDs
     */
    public List<Integer> getPermissionIdsForRole(int roleId) {
        String sql = "SELECT permission_id FROM role_permissions WHERE role_id = ?";
        List<Integer> ids = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("permission_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Sets the permissions for a role (replaces all existing mappings).
     * Uses a transaction: DELETE old + INSERT new.
     *
     * @param roleId        the role's ID
     * @param permissionIds list of permission IDs to assign
     * @return true if the operation succeeded
     */
    public boolean setRolePermissions(int roleId, List<Integer> permissionIds) {
        String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
        String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, roleId);
                deleteStmt.executeUpdate();
            }

            if (permissionIds != null && !permissionIds.isEmpty()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (int permId : permissionIds) {
                        insertStmt.setInt(1, roleId);
                        insertStmt.setInt(2, permId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a Permission object.
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setPermissionId(rs.getInt("permission_id"));
        p.setPermissionName(rs.getString("permission_name"));
        p.setDescription(rs.getString("description"));
        return p;
    }
}
