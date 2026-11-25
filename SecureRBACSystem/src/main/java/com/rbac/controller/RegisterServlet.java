package com.rbac.controller;

import com.rbac.dao.RoleDAO;
import com.rbac.dao.UserDAO;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.util.PasswordHasher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * RegisterServlet — Controller for new user registration.
 *
 * URL: /register
 * Architecture Doc §4: Validates new-user input, hashes password,
 *     inserts into users + default role into user_roles.
 *
 * PRD FR-1: System shall allow new users to register with username, email, password.
 * PRD FR-2: Passwords shall be hashed before storage; plain text must never touch the DB.
 * PRD FR-4: System shall reject duplicate usernames/emails at registration.
 *
 * AI Rules §C2: No plain-text passwords, ever — not even temporarily.
 *
 * Depends On: UserDAO (Architecture Doc §4)
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    /**
     * GET /register — Display the registration form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    /**
     * POST /register — Process new user registration.
     *
     * Flow:
     * 1. Validate inputs (non-empty, password match)
     * 2. Check for duplicate username/email (PRD FR-4)
     * 3. Hash password using BCrypt (PRD FR-2)
     * 4. Insert user via UserDAO
     * 5. Assign default "User" role via RoleDAO
     * 6. Redirect to login page with success message
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // --- Step 1: Input validation ---

        if (username == null || username.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        username = username.trim();
        email = email.trim();

        // Validate username length
        if (username.length() < 3 || username.length() > 50) {
            request.setAttribute("error", "Username must be between 3 and 50 characters.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Validate email format (basic check)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            request.setAttribute("error", "Please enter a valid email address.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters long.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // --- Step 2: Duplicate check (PRD FR-4) ---

        if (userDAO.findByUsername(username) != null) {
            request.setAttribute("error", "Username already exists. Please choose a different one.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            request.setAttribute("error", "Email already registered. Please use a different email.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // --- Step 3: Hash password (PRD FR-2, AI Rules §C2) ---
        // Password is hashed BEFORE any DB operation — plain text never touches the database.
        String hashedPassword = PasswordHasher.hashPassword(password);

        // --- Step 4: Insert user ---
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(hashedPassword);

        int userId = userDAO.insertUser(newUser);

        if (userId == -1) {
            request.setAttribute("error", "Registration failed. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // --- Step 5: Assign default "User" role ---
        // Per AI Rules §B1: Role name must exactly match what's in the DB
        Role defaultRole = roleDAO.getRoleByName("User");
        if (defaultRole != null) {
            roleDAO.assignRole(userId, defaultRole.getRoleId());
        }

        // --- Step 6: Redirect to login with success ---
        response.sendRedirect(request.getContextPath() + "/login?registered=true");
    }
}
