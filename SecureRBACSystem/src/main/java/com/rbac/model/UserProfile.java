package com.rbac.model;

import java.sql.Timestamp;

/**
 * UserProfile — POJO model class matching the 'user_profiles' table.
 *
 * Columns:
 *   user_id             INT, PK, FK → users
 *   first_name          VARCHAR(50)
 *   last_name           VARCHAR(50)
 *   phone_number        VARCHAR(20)
 *   profile_picture_url VARCHAR(500)
 *   last_login_date     TIMESTAMP
 */
public class UserProfile {

    private int userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;
    private Timestamp lastLoginDate;

    public UserProfile() {
    }

    // --- Getters and Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
