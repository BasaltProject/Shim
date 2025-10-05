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
import javax.crypto.spec.PBEKeySpec;

abstract class PBKDF2Core extends SecretKeyFactorySpi {
  private final String prfAlgo;

  PBKDF2Core(String prfAlgo) {
    this.prfAlgo = prfAlgo;
  }

  @Override
  protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
    if (!(keySpec instanceof PBEKeySpec)) {
      throw new InvalidKeySpecException("Only PBEKeySpec is accepted");
    }
    PBEKeySpec ks = (PBEKeySpec) keySpec;
    char[] passwd = ks.getPassword();
    byte[] salt = ks.getSalt();
    if (salt == null) {
      throw new InvalidKeySpecException("Salt not found");
    }
    int iterCount = ks.getIterationCount();
    int keyLength = ks.getKeyLength();
    if (keyLength == 0) {
      throw new InvalidKeySpecException("Key length not found");
    }
    return new PBKDF2KeyImpl(passwd, salt, iterCount, keyLength, prfAlgo, "PBKDF2With" + prfAlgo);
  }

  @Override
  protected KeySpec engineGetKeySpec(SecretKey key, Class<?> keySpecCl) throws InvalidKeySpecException {
    if (key instanceof PBKDF2KeyImpl) {
      if (keySpecCl.isAssignableFrom(PBEKeySpec.class)) {
        PBKDF2KeyImpl pbeKey = (PBKDF2KeyImpl) key;
        return new PBEKeySpec(pbeKey.getPassword(), pbeKey.getSalt(), pbeKey.getIterationCount(),
            pbeKey.getEncoded().length * 8);
      }
      throw new InvalidKeySpecException("Invalid key spec");
    }
    throw new InvalidKeySpecException("Invalid key " + "format/algorithm");
  }

  @Override
  protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
    if ((key != null) && (key.getAlgorithm().equalsIgnoreCase("PBKDF2With" + prfAlgo))
        && (key.getFormat().equalsIgnoreCase("RAW"))) {
      if (key instanceof PBKDF2KeyImpl) {
        return key;
      }
      throw new InvalidKeyException("Key is not a PBKDF2 key");
    }
    throw new InvalidKeyException("Only PBKDF2With" + prfAlgo + " key with RAW format is accepted");
  }

  public static final class HmacSHA1 extends PBKDF2Core {
    public HmacSHA1() {
      super("HmacSHA1");
    }
  }

  public static final class HmacSHA224 extends PBKDF2Core {
    public HmacSHA224() {
      super("HmacSHA224");
    }
  }

  public static final class HmacSHA256 extends PBKDF2Core {
    public HmacSHA256() {
      super("HmacSHA256");
    }
  }

  public static final class HmacSHA384 extends PBKDF2Core {
    public HmacSHA384() {
      super("HmacSHA384");
    }
  }

  public static final class HmacSHA512 extends PBKDF2Core {
    public HmacSHA512() {
      super("HmacSHA512");
    }
  }

  public static final class HmacSHA512_224 extends PBKDF2Core {
    public HmacSHA512_224() {
      super("HmacSHA512/224");
    }
  }

  public static final class HmacSHA512_256 extends PBKDF2Core {
    public HmacSHA512_256() {
      super("HmacSHA512/256");
    }
  }

  public static final class HmacSHA3_224 extends PBKDF2Core {
    public HmacSHA3_224() {
      super("HmacSHA3/224");
    }
  }

  public static final class HmacSHA3_256 extends PBKDF2Core {
    public HmacSHA3_256() {
      super("HmacSHA3/256");
    }
  }

  public static final class HmacSHA3_384 extends PBKDF2Core {
    public HmacSHA3_384() {
      super("HmacSHA3/384");
    }
  }

  public static final class HmacSHA3_512 extends PBKDF2Core {
    public HmacSHA3_512() {
      super("HmacSHA3/512");
    }
  }
}
