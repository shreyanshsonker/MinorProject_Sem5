<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password — Secure RBAC System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <div class="auth-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                         stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                </div>
                <h1>Reset Password</h1>
                <p class="auth-subtitle">Create a new password for your account</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    ${error}
                </div>
                <%-- If it's a token error, don't show the form --%>
                <c:if test="${empty token}">
                    <div style="text-align: center; margin-top: 1rem;">
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline">Back to Login</a>
                    </div>
                </c:if>
            </c:if>

            <c:if test="${not empty token}">
                <form action="${pageContext.request.contextPath}/reset-password" method="post" class="auth-form">
                    <input type="hidden" name="token" value="${token}">

                    <div class="form-group">
                        <label for="newPassword">New Password</label>
                        <input type="password" id="newPassword" name="newPassword" required minlength="6">
                    </div>

                    <div class="form-group">
                        <label for="confirmNewPassword">Confirm New Password</label>
                        <input type="password" id="confirmNewPassword" name="confirmNewPassword" required minlength="6">
                    </div>

                    <button type="submit" class="btn btn-primary btn-full">Reset Password</button>
                </form>
            </c:if>
        </div>
    </div>
</body>
</html>
