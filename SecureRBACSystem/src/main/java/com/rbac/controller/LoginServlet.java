package com.rbac.controller;

import com.rbac.dao.PermissionDAO;
import com.rbac.dao.RoleDAO;
import com.rbac.dao.SessionDAO;
import com.rbac.dao.UserDAO;
import com.rbac.dao.UserProfileDAO;
import com.rbac.model.User;
import com.rbac.model.UserSession;
import com.rbac.util.AuditLogger;
import com.rbac.util.PasswordHasher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * LoginServlet — Controller for user authentication.
 *
 * URL: /login
 * Architecture Doc §4: Accepts credentials, hashes input, delegates
 *     verification to DAO, creates session.
 * Architecture Doc §5: Implements the Login Sequence Diagram exactly.
 *
 * PRD FR-3: Validates login credentials against hashed values.
 * PRD FR-5: Failed login shows generic error — never reveals which field was wrong.
 * PRD FR-10: Valid HTTP session created only after successful authentication.
 *
 * Updated for v2:
 *   - Feature 2: Audit logging on login success/failure
 *   - Feature 3: Account lockout after 5 failed attempts (15-min cooldown)
 *   - Feature 3: Password expiration check (90 days)
 *   - Feature 4: Session tracking in user_sessions table
 *   - Feature 6: Update last login date in user_profiles
 *   - Feature 1: Store permissions list in session
 *
 * Depends On: UserDAO, RoleDAO, PermissionDAO, SessionDAO, UserProfileDAO,
 *             PasswordHasher, AuditLogger (Architecture Doc §4)
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final long PASSWORD_EXPIRY_DAYS = 90;

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();

    /**
     * GET /login — Display the login form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /**
     * POST /login — Process login credentials.
     *
     * Flow (Architecture Doc §5 — Login Sequence Diagram, updated for v2):
     * 1. Validate inputs
     * 2. Find user by username via UserDAO
     * 3. Check account lockout status
     * 4. Compare hashed password against stored hash
     * 5. On failure: increment attempts, possibly lock account, audit log
     * 6. On success: reset attempts, check password expiry, get role + permissions,
     *    create session, track session, audit log, update profile, redirect
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Input validation
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Please enter both username and password.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        username = username.trim();

        // Step 2: Find user by username (Architecture Doc §5)
        User user = userDAO.findByUsername(username);

        // Step 3: Check account lockout
        if (user != null && userDAO.isAccountLocked(user.getUserId())) {
            AuditLogger.log(request, user.getUserId(), "LOGIN_BLOCKED",
                    "USER", user.getUserId(), "Account is locked due to too many failed attempts");
            request.setAttribute("error",
                    "Your account is temporarily locked due to too many failed login attempts. Please try again later.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Step 4: Compare hashed passwords
        if (user != null && PasswordHasher.checkPassword(password, user.getPasswordHash())) {

            // Check if account is active
            if ("suspended".equals(user.getStatus())) {
                AuditLogger.log(request, user.getUserId(), "LOGIN_SUSPENDED",
                        "USER", user.getUserId(), "Login attempt on suspended account");
                request.setAttribute("error", "Your account has been suspended. Contact an administrator.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // --- SUCCESS PATH ---

            // Reset failed attempts on successful login
            userDAO.resetFailedAttempts(user.getUserId());

            // Check password expiration (90 days)
            boolean passwordExpired = false;
            Timestamp passwordChangedAt = user.getPasswordChangedAt();
            if (passwordChangedAt != null) {
                long daysSinceChange = (System.currentTimeMillis() - passwordChangedAt.getTime())
                        / (1000 * 60 * 60 * 24);
                if (daysSinceChange >= PASSWORD_EXPIRY_DAYS) {
                    passwordExpired = true;
                }
            }

            // Get role for user (Architecture Doc §5)
            String role = roleDAO.getRoleForUser(user.getUserId());

            // Get permissions for user (Feature 1)
            List<String> permissions = permissionDAO.getPermissionsForUser(user.getUserId());

            // Create HttpSession (Architecture Doc §5, PRD FR-10)
            // Invalidate any existing session first to prevent session fixation
            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                // Deactivate the old session in our tracking table
                sessionDAO.deactivateSession(existingSession.getId());
                existingSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", role);
            session.setAttribute("permissions", permissions);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            if (passwordExpired) {
                session.setAttribute("passwordExpired", true);
            }

            // Track session in user_sessions table (Feature 4)
            UserSession userSession = new UserSession();
            userSession.setSessionId(session.getId());
            userSession.setUserId(user.getUserId());
            userSession.setIpAddress(request.getRemoteAddr());
            userSession.setUserAgent(request.getHeader("User-Agent"));
            sessionDAO.createSession(userSession);

            // Update last login date in profile (Feature 6)
            userProfileDAO.updateLastLoginDate(user.getUserId());

            // Audit log: successful login (Feature 2)
            AuditLogger.log(request, user.getUserId(), "LOGIN_SUCCESS",
                    "USER", user.getUserId(), null);

            // Redirect based on MFA, role, or password expiration
            if (user.isMfaEnabled()) {
                session.setAttribute("mfaPending", true);
                response.sendRedirect(request.getContextPath() + "/mfa-verify");
            } else if (passwordExpired) {
                response.sendRedirect(request.getContextPath() + "/change-password");
            } else if ("Admin".equals(role) || "Super Admin".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                response.sendRedirect(request.getContextPath() + "/user_home.jsp");
            }

        } else {
            // --- FAILURE PATH ---

            if (user != null) {
                // Increment failed attempts
                userDAO.incrementFailedAttempts(user.getUserId());

                // Check if we need to lock the account
                User updatedUser = userDAO.findByUsername(username);
                if (updatedUser != null && updatedUser.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    Timestamp lockUntil = new Timestamp(
                            System.currentTimeMillis() + LOCKOUT_DURATION_MS);
                    userDAO.lockAccount(user.getUserId(), lockUntil);

                    AuditLogger.log(request, user.getUserId(), "ACCOUNT_LOCKED",
                            "USER", user.getUserId(),
                            "Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts");
                }

                // Audit log: failed login (Feature 2)
                AuditLogger.log(request, user.getUserId(), "LOGIN_FAILED",
                        "USER", user.getUserId(), "Invalid password");
            } else {
                // Unknown username — audit log with null userId
                AuditLogger.log(request, null, "LOGIN_FAILED",
                        "USER", null, "Unknown username: " + username);
            }

            // Generic error — PRD FR-5
            // "not reveal whether username or password was wrong"
            request.setAttribute("error", "Invalid credentials. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
