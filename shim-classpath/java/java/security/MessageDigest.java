/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

public abstract class MessageDigest extends MessageDigestSpi {
  private final String algorithm;

  protected MessageDigest(String algorithm) {
    this.algorithm = algorithm;
  }

  public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "algorithm");
    String norm = algorithm.toUpperCase(Locale.ROOT);
    if ("MD5".equals(norm)) {
      return new MD5Digest();
    }
    if ("SHA-1".equals(norm) || "SHA1".equals(norm)) {
      return new SHA1Digest();
    }
    if ("SHA-256".equals(norm) || "SHA256".equals(norm)) {
      return new SHA256Digest();
    }
    throw new NoSuchAlgorithmException(algorithm + " MessageDigest not available");
  }

  public void update(byte input) {
    engineUpdate(input);
  }

  public void update(byte[] input, int offset, int len) {
    engineUpdate(input, offset, len);
  }

  public void update(byte[] input) {
    engineUpdate(input, 0, input.length);
  }

  public final void update(ByteBuffer input) {
    engineUpdate(input);
  }

  public byte[] digest() {
    return engineDigest();
  }

  public int digest(byte[] buf, int offset, int len) throws DigestException {
    return engineDigest(buf, offset, len);
  }

  public byte[] digest(byte[] input) {
    update(input);
    return digest();
  }

  public void reset() {
    engineReset();
  }

  public final String getAlgorithm() {
    return algorithm;
  }

  public final int getDigestLength() {
    return engineGetDigestLength();
  }

  public static boolean isEqual(byte[] digesta, byte[] digestb) {
    if (digesta == digestb) return true;
    if (digesta == null || digestb == null) return false;
    if (digesta.length != digestb.length) return false;
    int result = 0;
    for (int i = 0; i < digesta.length; i++) {
      result |= digesta[i] ^ digestb[i];
    }
    return result == 0;
  }

  @Override
  public String toString() {
    return algorithm + " Message Digest";
  }

  private static final class MD5Digest extends MessageDigest {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private static final int[] S = {
      7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,
      5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,
      4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,
      6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21
    };

    private static final int[] K = {
      0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
      0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
      0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
      0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
      0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
      0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
      0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
      0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
      0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
      0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
      0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05,
      0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
      0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
      0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
      0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
      0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391
    };

    MD5Digest() {
      super("MD5");
    }

    @Override
    protected int engineGetDigestLength() {
      return 16;
    }

    @Override
    protected void engineUpdate(byte input) {
      buffer.write(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
      buffer.write(input, offset, len);
    }

    @Override
    protected void engineReset() {
      buffer.reset();
    }

    @Override
    protected byte[] engineDigest() {
      byte[] message = buffer.toByteArray();
      engineReset();
      int messageLenBytes = message.length;
      int numBlocks = ((messageLenBytes + 8) >>> 6) + 1;
      int totalLen = numBlocks << 6;
      byte[] padding = new byte[totalLen - messageLenBytes];
      padding[0] = (byte) 0x80;
      long messageLenBits = (long) messageLenBytes << 3;
      for (int i = 0; i < 8; i++) {
        padding[padding.length - 8 + i] = (byte) ((messageLenBits >>> (8 * i)) & 0xff);
      }
      int a0 = 0x67452301;
      int b0 = 0xefcdab89;
      int c0 = 0x98badcfe;
      int d0 = 0x10325476;
      int[] w = new int[16];
      for (int i = 0; i < numBlocks; i++) {
        int blockOffset = i << 6;
        for (int j = 0; j < 16; j++) {
          int byteOffset = blockOffset + (j << 2);
          int b00 = (byteOffset < messageLenBytes) ? (message[byteOffset] & 0xff) : (padding[byteOffset - messageLenBytes] & 0xff);
          int b01 = (byteOffset + 1 < messageLenBytes) ? (message[byteOffset + 1] & 0xff) : (padding[byteOffset + 1 - messageLenBytes] & 0xff);
          int b02 = (byteOffset + 2 < messageLenBytes) ? (message[byteOffset + 2] & 0xff) : (padding[byteOffset + 2 - messageLenBytes] & 0xff);
          int b03 = (byteOffset + 3 < messageLenBytes) ? (message[byteOffset + 3] & 0xff) : (padding[byteOffset + 3 - messageLenBytes] & 0xff);
          w[j] = b00 | (b01 << 8) | (b02 << 16) | (b03 << 24);
        }
        int a = a0;
        int b = b0;
        int c = c0;
        int d = d0;
        for (int j = 0; j < 64; j++) {
          int f, g;
          if (j < 16) {
            f = (b & c) | ((~b) & d);
            g = j;
          } else if (j < 32) {
            f = (d & b) | ((~d) & c);
            g = (5 * j + 1) % 16;
          } else if (j < 48) {
            f = b ^ c ^ d;
            g = (3 * j + 5) % 16;
          } else {
            f = c ^ (b | (~d));
            g = (7 * j) % 16;
          }
          int temp = d;
          d = c;
          c = b;
          int sum = a + f + K[j] + w[g];
          int rotated = (sum << S[j]) | (sum >>> (32 - S[j]));
          b = b + rotated;
          a = temp;
        }
        a0 += a;
        b0 += b;
        c0 += c;
        d0 += d;
      }
      byte[] md5 = new byte[16];
      int[] words = {a0, b0, c0, d0};
      for (int i = 0; i < 4; i++) {
        md5[i * 4] = (byte) (words[i] & 0xff);
        md5[i * 4 + 1] = (byte) ((words[i] >>> 8) & 0xff);
        md5[i * 4 + 2] = (byte) ((words[i] >>> 16) & 0xff);
        md5[i * 4 + 3] = (byte) ((words[i] >>> 24) & 0xff);
      }
      return md5;
    }
  }

  private static final class SHA1Digest extends MessageDigest {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    SHA1Digest() {
      super("SHA-1");
    }

    @Override
    protected int engineGetDigestLength() {
      return 20;
    }

    @Override
    protected void engineUpdate(byte input) {
      buffer.write(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
      buffer.write(input, offset, len);
    }

    @Override
    protected void engineReset() {
      buffer.reset();
    }

    @Override
    protected byte[] engineDigest() {
      byte[] message = buffer.toByteArray();
      engineReset();
      int messageLenBytes = message.length;
      int numBlocks = ((messageLenBytes + 8) >>> 6) + 1;
      int totalLen = numBlocks << 6;
      byte[] padded = new byte[totalLen];
      System.arraycopy(message, 0, padded, 0, messageLenBytes);
      padded[messageLenBytes] = (byte) 0x80;
      long messageLenBits = (long) messageLenBytes << 3;
      for (int i = 0; i < 8; i++) {
        padded[totalLen - 8 + i] = (byte) ((messageLenBits >>> (8 * (7 - i))) & 0xff);
      }

      int h0 = 0x67452301;
      int h1 = 0xEFCDAB89;
      int h2 = 0x98BADCFE;
      int h3 = 0x10325476;
      int h4 = 0xC3D2E1F0;

      int[] w = new int[80];
      for (int i = 0; i < numBlocks; i++) {
        int blockOffset = i << 6;
        for (int t = 0; t < 16; t++) {
          int ofs = blockOffset + (t << 2);
          w[t] = ((padded[ofs] & 0xFF) << 24)
               | ((padded[ofs + 1] & 0xFF) << 16)
               | ((padded[ofs + 2] & 0xFF) << 8)
               | (padded[ofs + 3] & 0xFF);
        }
        for (int t = 16; t < 80; t++) {
          int val = w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16];
          w[t] = (val << 1) | (val >>> 31);
        }

        int a = h0;
        int b = h1;
        int c = h2;
        int d = h3;
        int e = h4;

        for (int t = 0; t < 80; t++) {
          int f, k;
          if (t < 20) {
            f = (b & c) | ((~b) & d);
            k = 0x5A827999;
          } else if (t < 40) {
            f = b ^ c ^ d;
            k = 0x6ED9EBA1;
          } else if (t < 60) {
            f = (b & c) | (b & d) | (c & d);
            k = 0x8F1BBCDC;
          } else {
            f = b ^ c ^ d;
            k = 0xCA62C1D6;
          }
          int temp = ((a << 5) | (a >>> 27)) + f + e + k + w[t];
          e = d;
          d = c;
          c = (b << 30) | (b >>> 2);
          b = a;
          a = temp;
        }

        h0 += a;
        h1 += b;
        h2 += c;
        h3 += d;
        h4 += e;
      }

      byte[] digest = new byte[20];
      int[] h = {h0, h1, h2, h3, h4};
      for (int i = 0; i < 5; i++) {
        digest[i * 4] = (byte) ((h[i] >>> 24) & 0xFF);
        digest[i * 4 + 1] = (byte) ((h[i] >>> 16) & 0xFF);
        digest[i * 4 + 2] = (byte) ((h[i] >>> 8) & 0xFF);
        digest[i * 4 + 3] = (byte) (h[i] & 0xFF);
      }
      return digest;
    }
  }

  private static final class SHA256Digest extends MessageDigest {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private static final int[] K = {
      0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
      0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
      0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
      0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
      0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
      0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
      0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
      0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
      0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
      0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
      0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
      0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
      0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
      0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
      0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
      0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    };

    SHA256Digest() {
      super("SHA-256");
    }

    @Override
    protected int engineGetDigestLength() {
      return 32;
    }

    @Override
    protected void engineUpdate(byte input) {
      buffer.write(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
      buffer.write(input, offset, len);
    }

    @Override
    protected void engineReset() {
      buffer.reset();
    }

    @Override
    protected byte[] engineDigest() {
      byte[] message = buffer.toByteArray();
      engineReset();
      int messageLenBytes = message.length;
      int numBlocks = ((messageLenBytes + 8) >>> 6) + 1;
      int totalLen = numBlocks << 6;
      byte[] padded = new byte[totalLen];
      System.arraycopy(message, 0, padded, 0, messageLenBytes);
      padded[messageLenBytes] = (byte) 0x80;
      long messageLenBits = (long) messageLenBytes << 3;
      for (int i = 0; i < 8; i++) {
        padded[totalLen - 8 + i] = (byte) ((messageLenBits >>> (8 * (7 - i))) & 0xff);
      }

      int h0 = 0x6a09e667;
      int h1 = 0xbb67ae85;
      int h2 = 0x3c6ef372;
      int h3 = 0xa54ff53a;
      int h4 = 0x510e527f;
      int h5 = 0x9b05688c;
      int h6 = 0x1f83d9ab;
      int h7 = 0x5be0cd19;

      int[] w = new int[64];
      for (int i = 0; i < numBlocks; i++) {
        int blockOffset = i << 6;
        for (int t = 0; t < 16; t++) {
          int ofs = blockOffset + (t << 2);
          w[t] = ((padded[ofs] & 0xFF) << 24)
               | ((padded[ofs + 1] & 0xFF) << 16)
               | ((padded[ofs + 2] & 0xFF) << 8)
               | (padded[ofs + 3] & 0xFF);
        }
        for (int t = 16; t < 64; t++) {
          int s0 = (Integer.rotateRight(w[t - 15], 7)) ^ (Integer.rotateRight(w[t - 15], 18)) ^ (w[t - 15] >>> 3);
          int s1 = (Integer.rotateRight(w[t - 2], 17)) ^ (Integer.rotateRight(w[t - 2], 19)) ^ (w[t - 2] >>> 10);
          w[t] = w[t - 16] + s0 + w[t - 7] + s1;
        }

        int a = h0;
        int b = h1;
        int c = h2;
        int d = h3;
        int e = h4;
        int f = h5;
        int g = h6;
        int h = h7;

        for (int t = 0; t < 64; t++) {
          int s1 = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25);
          int ch = (e & f) ^ ((~e) & g);
          int temp1 = h + s1 + ch + K[t] + w[t];
          int s0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
          int maj = (a & b) ^ (a & c) ^ (b & c);
          int temp2 = s0 + maj;

          h = g;
          g = f;
          f = e;
          e = d + temp1;
          d = c;
          c = b;
          b = a;
          a = temp1 + temp2;
        }

        h0 += a;
        h1 += b;
        h2 += c;
        h3 += d;
        h4 += e;
        h5 += f;
        h6 += g;
        h7 += h;
      }

      byte[] digest = new byte[32];
      int[] hValues = {h0, h1, h2, h3, h4, h5, h6, h7};
      for (int i = 0; i < 8; i++) {
        digest[i * 4] = (byte) ((hValues[i] >>> 24) & 0xFF);
        digest[i * 4 + 1] = (byte) ((hValues[i] >>> 16) & 0xFF);
        digest[i * 4 + 2] = (byte) ((hValues[i] >>> 8) & 0xFF);
        digest[i * 4 + 3] = (byte) (hValues[i] & 0xFF);
      }
      return digest;
    }
  }
}
