<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Sessions — Secure RBAC System</title>
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

    <div class="dashboard-container">
        <div class="welcome-section">
            <h1>Active Sessions</h1>
            <p class="welcome-subtitle">Manage devices where you are currently logged in</p>
        </div>

        <c:if test="${not empty sessionScope.sessionMessage}">
            <div class="alert alert-success">
                ${sessionScope.sessionMessage}
            </div>
            <c:remove var="sessionMessage" scope="session"/>
        </c:if>

        <div class="table-card">
            <div class="table-header">
                <h2>My Devices</h2>
                <form action="${pageContext.request.contextPath}/sessions" method="post"
                      onsubmit="return confirm('Are you sure you want to log out of all other devices?');">
                    <input type="hidden" name="action" value="revokeAll">
                    <button type="submit" class="btn btn-danger btn-sm">Revoke All Other Sessions</button>
                </form>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>IP Address</th>
                            <th>User Agent (Browser/Device)</th>
                            <th>Login Time</th>
                            <th>Last Access Time</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="sess" items="${sessions}">
                            <tr style="${sess.sessionId == currentSessionId ? 'background-color: var(--primary-light);' : ''}">
                                <td>${sess.ipAddress}</td>
                                <td><small>${sess.userAgent}</small></td>
                                <td>${sess.loginTime}</td>
                                <td>${sess.lastAccessTime}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${sess.sessionId == currentSessionId}">
                                            <span class="badge badge-active">Current Session</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-info">Active</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${sess.sessionId != currentSessionId}">
                                        <form action="${pageContext.request.contextPath}/sessions" method="post"
                                              class="inline-form"
                                              onsubmit="return confirm('Revoke this session?');">
                                            <input type="hidden" name="action" value="revoke">
                                            <input type="hidden" name="sessionId" value="${sess.sessionId}">
                                            <button type="submit" class="btn btn-warning btn-xs">Revoke</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
