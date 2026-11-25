package com.rbac.util;

import java.security.SecureRandom;

/**
 * TokenGenerator — Utility for generating cryptographically secure random tokens.
 *
 * Feature 3: Account Security & Recovery.
 * Used for generating password reset tokens.
 *
 * Uses java.security.SecureRandom for cryptographic strength.
 */
public class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String HEX_CHARS = "0123456789abcdef";
    private static final int DEFAULT_TOKEN_LENGTH = 64; // 64 hex characters = 32 bytes of randomness

    /**
     * Generates a cryptographically secure random token as a hex string.
     *
     * @return a 64-character hexadecimal token string
     */
    public static String generateToken() {
        return generateToken(DEFAULT_TOKEN_LENGTH);
    }

    /**
     * Generates a cryptographically secure random token of specified length.
     *
     * @param length the desired length of the hex string
     * @return a hexadecimal token string of the specified length
     */
    public static String generateToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(HEX_CHARS.charAt(SECURE_RANDOM.nextInt(HEX_CHARS.length())));
        }
        return sb.toString();
    }

    // Prevent instantiation — utility class
    private TokenGenerator() {
    }
}
