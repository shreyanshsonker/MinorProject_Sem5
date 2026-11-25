package com.rbac.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthenticationFilter — Centralized security filter for RBAC enforcement.
 *
 * This is the SINGLE choke point for authentication and authorization checks.
 * Architecture Doc §4: "Intercepts every request to protected URLs;
 *     checks session validity before allowing continuation."
 * Architecture Doc §6: Implements the Protected Page Access sequence diagram.
 *
 * PRD FR-7:  A Servlet Filter shall intercept every request and verify role.
 * PRD FR-8:  Standard Users accessing admin URLs shall be redirected/blocked.
 * PRD FR-11: Every protected page shall verify an active session.
 * PRD FR-13: After logout, Back button shall not reveal cached content.
 * PRD FR-17: Non-admin users shall not see or reach dashboard controls.
 *
 * AI Rules §B2: "Role-checking logic must live only in the AuthenticationFilter /
 *     RBAC module — never duplicated inline inside individual Servlets or JSPs."
 * AI Rules §8 Red Lines: "Role-checking logic duplicated ad-hoc across multiple
 *     files instead of centralized in the filter" is NEVER acceptable.
 *
 * Updated for v2 features:
 *   - New routes: /profile, /change-password, /sessions (authenticated, any role)
 *   - New routes: /audit-logs (admin only)
 *   - New public routes: /reset-password (token-gated)
 *   - Password expiration: forces redirect to /change-password
 *
 * Depends On: HttpSession API (Architecture Doc §4)
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    /**
     * Intercepts every request. Logic per Architecture Doc §6:
     *
     * 1. Allow public resources (login, register, CSS, JS, reset-password) through
     * 2. Check if valid session exists → if not, redirect to /login
     * 3. Check password expiration → force redirect to /change-password
     * 4. Check role vs. required role for URL
     * 5. Set cache-control headers on every protected response
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // --- Step 1: Allow public resources through ---
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // --- Step 2: Check session exists (Architecture Doc §6) ---
        // PRD FR-11: "Every protected page shall verify an active session"
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            // No valid session → redirect to login (Architecture Doc §6)
            response.sendRedirect(contextPath + "/login");
            return;
        }

        // --- Step 3: Password expiration check ---
        // If the password is expired, force redirect to /change-password
        // (but allow access to /change-password and /logout to avoid infinite loop)
        Boolean passwordExpired = (Boolean) session.getAttribute("passwordExpired");
        if (passwordExpired != null && passwordExpired
                && !path.equals("/change-password") && !path.equals("/logout")) {
            response.sendRedirect(contextPath + "/change-password");
            return;
        }

        // --- Step 3b: MFA Check ---
        Boolean mfaPending = (Boolean) session.getAttribute("mfaPending");
        if (mfaPending != null && mfaPending
                && !path.equals("/mfa-verify") && !path.equals("/logout")) {
            response.sendRedirect(contextPath + "/mfa-verify");
            return;
        }

        // --- Step 4: Role-based authorization (Architecture Doc §6) ---
        String role = (String) session.getAttribute("role");

        // Admin-only pages: /admin*, /audit-logs*
        if (path.startsWith("/admin") || path.startsWith("/audit-logs")) {
            if (!"Admin".equals(role) && !"Super Admin".equals(role)) {
                // Role NOT authorized → redirect to access_denied.jsp (Architecture Doc §6)
                response.sendRedirect(contextPath + "/access_denied.jsp");
                return;
            }
        }

        // --- Step 5: Set cache-control headers on ALL protected responses ---
        // PRD FR-13: Prevent back-button cache access
        // Architecture Doc §8: "Cache-control headers + session invalidation combined"
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // All checks passed — forward request (Architecture Doc §6)
        chain.doFilter(request, response);
    }

    /**
     * Determines if a resource is publicly accessible (no session required).
     *
     * Public resources:
     *   - /login and /register (pages and servlet endpoints)
     *   - /css/* and /js/* (static assets)
     *   - Root path /
     *   - /reset-password (token-gated, public access needed)
     */
    private boolean isPublicResource(String path) {
        return path.equals("/")
                || path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.equals("/login.jsp")
                || path.equals("/register.jsp")
                || path.equals("/reset-password")
                || path.equals("/reset_password.jsp")
                || path.equals("/mfa_verify.jsp")
                || path.equals("/mfa_setup.jsp"); // Setup JSP needs to be somewhat public but we control via Servlet
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
