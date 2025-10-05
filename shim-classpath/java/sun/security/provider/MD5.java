/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.util.Arrays;

public final class MD5 extends DigestBase {
  private static final int[] kS = {
    7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21,
  };
  private static final int[] kK = new int[64];

  static {
    for (int i = 0; i < 64; i++) {
      kK[i] = (int) (long) Math.floor(Math.abs(Math.sin(i + 1)) * 4294967296.0);
    }
  }

  private int a;
  private int b;
  private int c;
  private int d;
  private final int[] x = new int[16];

  public MD5() {
    super("MD5", 16, 64);
    implReset();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    MD5 copy = (MD5) super.clone();
    return copy;
  }

  @Override
  void implReset() {
    a = 0x67452301;
    b = 0xefcdab89;
    c = 0x98badcfe;
    d = 0x10325476;
  }

  @Override
  void implDigest(byte[] out, int ofs) {
    long bits = bytesProcessed << 3;
    padBlock(bits, false, 8);
    int[] state = {a, b, c, d};
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out[ofs + i * 4 + j] = (byte) (state[i] >>> (8 * j));
      }
    }
  }

  @Override
  void implCompress(byte[] buf, int ofs) {
    for (int i = 0; i < 16; i++) {
      int p = ofs + i * 4;
      x[i] = (buf[p] & 0xff) | ((buf[p + 1] & 0xff) << 8) | ((buf[p + 2] & 0xff) << 16) | ((buf[p + 3] & 0xff) << 24);
    }
    int aa = a;
    int bb = b;
    int cc = c;
    int dd = d;
    for (int i = 0; i < 64; i++) {
      int f;
      int g;
      int round = i >>> 4;
      if (round == 0) {
        f = (bb & cc) | (~bb & dd);
        g = i;
      } else if (round == 1) {
        f = (dd & bb) | (~dd & cc);
        g = (5 * i + 1) & 15;
      } else if (round == 2) {
        f = bb ^ cc ^ dd;
        g = (3 * i + 5) & 15;
      } else {
        f = cc ^ (bb | ~dd);
        g = (7 * i) & 15;
      }
      int tmp = dd;
      dd = cc;
      cc = bb;
      bb = bb + Integer.rotateLeft(aa + f + kK[i] + x[g], kS[round * 4 + (i & 3)]);
      aa = tmp;
    }
    a += aa;
    b += bb;
    c += cc;
    d += dd;
  }
}
