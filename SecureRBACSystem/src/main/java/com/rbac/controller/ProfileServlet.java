package com.rbac.controller;

import com.rbac.dao.UserProfileDAO;
import com.rbac.model.UserProfile;
import com.rbac.util.AuditLogger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * ProfileServlet — Controller for viewing and editing user profiles.
 *
 * URL: /profile
 * Feature 6: Enhanced User Profiles.
 *
 * Any authenticated user can view/edit their own profile.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserProfileDAO userProfileDAO = new UserProfileDAO();

    /**
     * GET /profile — Display the current user's profile form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        UserProfile profile = userProfileDAO.getProfile(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        request.setAttribute("profile", profile);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    /**
     * POST /profile — Update the current user's profile.
     *
     * Fields: firstName, lastName, phoneNumber
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phoneNumber = request.getParameter("phoneNumber");

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFirstName(firstName != null ? firstName.trim() : null);
        profile.setLastName(lastName != null ? lastName.trim() : null);
        profile.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);

        if (userProfileDAO.createOrUpdateProfile(profile)) {
            session.setAttribute("profileMessage", "Profile updated successfully.");
            AuditLogger.log(request, userId, "UPDATE_PROFILE", "USER", userId, null);
        } else {
            session.setAttribute("profileMessage", "Failed to update profile.");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }
}
