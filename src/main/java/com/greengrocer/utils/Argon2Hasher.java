package com.greengrocer.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for hashing and verifying passwords using the Argon2id
 * algorithm.
 * <p>
 * This class provides a secure way to handle user passwords by generating salts
 * and using memory-hard hashing to resist brute-force attacks.
 * </p>
 * 
 * @author Burak Özevin
 */
public class Argon2Hasher {

    /** Length of the random salt in bytes. */
    private static final int SALT_LENGTH = 16;
    /** Length of the generated hash in bytes. */
    private static final int HASH_LENGTH = 32;
    /** Number of iterations (time cost). */
    private static final int OPS_LIMIT = 3;
    /**
     * Memory usage in blocks of 1KB.
     * <p>
     * 64MB = 65536 blocks.
     * </p>
     */
    private static final int MEMORY_BLOCKS = 65536;
    /** Degree of parallelism (number of threads). */
    private static final int PARALLELISM = 1;

    /**
     * Hashes a password using the Argon2id algorithm.
     * <p>
     * Generates a random salt and computes the hash.
     * The result is formatted as a standard Argon2 string:
     * {@code $argon2id$v=19$m=...,t=...,p=...$salt$hash}
     * </p>
     *
     * @param password The password characters to hash.
     * @return The encoded Argon2id hash string.
     * 
     * @author Burak Özevin
     */
    public String hash(char[] password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        Argon2id argon2 = new Argon2id();
        byte[] result = argon2.hash(OPS_LIMIT, MEMORY_BLOCKS, PARALLELISM, password, salt, HASH_LENGTH);

        // Encode salt and hash to Base64 for storage
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(result);

        // Format: $argon2id$v=19$m=65536,t=3,p=1$salt$hash
        return String.format("$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                MEMORY_BLOCKS,
                OPS_LIMIT,
                PARALLELISM,
                saltB64,
                hashB64);
    }

    /**
     * Verifies a password against an encoded Argon2id hash.
     * <p>
     * Parses the encoded hash to extract parameters and salt, then re-computes
     * the hash for the provided password to check for a match.
     * </p>
     *
     * @param password    The password characters to verify.
     * @param encodedHash The previously stored encoded Argon2id hash string.
     * @return {@code true} if the password matches the hash; {@code false}
     *         otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean verify(char[] password, String encodedHash) {
        try {
            String[] parts = encodedHash.split("\\$");
            if (parts.length != 6 || !parts[1].equals("argon2id")) {
                return false;
            }

            // Parse parameters
            String[] params = parts[3].split(",");
            int memory = Integer.parseInt(params[0].substring(2));
            int iterations = Integer.parseInt(params[1].substring(2));
            int parallelism = Integer.parseInt(params[2].substring(2));

            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] hash = Base64.getDecoder().decode(parts[5]);

            Argon2id argon2 = new Argon2id();
            byte[] calculatedHash = argon2.hash(iterations, memory, parallelism, password, salt, hash.length);

            // Constant time comparison
            int diff = 0;
            for (int i = 0; i < hash.length; i++) {
                diff |= hash[i] ^ calculatedHash[i];
            }
            return diff == 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
