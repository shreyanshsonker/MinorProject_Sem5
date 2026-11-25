package com.rbac.util;

import com.rbac.dao.AuditLogDAO;
import com.rbac.model.AuditLog;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AuditLogger — Static utility for logging audit events.
 *
 * Feature 2: Audit Logging.
 * Provides a one-line method for servlets to log actions without
 * manually constructing AuditLog objects each time.
 */
public class AuditLogger {

    private static final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Logs an audit event.
     *
     * @param request      the HTTP request (used to extract IP address)
     * @param userId       the user performing the action (null for anonymous events)
     * @param actionType   the type of action (e.g., "LOGIN_SUCCESS", "DELETE_USER")
     * @param targetEntity the entity being acted upon (e.g., "USER", "ROLE")
     * @param targetId     the ID of the target entity (null if not applicable)
     * @param details      additional details/context (free-form text or JSON)
     */
    public static void log(HttpServletRequest request, Integer userId,
                           String actionType, String targetEntity,
                           Integer targetId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setActionType(actionType);
        auditLog.setTargetEntity(targetEntity);
        auditLog.setTargetId(targetId);
        auditLog.setIpAddress(getClientIp(request));
        auditLog.setDetails(details);

        auditLogDAO.insertLog(auditLog);
    }

    /**
     * Extracts the client IP address from the request.
     * Checks X-Forwarded-For header first (for reverse proxies),
     * then falls back to getRemoteAddr().
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs; the first is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Prevent instantiation — utility class
    private AuditLogger() {
    }
}
