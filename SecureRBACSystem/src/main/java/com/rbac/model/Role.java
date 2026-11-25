package com.rbac.model;

/**
 * Role — POJO model class matching the 'roles' table.
 *
 * Columns (Architecture Doc §7 / PRD §7):
 *   role_id   INT, PK, AUTO_INCREMENT
 *   role_name VARCHAR(30)
 *
 * Per AI Rules §B1: Role names in code must exactly match role names
 * stored in the roles table (case-sensitive).
 */
public class Role {

    private int roleId;
    private String roleName;
    private Integer parentRoleId; // Feature 7: Hierarchical Roles (can be null)

    public Role() {
    }

    public Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // --- Getters and Setters ---

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getParentRoleId() {
        return parentRoleId;
    }

    public void setParentRoleId(Integer parentRoleId) {
        this.parentRoleId = parentRoleId;
    }
}
