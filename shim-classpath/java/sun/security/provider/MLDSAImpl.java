/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyFactorySpi;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/** Provider classes for ML-DSA: key pair generator, key factory and signature. */
public class MLDSAImpl {
  private MLDSAImpl() {
  }

  public static class KPGBase extends KeyPairGeneratorSpi {
    private final MLDSA.Params fixed;
    private MLDSA.Params params;
    private SecureRandom random;

    KPGBase(MLDSA.Params fixed) {
      this.fixed = fixed;
      this.params = fixed == null ? MLDSA.Params.ML_DSA_65 : fixed;
    }

    @Override
    public void initialize(int keysize, SecureRandom random) {
      throw new InvalidParameterException("Unsupported key size: " + keysize);
    }

    @Override
    public void initialize(AlgorithmParameterSpec spec, SecureRandom random) throws InvalidAlgorithmParameterException {
      if (!(spec instanceof NamedParameterSpec)) {
        throw new InvalidAlgorithmParameterException("Unsupported AlgorithmParameterSpec: " + spec);
      }
      String name = ((NamedParameterSpec) spec).getName();
      MLDSA.Params p = MLDSA.Params.byName(name);
      if (p == null || (fixed != null && p != fixed)) {
        throw new InvalidAlgorithmParameterException("Unsupported parameter set name: " + name);
      }
      this.params = p;
      this.random = random;
    }

    @Override
    public KeyPair generateKeyPair() {
      SecureRandom r = random != null ? random : new SecureRandom();
      byte[] seed = new byte[32];
      r.nextBytes(seed);
      byte[][] kp = MLDSA.keyGen(params, seed);
      return new KeyPair(new MLDSAKeys.Pub(params, kp[0]), new MLDSAKeys.Priv(params, seed, kp[1]));
    }
  }

  public static class KFBase extends KeyFactorySpi {
    private final MLDSA.Params fixed;

    KFBase(MLDSA.Params fixed) {
      this.fixed = fixed;
    }

    private void check(MLDSA.Params p) throws InvalidKeySpecException {
      if (fixed != null && p != fixed) {
        throw new InvalidKeySpecException("Unsupported parameter set");
      }
    }

    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
      if (!(keySpec instanceof X509EncodedKeySpec)) {
        throw new InvalidKeySpecException("Unsupported key spec: " + keySpec.getClass().getName());
      }
      try {
        MLDSAKeys.Pub k = MLDSAKeys.parsePublic(((X509EncodedKeySpec) keySpec).getEncoded());
        check(k.params);
        return k;
      } catch (InvalidKeyException e) {
        throw new InvalidKeySpecException(e.getMessage(), e);
      }
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
      if (!(keySpec instanceof PKCS8EncodedKeySpec)) {
        throw new InvalidKeySpecException("Unsupported key spec: " + keySpec.getClass().getName());
      }
      try {
        MLDSAKeys.Priv k = MLDSAKeys.parsePrivate(((PKCS8EncodedKeySpec) keySpec).getEncoded());
        check(k.params);
        return k;
      } catch (InvalidKeyException e) {
        throw new InvalidKeySpecException(e.getMessage(), e);
      }
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(java.security.Key key, Class<T> keySpec)
        throws InvalidKeySpecException {
      if (key instanceof MLDSAKeys.Pub && keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
        return keySpec.cast(new X509EncodedKeySpec(key.getEncoded()));
      }
      if (key instanceof MLDSAKeys.Priv && keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
        return keySpec.cast(new PKCS8EncodedKeySpec(key.getEncoded()));
      }
      throw new InvalidKeySpecException("Unsupported key spec: " + keySpec.getName());
    }

