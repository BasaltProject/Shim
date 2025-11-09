/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Optional;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class EdDSA {
  private EdDSA() {
  }

  static final class Curve {
    final String name;
    final String oid;
    final BigInteger p;
    final BigInteger a;
    final BigInteger d;
    final BigInteger order;
    final int length;
    final BigInteger bx;
    final BigInteger by;

    Curve(String name, String oid, BigInteger p, BigInteger a, BigInteger d, BigInteger order, int length,
        BigInteger bx, BigInteger by) {
      this.name = name;
      this.oid = oid;
      this.p = p;
      this.a = a.mod(p);
      this.d = d.mod(p);
      this.order = order;
      this.length = length;
      this.bx = bx;
      this.by = by;
    }

    boolean is25519() {
      return length == 32;
    }
  }

  private static final BigInteger kP25519 = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19));
  private static final BigInteger kP448 =
      BigInteger.ONE.shiftLeft(448).subtract(BigInteger.ONE.shiftLeft(224)).subtract(BigInteger.ONE);

  static final Curve kEd25519 = new Curve("Ed25519", "1.3.101.112", kP25519, BigInteger.ONE.negate(),
      BigInteger.valueOf(-121665).multiply(BigInteger.valueOf(121666).modInverse(kP25519)),
      BigInteger.ONE.shiftLeft(252).add(new BigInteger("27742317777372353535851937790883648493")), 32,
      new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202"),
      new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960"));
  static final Curve kEd448 = new Curve("Ed448", "1.3.101.113", kP448, BigInteger.ONE, BigInteger.valueOf(-39081),
      BigInteger.ONE.shiftLeft(446).subtract(
          new BigInteger("13818066809895115352007386748515426880336692474882178609894547503885")), 57,
      new BigInteger("224580040295924300187604334099896036246789641632564134246125461686950415467406032909029192869"
          + "357953282578032075146446173674602635247710"),
      new BigInteger("298819210078481492676017930443930673437544040154080242095928241372331506189835876003536878655"
          + "418784733982303233503462500531545062832660"));

  static Curve curveOf(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    if (spec instanceof NamedParameterSpec) {
      String n = ((NamedParameterSpec) spec).getName();
      if (n.equalsIgnoreCase("Ed25519")) {
        return kEd25519;
      }
      if (n.equalsIgnoreCase("Ed448")) {
        return kEd448;
      }
      throw new InvalidAlgorithmParameterException("Unknown named parameter: " + n);
    }
    throw new InvalidAlgorithmParameterException("Only NamedParameterSpec is supported");
  }

  private static Curve curveOid(String oid) {
    return oid.equals(kEd25519.oid) ? kEd25519 : oid.equals(kEd448.oid) ? kEd448 : null;
  }

  private static byte[] reverse(byte[] a) {
    byte[] r = new byte[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[a.length - 1 - i];
    }
    return r;
  }

  private static BigInteger decodeLe(byte[] a, int off, int len) {
    return new BigInteger(1, reverse(Arrays.copyOfRange(a, off, off + len)));
  }

  private static byte[] encodeLe(BigInteger v, int length) {
    byte[] be = v.toByteArray();
    byte[] out = new byte[length];
    for (int i = 0; i < length && i < be.length; i++) {
      out[i] = be[be.length - 1 - i];
    }
    return out;
  }

  static final class Point {
    final BigInteger x;
    final BigInteger y;
    final BigInteger z;

    Point(BigInteger x, BigInteger y, BigInteger z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  private static Point identity() {
    return new Point(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE);
  }

  private static Point add(Curve c, Point p1, Point p2) {
    BigInteger m = c.p;
    BigInteger a = p1.z.multiply(p2.z).mod(m);
    BigInteger b = a.multiply(a).mod(m);
    BigInteger cc = p1.x.multiply(p2.x).mod(m);
    BigInteger dd = p1.y.multiply(p2.y).mod(m);
    BigInteger e = c.d.multiply(cc).mod(m).multiply(dd).mod(m);
    BigInteger f = b.subtract(e).mod(m);
    BigInteger g = b.add(e).mod(m);
    BigInteger h = p1.x.add(p1.y).multiply(p2.x.add(p2.y)).subtract(cc).subtract(dd).mod(m);
    BigInteger x3 = a.multiply(f).mod(m).multiply(h).mod(m);
    BigInteger y3 = a.multiply(g).mod(m).multiply(dd.subtract(c.a.multiply(cc)).mod(m)).mod(m);
    BigInteger z3 = f.multiply(g).mod(m);
    return new Point(x3, y3, z3);
  }

  private static Point multiply(Curve c, Point p, BigInteger k) {
    Point r = identity();
    for (int i = k.bitLength() - 1; i >= 0; i--) {
      r = add(c, r, r);
      if (k.testBit(i)) {
        r = add(c, r, p);
      }
    }
    return r;
  }

  private static Point basePoint(Curve c) {
    return new Point(c.bx, c.by, BigInteger.ONE);
  }

  static byte[] encodePoint(Curve c, Point p) {
    BigInteger zi = p.z.modInverse(c.p);
    BigInteger x = p.x.multiply(zi).mod(c.p);
    BigInteger y = p.y.multiply(zi).mod(c.p);
    byte[] out = encodeLe(y, c.length);
    if (x.testBit(0)) {
      out[c.length - 1] |= (byte) 0x80;
    }
    return out;
  }

  static Point decodePoint(Curve c, byte[] enc) {
    if (enc.length != c.length) {
      return null;
    }
    boolean xOdd = (enc[c.length - 1] & 0x80) != 0;
    byte[] yb = enc.clone();
    yb[c.length - 1] &= 0x7f;
    BigInteger y = decodeLe(yb, 0, c.length);
    return fromY(c, xOdd, y);
  }

  static Point fromY(Curve c, boolean xOdd, BigInteger y) {
    BigInteger m = c.p;
    if (y.signum() < 0 || y.compareTo(m) >= 0) {
      return null;
    }
    BigInteger y2 = y.multiply(y).mod(m);
    BigInteger u = y2.subtract(BigInteger.ONE).mod(m);
    BigInteger v = c.d.multiply(y2).subtract(c.a).mod(m);
    BigInteger x2 = u.multiply(v.modInverse(m)).mod(m);
    BigInteger x;
    if (c.is25519()) {
      x = x2.modPow(m.add(BigInteger.valueOf(3)).shiftRight(3), m);
      if (x.multiply(x).mod(m).compareTo(x2) != 0) {
        x = x.multiply(BigInteger.TWO.modPow(m.subtract(BigInteger.ONE).shiftRight(2), m)).mod(m);
      }
    } else {
      x = x2.modPow(m.add(BigInteger.ONE).shiftRight(2), m);
    }
    if (x.multiply(x).mod(m).compareTo(x2) != 0) {
      return null;
    }
    if (x.signum() == 0 && xOdd) {
      return null;
    }
    if (x.testBit(0) != xOdd) {
      x = m.subtract(x).mod(m);
    }
    return new Point(x, y, BigInteger.ONE);
  }

  private static byte[] hash(Curve c, byte[] ctx, byte[]... parts) throws NoSuchAlgorithmException {
    if (c.is25519()) {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      for (byte[] p : parts) {
        md.update(p);
      }
      return md.digest();
    }
    byte[] dom = new byte[10 + ctx.length];
    System.arraycopy("SigEd448".getBytes(), 0, dom, 0, 8);
    dom[9] = (byte) ctx.length;
    System.arraycopy(ctx, 0, dom, 10, ctx.length);
    byte[][] all = new byte[parts.length + 1][];
    all[0] = dom;
    System.arraycopy(parts, 0, all, 1, parts.length);
    return Shake256.hash(114, all);
  }

  private static byte[] seedHash(Curve c, byte[] seed) throws NoSuchAlgorithmException {
    if (c.is25519()) {
      return MessageDigest.getInstance("SHA-512").digest(seed);
    }
    return Shake256.hash(114, seed);
  }

  private static BigInteger secretScalar(Curve c, byte[] h) {
    byte[] s = Arrays.copyOf(h, c.length);
    if (c.is25519()) {
      s[0] &= (byte) 248;
      s[31] &= 127;
      s[31] |= 64;
    } else {
      s[0] &= (byte) 252;
      s[55] |= (byte) 128;
      s[56] = 0;
    }
    return decodeLe(s, 0, c.length);
  }

  static byte[] publicFromSeed(Curve c, byte[] seed) throws NoSuchAlgorithmException {
    BigInteger s = secretScalar(c, seedHash(c, seed));
    return encodePoint(c, multiply(c, basePoint(c), s));
  }

  static final class PubKey implements EdECPublicKey {
    private static final long serialVersionUID = 1L;
    final Curve curve;
    final byte[] enc;
    final EdECPoint point;

    PubKey(Curve curve, byte[] enc) {
      this.curve = curve;
      this.enc = enc.clone();
      boolean odd = (enc[curve.length - 1] & 0x80) != 0;
      byte[] yb = enc.clone();
      yb[curve.length - 1] &= 0x7f;
      this.point = new EdECPoint(odd, decodeLe(yb, 0, curve.length));
    }

    @Override
    public EdECPoint getPoint() {
      return point;
    }

    @Override
    public NamedParameterSpec getParams() {
      return new NamedParameterSpec(curve.name);
    }

    @Override
    public String getAlgorithm() {
      return "EdDSA";
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
      body.putBitString(enc);
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, body);
      return out.toByteArray();
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(enc);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof PubKey && ((PubKey) o).curve == curve && Arrays.equals(((PubKey) o).enc, enc);
    }
  }

  static final class PrivKey implements EdECPrivateKey {
    private static final long serialVersionUID = 1L;
    final Curve curve;
    final byte[] seed;

    PrivKey(Curve curve, byte[] seed) {
      this.curve = curve;
      this.seed = seed.clone();
    }

    @Override
    public Optional<byte[]> getBytes() {
      return Optional.of(seed.clone());
    }

    @Override
    public NamedParameterSpec getParams() {
      return new NamedParameterSpec(curve.name);
    }

    @Override
    public String getAlgorithm() {
      return "EdDSA";
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
      inner.putOctetString(seed);
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
      return Arrays.hashCode(seed);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof PrivKey && ((PrivKey) o).curve == curve && Arrays.equals(((PrivKey) o).seed, seed);
    }
  }

  public static class KeyPairGen extends KeyPairGeneratorSpi {
    private Curve curve;
    private final boolean fixed;
    private SecureRandom random;

    KeyPairGen(Curve curve) {
      this.curve = curve == null ? kEd25519 : curve;
      this.fixed = curve != null;
    }

    @Override
    public void initialize(int keySize, SecureRandom random) {
      Curve c = keySize == 255 || keySize == 256 ? kEd25519 : keySize == 448 ? kEd448 : null;
      if (c == null || (fixed && c != curve)) {
        throw new InvalidParameterException("Unsupported key size: " + keySize);
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
      byte[] seed = new byte[curve.is25519() ? 32 : 57];
      r.nextBytes(seed);
      try {
        return new KeyPair(new PubKey(curve, publicFromSeed(curve, seed)), new PrivKey(curve, seed));
      } catch (NoSuchAlgorithmException e) {
        throw new ProviderException(e);
      }
    }
  }

  public static final class KeyPairGenAny extends KeyPairGen {
    public KeyPairGenAny() {
      super(null);
    }
  }

  public static final class KeyPairGen25519 extends KeyPairGen {
    public KeyPairGen25519() {
      super(kEd25519);
    }
  }

  public static final class KeyPairGen448 extends KeyPairGen {
    public KeyPairGen448() {
      super(kEd448);
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
        if (spec instanceof EdECPublicKeySpec) {
          EdECPublicKeySpec s = (EdECPublicKeySpec) spec;
          Curve c = check(curveOf(s.getParams()));
          byte[] enc = encodeLe(s.getPoint().getY(), c.length);
          if (s.getPoint().isXOdd()) {
            enc[c.length - 1] |= (byte) 0x80;
          }
          return new PubKey(c, enc);
        }
        if (spec instanceof X509EncodedKeySpec) {
          DerValue seq = new DerValue(((X509EncodedKeySpec) spec).getEncoded());
          DerInputStream in = seq.data();
          String oid = in.getDerValue().data().getOID().toString();
          Curve c = curveOid(oid);
          if (c == null) {
            throw new InvalidKeySpecException("Unsupported key algorithm: " + oid);
          }
          byte[] raw = in.getDerValue().getBitString();
          if (raw.length != check(c).length) {
            throw new InvalidKeySpecException("Key length is " + raw.length + ", should be " + c.length);
          }
          return new PubKey(c, raw);
        }
      } catch (IOException | InvalidAlgorithmParameterException e) {
        throw new InvalidKeySpecException(e);
      }
      throw new InvalidKeySpecException("Only EdECPublicKeySpec and X509EncodedKeySpec supported");
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec spec) throws InvalidKeySpecException {
      try {
        if (spec instanceof EdECPrivateKeySpec) {
          EdECPrivateKeySpec s = (EdECPrivateKeySpec) spec;
          Curve c = check(curveOf(s.getParams()));
          if (s.getBytes().length != c.length) {
            throw new InvalidKeySpecException("Key length is " + s.getBytes().length + ", should be " + c.length);
          }
          return new PrivKey(c, s.getBytes());
        }
        if (spec instanceof PKCS8EncodedKeySpec) {
          DerValue seq = new DerValue(((PKCS8EncodedKeySpec) spec).getEncoded());
          DerInputStream in = seq.data();
          in.getInteger();
          String oid = in.getDerValue().data().getOID().toString();
          Curve c = curveOid(oid);
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
      throw new InvalidKeySpecException("Only EdECPrivateKeySpec and PKCS8EncodedKeySpec supported");
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
      if (key instanceof PubKey) {
        PubKey k = (PubKey) key;
        if (keySpec.isAssignableFrom(EdECPublicKeySpec.class)) {
          return keySpec.cast(new EdECPublicKeySpec(k.getParams(), k.point));
        }
        if (keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
          return keySpec.cast(new X509EncodedKeySpec(k.getEncoded()));
        }
      } else if (key instanceof PrivKey) {
        PrivKey k = (PrivKey) key;
        if (keySpec.isAssignableFrom(EdECPrivateKeySpec.class)) {
          return keySpec.cast(new EdECPrivateKeySpec(k.getParams(), k.seed));
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
      super(kEd25519);
    }
  }

  public static final class Factory448 extends Factory {
    public Factory448() {
      super(kEd448);
    }
  }

  public static class Sig extends SignatureSpi {
    private final Curve fixed;
    private Curve curve;
    private PrivKey priv;
    private PubKey pub;
    private final ByteArrayOutputStream msg = new ByteArrayOutputStream();

    Sig(Curve fixed) {
      this.fixed = fixed;
    }

    private void init(Curve c) throws InvalidKeyException {
      if (fixed != null && fixed != c) {
        throw new InvalidKeyException("Parameters must be " + fixed.name);
      }
      curve = c;
      msg.reset();
    }

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
      if (!(publicKey instanceof PubKey)) {
        throw new InvalidKeyException("Unsupported key type");
      }
      init(((PubKey) publicKey).curve);
      pub = (PubKey) publicKey;
      priv = null;
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
      if (!(privateKey instanceof PrivKey)) {
        throw new InvalidKeyException("Unsupported key type");
      }
      init(((PrivKey) privateKey).curve);
      priv = (PrivKey) privateKey;
      pub = null;
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
      msg.write(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
      msg.write(b, off, len);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
      if (priv == null) {
        throw new SignatureException("Signature object not initialized for signing");
      }
      try {
        byte[] m = msg.toByteArray();
        msg.reset();
        Curve c = curve;
        byte[] ctx = new byte[0];
        byte[] h = seedHash(c, priv.seed);
        BigInteger s = secretScalar(c, h);
        byte[] prefix = Arrays.copyOfRange(h, c.length, h.length);
        byte[] a = encodePoint(c, multiply(c, basePoint(c), s));
        BigInteger r = new BigInteger(1, reverse(hash(c, ctx, prefix, m))).mod(c.order);
        byte[] rEnc = encodePoint(c, multiply(c, basePoint(c), r));
        BigInteger k = new BigInteger(1, reverse(hash(c, ctx, rEnc, a, m))).mod(c.order);
        BigInteger sig = r.add(k.multiply(s)).mod(c.order);
        byte[] out = new byte[c.length * 2];
        System.arraycopy(rEnc, 0, out, 0, c.length);
        System.arraycopy(encodeLe(sig, c.length), 0, out, c.length, c.length);
        return out;
      } catch (NoSuchAlgorithmException e) {
        throw new SignatureException(e);
      }
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
      if (pub == null) {
        throw new SignatureException("Signature object not initialized for verification");
      }
      Curve c = curve;
      byte[] m = msg.toByteArray();
      msg.reset();
      if (sigBytes.length != c.length * 2) {
        throw new SignatureException("signature length invalid");
      }
      try {
        byte[] rEnc = Arrays.copyOfRange(sigBytes, 0, c.length);
        BigInteger s = decodeLe(sigBytes, c.length, c.length);
        if (s.compareTo(c.order) >= 0) {
          throw new SignatureException("Non-canonical signature");
        }
        Point rp = decodePoint(c, rEnc);
        Point ap = decodePoint(c, pub.enc);
        if (rp == null || ap == null) {
          return false;
        }
        BigInteger k = new BigInteger(1, reverse(hash(c, new byte[0], rEnc, pub.enc, m))).mod(c.order);
        byte[] lhs = encodePoint(c, multiply(c, basePoint(c), s));
        byte[] rhs = encodePoint(c, add(c, rp, multiply(c, ap, k)));
        return MessageDigest.isEqual(lhs, rhs);
      } catch (NoSuchAlgorithmException e) {
        throw new SignatureException(e);
      }
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
      if (params != null) {
        throw new InvalidAlgorithmParameterException("Parameters not supported");
      }
    }

    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value) {
      throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    @Deprecated
    protected Object engineGetParameter(String param) {
      throw new UnsupportedOperationException("getParameter() not supported");
    }
  }

  public static final class SigAny extends Sig {
    public SigAny() {
      super(null);
    }
  }

  public static final class Sig25519 extends Sig {
    public Sig25519() {
      super(kEd25519);
    }
  }

  public static final class Sig448 extends Sig {
    public Sig448() {
      super(kEd448);
    }
  }
}
