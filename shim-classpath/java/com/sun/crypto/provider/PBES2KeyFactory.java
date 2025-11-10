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

abstract class PBES2KeyFactory extends SecretKeyFactorySpi {
  private final String algorithm;

  PBES2KeyFactory(String algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
    if (!(keySpec instanceof PBEKeySpec)) {
      throw new InvalidKeySpecException("Invalid key spec");
    }
    return new PBEKey(((PBEKeySpec) keySpec).getPassword(), algorithm);
  }

  @Override
  protected KeySpec engineGetKeySpec(SecretKey key, Class<?> keySpecCl) throws InvalidKeySpecException {
    if (key instanceof PBEKey && keySpecCl.isAssignableFrom(PBEKeySpec.class)) {
      return new PBEKeySpec(((PBEKey) key).password());
    }
    throw new InvalidKeySpecException("Invalid key spec");
  }

  @Override
  protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
    if (key != null && key.getAlgorithm().equalsIgnoreCase(algorithm) && "RAW".equalsIgnoreCase(key.getFormat())) {
      if (key instanceof PBEKey) {
        return key;
      }
      return new PBEKey(new String(key.getEncoded(), java.nio.charset.StandardCharsets.UTF_8).toCharArray(),
          algorithm);
    }
    throw new InvalidKeyException("Invalid key format/algorithm");
  }

  public static final class HmacSHA1AndAES_128 extends PBES2KeyFactory {
    public HmacSHA1AndAES_128() {
      super("PBEWithHmacSHA1AndAES_128");
    }
  }

  public static final class HmacSHA1AndAES_256 extends PBES2KeyFactory {
    public HmacSHA1AndAES_256() {
      super("PBEWithHmacSHA1AndAES_256");
    }
  }

  public static final class HmacSHA224AndAES_128 extends PBES2KeyFactory {
    public HmacSHA224AndAES_128() {
      super("PBEWithHmacSHA224AndAES_128");
    }
  }

  public static final class HmacSHA224AndAES_256 extends PBES2KeyFactory {
    public HmacSHA224AndAES_256() {
      super("PBEWithHmacSHA224AndAES_256");
    }
  }

  public static final class HmacSHA256AndAES_128 extends PBES2KeyFactory {
    public HmacSHA256AndAES_128() {
      super("PBEWithHmacSHA256AndAES_128");
    }
  }

  public static final class HmacSHA256AndAES_256 extends PBES2KeyFactory {
    public HmacSHA256AndAES_256() {
      super("PBEWithHmacSHA256AndAES_256");
    }
  }

  public static final class HmacSHA384AndAES_128 extends PBES2KeyFactory {
    public HmacSHA384AndAES_128() {
      super("PBEWithHmacSHA384AndAES_128");
    }
  }

  public static final class HmacSHA384AndAES_256 extends PBES2KeyFactory {
    public HmacSHA384AndAES_256() {
      super("PBEWithHmacSHA384AndAES_256");
    }
  }

  public static final class HmacSHA512AndAES_128 extends PBES2KeyFactory {
    public HmacSHA512AndAES_128() {
      super("PBEWithHmacSHA512AndAES_128");
    }
  }

  public static final class HmacSHA512AndAES_256 extends PBES2KeyFactory {
    public HmacSHA512AndAES_256() {
      super("PBEWithHmacSHA512AndAES_256");
    }
  }

  public static final class HmacSHA512_224AndAES_128 extends PBES2KeyFactory {
    public HmacSHA512_224AndAES_128() {
      super("PBEWithHmacSHA512/224AndAES_128");
    }
  }

  public static final class HmacSHA512_224AndAES_256 extends PBES2KeyFactory {
    public HmacSHA512_224AndAES_256() {
      super("PBEWithHmacSHA512/224AndAES_256");
    }
  }

  public static final class HmacSHA512_256AndAES_128 extends PBES2KeyFactory {
    public HmacSHA512_256AndAES_128() {
      super("PBEWithHmacSHA512/256AndAES_128");
    }
  }

  public static final class HmacSHA512_256AndAES_256 extends PBES2KeyFactory {
    public HmacSHA512_256AndAES_256() {
      super("PBEWithHmacSHA512/256AndAES_256");
    }
  }
}
