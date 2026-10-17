/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;

final class DESedeCrypt extends SymmetricCipher {
  private final DESCrypt d1 = new DESCrypt();
  private final DESCrypt d2 = new DESCrypt();
  private final DESCrypt d3 = new DESCrypt();

  @Override
  int getBlockSize() {
    return 8;
  }

  @Override
  void init(boolean decrypting, String algorithm, byte[] key) throws InvalidKeyException {
    if (!algorithm.equalsIgnoreCase("DESede") && !algorithm.equalsIgnoreCase("TripleDES")) {
      throw new InvalidKeyException("Wrong algorithm: DESede or TripleDES required");
    }
    if (key.length != 24) {
      throw new InvalidKeyException("Wrong key size");
    }
    byte[] k1 = java.util.Arrays.copyOfRange(key, 0, 8);
    byte[] k2 = java.util.Arrays.copyOfRange(key, 8, 16);
    byte[] k3 = java.util.Arrays.copyOfRange(key, 16, 24);
    d1.initRaw(false, k1);
    d2.initRaw(false, k2);
    d3.initRaw(false, k3);
  }

  private static long load(byte[] b, int o) {
    long v = 0;
    for (int i = 0; i < 8; i++) {
      v = (v << 8) | (b[o + i] & 0xffL);
    }
    return v;
  }

  private static void store(byte[] b, int o, long v) {
    for (int i = 7; i >= 0; i--) {
      b[o + i] = (byte) v;
      v >>>= 8;
    }
  }

  @Override
  void encryptBlock(byte[] plain, int plainOffset, byte[] cipher, int cipherOffset) {
    long v = load(plain, plainOffset);
    v = d1.processBlock(v, false);
    v = d2.processBlock(v, true);
    v = d3.processBlock(v, false);
    store(cipher, cipherOffset, v);
  }

  @Override
  void decryptBlock(byte[] cipher, int cipherOffset, byte[] plain, int plainOffset) {
    long v = load(cipher, cipherOffset);
    v = d3.processBlock(v, true);
    v = d2.processBlock(v, false);
    v = d1.processBlock(v, true);
    store(plain, plainOffset, v);
  }
}
