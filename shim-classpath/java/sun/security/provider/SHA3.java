/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.util.Arrays;

abstract class SHA3 extends DigestBase {
  private static final long[] kRc = {
    0x0000000000000001L, 0x0000000000008082L, 0x800000000000808aL,
    0x8000000080008000L, 0x000000000000808bL, 0x0000000080000001L,
    0x8000000080008081L, 0x8000000000008009L, 0x000000000000008aL,
    0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
    0x000000008000808bL, 0x800000000000008bL, 0x8000000000008089L,
    0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L,
    0x000000000000800aL, 0x800000008000000aL, 0x8000000080008081L,
    0x8000000000008080L, 0x0000000080000001L, 0x8000000080008008L
  };
  private static final int[] kRot = {
    1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44,
  };
  private static final int[] kPi = {
    10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1,
  };

  private long[] st = new long[25];
  private final byte suffix;
  private final int outLength;

  SHA3(String name, int outLength, int rate, byte suffix) {
    super(name, outLength, rate);
    this.suffix = suffix;
    this.outLength = outLength;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    SHA3 copy = (SHA3) super.clone();
    copy.st = st.clone();
    return copy;
  }

  @Override
  void implReset() {
    Arrays.fill(st, 0L);
  }

  static void keccak(long[] a) {
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

  @Override
  void implCompress(byte[] buf, int ofs) {
    int lanes = buffer.length / 8;
    for (int i = 0; i < lanes; i++) {
      long v = 0;
      for (int j = 7; j >= 0; j--) {
        v = (v << 8) | (buf[ofs + i * 8 + j] & 0xffL);
      }
      st[i] ^= v;
    }
    keccak(st);
  }

  @Override
  void implDigest(byte[] out, int ofs) {
    int rate = buffer.length;
    Arrays.fill(buffer, bufOfs, rate, (byte) 0);
    buffer[bufOfs] ^= suffix;
    buffer[rate - 1] ^= (byte) 0x80;
    implCompress(buffer, 0);
    bufOfs = 0;
    int produced = 0;
    while (produced < outLength) {
      int n = Math.min(rate, outLength - produced);
      for (int i = 0; i < n; i++) {
        out[ofs + produced + i] = (byte) (st[i >>> 3] >>> (8 * (i & 7)));
      }
      produced += n;
      if (produced < outLength) {
        keccak(st);
      }
    }
  }

  public static final class SHA224 extends SHA3 {
    public SHA224() {
      super("SHA3-224", 28, 144, (byte) 0x06);
    }
  }

  public static final class SHA256 extends SHA3 {
    public SHA256() {
      super("SHA3-256", 32, 136, (byte) 0x06);
    }
  }

  public static final class SHA384 extends SHA3 {
    public SHA384() {
      super("SHA3-384", 48, 104, (byte) 0x06);
    }
  }

  public static final class SHA512 extends SHA3 {
    public SHA512() {
      super("SHA3-512", 64, 72, (byte) 0x06);
    }
  }

  public static final class SHAKE128Hash extends SHA3 {
    public SHAKE128Hash() {
      super("SHAKE128-256", 32, 168, (byte) 0x1f);
    }
  }

  public static final class SHAKE256Hash extends SHA3 {
    public SHAKE256Hash() {
      super("SHAKE256-512", 64, 136, (byte) 0x1f);
    }
  }
}
