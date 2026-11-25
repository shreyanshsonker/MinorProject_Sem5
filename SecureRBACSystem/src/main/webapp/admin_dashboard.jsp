<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — Secure RBAC System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <%-- Navigation Bar --%>
    <nav class="navbar">
        <div class="nav-brand">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                 stroke-linejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <span>RBAC System</span>
        </div>
        <div class="nav-user">
            <span class="nav-username">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                     stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
                ${sessionScope.username}
            </span>
            <span class="nav-role badge badge-admin">${sessionScope.role}</span>
            <a href="${pageContext.request.contextPath}/audit-logs" class="btn btn-outline btn-sm">Audit Logs</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
        </div>
    </nav>

    <%-- Main Content --%>
    <div class="dashboard-container">
        <div class="welcome-section">
            <h1>Admin Dashboard</h1>
            <p class="welcome-subtitle">Manage users, roles, and system access</p>
        </div>

        <%-- Action result messages (from POST redirect) --%>
        <c:if test="${not empty sessionScope.adminMessage}">
            <div class="alert alert-success">
                ${sessionScope.adminMessage}
            </div>
            <c:remove var="adminMessage" scope="session"/>
        </c:if>

        <%-- User Management Table (PRD FR-14) --%>
        <div class="table-card">
            <div class="table-header">
                <h2>User Management</h2>
                <span class="badge badge-info">${users.size()} users</span>
            </div>

            <div class="table-responsive">
                <table class="data-table" id="usersTable">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Locked?</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td>${user.userId}</td>
                                <td>
                                    <strong>${user.username}</strong>
                                    <br>
                                    <small class="form-hint">${user.firstName} ${user.lastName}</small>
                                </td>
                                <td>${user.email}</td>
                                <td>
                                    <%-- Role change dropdown (PRD FR-16) --%>
                                    <form action="${pageContext.request.contextPath}/admin" method="post"
                                          class="inline-form">
                                        <input type="hidden" name="action" value="updateRole">
                                        <input type="hidden" name="userId" value="${user.userId}">
                                        <select name="roleId" class="select-sm"
                                                onchange="this.form.submit()">
                                            <c:forEach var="role" items="${roles}">
                                                <option value="${role.roleId}"
                                                    ${role.roleName == user.roleName ? 'selected' : ''}>
                                                    ${role.roleName}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </form>
                                </td>
                                <td>
                                    <span class="badge ${user.status == 'active' ? 'badge-active' : 'badge-suspended'}">
                                        ${user.status}
                                    </span>
                                </td>
                                <td>
                                    <c:if test="${not empty user.accountLockedUntil and user.accountLockedUntil.time > System.currentTimeMillis()}">
                                        <span class="badge badge-suspended">Locked</span>
                                    </c:if>
                                </td>
                                <td>${user.createdDate}</td>
                                <td class="actions-cell">
                                    <%-- Suspend/Activate toggle (PRD FR-15) --%>
                                    <c:choose>
                                        <c:when test="${user.status == 'active'}">
                                            <form action="${pageContext.request.contextPath}/admin" method="post"
                                                  class="inline-form">
                                                <input type="hidden" name="action" value="suspend">
                                                <input type="hidden" name="userId" value="${user.userId}">
                                                <button type="submit" class="btn btn-warning btn-xs"
                                                        title="Suspend user">Suspend</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/admin" method="post"
                                                  class="inline-form">
                                                <input type="hidden" name="action" value="activate">
                                                <input type="hidden" name="userId" value="${user.userId}">
                                                <button type="submit" class="btn btn-success btn-xs"
                                                        title="Activate user">Activate</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>

                                    <%-- Reset Password (Feature 3) --%>
                                    <form action="${pageContext.request.contextPath}/admin" method="post"
                                          class="inline-form">
                                        <input type="hidden" name="action" value="resetPassword">
                                        <input type="hidden" name="userId" value="${user.userId}">
                                        <button type="submit" class="btn btn-primary btn-xs"
                                                title="Generate Reset Link">Reset Pwd</button>
                                    </form>

                                    <%-- Unlock Account (Feature 3) --%>
                                    <c:if test="${not empty user.accountLockedUntil and user.accountLockedUntil.time > System.currentTimeMillis()}">
                                        <form action="${pageContext.request.contextPath}/admin" method="post"
                                              class="inline-form">
                                            <input type="hidden" name="action" value="unlockAccount">
                                            <input type="hidden" name="userId" value="${user.userId}">
                                            <button type="submit" class="btn btn-success btn-xs"
                                                    title="Unlock Account">Unlock</button>
                                        </form>
                                    </c:if>

                                    <%-- Delete button (PRD FR-15) --%>
                                    <form action="${pageContext.request.contextPath}/admin" method="post"
                                          class="inline-form"
                                          onsubmit="return confirm('Are you sure you want to delete this user?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="userId" value="${user.userId}">
                                        <button type="submit" class="btn btn-danger btn-xs"
                                                title="Delete user">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty users}">
                            <tr>
                                <td colspan="8" class="empty-state">No users found.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
        <%-- Role Permissions Management (Feature 1) --%>
        <div class="table-card" style="margin-top: 2rem;">
            <div class="table-header">
                <h2>Role Permissions</h2>
                <span class="badge badge-admin">Configuration</span>
            </div>
            <div style="padding: 1.5rem;">
                <div class="card-grid">
                    <c:forEach var="role" items="${roles}">
                        <div class="info-card">
                            <h3>${role.roleName}</h3>
                            <form action="${pageContext.request.contextPath}/admin" method="post" style="margin-top: 1rem;">
                                <input type="hidden" name="action" value="updateRolePermissions">
                                <input type="hidden" name="userId" value="0"> <%-- placeholder --%>
                                <input type="hidden" name="targetRoleId" value="${role.roleId}">
                                
                                <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1rem;">
                                    <c:forEach var="perm" items="${allPermissions}">
                                        <label style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.875rem; cursor: pointer;">
                                            <input type="checkbox" name="permissionIds" value="${perm.permissionId}"
                                                <c:if test="${rolePermissionsMap[role.roleId].contains(perm.permissionId)}">checked</c:if>>
                                            ${perm.permissionName}
                                        </label>
                                    </c:forEach>
                                </div>
                                <button type="submit" class="btn btn-primary btn-sm">Save Permissions</button>
                            </form>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