    @Override
    protected java.security.Key engineTranslateKey(java.security.Key key) throws InvalidKeyException {
      if (key instanceof MLDSAKeys.Pub || key instanceof MLDSAKeys.Priv) {
        return key;
      }
      try {
        if (key instanceof PublicKey && "X.509".equals(key.getFormat())) {
          return engineGeneratePublic(new X509EncodedKeySpec(key.getEncoded()));
        }
        if (key instanceof PrivateKey && "PKCS#8".equals(key.getFormat())) {
          return engineGeneratePrivate(new PKCS8EncodedKeySpec(key.getEncoded()));
        }
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e.getMessage(), e);
      }
      throw new InvalidKeyException("Unsupported key type");
    }
  }

  public static class SigBase extends SignatureSpi {
    private final MLDSA.Params fixed;
    private final ByteArrayOutputStream message = new ByteArrayOutputStream();
    private MLDSAKeys.Pub pub;
    private MLDSAKeys.Priv priv;
    private SecureRandom random;

    SigBase(MLDSA.Params fixed) {
      this.fixed = fixed;
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
      if (!(publicKey instanceof MLDSAKeys.Pub)) {
        throw new InvalidKeyException("Unsupported key type");
      }
      MLDSAKeys.Pub k = (MLDSAKeys.Pub) publicKey;
      if (fixed != null && k.params != fixed) {
        throw new InvalidKeyException("Unsupported parameter set");
      }
      pub = k;
      priv = null;
      message.reset();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
      engineInitSign(privateKey, null);
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
      if (!(privateKey instanceof MLDSAKeys.Priv)) {
        throw new InvalidKeyException("Unsupported key type");
      }
      MLDSAKeys.Priv k = (MLDSAKeys.Priv) privateKey;
      if (fixed != null && k.params != fixed) {
        throw new InvalidKeyException("Unsupported parameter set");
      }
      priv = k;
      pub = null;
      this.random = random;
      message.reset();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
      message.write(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
      message.write(b, off, len);
    }

    private byte[] encoded() {
      byte[] m = message.toByteArray();
      message.reset();
      byte[] out = new byte[m.length + 2];
      System.arraycopy(m, 0, out, 2, m.length);
      return out;
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
      if (priv == null) {
        throw new SignatureException("No private key");
      }
      byte[] rnd = new byte[32];
      (random != null ? random : new SecureRandom()).nextBytes(rnd);
      return MLDSA.sign(priv.params, priv.expanded, encoded(), rnd);
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
      if (pub == null) {
        throw new SignatureException("No public key");
      }
      byte[] m = encoded();
      if (sigBytes.length != pub.params.signatureSize()) {
        throw new SignatureException("Invalid signature length");
      }
      return MLDSA.verify(pub.params, pub.raw, m, sigBytes);
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) {
      throw new InvalidParameterException("No parameter accepted");
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String param) {
      throw new InvalidParameterException("No parameter accepted");
    }
  }

  public static final class KPGAny extends KPGBase {
    public KPGAny() {
      super(null);
    }
  }

  public static final class KFAny extends KFBase {
    public KFAny() {
      super(null);
    }
  }

  public static final class SigAny extends SigBase {
    public SigAny() {
      super(null);
    }
  }

  public static final class KPG44 extends KPGBase {
    public KPG44() {
      super(MLDSA.Params.ML_DSA_44);
    }
  }

  public static final class KF44 extends KFBase {
    public KF44() {
      super(MLDSA.Params.ML_DSA_44);
    }
  }

  public static final class Sig44 extends SigBase {
    public Sig44() {
      super(MLDSA.Params.ML_DSA_44);
    }
  }

  public static final class KPG65 extends KPGBase {
    public KPG65() {
      super(MLDSA.Params.ML_DSA_65);
    }
  }

  public static final class KF65 extends KFBase {
    public KF65() {
      super(MLDSA.Params.ML_DSA_65);
    }
  }

  public static final class Sig65 extends SigBase {
    public Sig65() {
      super(MLDSA.Params.ML_DSA_65);
    }
  }

  public static final class KPG87 extends KPGBase {
    public KPG87() {
      super(MLDSA.Params.ML_DSA_87);
    }
  }

  public static final class KF87 extends KFBase {
    public KF87() {
      super(MLDSA.Params.ML_DSA_87);
    }
  }

  public static final class Sig87 extends SigBase {
    public Sig87() {
      super(MLDSA.Params.ML_DSA_87);
    }
  }

}
