<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Access Denied — Secure RBAC System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card error-card">
            <div class="auth-header">
                <div class="auth-icon error-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                         stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                    </svg>
                </div>
                <h1>Access Denied</h1>
                <p class="auth-subtitle">You do not have permission to access this page.</p>
            </div>

            <div class="error-details">
                <p>Your current role (<strong>${sessionScope.role}</strong>) does not have
                   the required permissions for the requested resource.</p>
                <p>If you believe this is an error, please contact your system administrator.</p>
            </div>

            <div class="error-actions">
                <a href="${pageContext.request.contextPath}/user_home.jsp" class="btn btn-primary">
                    Go to Home
                </a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline">
                    Logout
                </a>
            </div>
        </div>
    </div>
</body>
</html>
