/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

public final class SHA extends DigestBase {
  private int[] state = new int[5];
  private final int[] w = new int[80];

  public SHA() {
    super("SHA-1", 20, 64);
    implReset();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    SHA copy = (SHA) super.clone();
    copy.state = state.clone();
    return copy;
  }

  @Override
  void implReset() {
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;
    state[4] = 0xc3d2e1f0;
  }

  @Override
  void implDigest(byte[] out, int ofs) {
    padBlock(bytesProcessed << 3, true, 8);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        out[ofs + i * 4 + j] = (byte) (state[i] >>> (24 - 8 * j));
      }
    }
  }

  @Override
  void implCompress(byte[] buf, int ofs) {
    for (int i = 0; i < 16; i++) {
      int p = ofs + i * 4;
      w[i] = ((buf[p] & 0xff) << 24) | ((buf[p + 1] & 0xff) << 16) | ((buf[p + 2] & 0xff) << 8) | (buf[p + 3] & 0xff);
    }
    for (int i = 16; i < 80; i++) {
      w[i] = Integer.rotateLeft(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1);
    }
    int a = state[0];
    int b = state[1];
    int c = state[2];
    int d = state[3];
    int e = state[4];
    for (int i = 0; i < 80; i++) {
      int f;
      int k;
      if (i < 20) {
        f = (b & c) | (~b & d);
        k = 0x5a827999;
      } else if (i < 40) {
        f = b ^ c ^ d;
        k = 0x6ed9eba1;
      } else if (i < 60) {
        f = (b & c) | (b & d) | (c & d);
        k = 0x8f1bbcdc;
      } else {
        f = b ^ c ^ d;
        k = 0xca62c1d6;
      }
      int t = Integer.rotateLeft(a, 5) + f + e + k + w[i];
      e = d;
      d = c;
      c = Integer.rotateLeft(b, 30);
      b = a;
      a = t;
    }
    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;
    state[4] += e;
  }
}
