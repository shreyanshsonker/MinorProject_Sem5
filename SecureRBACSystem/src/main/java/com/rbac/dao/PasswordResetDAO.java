package com.rbac.dao;

import com.rbac.model.PasswordResetToken;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * PasswordResetDAO — Data Access Object for the 'password_reset_tokens' table.
 *
 * Feature 3: Account Security & Recovery.
 * All queries use PreparedStatement exclusively (AI Rules §A3).
 */
public class PasswordResetDAO {

    /**
     * Creates a new password reset token for a user.
     *
     * @param userId    the user's ID
     * @param token     the secure random token string
     * @param expiresAt when the token expires
     * @return true if insertion succeeded
     */
    public boolean createToken(int userId, String token, Timestamp expiresAt) {
        String sql = "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, token);
            stmt.setTimestamp(3, expiresAt);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds a valid (not used, not expired) reset token.
     *
     * @param token the token string to look up
     * @return the PasswordResetToken if valid, null otherwise
     */
    public PasswordResetToken findValidToken(String token) {
        String sql = "SELECT * FROM password_reset_tokens "
                + "WHERE token = ? AND used = FALSE AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PasswordResetToken prt = new PasswordResetToken();
                    prt.setTokenId(rs.getInt("token_id"));
                    prt.setUserId(rs.getInt("user_id"));
                    prt.setToken(rs.getString("token"));
                    prt.setExpiresAt(rs.getTimestamp("expires_at"));
                    prt.setUsed(rs.getBoolean("used"));
                    prt.setCreatedAt(rs.getTimestamp("created_at"));
                    return prt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Marks a token as used after a successful password reset.
     *
     * @param token the token string to mark
     * @return true if update succeeded
     */
    public boolean markTokenUsed(String token) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE token = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes all expired tokens (housekeeping).
     *
     * @return number of tokens deleted
     */
    public int deleteExpiredTokens() {
        String sql = "DELETE FROM password_reset_tokens WHERE expires_at <= CURRENT_TIMESTAMP OR used = TRUE";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
