/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;

final class AESCrypt extends SymmetricCipher {
  private static final int[] kSbox = new int[256];
  private static final int[] kInvSbox = new int[256];
  private static final int[] kTe0 = new int[256];
  private static final int[] kTe1 = new int[256];
  private static final int[] kTe2 = new int[256];
  private static final int[] kTe3 = new int[256];
  private static final int[] kTd0 = new int[256];
  private static final int[] kTd1 = new int[256];
  private static final int[] kTd2 = new int[256];
  private static final int[] kTd3 = new int[256];
  private static final int[] kRcon = new int[11];

  static {
    int[] exp = new int[256];
    int[] log = new int[256];
    int x = 1;
    for (int i = 0; i < 255; i++) {
      exp[i] = x;
      log[x] = i;
      int xt = x << 1;
      if ((xt & 0x100) != 0) {
        xt ^= 0x11b;
      }
      x ^= xt;
    }
    for (int i = 0; i < 256; i++) {
      int inv = i == 0 ? 0 : exp[(255 - log[i]) % 255];
      int s = inv;
      s ^= ((inv << 1) | (inv >> 7)) & 0xff;
      s ^= ((inv << 2) | (inv >> 6)) & 0xff;
      s ^= ((inv << 3) | (inv >> 5)) & 0xff;
      s ^= ((inv << 4) | (inv >> 4)) & 0xff;
      s ^= 0x63;
      kSbox[i] = s & 0xff;
      kInvSbox[s & 0xff] = i;
    }
    for (int i = 0; i < 256; i++) {
      int s = kSbox[i];
      int s2 = mul2(s);
      int s3 = s2 ^ s;
      int t = (s2 << 24) | (s << 16) | (s << 8) | s3;
      kTe0[i] = t;
      kTe1[i] = Integer.rotateRight(t, 8);
      kTe2[i] = Integer.rotateRight(t, 16);
      kTe3[i] = Integer.rotateRight(t, 24);
      int v = kInvSbox[i];
      int v9 = mulBy(v, 9);
      int vb = mulBy(v, 11);
      int vd = mulBy(v, 13);
      int ve = mulBy(v, 14);
      int d = (ve << 24) | (v9 << 16) | (vd << 8) | vb;
      kTd0[i] = d;
      kTd1[i] = Integer.rotateRight(d, 8);
      kTd2[i] = Integer.rotateRight(d, 16);
      kTd3[i] = Integer.rotateRight(d, 24);
    }
    int r = 1;
    for (int i = 1; i <= 10; i++) {
      kRcon[i] = r << 24;
      r = mul2(r);
    }
  }

  private static int mul2(int a) {
    int r = a << 1;
    if ((r & 0x100) != 0) {
      r ^= 0x11b;
    }
    return r & 0xff;
  }

  private static int mulBy(int a, int b) {
    int r = 0;
    int x = a;
    for (int i = 0; i < 8; i++) {
      if ((b & (1 << i)) != 0) {
        r ^= x;
      }
      x = mul2(x);
    }
    return r;
  }

  private int[] encKey;
  private int[] decKey;
  private int rounds;
  private byte[] lastKey;

  AESCrypt() {
  }

  @Override
  int getBlockSize() {
    return 16;
  }

  static boolean isKeySizeValid(int len) {
    return len == 16 || len == 24 || len == 32;
  }

  @Override
  void init(boolean decrypting, String algorithm, byte[] key) throws InvalidKeyException {
    if (!algorithm.equalsIgnoreCase("AES") && !algorithm.equalsIgnoreCase("Rijndael")) {
      throw new InvalidKeyException("Wrong algorithm: AES or Rijndael required");
    }
    if (!isKeySizeValid(key.length)) {
      throw new InvalidKeyException("Invalid AES key length: " + key.length + " bytes");
    }
    if (lastKey == null || !java.util.Arrays.equals(lastKey, key)) {
      expandKey(key);
      lastKey = key.clone();
    }
  }

