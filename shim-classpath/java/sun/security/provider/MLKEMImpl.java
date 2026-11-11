/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyFactorySpi;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.DecapsulateException;
import javax.crypto.KEM;
import javax.crypto.KEMSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** Provider classes for ML-KEM: key pair generator, key factory and KEM. */
public class MLKEMImpl {
  private MLKEMImpl() {
  }

  public static class KPGBase extends KeyPairGeneratorSpi {
    private final MLKEM.Params fixed;
    private MLKEM.Params params;
    private SecureRandom random;

    KPGBase(MLKEM.Params fixed) {
      this.fixed = fixed;
      this.params = fixed == null ? MLKEM.Params.ML_KEM_768 : fixed;
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
      MLKEM.Params p = MLKEM.Params.byName(name);
      if (p == null || (fixed != null && p != fixed)) {
        throw new InvalidAlgorithmParameterException("Unsupported parameter set name: " + name);
      }
      this.params = p;
      this.random = random;
    }

    @Override
    public KeyPair generateKeyPair() {
      SecureRandom r = random != null ? random : new SecureRandom();
      byte[] seed = new byte[64];
      r.nextBytes(seed);
      byte[][] kp = MLKEM.keyGen(params, Arrays.copyOfRange(seed, 0, 32), Arrays.copyOfRange(seed, 32, 64));
      return new KeyPair(new MLKEMKeys.Pub(params, kp[0]), new MLKEMKeys.Priv(params, seed, kp[1]));
    }
  }

  public static class KFBase extends KeyFactorySpi {
    private final MLKEM.Params fixed;

    KFBase(MLKEM.Params fixed) {
      this.fixed = fixed;
    }

    private void check(MLKEM.Params p) throws InvalidKeySpecException {
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
        MLKEMKeys.Pub k = MLKEMKeys.parsePublic(((X509EncodedKeySpec) keySpec).getEncoded());
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
        MLKEMKeys.Priv k = MLKEMKeys.parsePrivate(((PKCS8EncodedKeySpec) keySpec).getEncoded());
        check(k.params);
        return k;
      } catch (InvalidKeyException e) {
        throw new InvalidKeySpecException(e.getMessage(), e);
      }
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(java.security.Key key, Class<T> keySpec)
        throws InvalidKeySpecException {
      if (key instanceof MLKEMKeys.Pub && keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
        return keySpec.cast(new X509EncodedKeySpec(key.getEncoded()));
      }
      if (key instanceof MLKEMKeys.Priv && keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
        return keySpec.cast(new PKCS8EncodedKeySpec(key.getEncoded()));
      }
      throw new InvalidKeySpecException("Unsupported key spec: " + keySpec.getName());
    }

    @Override
    protected java.security.Key engineTranslateKey(java.security.Key key) throws InvalidKeyException {
      if (key instanceof MLKEMKeys.Pub || key instanceof MLKEMKeys.Priv) {
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

  public static class KemBase implements KEMSpi {
    private final MLKEM.Params fixed;

    KemBase(MLKEM.Params fixed) {
      this.fixed = fixed;
    }

    @Override
    public EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey, AlgorithmParameterSpec spec,
        SecureRandom secureRandom) throws InvalidAlgorithmParameterException, InvalidKeyException {
      if (spec != null) {
        throw new InvalidAlgorithmParameterException("no spec needed");
      }
      if (!(publicKey instanceof MLKEMKeys.Pub)) {
        throw new InvalidKeyException("unsupported key");
      }
      final MLKEMKeys.Pub k = (MLKEMKeys.Pub) publicKey;
      if (fixed != null && k.params != fixed) {
        throw new InvalidKeyException("Unsupported parameter set");
      }
      if (!MLKEM.checkPublicKey(k.params, k.raw)) {
        throw new InvalidKeyException("Modulus check failed");
      }
      final SecureRandom rnd = secureRandom != null ? secureRandom : new SecureRandom();
      return new EncapsulatorSpi() {
        @Override
        public KEM.Encapsulated engineEncapsulate(int from, int to, String algorithm) {
          if (from < 0 || from > to || to > 32) {
            throw new IndexOutOfBoundsException("Invalid range");
          }
          byte[] m = new byte[32];
          rnd.nextBytes(m);
          byte[][] r = MLKEM.encaps(k.params, k.raw, m);
          return new KEM.Encapsulated(new SecretKeySpec(r[0], from, to - from, algorithm), r[1], null);
        }

        @Override
        public int engineSecretSize() {
          return 32;
        }

        @Override
        public int engineEncapsulationSize() {
          return k.params.ciphertextSize();
        }
      };
    }

    @Override
    public DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
        throws InvalidAlgorithmParameterException, InvalidKeyException {
      if (spec != null) {
        throw new InvalidAlgorithmParameterException("no spec needed");
      }
      if (!(privateKey instanceof MLKEMKeys.Priv)) {
        throw new InvalidKeyException("unsupported key");
      }
      final MLKEMKeys.Priv k = (MLKEMKeys.Priv) privateKey;
      if (fixed != null && k.params != fixed) {
        throw new InvalidKeyException("Unsupported parameter set");
      }
      return new DecapsulatorSpi() {
        @Override
        public SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm)
            throws DecapsulateException {
          if (from < 0 || from > to || to > 32) {
            throw new IndexOutOfBoundsException("Invalid range");
          }
          if (encapsulation.length != k.params.ciphertextSize()) {
            throw new DecapsulateException("Invalid key encapsulation message length");
          }
          byte[] key = MLKEM.decaps(k.params, k.expanded, encapsulation);
          return new SecretKeySpec(key, from, to - from, algorithm);
        }

        @Override
        public int engineSecretSize() {
          return 32;
        }

        @Override
        public int engineEncapsulationSize() {
          return k.params.ciphertextSize();
        }
      };
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

  public static final class KemAny extends KemBase {
    public KemAny() {
      super(null);
    }
  }

  public static final class KPG512 extends KPGBase {
    public KPG512() {
      super(MLKEM.Params.ML_KEM_512);
    }
  }

  public static final class KF512 extends KFBase {
    public KF512() {
      super(MLKEM.Params.ML_KEM_512);
    }
  }

  public static final class Kem512 extends KemBase {
    public Kem512() {
      super(MLKEM.Params.ML_KEM_512);
    }
  }

  public static final class KPG768 extends KPGBase {
    public KPG768() {
      super(MLKEM.Params.ML_KEM_768);
    }
  }

  public static final class KF768 extends KFBase {
    public KF768() {
      super(MLKEM.Params.ML_KEM_768);
    }
  }

  public static final class Kem768 extends KemBase {
    public Kem768() {
      super(MLKEM.Params.ML_KEM_768);
    }
  }

  public static final class KPG1024 extends KPGBase {
    public KPG1024() {
      super(MLKEM.Params.ML_KEM_1024);
    }
  }

  public static final class KF1024 extends KFBase {
    public KF1024() {
      super(MLKEM.Params.ML_KEM_1024);
    }
  }

  public static final class Kem1024 extends KemBase {
    public Kem1024() {
      super(MLKEM.Params.ML_KEM_1024);
    }
  }

}
