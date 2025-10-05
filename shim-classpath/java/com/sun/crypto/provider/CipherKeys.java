/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;
import java.security.Key;

final class CipherKeys {
  private CipherKeys() {
  }

  static byte[] rawKeyBytes(Key key) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("Key must not be null");
    }
    if (!"RAW".equalsIgnoreCase(key.getFormat())) {
      throw new InvalidKeyException("Wrong format: RAW bytes needed");
    }
    byte[] encoded = key.getEncoded();
    if (encoded == null) {
      throw new InvalidKeyException("RAW key bytes missing");
    }
    return encoded;
  }
}
