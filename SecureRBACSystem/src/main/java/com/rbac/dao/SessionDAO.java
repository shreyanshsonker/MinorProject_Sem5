package com.rbac.dao;

import com.rbac.model.UserSession;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SessionDAO — Data Access Object for the 'user_sessions' table.
 *
 * Feature 4: Session Management.
 * All queries use PreparedStatement exclusively (AI Rules §A3).
 */
public class SessionDAO {

    /**
     * Creates a new session record when a user logs in.
     *
     * @param session the UserSession object to insert
     * @return true if insertion succeeded
     */
    public boolean createSession(UserSession session) {
        String sql = "INSERT INTO user_sessions (session_id, user_id, ip_address, user_agent) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, session.getSessionId());
            stmt.setInt(2, session.getUserId());
            stmt.setString(3, session.getIpAddress());
            stmt.setString(4, session.getUserAgent());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets all active sessions for a specific user.
     *
     * @param userId the user's ID
     * @return list of active UserSession objects
     */
    public List<UserSession> getActiveSessionsForUser(int userId) {
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND is_active = TRUE "
                + "ORDER BY last_active DESC";

        List<UserSession> sessions = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    /**
     * Deactivates a specific session (marks as inactive).
     *
     * @param sessionId the session ID to deactivate
     * @return true if update succeeded
     */
    public boolean deactivateSession(String sessionId) {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE session_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deactivates all sessions for a user EXCEPT the specified session.
     * Used for "Revoke all other sessions" feature.
     *
     * @param userId          the user's ID
     * @param exceptSessionId the session to keep active (current session)
     * @return number of sessions deactivated
     */
    public int deactivateAllSessionsForUser(int userId, String exceptSessionId) {
        String sql = "UPDATE user_sessions SET is_active = FALSE "
                + "WHERE user_id = ? AND session_id != ? AND is_active = TRUE";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, exceptSessionId);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Updates the last_active timestamp for a session.
     * Called on each request to keep the session record fresh.
     *
     * @param sessionId the session ID to update
     * @return true if update succeeded
     */
    public boolean updateLastActive(String sessionId) {
        String sql = "UPDATE user_sessions SET last_active = CURRENT_TIMESTAMP WHERE session_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a UserSession object.
     */
    private UserSession mapResultSetToSession(ResultSet rs) throws SQLException {
        UserSession session = new UserSession();
        session.setSessionId(rs.getString("session_id"));
        session.setUserId(rs.getInt("user_id"));
        session.setIpAddress(rs.getString("ip_address"));
        session.setUserAgent(rs.getString("user_agent"));
        session.setLoginTime(rs.getTimestamp("login_time"));
        session.setLastActive(rs.getTimestamp("last_active"));
        session.setActive(rs.getBoolean("is_active"));
        return session;
    }
}
