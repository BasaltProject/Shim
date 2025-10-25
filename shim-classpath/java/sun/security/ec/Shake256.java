/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

final class Shake256 {
  private static final long[] kRc = {
    0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL, 0x8000000080008000L,
    0x000000000000808bL, 0x0000000080000001L, 0x8000000080008081L, 0x8000000000008009L,
    0x000000000000008aL, 0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
    0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L, 0x8000000000008003L,
    0x8000000000008002L, 0x8000000000000080L, 0x000000000000800aL, 0x800000008000000aL,
    0x8000000080008081L, 0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
  };
  private static final int[] kRot = {
    1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44
  };
  private static final int[] kPi = {
    10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
  };
  private static final int kRate = 136;

  private final long[] st = new long[25];
  private final byte[] buf = new byte[kRate];
  private int pos;

  void update(byte[] data) {
    for (byte b : data) {
      buf[pos++] = b;
      if (pos == kRate) {
        absorb();
      }
    }
  }

  byte[] digest(int length) {
    java.util.Arrays.fill(buf, pos, kRate, (byte) 0);
    buf[pos] ^= 0x1f;
    buf[kRate - 1] ^= (byte) 0x80;
    absorb();
    byte[] out = new byte[length];
    int done = 0;
    while (done < length) {
      int n = Math.min(kRate, length - done);
      for (int i = 0; i < n; i++) {
        out[done + i] = (byte) (st[i >>> 3] >>> (8 * (i & 7)));
      }
      done += n;
      if (done < length) {
        keccak();
      }
    }
    return out;
  }

  static byte[] hash(int length, byte[]... parts) {
    Shake256 s = new Shake256();
    for (byte[] p : parts) {
      s.update(p);
    }
    return s.digest(length);
  }

  private void absorb() {
    for (int i = 0; i < kRate / 8; i++) {
      long v = 0;
      for (int j = 7; j >= 0; j--) {
        v = (v << 8) | (buf[i * 8 + j] & 0xffL);
      }
      st[i] ^= v;
    }
    keccak();
    pos = 0;
    java.util.Arrays.fill(buf, (byte) 0);
  }

  private void keccak() {
    long[] a = st;
    long[] bc = new long[5];
    for (int round = 0; round < 24; round++) {
      for (int i = 0; i < 5; i++) {
        bc[i] = a[i] ^ a[i + 5] ^ a[i + 10] ^ a[i + 15] ^ a[i + 20];
      }
      for (int i = 0; i < 5; i++) {
        long t = bc[(i + 4) % 5] ^ Long.rotateLeft(bc[(i + 1) % 5], 1);
        for (int j = 0; j < 25; j += 5) {
          a[j + i] ^= t;
        }
      }
      long t = a[1];
      for (int i = 0; i < 24; i++) {
        int j = kPi[i];
        long tmp = a[j];
        a[j] = Long.rotateLeft(t, kRot[i]);
        t = tmp;
      }
      for (int j = 0; j < 25; j += 5) {
        for (int i = 0; i < 5; i++) {
          bc[i] = a[j + i];
        }
        for (int i = 0; i < 5; i++) {
          a[j + i] ^= (~bc[(i + 1) % 5]) & bc[(i + 2) % 5];
        }
      }
      a[0] ^= kRc[round];
    }
  }
}
