/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.XECPrivateKeySpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class XDH {
  private XDH() {
  }

  static final class Curve {
    final String name;
    final String oid;
    final BigInteger p;
    final BigInteger a24;
    final int bits;
    final int length;
    final BigInteger base;

    Curve(String name, String oid, BigInteger p, long a24, int bits, int length, int base) {
      this.name = name;
      this.oid = oid;
      this.p = p;
      this.a24 = BigInteger.valueOf(a24);
      this.bits = bits;
      this.length = length;
      this.base = BigInteger.valueOf(base);
    }
  }

  static final Curve kX25519 = new Curve("X25519", "1.3.101.110",
      BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19)), 121665, 255, 32, 9);
  static final Curve kX448 = new Curve("X448", "1.3.101.111",
      BigInteger.ONE.shiftLeft(448).subtract(BigInteger.ONE.shiftLeft(224)).subtract(BigInteger.ONE), 39081, 448, 56,
          5);

  static Curve curveOf(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    if (spec instanceof NamedParameterSpec) {
      String n = ((NamedParameterSpec) spec).getName();
      if (n.equalsIgnoreCase("X25519")) {
        return kX25519;
      }
      if (n.equalsIgnoreCase("X448")) {
        return kX448;
      }
      throw new InvalidAlgorithmParameterException("Unknown named parameter: " + n);
    }
    throw new InvalidAlgorithmParameterException("Only NamedParameterSpec is supported");
  }

  private static byte[] reverse(byte[] a) {
    byte[] r = new byte[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[a.length - 1 - i];
    }
    return r;
  }

  private static BigInteger decodeLe(byte[] a) {
    return new BigInteger(1, reverse(a));
  }

  private static byte[] encodeLe(BigInteger v, int length) {
    byte[] be = v.toByteArray();
    byte[] out = new byte[length];
    for (int i = 0; i < length && i < be.length; i++) {
      out[i] = be[be.length - 1 - i];
    }
    return out;
  }

  static byte[] scalarMult(Curve c, byte[] k, BigInteger uIn) {
    byte[] kc = k.clone();
    if (c == kX25519) {
      kc[0] &= (byte) 248;
      kc[31] &= 127;
      kc[31] |= 64;
    } else {
      kc[0] &= (byte) 252;
      kc[55] |= (byte) 128;
    }
    BigInteger scalar = decodeLe(kc);
    BigInteger p = c.p;
    BigInteger x1 = uIn.mod(p);
    BigInteger x2 = BigInteger.ONE;
    BigInteger z2 = BigInteger.ZERO;
    BigInteger x3 = x1;
    BigInteger z3 = BigInteger.ONE;
    boolean swap = false;
    for (int t = c.bits - 1; t >= 0; t--) {
      boolean bit = scalar.testBit(t);
      if (swap != bit) {
        BigInteger tx = x2;
        x2 = x3;
        x3 = tx;
        BigInteger tz = z2;
        z2 = z3;
        z3 = tz;
      }
      swap = bit;
      BigInteger a = x2.add(z2).mod(p);
      BigInteger aa = a.multiply(a).mod(p);
      BigInteger b = x2.subtract(z2).mod(p);
      BigInteger bb = b.multiply(b).mod(p);
      BigInteger e = aa.subtract(bb).mod(p);
      BigInteger cc = x3.add(z3).mod(p);
      BigInteger d = x3.subtract(z3).mod(p);
      BigInteger da = d.multiply(a).mod(p);
      BigInteger cb = cc.multiply(b).mod(p);
      BigInteger s = da.add(cb).mod(p);
      x3 = s.multiply(s).mod(p);
      BigInteger df = da.subtract(cb).mod(p);
      z3 = x1.multiply(df.multiply(df).mod(p)).mod(p);
      x2 = aa.multiply(bb).mod(p);
      z2 = e.multiply(aa.add(c.a24.multiply(e)).mod(p)).mod(p);
    }
    if (swap) {
      x2 = x3;
      z2 = z3;
    }
    BigInteger result = x2.multiply(z2.modPow(p.subtract(BigInteger.TWO), p)).mod(p);
    return encodeLe(result, c.length);
  }

  public static byte[] publicKey(String curveName, byte[] scalar) {
    Curve c = curveName.equals("X25519") ? kX25519 : kX448;
    byte[] u = scalarMult(c, scalar, c.base);
    byte[] out = new byte[u.length];
    for (int i = 0; i < u.length; i++) {
      out[i] = u[i];
    }
    return out;
  }

  static final class PubKey implements XECPublicKey {
    private static final long serialVersionUID = 1L;
    final Curve curve;
    final BigInteger u;

    PubKey(Curve curve, BigInteger u) {
      this.curve = curve;
      this.u = u.mod(curve.p);
    }

    @Override
    public BigInteger getU() {
      return u;
    }

    @Override
    public AlgorithmParameterSpec getParams() {
      return new NamedParameterSpec(curve.name);
    }

    @Override
    public String getAlgorithm() {
      return "XDH";
    }

    @Override
    public String getFormat() {
      return "X.509";
    }

    @Override
    public byte[] getEncoded() {
      DerOutputStream alg = new DerOutputStream();
      alg.putOID(ObjectIdentifier.of(curve.oid));
      DerOutputStream body = new DerOutputStream();
      body.write(DerValue.tag_Sequence, alg);
      body.putBitString(encodeLe(u, curve.length));
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, body);
      return out.toByteArray();
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(getEncoded());
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof PubKey && ((PubKey) o).curve == curve && ((PubKey) o).u.equals(u);
    }
  }

  static final class PrivKey implements XECPrivateKey {
    private static final long serialVersionUID = 1L;
    final Curve curve;
    final byte[] k;

    PrivKey(Curve curve, byte[] k) {
      this.curve = curve;
      this.k = k.clone();
    }

    @Override
    public Optional<byte[]> getScalar() {
      return Optional.of(k.clone());
    }

    @Override
    public AlgorithmParameterSpec getParams() {
      return new NamedParameterSpec(curve.name);
    }

    @Override
    public String getAlgorithm() {
      return "XDH";
    }

    @Override
    public String getFormat() {
      return "PKCS#8";
    }

    @Override
    public byte[] getEncoded() {
      DerOutputStream alg = new DerOutputStream();
      alg.putOID(ObjectIdentifier.of(curve.oid));
      DerOutputStream inner = new DerOutputStream();
      inner.putOctetString(k);
      DerOutputStream body = new DerOutputStream();
      body.putInteger(0);
      body.write(DerValue.tag_Sequence, alg);
      body.putOctetString(inner.toByteArray());
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, body);
      return out.toByteArray();
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(k);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof PrivKey && ((PrivKey) o).curve == curve && Arrays.equals(((PrivKey) o).k, k);
    }
  }

  public static class KeyPairGen extends KeyPairGeneratorSpi {
    private Curve curve;
    private final boolean fixed;
    private SecureRandom random;

    KeyPairGen(Curve curve) {
      this.curve = curve == null ? kX25519 : curve;
      this.fixed = curve != null;
    }

    @Override
    public void initialize(int keySize, SecureRandom random) {
      Curve c = keySize == 255 || keySize == 256 ? kX25519 : keySize == 448 ? kX448 : null;
      if (c == null || (fixed && c != curve)) {
        throw new InvalidParameterException("keySize must be 255 or 448");
      }
      this.curve = c;
      this.random = random;
    }

    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
        throws InvalidAlgorithmParameterException {
      Curve c = curveOf(params);
      if (fixed && c != curve) {
        throw new InvalidAlgorithmParameterException("Parameters must be " + curve.name);
      }
      this.curve = c;
      this.random = random;
    }

    @Override
    public KeyPair generateKeyPair() {
      SecureRandom r = random == null ? new SecureRandom() : random;
      byte[] k = new byte[curve.length];
      r.nextBytes(k);
      byte[] u = scalarMult(curve, k, curve.base);
      return new KeyPair(new PubKey(curve, decodeLe(u)), new PrivKey(curve, k));
    }
  }

  public static final class KeyPairGenAny extends KeyPairGen {
    public KeyPairGenAny() {
      super(null);
    }
  }

  public static final class KeyPairGen25519 extends KeyPairGen {
    public KeyPairGen25519() {
      super(kX25519);
    }
  }

  public static final class KeyPairGen448 extends KeyPairGen {
    public KeyPairGen448() {
      super(kX448);
    }
  }

  public static class Factory extends KeyFactorySpi {
    private final Curve fixed;

    Factory(Curve fixed) {
      this.fixed = fixed;
    }

    private Curve check(Curve c) throws InvalidKeySpecException {
      if (fixed != null && fixed != c) {
        throw new InvalidKeySpecException("Parameters must be " + fixed.name);
      }
      return c;
    }

    @Override
    protected PublicKey engineGeneratePublic(KeySpec spec) throws InvalidKeySpecException {
      try {
        if (spec instanceof XECPublicKeySpec) {
          XECPublicKeySpec s = (XECPublicKeySpec) spec;
          return new PubKey(check(curveOf(s.getParams())), s.getU());
        }
        if (spec instanceof X509EncodedKeySpec) {
          DerValue seq = new DerValue(((X509EncodedKeySpec) spec).getEncoded());
          DerInputStream in = seq.data();
          String oid = in.getDerValue().data().getOID().toString();
          Curve c = oid.equals(kX25519.oid) ? kX25519 : oid.equals(kX448.oid) ? kX448 : null;
          if (c == null) {
            throw new InvalidKeySpecException("Unsupported key algorithm: " + oid);
          }
          byte[] raw = in.getDerValue().getBitString();
          if (raw.length != check(c).length) {
            throw new InvalidKeySpecException("Key length is " + raw.length + ", should be " + c.length);
          }
          return new PubKey(c, decodeLe(raw));
        }
      } catch (IOException | InvalidAlgorithmParameterException e) {
        throw new InvalidKeySpecException(e);
      }
      throw new InvalidKeySpecException("Only XECPublicKeySpec and X509EncodedKeySpec supported");
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec spec) throws InvalidKeySpecException {
      try {
        if (spec instanceof XECPrivateKeySpec) {
          XECPrivateKeySpec s = (XECPrivateKeySpec) spec;
          Curve c = check(curveOf(s.getParams()));
          if (s.getScalar().length != c.length) {
            throw new InvalidKeySpecException("Key length is " + s.getScalar().length + ", should be " + c.length);
          }
          return new PrivKey(c, s.getScalar());
        }
        if (spec instanceof PKCS8EncodedKeySpec) {
          DerValue seq = new DerValue(((PKCS8EncodedKeySpec) spec).getEncoded());
          DerInputStream in = seq.data();
          in.getInteger();
          String oid = in.getDerValue().data().getOID().toString();
          Curve c = oid.equals(kX25519.oid) ? kX25519 : oid.equals(kX448.oid) ? kX448 : null;
          if (c == null) {
            throw new InvalidKeySpecException("Unsupported key algorithm: " + oid);
          }
          byte[] k = new DerValue(in.getDerValue().getDataBytes()).getDataBytes();
          if (k.length != check(c).length) {
            throw new InvalidKeySpecException("Key length is " + k.length + ", should be " + c.length);
          }
          return new PrivKey(c, k);
        }
      } catch (IOException | InvalidAlgorithmParameterException e) {
        throw new InvalidKeySpecException(e);
      }
      throw new InvalidKeySpecException("Only XECPrivateKeySpec and PKCS8EncodedKeySpec supported");
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
      if (key instanceof PubKey) {
        PubKey k = (PubKey) key;
        if (keySpec.isAssignableFrom(XECPublicKeySpec.class)) {
          return keySpec.cast(new XECPublicKeySpec(k.getParams(), k.u));
        }
        if (keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
          return keySpec.cast(new X509EncodedKeySpec(k.getEncoded()));
        }
      } else if (key instanceof PrivKey) {
        PrivKey k = (PrivKey) key;
        if (keySpec.isAssignableFrom(XECPrivateKeySpec.class)) {
          return keySpec.cast(new XECPrivateKeySpec(k.getParams(), k.k));
        }
        if (keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class)) {
          return keySpec.cast(new PKCS8EncodedKeySpec(k.getEncoded()));
        }
      }
      throw new InvalidKeySpecException("Unsupported key or spec");
    }

    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
      if (key instanceof PubKey || key instanceof PrivKey) {
        return key;
      }
      throw new InvalidKeyException("Unsupported key");
    }
  }

  public static final class FactoryAny extends Factory {
    public FactoryAny() {
      super(null);
    }
  }

  public static final class Factory25519 extends Factory {
    public Factory25519() {
      super(kX25519);
    }
  }

  public static final class Factory448 extends Factory {
    public Factory448() {
      super(kX448);
    }
  }

  public static class Agreement extends KeyAgreementSpi {
    private final Curve fixed;
    private PrivKey priv;
    private PubKey pub;
    private byte[] secret;

    Agreement(Curve fixed) {
      this.fixed = fixed;
    }

    @Override
    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
      if (!(key instanceof PrivKey)) {
        throw new InvalidKeyException("Unsupported key type");
      }
      PrivKey k = (PrivKey) key;
      if (fixed != null && fixed != k.curve) {
        throw new InvalidKeyException("Parameters must be " + fixed.name);
      }
      priv = k;
      pub = null;
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException,
        InvalidAlgorithmParameterException {
      if (params != null) {
        throw new InvalidAlgorithmParameterException("Parameters not supported");
      }
      engineInit(key, random);
    }

    @Override
    protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
      if (priv == null) {
        throw new IllegalStateException("Not initialized");
      }
      if (pub != null) {
        throw new IllegalStateException("Phase already executed");
      }
      if (!lastPhase) {
        throw new IllegalStateException("Only two party agreement supported, lastPhase must be true");
      }
      if (!(key instanceof PubKey) || ((PubKey) key).curve != priv.curve) {
        throw new InvalidKeyException("Public key parameters are not compatible with private key.");
      }
      pub = (PubKey) key;
      secret = scalarMult(priv.curve, priv.k, pub.u);
      boolean zero = true;
      for (byte b : secret) {
        zero &= b == 0;
      }
      if (zero) {
        secret = null;
        throw new InvalidKeyException("Point has small order");
      }
      return null;
    }

    @Override
    protected byte[] engineGenerateSecret() {
      if (priv == null || pub == null || secret == null) {
        throw new IllegalStateException("Not initialized correctly");
      }
      byte[] out = secret;
      priv = null;
      pub = null;
      secret = null;
      return out;
    }

    @Override
    protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws ShortBufferException {
      byte[] s = engineGenerateSecret();
      if (sharedSecret.length - offset < s.length) {
        throw new ShortBufferException("Need " + s.length + " bytes, only " + (sharedSecret.length - offset)
            + " available");
      }
      System.arraycopy(s, 0, sharedSecret, offset, s.length);
      return s.length;
    }

    @Override
    protected SecretKey engineGenerateSecret(String algorithm) {
      return new SecretKeySpec(engineGenerateSecret(), algorithm);
    }
  }

  public static final class AgreementAny extends Agreement {
    public AgreementAny() {
      super(null);
    }
  }

  public static final class Agreement25519 extends Agreement {
    public Agreement25519() {
      super(kX25519);
    }
  }

  public static final class Agreement448 extends Agreement {
    public Agreement448() {
      super(kX448);
    }
  }
}
