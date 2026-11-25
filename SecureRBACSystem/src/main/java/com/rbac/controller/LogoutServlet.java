package com.rbac.controller;

import com.rbac.dao.SessionDAO;
import com.rbac.util.AuditLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet — Controller for session termination.
 *
 * URL: /logout
 * Architecture Doc §4: Invalidates the HTTP session completely.
 *
 * PRD FR-12: On logout, the session shall be fully invalidated server-side.
 * PRD FR-13: After logout, pressing "Back" shall not reveal cached content.
 *
 * Architecture Doc §8 (Security):
 *   - Session.invalidate() on logout
 *   - Prevent Back-Button Cache Access via cache-control headers
 *
 * Updated for v2:
 *   - Feature 2: Audit log LOGOUT event
 *   - Feature 4: Deactivate session in user_sessions table
 *
 * Depends On: HttpSession API, SessionDAO, AuditLogger (Architecture Doc §4)
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final SessionDAO sessionDAO = new SessionDAO();

    /**
     * GET /logout — Destroy session and redirect to login.
     *
     * Steps:
     * 1. Get the existing session (if any)
     * 2. Log the logout event (Feature 2)
     * 3. Deactivate session in tracking table (Feature 4)
     * 4. Invalidate session completely (PRD FR-12)
     * 5. Set cache-control headers (PRD FR-13, Architecture Doc §8)
     * 6. Redirect to login page
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Step 1: Get existing session
        HttpSession session = request.getSession(false);

        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");

            // Step 2: Audit log (Feature 2)
            if (userId != null) {
                AuditLogger.log(request, userId, "LOGOUT", "USER", userId, null);
            }

            // Step 3: Deactivate session in tracking table (Feature 4)
            sessionDAO.deactivateSession(session.getId());

            // Step 4: Invalidate completely (PRD FR-12)
            session.invalidate();
        }

        // Step 5: Cache-control headers to prevent back-button leakage (PRD FR-13)
        // Architecture Doc §8: "Cache-control headers + session invalidation combined"
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Step 6: Redirect to login
        response.sendRedirect(request.getContextPath() + "/login?loggedout=true");
    }
}
