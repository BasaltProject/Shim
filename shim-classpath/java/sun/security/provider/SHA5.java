/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

abstract class SHA5 extends DigestBase {
  private static final long[] kK = {
    0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL,
    0xe9b5dba58189dbbcL, 0x3956c25bf348b538L, 0x59f111f1b605d019L,
    0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L, 0xd807aa98a3030242L,
    0x12835b0145706fbeL, 0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L,
    0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L,
    0xc19bf174cf692694L, 0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L,
    0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L, 0x2de92c6f592b0275L,
    0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L,
    0x983e5152ee66dfabL, 0xa831c66d2db43210L, 0xb00327c898fb213fL,
    0xbf597fc7beef0ee4L, 0xc6e00bf33da88fc2L, 0xd5a79147930aa725L,
    0x06ca6351e003826fL, 0x142929670a0e6e70L, 0x27b70a8546d22ffcL,
    0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL, 0x53380d139d95b3dfL,
    0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L,
    0x92722c851482353bL, 0xa2bfe8a14cf10364L, 0xa81a664bbc423001L,
    0xc24b8b70d0f89791L, 0xc76c51a30654be30L, 0xd192e819d6ef5218L,
    0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L,
    0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L, 0x2748774cdf8eeb99L,
    0x34b0bcb5e19b48a8L, 0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL,
    0x5b9cca4f7763e373L, 0x682e6ff3d6b2b8a3L, 0x748f82ee5defb2fcL,
    0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL,
    0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L,
    0xc67178f2e372532bL, 0xca273eceea26619cL, 0xd186b8c721c0c207L,
    0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L, 0x06f067aa72176fbaL,
    0x0a637dc5a2c898a6L, 0x113f9804bef90daeL, 0x1b710b35131c471bL,
    0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL,
    0x431d67c49c100d4cL, 0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL,
    0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L
  };

  private static final long[] kIv512 = {
    0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
    0x510e527fade682d1L, 0x9b05688c2b3e6c1fL, 0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L,
  };
  private static final long[] kIv384 = {
    0xcbbb9d5dc1059ed8L, 0x629a292a367cd507L, 0x9159015a3070dd17L, 0x152fecd8f70e5939L,
    0x67332667ffc00b31L, 0x8eb44a8768581511L, 0xdb0c2e0d64f98fa7L, 0x47b5481dbefa4fa4L,
  };

  private long[] state = new long[8];
  private final long[] w = new long[80];
  private final long[] iv;

  SHA5(String name, int digestLength, long[] iv) {
    super(name, digestLength, 128);
    this.iv = iv;
    implReset();
  }

  private static long[] truncatedIv(String label) {
    long[] seed = new long[8];
    for (int i = 0; i < 8; i++) {
      seed[i] = kIv512[i] ^ 0xa5a5a5a5a5a5a5a5L;
    }
    SHA5 h = new SHA512(seed);
    byte[] text = label.getBytes();
    h.engineUpdate(text, 0, text.length);
    byte[] out = h.engineDigest();
    long[] result = new long[8];
    for (int i = 0; i < 8; i++) {
      long v = 0;
      for (int j = 0; j < 8; j++) {
        v = (v << 8) | (out[i * 8 + j] & 0xffL);
      }
      result[i] = v;
    }
    return result;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    SHA5 copy = (SHA5) super.clone();
    copy.state = state.clone();
    return copy;
  }

  @Override
  void implReset() {
    System.arraycopy(iv, 0, state, 0, 8);
  }

  @Override
  void implDigest(byte[] out, int ofs) {
    long bits = bytesProcessed << 3;
    buffer[bufOfs++] = (byte) 0x80;
    if (bufOfs > 112) {
      java.util.Arrays.fill(buffer, bufOfs, 128, (byte) 0);
      implCompress(buffer, 0);
      bufOfs = 0;
    }
    java.util.Arrays.fill(buffer, bufOfs, 128, (byte) 0);
    for (int i = 0; i < 8; i++) {
      buffer[127 - i] = (byte) (bits >>> (8 * i));
    }
    implCompress(buffer, 0);
    bufOfs = 0;
    byte[] full = new byte[64];
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        full[i * 8 + j] = (byte) (state[i] >>> (56 - 8 * j));
      }
    }
    System.arraycopy(full, 0, out, ofs, engineGetDigestLength());
  }

  @Override
  void implCompress(byte[] buf, int ofs) {
    long[] w = this.w;
    for (int i = 0; i < 16; i++) {
      int p = ofs + i * 8;
      long v = 0;
      for (int j = 0; j < 8; j++) {
        v = (v << 8) | (buf[p + j] & 0xffL);
      }
      w[i] = v;
    }
    for (int i = 16; i < 80; i++) {
      long s0 = Long.rotateRight(w[i - 15], 1) ^ Long.rotateRight(w[i - 15], 8) ^ (w[i - 15] >>> 7);
      long s1 = Long.rotateRight(w[i - 2], 19) ^ Long.rotateRight(w[i - 2], 61) ^ (w[i - 2] >>> 6);
      w[i] = w[i - 16] + s0 + w[i - 7] + s1;
    }
    long a = state[0];
    long b = state[1];
    long c = state[2];
    long d = state[3];
    long e = state[4];
    long f = state[5];
    long g = state[6];
    long h = state[7];
    for (int i = 0; i < 80; i++) {
      long s1 = Long.rotateRight(e, 14) ^ Long.rotateRight(e, 18) ^ Long.rotateRight(e, 41);
      long ch = (e & f) ^ (~e & g);
      long t1 = h + s1 + ch + kK[i] + w[i];
      long s0 = Long.rotateRight(a, 28) ^ Long.rotateRight(a, 34) ^ Long.rotateRight(a, 39);
      long maj = (a & b) ^ (a & c) ^ (b & c);
      long t2 = s0 + maj;
      h = g;
      g = f;
      f = e;
      e = d + t1;
      d = c;
      c = b;
      b = a;
      a = t1 + t2;
    }
    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;
    state[4] += e;
    state[5] += f;
    state[6] += g;
    state[7] += h;
  }

  public static final class SHA512 extends SHA5 {
    public SHA512() {
      this(kIv512);
    }

    private SHA512(long[] iv) {
      super("SHA-512", 64, iv);
    }
  }

  public static final class SHA384 extends SHA5 {
    public SHA384() {
      super("SHA-384", 48, kIv384);
    }
  }

  public static final class SHA512_224 extends SHA5 {
    private static final long[] kIv = truncatedIv("SHA-512/224");

    public SHA512_224() {
      super("SHA-512/224", 28, kIv);
    }
  }

  public static final class SHA512_256 extends SHA5 {
    private static final long[] kIv = truncatedIv("SHA-512/256");

    public SHA512_256() {
      super("SHA-512/256", 32, kIv);
    }
  }
}
