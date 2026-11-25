<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Change Password — Secure RBAC System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
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
            <c:if test="${empty sessionScope.passwordExpired or not sessionScope.passwordExpired}">
                <span class="nav-role badge badge-user">${sessionScope.role}</span>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-outline btn-sm">Dashboard</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
        </div>
    </nav>

    <div class="dashboard-container" style="max-width: 500px;">
        <div class="welcome-section">
            <h1>Change Password</h1>
            <p class="welcome-subtitle">Ensure your account is secure</p>
        </div>

        <c:if test="${not empty warning}">
            <div class="alert alert-warning" style="background: var(--warning-light); color: var(--warning); border: 1px solid var(--warning);">
                ${warning}
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-error">
                ${error}
            </div>
        </c:if>

        <div class="table-card" style="padding: 1.5rem;">
            <form action="${pageContext.request.contextPath}/change-password" method="post" class="auth-form">
                <div class="form-group">
                    <label for="currentPassword">Current Password</label>
                    <input type="password" id="currentPassword" name="currentPassword" required>
                </div>

                <div class="form-group">
                    <label for="newPassword">New Password</label>
                    <input type="password" id="newPassword" name="newPassword" required minlength="6">
                    <span class="form-hint">Must be at least 6 characters long.</span>
                </div>

                <div class="form-group">
                    <label for="confirmNewPassword">Confirm New Password</label>
                    <input type="password" id="confirmNewPassword" name="confirmNewPassword" required minlength="6">
                </div>

                <button type="submit" class="btn btn-primary" style="margin-top: 1rem;">Update Password</button>
            </form>
        </div>
    </div>
</body>
</html>
