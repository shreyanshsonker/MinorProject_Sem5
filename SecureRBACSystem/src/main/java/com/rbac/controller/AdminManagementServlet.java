package com.rbac.controller;

import com.rbac.dao.PasswordResetDAO;
import com.rbac.dao.PermissionDAO;
import com.rbac.dao.RoleDAO;
import com.rbac.dao.UserDAO;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.util.AuditLogger;
import com.rbac.util.TokenGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * AdminManagementServlet — Controller for admin dashboard CRUD operations.
 *
 * URL: /admin
 * Architecture Doc §4: Handles CRUD operations on users/roles
 *     triggered from the admin dashboard.
 *
 * PRD FR-14: Admin shall view a list of all registered users with roles/status.
 * PRD FR-15: Admin shall be able to create, update, delete (or suspend) user accounts.
 * PRD FR-16: Admin shall reassign a user's role via dropdown/form, not raw DB access.
 * PRD FR-17: Non-admin users shall not see or reach dashboard management controls.
 *     (Enforced by AuthenticationFilter — not duplicated here per AI Rules §B2)
 *
 * Updated for v2:
 *   - Feature 2: Audit logging on every admin action
 *   - Feature 3: New actions — resetPassword, unlockAccount
 *
 * Depends On: UserDAO, RoleDAO, PasswordResetDAO, AuditLogger (Architecture Doc §4)
 */
@WebServlet("/admin")
public class AdminManagementServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final PasswordResetDAO passwordResetDAO = new PasswordResetDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();

    /**
     * GET /admin — Display the admin dashboard with all users, roles, and permissions.
     * (PRD FR-14)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Load all users with their roles and profile data
        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);

        // Load all available roles
        List<Role> roles = roleDAO.getAllRoles();
        request.setAttribute("roles", roles);

        // Load all permissions and map them to roles (Feature 1)
        List<com.rbac.model.Permission> allPermissions = permissionDAO.getAllPermissions();
        request.setAttribute("allPermissions", allPermissions);

        java.util.Map<Integer, List<Integer>> rolePermissionsMap = new java.util.HashMap<>();
        for (Role r : roles) {
            rolePermissionsMap.put(r.getRoleId(), permissionDAO.getPermissionIdsForRole(r.getRoleId()));
        }
        request.setAttribute("rolePermissionsMap", rolePermissionsMap);

        // Forward to admin dashboard JSP
        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }

    /**
     * POST /admin — Handle CRUD actions on users.
     *
     * Actions (via 'action' parameter):
     *   - updateRole:     Change a user's role (PRD FR-9, FR-16)
     *   - suspend:        Set user status to 'suspended' (PRD FR-15)
     *   - activate:       Set user status to 'active' (PRD FR-15)
     *   - delete:         Remove user account (PRD FR-15)
     *   - resetPassword:  Generate a password reset token (Feature 3)
     *   - unlockAccount:  Clear account lockout (Feature 3)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");

        if (action == null || userIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        Integer adminUserId = (Integer) request.getSession().getAttribute("userId");
        String message = null;

        switch (action) {
            case "updateRole":
                String roleIdStr = request.getParameter("roleId");
                if (roleIdStr != null) {
                    try {
                        int roleId = Integer.parseInt(roleIdStr);
                        if (roleDAO.updateRole(userId, roleId)) {
                            message = "Role updated successfully.";
                            AuditLogger.log(request, adminUserId, "UPDATE_ROLE",
                                    "USER", userId, "New role_id: " + roleId);
                        } else {
                            message = "Failed to update role.";
                        }
                    } catch (NumberFormatException e) {
                        message = "Invalid role selection.";
                    }
                }
                break;

            case "suspend":
                if (userDAO.updateUserStatus(userId, "suspended")) {
                    message = "User suspended successfully.";
                    AuditLogger.log(request, adminUserId, "SUSPEND_USER",
                            "USER", userId, null);
                } else {
                    message = "Failed to suspend user.";
                }
                break;

            case "activate":
                if (userDAO.updateUserStatus(userId, "active")) {
                    message = "User activated successfully.";
                    AuditLogger.log(request, adminUserId, "ACTIVATE_USER",
                            "USER", userId, null);
                } else {
                    message = "Failed to activate user.";
                }
                break;

            case "delete":
                if (userDAO.deleteUser(userId)) {
                    message = "User deleted successfully.";
                    AuditLogger.log(request, adminUserId, "DELETE_USER",
                            "USER", userId, null);
                } else {
                    message = "Failed to delete user.";
                }
                break;

            case "resetPassword":
                // Generate a password reset token (Feature 3)
                String token = TokenGenerator.generateToken();
                Timestamp expiresAt = new Timestamp(
                        System.currentTimeMillis() + (60 * 60 * 1000)); // 1 hour
                if (passwordResetDAO.createToken(userId, token, expiresAt)) {
                    String resetLink = request.getContextPath() + "/reset-password?token=" + token;
                    message = "Password reset link generated: " + resetLink;
                    AuditLogger.log(request, adminUserId, "GENERATE_RESET_TOKEN",
                            "USER", userId, null);
                } else {
                    message = "Failed to generate reset token.";
                }
                break;

            case "unlockAccount":
                // Clear account lockout (Feature 3)
                if (userDAO.unlockAccount(userId)) {
                    message = "Account unlocked successfully.";
                    AuditLogger.log(request, adminUserId, "UNLOCK_ACCOUNT",
                            "USER", userId, null);
                } else {
                    message = "Failed to unlock account.";
                }
                break;

            case "updateRolePermissions":
                // Handle updating permissions for a specific role (Feature 1)
                try {
                    int targetRoleId = Integer.parseInt(request.getParameter("targetRoleId"));
                    String[] permIdsStr = request.getParameterValues("permissionIds");
                    
                    List<Integer> newPermIds = new java.util.ArrayList<>();
                    if (permIdsStr != null) {
                        for (String pId : permIdsStr) {
                            newPermIds.add(Integer.parseInt(pId));
                        }
                    }
                    
                    if (permissionDAO.setRolePermissions(targetRoleId, newPermIds)) {
                        message = "Role permissions updated successfully.";
                        AuditLogger.log(request, adminUserId, "UPDATE_ROLE_PERMISSIONS",
                                "ROLE", targetRoleId, "Updated permissions");
                    } else {
                        message = "Failed to update role permissions.";
                    }
                } catch (NumberFormatException e) {
                    message = "Invalid input for role permissions.";
                }
                break;

            default:
                message = "Unknown action.";
                break;
        }

        // Redirect back to admin dashboard (PRG pattern to prevent form resubmission)
        if (message != null) {
            request.getSession().setAttribute("adminMessage", message);
        }
        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
