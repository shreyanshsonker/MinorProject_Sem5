package com.rbac.controller;

import com.rbac.dao.UserDAO;
import com.rbac.model.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/mfa-verify")
public class MFAVerifyServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Boolean mfaPending = (Boolean) session.getAttribute("mfaPending");
        if (mfaPending == null || !mfaPending) {
            // No MFA pending, just redirect to home
            response.sendRedirect(request.getContextPath() + "/user_home.jsp");
            return;
        }

        request.getRequestDispatcher("/mfa_verify.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Boolean mfaPending = (Boolean) session.getAttribute("mfaPending");
        if (mfaPending == null || !mfaPending) {
            response.sendRedirect(request.getContextPath() + "/user_home.jsp");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userDAO.findById(userId);
        String codeStr = request.getParameter("mfaCode");
        
        try {
            int code = Integer.parseInt(codeStr);
            boolean isCodeValid = gAuth.authorize(user.getMfaSecret(), code);

            if (isCodeValid) {
                // MFA successful, remove pending flag
                session.removeAttribute("mfaPending");

                // Check role to determine redirect
                String role = (String) session.getAttribute("role");
                Boolean passwordExpired = (Boolean) session.getAttribute("passwordExpired");

                if (passwordExpired != null && passwordExpired) {
                    response.sendRedirect(request.getContextPath() + "/change-password");
                } else if ("Admin".equals(role) || "Super Admin".equals(role)) {
                    response.sendRedirect(request.getContextPath() + "/admin");
                } else {
                    response.sendRedirect(request.getContextPath() + "/user_home.jsp");
                }
            } else {
                request.setAttribute("error", "Invalid MFA code. Please try again.");
                request.getRequestDispatcher("/mfa_verify.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid MFA code format.");
            request.getRequestDispatcher("/mfa_verify.jsp").forward(request, response);
        }
    }
}