  private void expandKey(byte[] key) {
    int nk = key.length / 4;
    rounds = nk + 6;
    int total = 4 * (rounds + 1);
    int[] w = new int[total];
    for (int i = 0; i < nk; i++) {
      w[i] = ((key[4 * i] & 0xff) << 24) | ((key[4 * i + 1] & 0xff) << 16) | ((key[4 * i + 2] & 0xff) << 8)
          | (key[4 * i + 3] & 0xff);
    }
    for (int i = nk; i < total; i++) {
      int t = w[i - 1];
      if (i % nk == 0) {
        t = subWord(Integer.rotateLeft(t, 8)) ^ kRcon[i / nk];
      } else if (nk > 6 && i % nk == 4) {
        t = subWord(t);
      }
      w[i] = w[i - nk] ^ t;
    }
    encKey = w;
    int[] d = new int[total];
    for (int r = 0; r <= rounds; r++) {
      for (int c = 0; c < 4; c++) {
        int v = w[4 * (rounds - r) + c];
        if (r > 0 && r < rounds) {
          v = kTd0[kSbox[(v >>> 24) & 0xff]] ^ kTd1[kSbox[(v >>> 16) & 0xff]] ^ kTd2[kSbox[(v >>> 8) & 0xff]]
              ^ kTd3[kSbox[v & 0xff]];
        }
        d[4 * r + c] = v;
      }
    }
    decKey = d;
  }

  private static int subWord(int w) {
    return (kSbox[(w >>> 24) & 0xff] << 24) | (kSbox[(w >>> 16) & 0xff] << 16) | (kSbox[(w >>> 8) & 0xff] << 8)
        | kSbox[w & 0xff];
  }

  @Override
  void encryptBlock(byte[] in, int inOff, byte[] out, int outOff) {
    int[] k = encKey;
    int s0 = getInt(in, inOff) ^ k[0];
    int s1 = getInt(in, inOff + 4) ^ k[1];
    int s2 = getInt(in, inOff + 8) ^ k[2];
    int s3 = getInt(in, inOff + 12) ^ k[3];
    int ki = 4;
    for (int r = 1; r < rounds; r++) {
      int t0 = kTe0[s0 >>> 24] ^ kTe1[(s1 >>> 16) & 0xff] ^ kTe2[(s2 >>> 8) & 0xff] ^ kTe3[s3 & 0xff] ^ k[ki];
      int t1 = kTe0[s1 >>> 24] ^ kTe1[(s2 >>> 16) & 0xff] ^ kTe2[(s3 >>> 8) & 0xff] ^ kTe3[s0 & 0xff] ^ k[ki + 1];
      int t2 = kTe0[s2 >>> 24] ^ kTe1[(s3 >>> 16) & 0xff] ^ kTe2[(s0 >>> 8) & 0xff] ^ kTe3[s1 & 0xff] ^ k[ki + 2];
      int t3 = kTe0[s3 >>> 24] ^ kTe1[(s0 >>> 16) & 0xff] ^ kTe2[(s1 >>> 8) & 0xff] ^ kTe3[s2 & 0xff] ^ k[ki + 3];
      s0 = t0;
      s1 = t1;
      s2 = t2;
      s3 = t3;
      ki += 4;
    }
    int o0 = ((kSbox[s0 >>> 24] << 24) | (kSbox[(s1 >>> 16) & 0xff] << 16) | (kSbox[(s2 >>> 8) & 0xff] << 8)
        | kSbox[s3 & 0xff]) ^ k[ki];
    int o1 = ((kSbox[s1 >>> 24] << 24) | (kSbox[(s2 >>> 16) & 0xff] << 16) | (kSbox[(s3 >>> 8) & 0xff] << 8)
        | kSbox[s0 & 0xff]) ^ k[ki + 1];
    int o2 = ((kSbox[s2 >>> 24] << 24) | (kSbox[(s3 >>> 16) & 0xff] << 16) | (kSbox[(s0 >>> 8) & 0xff] << 8)
        | kSbox[s1 & 0xff]) ^ k[ki + 2];
    int o3 = ((kSbox[s3 >>> 24] << 24) | (kSbox[(s0 >>> 16) & 0xff] << 16) | (kSbox[(s1 >>> 8) & 0xff] << 8)
        | kSbox[s2 & 0xff]) ^ k[ki + 3];
    putInt(out, outOff, o0);
    putInt(out, outOff + 4, o1);
    putInt(out, outOff + 8, o2);
    putInt(out, outOff + 12, o3);
  }

