/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.math.BigInteger;
import java.security.InvalidKeyException;

final class BlowfishCrypt extends SymmetricCipher {
  private static int[] kInitial;

  private final int[] p = new int[18];
  private final int[] s = new int[1024];

  private static synchronized int[] initial() {
    if (kInitial == null) {
      int words = 18 + 1024;
      int bits = words * 32 + 64;
      BigInteger one = BigInteger.ONE.shiftLeft(bits + 16);
      BigInteger pi = arctan(5, one).shiftLeft(4).subtract(arctan(239, one).shiftLeft(2)).shiftRight(16);
      int[] out = new int[words];
      for (int i = 0; i < words; i++) {
        out[i] = pi.shiftRight(bits - 32 * (i + 1)).intValue();
      }
      kInitial = out;
    }
    return kInitial;
  }

  private static BigInteger arctan(int x, BigInteger one) {
    BigInteger xx = BigInteger.valueOf((long) x * x);
    BigInteger term = one.divide(BigInteger.valueOf(x));
    BigInteger sum = term;
    int n = 1;
    boolean negative = true;
    while (term.signum() != 0) {
      term = term.divide(xx);
      n += 2;
      BigInteger part = term.divide(BigInteger.valueOf(n));
      sum = negative ? sum.subtract(part) : sum.add(part);
      negative = !negative;
    }
    return sum;
  }

  @Override
  int getBlockSize() {
    return 8;
  }

  @Override
  void init(boolean decrypting, String algorithm, byte[] key) throws InvalidKeyException {
    if (!algorithm.equalsIgnoreCase("Blowfish")) {
      throw new InvalidKeyException("Key algorithm must be Blowfish");
    }
    if (key.length < 1 || key.length > 56) {
      throw new InvalidKeyException("Key length must be between 1 and 56 bytes");
    }
    int[] init = initial();
    System.arraycopy(init, 0, p, 0, 18);
    System.arraycopy(init, 18, s, 0, 1024);
    int k = 0;
    for (int i = 0; i < 18; i++) {
      int w = 0;
      for (int j = 0; j < 4; j++) {
        w = (w << 8) | (key[k] & 0xff);
        k = (k + 1) % key.length;
      }
      p[i] ^= w;
    }
    int[] block = {0, 0};
    for (int i = 0; i < 18; i += 2) {
      encrypt(block);
      p[i] = block[0];
      p[i + 1] = block[1];
    }
    for (int i = 0; i < 1024; i += 2) {
      encrypt(block);
      s[i] = block[0];
      s[i + 1] = block[1];
    }
  }

  private int f(int x) {
    return ((s[x >>> 24] + s[256 + ((x >>> 16) & 0xff)]) ^ s[512 + ((x >>> 8) & 0xff)]) + s[768 + (x & 0xff)];
  }

  private void encrypt(int[] lr) {
    int l = lr[0];
    int r = lr[1];
    for (int i = 0; i < 16; i += 2) {
      l ^= p[i];
      r ^= f(l);
      r ^= p[i + 1];
      l ^= f(r);
    }
    lr[0] = r ^ p[17];
    lr[1] = l ^ p[16];
  }

  private void decrypt(int[] lr) {
    int l = lr[0];
    int r = lr[1];
    for (int i = 17; i > 1; i -= 2) {
      l ^= p[i];
      r ^= f(l);
      r ^= p[i - 1];
      l ^= f(r);
    }
    lr[0] = r ^ p[0];
    lr[1] = l ^ p[1];
  }

  private static int word(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private static void put(byte[] b, int o, int v) {
    b[o] = (byte) (v >>> 24);
    b[o + 1] = (byte) (v >>> 16);
    b[o + 2] = (byte) (v >>> 8);
    b[o + 3] = (byte) v;
  }

  @Override
  void encryptBlock(byte[] plain, int po, byte[] cipher, int co) {
    int[] lr = {word(plain, po), word(plain, po + 4)};
    encrypt(lr);
    put(cipher, co, lr[0]);
    put(cipher, co + 4, lr[1]);
  }

  @Override
  void decryptBlock(byte[] cipher, int co, byte[] plain, int po) {
    int[] lr = {word(cipher, co), word(cipher, co + 4)};
    decrypt(lr);
    put(plain, po, lr[0]);
    put(plain, po + 4, lr[1]);
  }
}
