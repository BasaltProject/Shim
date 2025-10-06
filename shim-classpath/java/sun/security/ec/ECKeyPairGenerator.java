/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import sun.security.util.CurveDB;
import sun.security.util.NamedCurve;

public final class ECKeyPairGenerator extends KeyPairGeneratorSpi {
  private static final int kDefaultKeySize = 384;

  private NamedCurve params;
  private SecureRandom random;

  public ECKeyPairGenerator() {
    try {
      initialize(kDefaultKeySize, null);
    } catch (InvalidParameterException e) {
      throw new ProviderException(e);
    }
  }

  @Override
  public void initialize(int keySize, SecureRandom random) {
    if (keySize < 112) {
      throw new InvalidParameterException("Key size must be at least 112 bits");
    }
    if (keySize > 571) {
      throw new InvalidParameterException("Key size must be at most 571 bits");
    }
    CurveDB.Info info = CurveDB.lookupInfoBySize(keySize);
    if (info == null) {
      throw new InvalidParameterException("No EC parameters available for key size " + keySize + " bits");
    }
    if (!info.supported) {
      throw new ProviderException("Curve not supported:  " + info);
    }
    this.params = CurveDB.curve(info);
    this.random = random;
  }

  @Override
  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    NamedCurve nc;
    if (params instanceof ECParameterSpec) {
      nc = CurveDB.lookup((ECParameterSpec) params);
      if (nc == null) {
        throw new InvalidAlgorithmParameterException("Unsupported curve: " + params);
      }
    } else if (params instanceof ECGenParameterSpec) {
      String name = ((ECGenParameterSpec) params).getName();
      CurveDB.Info info = CurveDB.lookupInfo(name);
      if (info == null) {
        throw new InvalidAlgorithmParameterException("Unknown curve name: " + name);
      }
      if (!info.supported) {
        throw new InvalidAlgorithmParameterException("Curve not supported: " + info);
      }
      nc = CurveDB.curve(info);
    } else {
      throw new InvalidAlgorithmParameterException("ECParameterSpec or ECGenParameterSpec required for EC");
    }
    this.params = nc;
    this.random = random;
  }

  @Override
  public KeyPair generateKeyPair() {
    ECOperations ops = new ECOperations(params);
    SecureRandom rnd = random == null ? new SecureRandom() : random;
    BigInteger d = ops.randomScalar(rnd);
    ECPoint q = ops.multiplyG(d);
    try {
      return new KeyPair(new ECPublicKeyImpl(q, params), new ECPrivateKeyImpl(d, params));
    } catch (InvalidKeyException e) {
      throw new ProviderException(e);
    }
  }
}
