<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Audit Logs — Secure RBAC System</title>
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
            <span class="nav-role badge badge-admin">${sessionScope.role}</span>
            <a href="${pageContext.request.contextPath}/admin" class="btn btn-outline btn-sm">Dashboard</a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
        </div>
    </nav>

    <div class="dashboard-container">
        <div class="welcome-section">
            <h1>Audit Logs</h1>
            <p class="welcome-subtitle">System-wide security events and user activities</p>
        </div>

        <div class="table-card">
            <div class="table-header">
                <h2>Security Events</h2>
                <span class="badge badge-info">${totalLogs} total records</span>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Log ID</th>
                            <th>User ID</th>
                            <th>Action Type</th>
                            <th>Target Entity</th>
                            <th>Target ID</th>
                            <th>IP Address</th>
                            <th>Timestamp</th>
                            <th>Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td>${log.logId}</td>
                                <td>${log.userId != null ? log.userId : 'System/Anon'}</td>
                                <td><span class="badge badge-info">${log.actionType}</span></td>
                                <td>${log.targetEntity != null ? log.targetEntity : '-'}</td>
                                <td>${log.targetId != null ? log.targetId : '-'}</td>
                                <td>${log.ipAddress != null ? log.ipAddress : '-'}</td>
                                <td>${log.timestamp}</td>
                                <td>${log.details != null ? log.details : '-'}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty logs}">
                            <tr>
                                <td colspan="8" class="empty-state">No audit logs found.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <%-- Pagination Controls --%>
            <c:if test="${totalPages > 1}">
                <div style="padding: 1rem; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border);">
                    <div style="font-size: 0.875rem; color: var(--text-muted);">
                        Page ${currentPage} of ${totalPages}
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <c:if test="${currentPage > 1}">
                            <a href="?page=${currentPage - 1}" class="btn btn-outline btn-sm">Previous</a>
                        </c:if>
                        <c:if test="${currentPage < totalPages}">
                            <a href="?page=${currentPage + 1}" class="btn btn-outline btn-sm">Next</a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>
