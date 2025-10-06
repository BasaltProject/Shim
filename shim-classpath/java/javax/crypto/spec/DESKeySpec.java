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

public class DESKeySpec implements KeySpec {
  public static final int DES_KEY_LEN = 8;

  private byte[] key;

  private static final byte[][] kWeakKeys = {
    {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01},
    {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE},
    {(byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E},
    {(byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1},
    {(byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE},
    {(byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01},
    {(byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1},
    {(byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E},
    {(byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1},
    {(byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1, (byte) 0x01},
    {(byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE},
    {(byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E},
    {(byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E},
    {(byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E, (byte) 0x01},
    {(byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE},
    {(byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1},
  };

  public DESKeySpec(byte[] key) throws InvalidKeyException {
    this(key, 0);
  }

  public DESKeySpec(byte[] key, int offset) throws InvalidKeyException {
    if (key.length - offset < DES_KEY_LEN) {
      throw new InvalidKeyException("Wrong key size");
    }
    this.key = new byte[DES_KEY_LEN];
    System.arraycopy(key, offset, this.key, 0, DES_KEY_LEN);
  }

  public byte[] getKey() {
    return this.key.clone();
  }

  public static boolean isParityAdjusted(byte[] key, int offset) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("null key");
    }
    if (key.length - offset < DES_KEY_LEN) {
      throw new InvalidKeyException("Wrong key size");
    }
    for (int i = 0; i < DES_KEY_LEN; i++) {
      if ((Integer.bitCount(key[offset + i] & 0xff) & 1) == 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean isWeak(byte[] key, int offset) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("null key");
    }
    if (key.length - offset < DES_KEY_LEN) {
      throw new InvalidKeyException("Wrong key size");
    }
    for (int i = 0; i < kWeakKeys.length; i++) {
      boolean found = true;
      for (int j = 0; j < DES_KEY_LEN && found; j++) {
        if (kWeakKeys[i][j] != key[j + offset]) {
          found = false;
        }
      }
      if (found) {
        return found;
      }
    }
    return false;
  }
}
