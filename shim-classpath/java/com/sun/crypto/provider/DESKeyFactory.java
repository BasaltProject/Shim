/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class DESKeyFactory extends SecretKeyFactorySpi {
  public DESKeyFactory() {
  }

  @Override
  protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
    try {
      if (keySpec instanceof DESKeySpec) {
        return new DESKey(((DESKeySpec) keySpec).getKey());
      }
      if (keySpec instanceof SecretKeySpec) {
        return new DESKey(((SecretKeySpec) keySpec).getEncoded());
      }
      throw new InvalidKeySpecException("Inappropriate key specification");
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException(e.getMessage());
    }
  }

  @Override
  protected KeySpec engineGetKeySpec(SecretKey key, Class<?> keySpec) throws InvalidKeySpecException {
    try {
      if ((key instanceof SecretKey) && key.getAlgorithm().equalsIgnoreCase("DES") && (key.getFormat().equalsIgnoreCase("RAW"))) {
        if ((keySpec != null) && DESKeySpec.class.isAssignableFrom(keySpec)) {
          return new DESKeySpec(key.getEncoded());
        }
        throw new InvalidKeySpecException("Inappropriate key specification");
      }
      throw new InvalidKeySpecException("Inappropriate key format/algorithm");
    } catch (InvalidKeyException e) {
      throw new InvalidKeySpecException("Secret key has wrong size");
    }
  }

  @Override
  protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
    try {
      if ((key != null) && key.getAlgorithm().equalsIgnoreCase("DES") && (key.getFormat().equalsIgnoreCase("RAW"))) {
        if (key instanceof DESKey) {
          return key;
        }
        DESKeySpec ks = (DESKeySpec) engineGetKeySpec(key, DESKeySpec.class);
        return engineGenerateSecret(ks);
      }
      throw new InvalidKeyException("Inappropriate key format/algorithm");
    } catch (InvalidKeySpecException e) {
      throw new InvalidKeyException("Cannot translate key");
    }
  }
}
