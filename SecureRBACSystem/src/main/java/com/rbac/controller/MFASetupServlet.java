package com.rbac.controller;

import com.rbac.dao.UserDAO;
import com.rbac.model.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/mfa-setup")
public class MFASetupServlet extends HttpServlet {

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

        int userId = (int) session.getAttribute("userId");
        User user = userDAO.findById(userId);

        if (user.isMfaEnabled()) {
            request.setAttribute("error", "MFA is already enabled.");
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
            return;
        }

        // Generate a new MFA key
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String mfaSecret = key.getKey();
        
        // Save it to user, but don't enable it yet
        userDAO.updateMfaSettings(userId, mfaSecret, false);

        // We can generate a QR code URL. GoogleAuth doesn't natively generate the image, 
        // but it can generate the otpauth:// URI.
        // String qrCodeData = "otpauth://totp/SecureRBAC:" + user.getUsername() + "?secret=" + mfaSecret + "&issuer=SecureRBAC";
        // Alternatively, just show the secret key to the user for manual entry in MVP.

        request.setAttribute("mfaSecret", mfaSecret);
        request.getRequestDispatcher("/mfa_setup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user = userDAO.findById(userId);

        String codeStr = request.getParameter("mfaCode");
        
        try {
            int code = Integer.parseInt(codeStr);
            boolean isCodeValid = gAuth.authorize(user.getMfaSecret(), code);

            if (isCodeValid) {
                // Enable MFA
                userDAO.updateMfaSettings(userId, user.getMfaSecret(), true);
                request.setAttribute("success", "MFA has been successfully enabled.");
                request.getRequestDispatcher("/profile.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Invalid MFA code. Please try again.");
                request.setAttribute("mfaSecret", user.getMfaSecret());
                request.getRequestDispatcher("/mfa_setup.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid MFA code format.");
            request.setAttribute("mfaSecret", user.getMfaSecret());
            request.getRequestDispatcher("/mfa_setup.jsp").forward(request, response);
        }
    }
}
