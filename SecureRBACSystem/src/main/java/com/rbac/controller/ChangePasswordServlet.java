package com.rbac.controller;

import com.rbac.dao.UserDAO;
import com.rbac.model.User;
import com.rbac.util.AuditLogger;
import com.rbac.util.PasswordHasher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * ChangePasswordServlet — Controller for changing the current user's password.
 *
 * URL: /change-password
 * Feature 3: Account Security — Password Change & Expiration.
 *
 * Any authenticated user can change their own password.
 * Also handles forced password change when password has expired.
 */
@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    /**
     * GET /change-password — Display the password change form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Boolean passwordExpired = (Boolean) session.getAttribute("passwordExpired");

        if (passwordExpired != null && passwordExpired) {
            request.setAttribute("warning",
                    "Your password has expired. Please set a new password to continue.");
        }

        request.getRequestDispatcher("/change_password.jsp").forward(request, response);
    }

    /**
     * POST /change-password — Process password change.
     *
     * Fields: currentPassword, newPassword, confirmNewPassword
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmNewPassword = request.getParameter("confirmNewPassword");

        // Validation
        if (currentPassword == null || currentPassword.isEmpty()
                || newPassword == null || newPassword.isEmpty()
                || confirmNewPassword == null || confirmNewPassword.isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 6) {
            request.setAttribute("error", "New password must be at least 6 characters long.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            request.setAttribute("error", "New passwords do not match.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
            return;
        }

        // Verify current password
        User user = userDAO.findById(userId);
        if (user == null || !PasswordHasher.checkPassword(currentPassword, user.getPasswordHash())) {
            request.setAttribute("error", "Current password is incorrect.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
            return;
        }

        // Hash and update
        String newHash = PasswordHasher.hashPassword(newPassword);
        if (userDAO.updatePassword(userId, newHash)) {
            // Clear the password expired flag
            session.removeAttribute("passwordExpired");

            AuditLogger.log(request, userId, "PASSWORD_CHANGED", "USER", userId, null);

            session.setAttribute("profileMessage", "Password changed successfully.");
            response.sendRedirect(request.getContextPath() + "/profile");
        } else {
            request.setAttribute("error", "Failed to change password. Please try again.");
            request.getRequestDispatcher("/change_password.jsp").forward(request, response);
        }
    }
}
