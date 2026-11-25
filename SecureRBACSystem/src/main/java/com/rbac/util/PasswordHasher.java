package com.rbac.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHasher — Utility class wrapping BCrypt hashing logic.
 *
 * Design Pattern: Strategy (Architecture Doc §11)
 * Responsibility: Hashes passwords and verifies hashes.
 *
 * Per AI Rules §C1: All password-handling code reviewed line-by-line.
 * Per AI Rules §C2: No plain-text passwords, ever — not even temporarily.
 * Per AI Rules §8 Red Lines: Plain-text password storage is never acceptable.
 *
 * Library: org.mindrot:jbcrypt:0.4 (verified on Maven Central)
 * BCrypt automatically handles salting — each hash includes a unique salt.
 */
public class PasswordHasher {

    // BCrypt work factor (log2 rounds). 10 = 2^10 = 1024 rounds.
    // Good balance of security vs. performance for this project scope.
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainText the raw password from user input
     * @return the BCrypt-hashed string (includes salt and work factor)
     */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Checks whether a plain-text password matches a stored BCrypt hash.
     *
     * @param plainText      the raw password from user input
     * @param hashedPassword the stored BCrypt hash from the database
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String plainText, String hashedPassword) {
        return BCrypt.checkpw(plainText, hashedPassword);
    }

    // Prevent instantiation — utility class
    private PasswordHasher() {
    }
}
