<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home — Secure RBAC System</title>
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
            <span class="nav-role badge badge-user">${sessionScope.role}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
        </div>
    </nav>

    <%-- Main Content --%>
    <div class="dashboard-container">
        <div class="welcome-section">
            <h1>Welcome, ${sessionScope.username}!</h1>
            <p class="welcome-subtitle">You are logged in as a <strong>${sessionScope.role}</strong>.</p>
        </div>

        <div class="card-grid">
            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                         stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                    </svg>
                </div>
                <h3>My Profile</h3>
                <p>Username: <strong>${sessionScope.username}</strong></p>
                <p>Role: <strong>${sessionScope.role}</strong></p>
                <div style="margin-top: 1rem; display: flex; gap: 0.5rem;">
                    <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline btn-sm">Edit Profile</a>
                    <a href="${pageContext.request.contextPath}/change-password" class="btn btn-outline btn-sm">Change Password</a>
                </div>
            </div>

            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                         stroke-linejoin="round">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    </svg>
                </div>
                <h3>Access Level</h3>
                <p>Your current role grants you <strong>${sessionScope.role}</strong>-level access to the system.</p>
                <div style="margin-top: 1rem;">
                    <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 0.5rem;">Permissions:</p>
                    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                        <c:forEach var="perm" items="${sessionScope.permissions}">
                            <span class="badge badge-info">${perm}</span>
                        </c:forEach>
                        <c:if test="${empty sessionScope.permissions}">
                            <span class="badge" style="background: var(--border); color: var(--text-secondary);">None</span>
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="info-card">
                <div class="info-card-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                         stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <polyline points="12 6 12 12 16 14"/>
                    </svg>
                </div>
                <h3>Session Info</h3>
                <p>Session is active and secure.</p>
                <p>Your session will expire after 30 minutes of inactivity.</p>
                <div style="margin-top: 1rem;">
                    <a href="${pageContext.request.contextPath}/sessions" class="btn btn-outline btn-sm">Manage Sessions</a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
