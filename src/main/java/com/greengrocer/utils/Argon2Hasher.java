package com.greengrocer.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class Argon2Hasher {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int OPS_LIMIT = 3; // Iterations
    // Note: Our custom impl takes memory in blocks of 1KB.
    // 64MB = 65536 blocks.
    private static final int MEMORY_BLOCKS = 65536;
    private static final int PARALLELISM = 1;

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
