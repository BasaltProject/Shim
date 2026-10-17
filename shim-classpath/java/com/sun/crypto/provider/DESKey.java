/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;
import javax.crypto.SecretKey;

final class DESKey implements SecretKey {
  private static final long serialVersionUID = 7724250625251774316L;

  private byte[] key;

  DESKey(byte[] key) throws InvalidKeyException {
    this(key, 0);
  }

  DESKey(byte[] key, int offset) throws InvalidKeyException {
    if (key == null || key.length - offset < 8) {
      throw new InvalidKeyException("Wrong key size");
    }
    this.key = new byte[8];
    System.arraycopy(key, offset, this.key, 0, 8);
  }

  @Override
  public byte[] getEncoded() {
    return this.key.clone();
  }

  @Override
  public String getAlgorithm() {
    return "DES";
  }

  @Override
  public String getFormat() {
    return "RAW";
  }

  @Override
  public int hashCode() {
    int retval = 0;
    for (int i = 1; i < this.key.length; i++) {
      retval += this.key[i] * i;
    }
    return retval ^ "des".hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SecretKey)) {
      return false;
    }
    String thatAlg = ((SecretKey) obj).getAlgorithm();
    if (!(thatAlg.equalsIgnoreCase("DES"))) {
      return false;
    }
    byte[] thatKey = ((SecretKey) obj).getEncoded();
    boolean ret = java.security.MessageDigest.isEqual(this.key, thatKey);
    java.util.Arrays.fill(thatKey, (byte) 0x00);
    return ret;
  }
}
