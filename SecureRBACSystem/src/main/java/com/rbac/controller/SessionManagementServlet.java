package com.rbac.controller;

import com.rbac.dao.SessionDAO;
import com.rbac.model.UserSession;
import com.rbac.util.AuditLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * SessionManagementServlet — Controller for viewing and revoking user sessions.
 *
 * URL: /sessions
 * Feature 4: Session Management.
 *
 * Allows any authenticated user to view their active sessions
 * and revoke individual or all other sessions.
 */
@WebServlet("/sessions")
public class SessionManagementServlet extends HttpServlet {

    private final SessionDAO sessionDAO = new SessionDAO();

    /**
     * GET /sessions — Display the current user's active sessions.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String currentSessionId = session.getId();

        List<UserSession> sessions = sessionDAO.getActiveSessionsForUser(userId);

        request.setAttribute("sessions", sessions);
        request.setAttribute("currentSessionId", currentSessionId);

        request.getRequestDispatcher("/sessions.jsp").forward(request, response);
    }

    /**
     * POST /sessions — Handle session revocation actions.
     *
     * Actions (via 'action' parameter):
     *   - revoke:    Revoke a specific session (requires 'sessionId' param)
     *   - revokeAll: Revoke all sessions except the current one
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");
        String currentSessionId = session.getId();
        String action = request.getParameter("action");

        String message = null;

        if ("revoke".equals(action)) {
            String targetSessionId = request.getParameter("sessionId");
            if (targetSessionId != null && !targetSessionId.equals(currentSessionId)) {
                sessionDAO.deactivateSession(targetSessionId);
                message = "Session revoked successfully.";
                AuditLogger.log(request, userId, "REVOKE_SESSION",
                        "SESSION", null, "Revoked session: " + targetSessionId);
            } else {
                message = "Cannot revoke the current session.";
            }
        } else if ("revokeAll".equals(action)) {
            int count = sessionDAO.deactivateAllSessionsForUser(userId, currentSessionId);
            message = count + " other session(s) revoked.";
            AuditLogger.log(request, userId, "REVOKE_ALL_SESSIONS",
                    "SESSION", null, "Revoked " + count + " sessions");
        }

        if (message != null) {
            request.getSession().setAttribute("sessionMessage", message);
        }
        response.sendRedirect(request.getContextPath() + "/sessions");
    }
}
