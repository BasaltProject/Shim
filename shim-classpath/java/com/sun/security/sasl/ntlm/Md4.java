/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl.ntlm;

/** MD4 digest (RFC 1320), used only for NTLM password hashes. */
final class Md4 {
  private Md4() {
  }

  private static int rol(int x, int n) {
    return (x << n) | (x >>> (32 - n));
  }

  static byte[] digest(byte[] in) {
    int padded = ((in.length + 8) / 64 + 1) * 64;
    byte[] m = new byte[padded];
    System.arraycopy(in, 0, m, 0, in.length);
    m[in.length] = (byte) 0x80;
    long bits = (long) in.length * 8;
    for (int i = 0; i < 8; i++) {
      m[padded - 8 + i] = (byte) (bits >>> (8 * i));
    }
    int a = 0x67452301;
    int b = 0xefcdab89;
    int c = 0x98badcfe;
    int d = 0x10325476;
    int[] x = new int[16];
    for (int off = 0; off < padded; off += 64) {
      for (int i = 0; i < 16; i++) {
        x[i] = (m[off + 4 * i] & 0xff) | ((m[off + 4 * i + 1] & 0xff) << 8) | ((m[off + 4 * i + 2] & 0xff) << 16)
            | ((m[off + 4 * i + 3] & 0xff) << 24);
      }
      int aa = a;
      int bb = b;
      int cc = c;
      int dd = d;
      int[] s1 = {3, 7, 11, 19};
      for (int i = 0; i < 16; i++) {
        int f = (b & c) | (~b & d);
        int t = rol(a + f + x[i], s1[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      int[] s2 = {3, 5, 9, 13};
      for (int i = 0; i < 16; i++) {
        int k = (i % 4) * 4 + i / 4;
        int g = (b & c) | (b & d) | (c & d);
        int t = rol(a + g + x[k] + 0x5a827999, s2[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      int[] s3 = {3, 9, 11, 15};
      int[] order = {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15};
      for (int i = 0; i < 16; i++) {
        int h = b ^ c ^ d;
        int t = rol(a + h + x[order[i]] + 0x6ed9eba1, s3[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      a += aa;
      b += bb;
      c += cc;
      d += dd;
    }
    byte[] out = new byte[16];
    int[] r = {a, b, c, d};
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out[4 * i + j] = (byte) (r[i] >>> (8 * j));
      }
    }
    return out;
  }
}
