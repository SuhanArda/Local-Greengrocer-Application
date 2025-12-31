package com.greengrocer.utils;

/**
 * Minimal implementation of Blake2b for Argon2.
 * Based on RFC 7693.
 */
public class Blake2b {

    private static final long[] IV = {
            0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
            0x510e527fade682d1L, 0x9b05688c2b3e6c1fL, 0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L
    };

    private static final byte[][] SIGMA = {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
            { 14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 },
            { 11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4 },
            { 7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8 },
            { 9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13 },
            { 2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9 },
            { 12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11 },
            { 13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10 },
            { 6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5 },
            { 10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0 },
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
            { 14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 }
    };

    private final long[] h = new long[8];
    private final long[] m = new long[16];
    private final byte[] buffer = new byte[128];
    private int bufferLen = 0;
    private long t0 = 0; // Low 64 bits of offset
    private long t1 = 0; // High 64 bits of offset
    private final int outlen;

    public Blake2b(int outlen) {
        this.outlen = outlen;
        if (outlen < 1 || outlen > 64)
            throw new IllegalArgumentException("Invalid output length");

        System.arraycopy(IV, 0, h, 0, 8);
        h[0] ^= 0x01010000 | outlen;
    }

    public void update(byte[] in) {
        if (in == null)
            return;
        update(in, 0, in.length);
    }

    public void update(byte[] in, int offset, int len) {
        for (int i = 0; i < len; i++) {
            if (bufferLen == 128) {
                t0 += 128;
                if (t0 == 0)
                    t1++; // Overflow
                compress(buffer, 0);
                bufferLen = 0;
            }
            buffer[bufferLen++] = in[offset + i];
        }
    }

    public void update(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) (value);
        b[1] = (byte) (value >> 8);
        b[2] = (byte) (value >> 16);
        b[3] = (byte) (value >> 24);
        update(b);
    }

    public byte[] digest() {
        t0 += bufferLen;
        if (t0 < bufferLen)
            t1++; // Overflow

        while (bufferLen < 128) {
            buffer[bufferLen++] = 0;
        }
        compress(buffer, 0, true);

        byte[] out = new byte[outlen];
        for (int i = 0; i < outlen && i < 64; i++) {
            out[i] = (byte) (h[i >> 3] >> (8 * (i & 7)));
        }
        return out;
    }

    private void compress(byte[] buf, int off) {
        compress(buf, off, false);
    }

    private void compress(byte[] buf, int off, boolean isLast) {
        long[] v = new long[16];
        System.arraycopy(h, 0, v, 0, 8);
        System.arraycopy(IV, 0, v, 8, 8);

        v[12] ^= t0;
        v[13] ^= t1;
        if (isLast) {
            v[14] = ~v[14];
        }

        for (int i = 0; i < 16; i++) {
            m[i] = bytesToLong(buf, off + i * 8);
        }

        for (int i = 0; i < 12; i++) {
            mix(v, 0, 4, 8, 12, m[SIGMA[i][0]], m[SIGMA[i][1]]);
            mix(v, 1, 5, 9, 13, m[SIGMA[i][2]], m[SIGMA[i][3]]);
            mix(v, 2, 6, 10, 14, m[SIGMA[i][4]], m[SIGMA[i][5]]);
            mix(v, 3, 7, 11, 15, m[SIGMA[i][6]], m[SIGMA[i][7]]);
            mix(v, 0, 5, 10, 15, m[SIGMA[i][8]], m[SIGMA[i][9]]);
            mix(v, 1, 6, 11, 12, m[SIGMA[i][10]], m[SIGMA[i][11]]);
            mix(v, 2, 7, 8, 13, m[SIGMA[i][12]], m[SIGMA[i][13]]);
            mix(v, 3, 4, 9, 14, m[SIGMA[i][14]], m[SIGMA[i][15]]);
        }

        for (int i = 0; i < 8; i++) {
            h[i] ^= v[i] ^ v[i + 8];
        }
    }

    private void mix(long[] v, int a, int b, int c, int d, long x, long y) {
        v[a] = v[a] + v[b] + x;
        v[d] = Long.rotateRight(v[d] ^ v[a], 32);
        v[c] = v[c] + v[d];
        v[b] = Long.rotateRight(v[b] ^ v[c], 24);
        v[a] = v[a] + v[b] + y;
        v[d] = Long.rotateRight(v[d] ^ v[a], 16);
        v[c] = v[c] + v[d];
        v[b] = Long.rotateRight(v[b] ^ v[c], 63);
    }

    private static long bytesToLong(byte[] b, int off) {
        return ((long) b[off] & 0xff) |
                ((long) b[off + 1] & 0xff) << 8 |
                ((long) b[off + 2] & 0xff) << 16 |
                ((long) b[off + 3] & 0xff) << 24 |
                ((long) b[off + 4] & 0xff) << 32 |
                ((long) b[off + 5] & 0xff) << 40 |
                ((long) b[off + 6] & 0xff) << 48 |
                ((long) b[off + 7] & 0xff) << 56;
    }
}
