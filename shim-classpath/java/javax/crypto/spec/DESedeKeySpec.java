/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

public class DESedeKeySpec implements KeySpec {
  public static final int DES_EDE_KEY_LEN = 24;

  private byte[] key;

  public DESedeKeySpec(byte[] key) throws InvalidKeyException {
    this(key, 0);
  }

  public DESedeKeySpec(byte[] key, int offset) throws InvalidKeyException {
    if (key.length - offset < 24) {
      throw new InvalidKeyException("Wrong key size");
    }
    this.key = new byte[24];
    System.arraycopy(key, offset, this.key, 0, 24);
  }

  public byte[] getKey() {
    return this.key.clone();
  }

  public static boolean isParityAdjusted(byte[] key, int offset) throws InvalidKeyException {
    if (key.length - offset < 24) {
      throw new InvalidKeyException("Wrong key size");
    }
    for (int i = 0; i < 24; i++) {
      if ((Integer.bitCount(key[offset + i] & 0xff) & 1) == 0) {
        return false;
      }
    }
    return true;
  }
}
