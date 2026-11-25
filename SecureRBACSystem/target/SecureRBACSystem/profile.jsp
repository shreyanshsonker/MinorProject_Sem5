<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile — Secure RBAC System</title>
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
            <span class="nav-role badge badge-user">${sessionScope.role}</span>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-outline btn-sm">Dashboard</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
        </div>
    </nav>

    <div class="dashboard-container" style="max-width: 600px;">
        <div class="welcome-section">
            <h1>My Profile</h1>
            <p class="welcome-subtitle">Update your personal information</p>
        </div>

        <c:if test="${not empty sessionScope.profileMessage}">
            <div class="alert alert-success">
                ${sessionScope.profileMessage}
            </div>
            <c:remove var="profileMessage" scope="session"/>
        </c:if>

        <div class="table-card" style="padding: 1.5rem;">
            <form action="${pageContext.request.contextPath}/profile" method="post" class="auth-form">
                <div class="form-group">
                    <label for="firstName">First Name</label>
                    <input type="text" id="firstName" name="firstName" value="${profile.firstName}">
                </div>

                <div class="form-group">
                    <label for="lastName">Last Name</label>
                    <input type="text" id="lastName" name="lastName" value="${profile.lastName}">
                </div>

                <div class="form-group">
                    <label for="phoneNumber">Phone Number</label>
                    <input type="text" id="phoneNumber" name="phoneNumber" value="${profile.phoneNumber}">
                </div>

                <div class="form-group" style="margin-top: 1rem;">
                    <label>Last Login</label>
                    <p style="color: var(--text-secondary); font-size: 0.875rem;">
                        ${profile.lastLoginDate != null ? profile.lastLoginDate : 'N/A'}
                    </p>
                </div>

                <div class="flex gap-4" style="margin-top: 1rem;">
                    <button type="submit" class="btn btn-primary" style="flex: 1;">Save Profile</button>
                    <a href="${pageContext.request.contextPath}/mfa-setup" class="btn btn-outline" style="flex: 1; text-align: center;">Setup MFA</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
