package com.rbac.controller;

import com.rbac.dao.AuditLogDAO;
import com.rbac.model.AuditLog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * AuditLogServlet — Controller for viewing audit logs.
 *
 * URL: /audit-logs
 * Feature 2: Audit Logging.
 *
 * Admin-only access (enforced by AuthenticationFilter).
 * Displays paginated audit log entries.
 */
@WebServlet("/audit-logs")
public class AuditLogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 25;
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * GET /audit-logs — Display paginated audit logs.
     *
     * Query params:
     *   - page (optional, default=1): the page number
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Parse page number
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr));
            } catch (NumberFormatException ignored) {
            }
        }

        int offset = (page - 1) * PAGE_SIZE;

        // Fetch logs and total count
        List<AuditLog> logs = auditLogDAO.getAllLogs(PAGE_SIZE, offset);
        int totalLogs = auditLogDAO.getTotalLogCount();
        int totalPages = (int) Math.ceil((double) totalLogs / PAGE_SIZE);

        request.setAttribute("logs", logs);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalLogs", totalLogs);

        request.getRequestDispatcher("/audit_logs.jsp").forward(request, response);
    }
}
