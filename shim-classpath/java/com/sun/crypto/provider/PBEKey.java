/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.SecretKey;

final class PBEKey implements SecretKey {
  private static final long serialVersionUID = -2234768909660948176L;

  private final byte[] key;
  private final String type;

  PBEKey(char[] password, String type) {
    this.key = new String(password).getBytes(StandardCharsets.UTF_8);
    this.type = type;
  }

  @Override
  public byte[] getEncoded() {
    return key.clone();
  }

  @Override
  public String getAlgorithm() {
    return type;
  }

  @Override
  public String getFormat() {
    return "RAW";
  }

  char[] password() {
    return new String(key, StandardCharsets.UTF_8).toCharArray();
  }

  @Override
  public int hashCode() {
    int retval = 0;
    for (int i = 1; i < key.length; i++) {
      retval += key[i] * i;
    }
    return retval ^ getAlgorithm().toLowerCase(Locale.ENGLISH).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SecretKey)) {
      return false;
    }
    SecretKey that = (SecretKey) obj;
    if (!that.getAlgorithm().equalsIgnoreCase(type)) {
      return false;
    }
    byte[] thatKey = that.getEncoded();
    boolean ret = MessageDigest.isEqual(key, thatKey);
    Arrays.fill(thatKey, (byte) 0x00);
    return ret;
  }
}
