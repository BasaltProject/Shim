/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public abstract class RSAKeyPairGenerator extends KeyPairGeneratorSpi {
  private final String type;
  private BigInteger publicExponent = RSAKeyGenParameterSpec.F4;
  private int keySize = 3072;
  private AlgorithmParameterSpec keyParams;
  private SecureRandom random;

  RSAKeyPairGenerator(String type) {
    this.type = type;
  }

  public static final class Legacy extends RSAKeyPairGenerator {
    public Legacy() {
      super("RSA");
    }
  }

  public static final class PSS extends RSAKeyPairGenerator {
    public PSS() {
      super("RSASSA-PSS");
    }
  }

  @Override
  public void initialize(int keySize, SecureRandom random) {
    try {
      initialize(new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4), random);
    } catch (InvalidAlgorithmParameterException iape) {
      throw new InvalidParameterException(iape.toString());
    }
  }

  @Override
  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
    if (!(params instanceof RSAKeyGenParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Params must be instance of RSAKeyGenParameterSpec");
    }
    RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) params;
    int tmpKeySize = rsaSpec.getKeysize();
    BigInteger tmpPublicExponent = rsaSpec.getPublicExponent();
    AlgorithmParameterSpec tmpParams = rsaSpec.getKeyParams();
    if (tmpPublicExponent == null) {
      tmpPublicExponent = RSAKeyGenParameterSpec.F4;
    } else {
      if (tmpPublicExponent.compareTo(RSAKeyGenParameterSpec.F0) < 0) {
        throw new InvalidAlgorithmParameterException("Public exponent must be 3 or larger");
      }
      if (tmpPublicExponent.bitLength() > tmpKeySize) {
        throw new InvalidAlgorithmParameterException("Public exponent must be no longer than " + tmpKeySize + " bits");
      }
      if (!tmpPublicExponent.testBit(0)) {
        throw new InvalidAlgorithmParameterException("Public exponent must be an odd number");
      }
    }
    try {
      RSAKeyFactory.checkKeyLengths(tmpKeySize, tmpPublicExponent, 512, 64 * 1024);
    } catch (InvalidKeyException e) {
      throw new InvalidAlgorithmParameterException("Invalid key sizes", e);
    }
    if (tmpParams != null) {
      if (type.equals("RSA")) {
        throw new InvalidAlgorithmParameterException("Invalid key parameters");
      }
      if (!(tmpParams instanceof PSSParameterSpec)) {
        throw new InvalidAlgorithmParameterException("Params must be PSSParameterSpec");
      }
    }
    this.keySize = tmpKeySize;
    this.publicExponent = tmpPublicExponent;
    this.keyParams = tmpParams;
    this.random = random;
  }

  @Override
  public KeyPair generateKeyPair() {
    int lp = (keySize + 1) >> 1;
    int lq = keySize - lp;
    SecureRandom rnd = random == null ? new SecureRandom() : random;
    BigInteger e = publicExponent;
    while (true) {
      BigInteger p = BigInteger.probablePrime(lp, rnd);
      BigInteger q;
      do {
        q = BigInteger.probablePrime(lq, rnd);
      } while (q.equals(p));
      if (p.compareTo(q) < 0) {
        BigInteger t = p;
        p = q;
        q = t;
      }
      BigInteger n = p.multiply(q);
      if (n.bitLength() < keySize) {
        continue;
      }
      BigInteger p1 = p.subtract(BigInteger.ONE);
      BigInteger q1 = q.subtract(BigInteger.ONE);
      BigInteger phi = p1.multiply(q1);
      if (!e.gcd(phi).equals(BigInteger.ONE)) {
        continue;
      }
      BigInteger d = e.modInverse(phi);
      BigInteger pe = d.mod(p1);
      BigInteger qe = d.mod(q1);
      BigInteger coeff = q.modInverse(p);
      try {
        PublicKey publicKey = new RSAPublicKeyImpl(type, keyParams, n, e);
        PrivateKey privateKey = new RSAPrivateCrtKeyImpl(type, keyParams, n, e, d, p, q, pe, qe, coeff);
        return new KeyPair(publicKey, privateKey);
      } catch (InvalidKeyException exc) {
        throw new RuntimeException(exc);
      }
    }
  }
}
