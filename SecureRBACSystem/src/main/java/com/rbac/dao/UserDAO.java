package com.rbac.dao;

import com.rbac.model.User;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Data Access Object for the 'users' table.
 *
 * Architecture Doc §4: "Only DAO classes are allowed to contain SQL.
 *     Servlets never write raw queries."
 * AI Rules §A3: "All queries must use PreparedStatement."
 * PRD FR-18: "All database queries shall use PreparedStatement, never
 *     string-concatenated SQL."
 *
 * Every method uses PreparedStatement exclusively — no string concatenation.
 */
public class UserDAO {

    /**
     * Finds a user by username.
     * Used by: LoginServlet (login flow), RegisterServlet (duplicate check)
     *
     * @param username the username to search for
     * @return the User object if found, null otherwise
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a user by email.
     * Used by: RegisterServlet (duplicate check — PRD FR-4)
     *
     * @param email the email to search for
     * @return the User object if found, null otherwise
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a user by ID.
     * Used by: AdminManagementServlet
     *
     * @param userId the user ID to search for
     * @return the User object if found, null otherwise
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a new user into the database.
     * Used by: RegisterServlet
     * Note: password_hash must already be hashed before calling this method.
     *
     * @param user the User object to insert (userId is auto-generated)
     * @return the auto-generated user_id, or -1 on failure
     */
    public int insertUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, status, mfa_secret, mfa_enabled) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getStatus() != null ? user.getStatus() : "active");
            stmt.setString(5, user.getMfaSecret());
            stmt.setBoolean(6, user.isMfaEnabled());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves all users with their assigned role names and profile data.
     * Used by: AdminManagementServlet (PRD FR-14 — admin views all users)
     *
     * @return list of all User objects with roleName and profile data populated
     */
    public List<User> getAllUsers() {
        String sql = "SELECT u.user_id, u.username, u.email, u.password_hash, "
                + "u.created_date, u.status, u.failed_login_attempts, "
                + "u.last_failed_login, u.password_changed_at, u.account_locked_until, "
                + "u.mfa_secret, u.mfa_enabled, "
                + "r.role_name, p.first_name, p.last_name "
                + "FROM users u "
                + "LEFT JOIN user_roles ur ON u.user_id = ur.user_id "
                + "LEFT JOIN roles r ON ur.role_id = r.role_id "
                + "LEFT JOIN user_profiles p ON u.user_id = p.user_id "
                + "ORDER BY u.user_id";

        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                user.setRoleName(rs.getString("role_name"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Updates a user's status (active/suspended).
     * Used by: AdminManagementServlet (PRD FR-15)
     *
     * @param userId the user ID to update
     * @param status the new status ("active" or "suspended")
     * @return true if update succeeded
     */
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a user from the database.
     * Used by: AdminManagementServlet (PRD FR-15)
     * Note: CASCADE on user_roles FK will automatically remove the role mapping.
     *
     * @param userId the user ID to delete
     * @return true if deletion succeeded
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===================================================================
    // Feature 3: Account Security — Lockout & Password Methods
    // ===================================================================

    /**
     * Increments the failed login attempt counter and records the timestamp.
     *
     * @param userId the user's ID
     * @return true if update succeeded
     */
    public boolean incrementFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1, "
                + "last_failed_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resets the failed login attempt counter to 0 after a successful login.
     *
     * @param userId the user's ID
     * @return true if update succeeded
     */
    public boolean resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = 0, "
                + "account_locked_until = NULL WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Locks a user's account until the specified timestamp.
     *
     * @param userId    the user's ID
     * @param lockUntil the timestamp until which the account is locked
     * @return true if update succeeded
     */
    public boolean lockAccount(int userId, java.sql.Timestamp lockUntil) {
        String sql = "UPDATE users SET account_locked_until = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, lockUntil);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Unlocks a user's account by clearing the lockout timestamp and resetting attempts.
     *
     * @param userId the user's ID
     * @return true if update succeeded
     */
    public boolean unlockAccount(int userId) {
        String sql = "UPDATE users SET account_locked_until = NULL, "
                + "failed_login_attempts = 0 WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a user's account is currently locked.
     *
     * @param userId the user's ID
     * @return true if the account is locked (account_locked_until is in the future)
     */
    public boolean isAccountLocked(int userId) {
        String sql = "SELECT account_locked_until FROM users WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp lockedUntil = rs.getTimestamp("account_locked_until");
                    if (lockedUntil != null) {
                        return lockedUntil.after(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates a user's password hash and resets the password_changed_at timestamp.
     *
     * @param userId          the user's ID
     * @param newPasswordHash the new BCrypt-hashed password
     * @return true if update succeeded
     */
    /**
     * Updates MFA settings for a user.
     */
    public boolean updateMfaSettings(int userId, String mfaSecret, boolean mfaEnabled) {
        String sql = "UPDATE users SET mfa_secret = ?, mfa_enabled = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mfaSecret);
            stmt.setBoolean(2, mfaEnabled);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, password_changed_at = CURRENT_TIMESTAMP, "
                + "failed_login_attempts = 0, account_locked_until = NULL WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a User object.
     * Private helper — keeps mapping logic in one place.
     * Updated to include account security fields.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedDate(rs.getTimestamp("created_date"));
        user.setStatus(rs.getString("status"));

        // MFA fields (from Feature 5)
        try {
            user.setMfaSecret(rs.getString("mfa_secret"));
            user.setMfaEnabled(rs.getBoolean("mfa_enabled"));
        } catch (SQLException e) {
            // Ignore if column not found in specific partial queries
        }

        // Account security fields — may not be present in all queries
        try {
            user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
            user.setLastFailedLogin(rs.getTimestamp("last_failed_login"));
            user.setPasswordChangedAt(rs.getTimestamp("password_changed_at"));
            user.setAccountLockedUntil(rs.getTimestamp("account_locked_until"));
        } catch (SQLException ignored) {
            // These columns may not be in the SELECT for some queries
        }

        return user;
    }
}

