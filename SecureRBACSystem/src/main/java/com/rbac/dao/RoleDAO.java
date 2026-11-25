package com.rbac.dao;

import com.rbac.model.Role;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RoleDAO — Data Access Object for the 'roles' and 'user_roles' tables.
 *
 * Architecture Doc §4: "Only DAO classes are allowed to contain SQL."
 * AI Rules §A3: "All queries must use PreparedStatement."
 *
 * This class handles:
 *   - Fetching a user's assigned role (for login session setup)
 *   - Listing all available roles (for admin dashboard dropdown)
 *   - Assigning a role to a user (registration / admin action)
 *   - Updating a user's role (admin action — PRD FR-9, FR-16)
 */
public class RoleDAO {

    /**
     * Gets the role name assigned to a specific user.
     * Used by: LoginServlet (Architecture Doc §5 — Login Sequence Diagram)
     *
     * SQL matches Architecture Doc §5:
     *   "SELECT role_name FROM roles JOIN user_roles ..."
     *
     * @param userId the user's ID
     * @return the role name (e.g., "Admin", "Manager", "User"), or null if not found
     */
    public String getRoleForUser(int userId) {
        String sql = "SELECT r.role_name FROM roles r "
                + "JOIN user_roles ur ON r.role_id = ur.role_id "
                + "WHERE ur.user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all available roles from the database.
     * Used by: AdminManagementServlet (dropdown for role assignment — PRD FR-16)
     *
     * @return list of all Role objects
     */
    public List<Role> getAllRoles() {
        String sql = "SELECT * FROM roles ORDER BY role_id";
        List<Role> roles = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Gets a role by its name.
     * Used by: RegisterServlet (to find the "User" role ID for default assignment)
     *
     * Per AI Rules §B1: Role names must exactly match what's stored in the DB.
     *
     * @param roleName the exact role name (case-sensitive)
     * @return the Role object, or null if not found
     */
    public Role getRoleByName(String roleName) {
        String sql = "SELECT * FROM roles WHERE role_name = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roleName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    role.setRoleName(rs.getString("role_name"));
                    return role;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Assigns a role to a user (inserts into user_roles).
     * Used by: RegisterServlet (default role), AdminManagementServlet
     *
     * @param userId the user's ID
     * @param roleId the role's ID
     * @return true if insertion succeeded
     */
    public boolean assignRole(int userId, int roleId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, roleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates a user's role (changes the role_id in user_roles).
     * Used by: AdminManagementServlet (PRD FR-9, FR-16)
     *
     * Per Architecture Doc §7 normalization note:
     *   "Changing a user's permissions becomes an UPDATE on user_roles,
     *    never a code change or redeploy."
     *
     * @param userId the user's ID
     * @param roleId the new role's ID
     * @return true if update succeeded
     */
    public boolean updateRole(int userId, int roleId) {
        String sql = "UPDATE user_roles SET role_id = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
