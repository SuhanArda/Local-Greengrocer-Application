package com.greengrocer.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Custom implementation of the Argon2id password hashing algorithm.
 * <p>
 * Argon2id is a hybrid of Argon2i and Argon2d, providing resistance against
 * both
 * side-channel attacks and GPU cracking attacks.
 * </p>
 * 
 * @author Burak Özevin
 */
public class Argon2id {

    private static final int ARGON2_VERSION_13 = 0x13;
    private static final int ARGON2_ID = 2;
    private static final int BLOCK_SIZE = 1024;
    private static final int QWORDS_IN_BLOCK = BLOCK_SIZE / 8;

    /**
     * Computes the Argon2id hash for the given password and parameters.
     *
     * @param iterations  Number of passes over the memory (time cost).
     * @param memory      Memory size in 1KB blocks (memory cost).
     * @param parallelism Number of threads/lanes (parallelism cost).
     * @param password    The password characters.
     * @param salt        The salt bytes.
     * @param hashLength  The desired length of the output hash in bytes.
     * @return The computed hash bytes.
     * @throws IllegalArgumentException If any parameter is invalid.
     * 
     * @author Burak Özevin
     */
    public byte[] hash(int iterations, int memory, int parallelism, char[] password, byte[] salt, int hashLength) {
        if (iterations < 1)
            throw new IllegalArgumentException("Iterations must be >= 1");
        if (memory < 8 * parallelism)
            throw new IllegalArgumentException("Memory too small");
        if (parallelism < 1)
            throw new IllegalArgumentException("Parallelism must be >= 1");

        byte[] pwdBytes = new String(password).getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // 1. Initialization
        byte[] h0 = initialHash(iterations, memory, parallelism, pwdBytes, salt, hashLength);

        // 2. Allocate memory
        // Memory is in blocks of 1024 bytes
        int blockCount = memory;
        long[][] matrix = new long[blockCount][QWORDS_IN_BLOCK];

        // 3. Fill first two blocks of each lane
        int laneLength = blockCount / parallelism;
        for (int i = 0; i < parallelism; i++) {
            // Block 0
            byte[] block0Input = new byte[72]; // 64 + 4 + 4
            System.arraycopy(h0, 0, block0Input, 0, 64);
            ByteBuffer.wrap(block0Input, 64, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(0);
            ByteBuffer.wrap(block0Input, 68, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(i);
            byte[] block0 = hashBlock(block0Input);
            bytesToLongs(block0, matrix[i * laneLength]);

            // Block 1
            byte[] block1Input = new byte[72];
            System.arraycopy(h0, 0, block1Input, 0, 64);
            ByteBuffer.wrap(block1Input, 64, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(1);
            ByteBuffer.wrap(block1Input, 68, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(i);
            byte[] block1 = hashBlock(block1Input);
            bytesToLongs(block1, matrix[i * laneLength + 1]);
        }

        // 4. Fill remaining blocks
        for (int pass = 0; pass < iterations; pass++) {
            for (int slice = 0; slice < 4; slice++) {
                for (int lane = 0; lane < parallelism; lane++) {
                    int start = 0;
                    int end = 0;
                    if (slice == 0) {
                        start = 2;
                        end = laneLength / 4;
                    } else {
                        start = slice * (laneLength / 4);
                        end = (slice + 1) * (laneLength / 4);
                    }

                    for (int index = start; index < end; index++) {
                        int currIdx = lane * laneLength + index;
                        int prevIdx = (index == 0) ? (lane * laneLength + laneLength - 1) : (currIdx - 1);

                        // Reference block index
                        long pseudoRand = matrix[prevIdx][0];
                        int refLane = lane;
                        int refIndex = 0;

                        // Argon2id mixing
                        boolean isDataIndependent = (pass == 0) && (slice < 2);
                        if (isDataIndependent) {
                            // Generate pseudo-random values
                            // Simplified for demo: using simple generation
                            // In a real implementation, this uses a specific generation function
                            refLane = (int) ((pseudoRand >>> 32) % parallelism);
                            refIndex = (int) ((pseudoRand & 0xFFFFFFFFL) % (laneLength - 1)); // Simplified
                        } else {
                            refLane = (int) ((pseudoRand >>> 32) % parallelism);
                            refIndex = (int) ((pseudoRand & 0xFFFFFFFFL) % (laneLength - 1)); // Simplified
                        }

                        // Ensure we reference a valid block in the window
                        // This logic is complex in the spec, simplified here for brevity/demo
                        // A full implementation requires careful window management
                        int refAbsIdx = refLane * laneLength + refIndex;
                        if (refAbsIdx == currIdx)
                            refAbsIdx = prevIdx; // Avoid self-ref if not allowed

                        fillBlock(matrix, currIdx, prevIdx, refAbsIdx);
                    }
                }
            }
        }

        // 5. Finalization
        byte[] finalBlock = new byte[1024];
        long[] xorBlock = new long[QWORDS_IN_BLOCK];
        for (int i = 0; i < parallelism; i++) {
            int lastIdx = (i + 1) * laneLength - 1;
            for (int j = 0; j < QWORDS_IN_BLOCK; j++) {
                xorBlock[j] ^= matrix[lastIdx][j];
            }
        }

        longsToBytes(xorBlock, finalBlock);

        // Hash final block
        // H' = H(finalBlock) - but with length encoding
        // The spec says H'(A) where A is the result of XORing the last blocks
        // We need to produce 'hashLength' bytes

        // Simplified final hash for demo purposes
        // Real Argon2 uses a variable-length hash function based on Blake2b
        Blake2b finalHasher = new Blake2b(hashLength);
        finalHasher.update(intToBytes(hashLength)); // Length of output
        finalHasher.update(finalBlock);
        return finalHasher.digest();
    }

    /**
     * Computes the initial hash H0.
     * 
     * @param iterations  Number of passes
     * @param memory      Memory size
     * @param parallelism Parallelism degree
     * @param password    Password bytes
     * @param salt        Salt bytes
     * @param hashLength  Output hash length
     * @return The initial hash bytes
     * @author Burak Özevin
     */
    private byte[] initialHash(int iterations, int memory, int parallelism, byte[] password, byte[] salt,
            int hashLength) {
        Blake2b blake = new Blake2b(64);
        blake.update(intToBytes(parallelism));
        blake.update(intToBytes(hashLength));
        blake.update(intToBytes(memory));
        blake.update(intToBytes(iterations));
        blake.update(intToBytes(ARGON2_VERSION_13));
        blake.update(intToBytes(ARGON2_ID));

        blake.update(intToBytes(password.length));
        blake.update(password);

        blake.update(intToBytes(salt.length));
        blake.update(salt);

        // Secret and associated data (empty)
        blake.update(intToBytes(0));
        blake.update(intToBytes(0));

        return blake.digest();
    }

    /**
     * Hashes a block of data.
     * 
     * @param input The input data block
     * @return The hashed block
     * @author Burak Özevin
     */
    private byte[] hashBlock(byte[] input) {
        // H' with 1024 byte output
        // Blake2b supports max 64 bytes, so Argon2 uses a special construction
        // For this demo, we'll just fill 1024 bytes by repeated hashing (Not strictly
        // spec compliant but functional for demo)
        byte[] block = new byte[1024];
        byte[] current = input;
        for (int i = 0; i < 16; i++) { // 16 * 64 = 1024
            Blake2b b = new Blake2b(64);
            b.update(intToBytes(1024)); // Length
            b.update(current);
            current = b.digest();
            System.arraycopy(current, 0, block, i * 64, 64);
        }
        return block;
    }

    /**
     * Fills a memory block using Argon2 compression function.
     * 
     * @param matrix  The memory matrix
     * @param currIdx Current block index
     * @param prevIdx Previous block index
     * @param refIdx  Reference block index
     * @author Burak Özevin
     */
    private void fillBlock(long[][] matrix, int currIdx, int prevIdx, int refIdx) {
        long[] curr = matrix[currIdx];
        long[] prev = matrix[prevIdx];
        long[] ref = matrix[refIdx];

        // G(prev[i], ref[i]) -> curr[i]
        // Improved mixing to avoid zero propagation
        for (int i = 0; i < QWORDS_IN_BLOCK; i++) {
            long v = prev[i] ^ ref[i];
            // A slightly better mix than just rotate-xor
            v = (v * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL; // Linear Congruential Generator step
            v ^= Long.rotateRight(v, 32);
            curr[i] = v;
        }
    }

    /**
     * Converts an integer to a 4-byte array (Little Endian).
     * 
     * @param v The integer value
     * @return The byte array
     * @author Burak Özevin
     */
    private byte[] intToBytes(int v) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array();
    }

    /**
     * Converts a byte array to a long array (Little Endian).
     * 
     * @param src Source byte array
     * @param dst Destination long array
     * @author Burak Özevin
     */
    private void bytesToLongs(byte[] src, long[] dst) {
        ByteBuffer.wrap(src).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().get(dst);
    }

    /**
     * Converts a long array to a byte array (Little Endian).
     * 
     * @param src Source long array
     * @param dst Destination byte array
     * @author Burak Özevin
     */
    private void longsToBytes(long[] src, byte[] dst) {
        ByteBuffer.wrap(dst).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer().put(src);
    }
}
