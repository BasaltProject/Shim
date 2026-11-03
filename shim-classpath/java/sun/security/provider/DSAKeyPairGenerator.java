/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.util.HashMap;
import java.util.Map;

public final class DSAKeyPairGenerator extends KeyPairGeneratorSpi {
  private int size = 2048;
  private DSAParameterSpec spec;
  private SecureRandom random;

  public DSAKeyPairGenerator() {
  }

  @Override
  public void initialize(int keysize, SecureRandom random) {
    if (keysize < 512 || keysize > 3072 || keysize % 64 != 0 && keysize != 1024) {
      throw new InvalidParameterException("Unsupported prime and subprime size combination: " + keysize
          + ", 160" + " (DSA key size must be a multiple of 64 between 512 and 1024, or 2048, 3072)");
    }
    this.size = keysize;
    this.spec = null;
    this.random = random;
  }

  @Override
  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    if (!(params instanceof DSAParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Inappropriate parameter");
    }
    this.spec = (DSAParameterSpec) params;
    this.size = spec.getP().bitLength();
    this.random = random;
  }

  @Override
  public KeyPair generateKeyPair() {
    SecureRandom rnd = random != null ? random : new SecureRandom();
    DSAParameterSpec ps = spec != null ? spec : DSAParameterGenerator.generate(size, rnd);
    BigInteger q = ps.getQ();
    BigInteger x;
    do {
      x = new BigInteger(q.bitLength() + 64, rnd).mod(q.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    } while (x.signum() <= 0);
    BigInteger y = ps.getG().modPow(x, ps.getP());
    return new KeyPair(new DSAKeys.Pub(y, ps.getP(), q, ps.getG()), new DSAKeys.Priv(x, ps.getP(), q, ps.getG()));
  }
}
