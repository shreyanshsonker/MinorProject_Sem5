<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Secure RBAC - Verify MFA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-container">
    <div class="auth-card">
        <div class="auth-header">
            <h1>Two-Factor Authentication</h1>
            <p class="auth-subtitle">Enter the 6-digit code from your authenticator app.</p>
        </div>

        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-error">
                <strong>Error:</strong> ${requestScope.error}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/mfa-verify" method="post" class="auth-form">
            <div class="form-group">
                <label for="mfaCode">Authentication Code</label>
                <input type="text" id="mfaCode" name="mfaCode" placeholder="000000" 
                       pattern="[0-9]{6}" maxlength="6" autocomplete="off" required autofocus>
            </div>

            <button type="submit" class="btn btn-primary btn-full">Verify & Login</button>
        </form>

        <div class="auth-footer text-center mt-4">
            <a href="${pageContext.request.contextPath}/logout">Cancel and Logout</a>
        </div>
    </div>
</div>
</body>
</html>
