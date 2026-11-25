package com.rbac.dao;

import com.rbac.model.UserProfile;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserProfileDAO — Data Access Object for the 'user_profiles' table.
 *
 * Feature 6: Enhanced User Profiles.
 * All queries use PreparedStatement exclusively (AI Rules §A3).
 */
public class UserProfileDAO {

    /**
     * Gets the profile for a specific user.
     *
     * @param userId the user's ID
     * @return the UserProfile object, or null if not found
     */
    public UserProfile getProfile(int userId) {
        String sql = "SELECT * FROM user_profiles WHERE user_id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates or updates a user profile using INSERT ... ON DUPLICATE KEY UPDATE.
     * This handles both first-time profile creation and subsequent updates.
     *
     * @param profile the UserProfile object to save
     * @return true if the operation succeeded
     */
    public boolean createOrUpdateProfile(UserProfile profile) {
        String sql = "INSERT INTO user_profiles (user_id, first_name, last_name, phone_number, profile_picture_url) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "first_name = VALUES(first_name), "
                + "last_name = VALUES(last_name), "
                + "phone_number = VALUES(phone_number), "
                + "profile_picture_url = VALUES(profile_picture_url)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getFirstName());
            stmt.setString(3, profile.getLastName());
            stmt.setString(4, profile.getPhoneNumber());
            stmt.setString(5, profile.getProfilePictureUrl());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the last login date for a user.
     * Called on each successful login.
     *
     * @param userId the user's ID
     * @return true if update succeeded
     */
    public boolean updateLastLoginDate(int userId) {
        // First ensure a profile row exists
        String upsertSql = "INSERT INTO user_profiles (user_id, last_login_date) "
                + "VALUES (?, CURRENT_TIMESTAMP) "
                + "ON DUPLICATE KEY UPDATE last_login_date = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertSql)) {

            stmt.setInt(1, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row to a UserProfile object.
     */
    private UserProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        UserProfile profile = new UserProfile();
        profile.setUserId(rs.getInt("user_id"));
        profile.setFirstName(rs.getString("first_name"));
        profile.setLastName(rs.getString("last_name"));
        profile.setPhoneNumber(rs.getString("phone_number"));
        profile.setProfilePictureUrl(rs.getString("profile_picture_url"));
        profile.setLastLoginDate(rs.getTimestamp("last_login_date"));
        return profile;
    }
}