  @Override
  void decryptBlock(byte[] in, int inOff, byte[] out, int outOff) {
    int[] k = decKey;
    int s0 = getInt(in, inOff) ^ k[0];
    int s1 = getInt(in, inOff + 4) ^ k[1];
    int s2 = getInt(in, inOff + 8) ^ k[2];
    int s3 = getInt(in, inOff + 12) ^ k[3];
    int ki = 4;
    for (int r = 1; r < rounds; r++) {
      int t0 = kTd0[s0 >>> 24] ^ kTd1[(s3 >>> 16) & 0xff] ^ kTd2[(s2 >>> 8) & 0xff] ^ kTd3[s1 & 0xff] ^ k[ki];
      int t1 = kTd0[s1 >>> 24] ^ kTd1[(s0 >>> 16) & 0xff] ^ kTd2[(s3 >>> 8) & 0xff] ^ kTd3[s2 & 0xff] ^ k[ki + 1];
      int t2 = kTd0[s2 >>> 24] ^ kTd1[(s1 >>> 16) & 0xff] ^ kTd2[(s0 >>> 8) & 0xff] ^ kTd3[s3 & 0xff] ^ k[ki + 2];
      int t3 = kTd0[s3 >>> 24] ^ kTd1[(s2 >>> 16) & 0xff] ^ kTd2[(s1 >>> 8) & 0xff] ^ kTd3[s0 & 0xff] ^ k[ki + 3];
      s0 = t0;
      s1 = t1;
      s2 = t2;
      s3 = t3;
      ki += 4;
    }
    int o0 = ((kInvSbox[s0 >>> 24] << 24) | (kInvSbox[(s3 >>> 16) & 0xff] << 16) | (kInvSbox[(s2 >>> 8) & 0xff] << 8)
        | kInvSbox[s1 & 0xff]) ^ k[ki];
    int o1 = ((kInvSbox[s1 >>> 24] << 24) | (kInvSbox[(s0 >>> 16) & 0xff] << 16) | (kInvSbox[(s3 >>> 8) & 0xff] << 8)
        | kInvSbox[s2 & 0xff]) ^ k[ki + 1];
    int o2 = ((kInvSbox[s2 >>> 24] << 24) | (kInvSbox[(s1 >>> 16) & 0xff] << 16) | (kInvSbox[(s0 >>> 8) & 0xff] << 8)
        | kInvSbox[s3 & 0xff]) ^ k[ki + 2];
    int o3 = ((kInvSbox[s3 >>> 24] << 24) | (kInvSbox[(s2 >>> 16) & 0xff] << 16) | (kInvSbox[(s1 >>> 8) & 0xff] << 8)
        | kInvSbox[s0 & 0xff]) ^ k[ki + 3];
    putInt(out, outOff, o0);
    putInt(out, outOff + 4, o1);
    putInt(out, outOff + 8, o2);
    putInt(out, outOff + 12, o3);
  }

  private static int getInt(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private static void putInt(byte[] b, int o, int v) {
    b[o] = (byte) (v >>> 24);
    b[o + 1] = (byte) (v >>> 16);
    b[o + 2] = (byte) (v >>> 8);
    b[o + 3] = (byte) v;
  }
}
