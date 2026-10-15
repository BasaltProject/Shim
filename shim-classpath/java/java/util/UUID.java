/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.Serializable;

public final class UUID implements Serializable, Comparable<UUID> {
  private static final long serialVersionUID = -4856846379924848570L;

  private final long mostSigBits;
  private final long leastSigBits;

  public UUID(long mostSigBits, long leastSigBits) {
    this.mostSigBits = mostSigBits;
    this.leastSigBits = leastSigBits;
  }

  private UUID(byte[] data) {
    long msb = 0;
    long lsb = 0;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (data[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (data[i] & 0xff);
    }
    this.mostSigBits = msb;
    this.leastSigBits = lsb;
  }

  public static UUID randomUUID() {
    byte[] array = new byte[16];
    new Random().nextBytes(array);

    array[6] &= 0x0f;
    array[6] |= 0x40;
    array[8] &= 0x3f;
    array[8] |= 0x80;

    return new UUID(array);
  }

  public static UUID nameUUIDFromBytes(byte[] name) {
    byte[] md5Bytes = computeMD5(name);
    md5Bytes[6] &= 0x0f;
    md5Bytes[6] |= 0x30;
    md5Bytes[8] &= 0x3f;
    md5Bytes[8] |= 0x80;
    return new UUID(md5Bytes);
  }

  public static UUID fromString(String name) {
    String[] components = name.split("-");
    if (components.length != 5) {
      throw new IllegalArgumentException("Invalid UUID string: " + name);
    }

    long mostSigBits = Long.parseUnsignedLong(components[0], 16);
    mostSigBits <<= 16;
    mostSigBits |= Long.parseUnsignedLong(components[1], 16);
    mostSigBits <<= 16;
    mostSigBits |= Long.parseUnsignedLong(components[2], 16);

    long leastSigBits = Long.parseUnsignedLong(components[3], 16);
    leastSigBits <<= 48;
    leastSigBits |= Long.parseUnsignedLong(components[4], 16);

    return new UUID(mostSigBits, leastSigBits);
  }

  public long getLeastSignificantBits() {
    return leastSigBits;
  }

  public long getMostSignificantBits() {
    return mostSigBits;
  }

  public int version() {
    return (int) ((mostSigBits >> 12) & 0x0f);
  }

  public int variant() {
    return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
  }

  public long timestamp() {
    if (version() != 1) {
      throw new UnsupportedOperationException("Not a time-based UUID");
    }
    return (mostSigBits >>> 32)
        | ((mostSigBits & 0xFFFF0000L) << 16)
        | ((mostSigBits & 0x0FFFL) << 48);
  }

  public int clockSequence() {
    if (version() != 1) {
      throw new UnsupportedOperationException("Not a time-based UUID");
    }
    return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
  }

  public long node() {
    if (version() != 1) {
      throw new UnsupportedOperationException("Not a time-based UUID");
    }
    return leastSigBits & 0x0000FFFFFFFFFFFFL;
  }

  @Override
  public String toString() {
    return digits(mostSigBits >> 32, 8) + "-"
        + digits(mostSigBits >> 16, 4) + "-"
        + digits(mostSigBits, 4) + "-"
        + digits(leastSigBits >> 48, 4) + "-"
        + digits(leastSigBits, 12);
  }

  private static String digits(long val, int digits) {
    long hi = 1L << (digits * 4);
    return Long.toHexString(hi | (val & (hi - 1))).substring(1);
  }

  @Override
  public int hashCode() {
    long hilo = mostSigBits ^ leastSigBits;
    return ((int) (hilo >> 32)) ^ (int) hilo;
  }

  @Override
  public boolean equals(Object obj) {
    if ((null == obj) || (obj.getClass() != UUID.class)) {
      return false;
    }
    UUID id = (UUID) obj;
    return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
  }

  @Override
  public int compareTo(UUID val) {
    return (this.mostSigBits < val.mostSigBits ? -1 :
           (this.mostSigBits > val.mostSigBits ? 1 :
           (this.leastSigBits < val.leastSigBits ? -1 :
           (this.leastSigBits > val.leastSigBits ? 1 :
            0))));
  }

  private static final int[] MD5_S = {
      7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,
      5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,
      4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,
      6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21
  };

  private static final int[] MD5_K = {
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

  private static byte[] computeMD5(byte[] message) {
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
        int f;
        int g;
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
        int sum = a + f + MD5_K[j] + w[g];
        int rotated = (sum << MD5_S[j]) | (sum >>> (32 - MD5_S[j]));
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
