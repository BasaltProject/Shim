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
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public abstract class ECDSASignature extends SignatureSpi {
  private final MessageDigest md;
  private final boolean p1363;
  private ByteArrayOutputStream rawData;
  private ECPublicKey publicKey;
  private ECPrivateKey privateKey;
  private SecureRandom random;

  ECDSASignature(String digestName, boolean p1363) {
    this.p1363 = p1363;
    if (digestName == null) {
      this.md = null;
      this.rawData = new ByteArrayOutputStream();
    } else {
      try {
        this.md = MessageDigest.getInstance(digestName);
      } catch (NoSuchAlgorithmException e) {
        throw new ProviderException(e);
      }
    }
  }

  @Override
  protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
    this.publicKey = (ECPublicKey) ECKeyFactory.toECKey(publicKey);
    this.privateKey = null;
    reset();
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
    engineInitSign(privateKey, null);
  }

  @Override
  protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
    this.privateKey = (ECPrivateKey) ECKeyFactory.toECKey(privateKey);
    this.publicKey = null;
    this.random = random;
    reset();
  }

  private void reset() {
    if (md != null) {
      md.reset();
    } else {
      rawData.reset();
    }
  }

  @Override
  protected void engineUpdate(byte b) throws SignatureException {
    if (md != null) {
      md.update(b);
    } else {
      rawData.write(b);
    }
  }

  @Override
  protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
    if (md != null) {
      md.update(b, off, len);
    } else {
      rawData.write(b, off, len);
    }
  }

  @Override
  protected void engineUpdate(ByteBuffer input) {
    int len = input.remaining();
    byte[] tmp = new byte[len];
    input.get(tmp);
    try {
      engineUpdate(tmp, 0, len);
    } catch (SignatureException e) {
      throw new ProviderException(e);
    }
  }

  private byte[] digest() {
    if (md != null) {
      return md.digest();
    }
    byte[] data = rawData.toByteArray();
    rawData.reset();
    return data;
  }

  private static BigInteger hashToInt(byte[] digest, int orderBits) {
    BigInteger e = new BigInteger(1, digest);
    int excess = digest.length * 8 - orderBits;
    if (excess > 0) {
      e = e.shiftRight(excess);
    }
    return e;
  }

  @Override
  protected byte[] engineSign() throws SignatureException {
    ECParameterSpec params = privateKey.getParams();
    ECOperations ops = new ECOperations(params);
    BigInteger n = ops.order();
    BigInteger d = privateKey.getS();
    byte[] digest = digest();
    BigInteger e = hashToInt(digest, n.bitLength());
    SecureRandom rnd = random == null ? new SecureRandom() : random;
    BigInteger r;
    BigInteger s = BigInteger.ZERO;
    do {
      BigInteger k = ops.randomScalar(rnd);
      ECPoint kg = ops.multiplyG(k);
      r = kg.getAffineX().mod(n);
      if (r.signum() == 0) {
        continue;
      }
      s = k.modInverse(n).multiply(e.add(r.multiply(d))).mod(n);
    } while (r.signum() == 0 || s.signum() == 0);
    int fieldBytes = (n.bitLength() + 7) >> 3;
    if (p1363) {
      byte[] out = new byte[2 * fieldBytes];
      System.arraycopy(ECUtil.unsigned(r, fieldBytes), 0, out, 0, fieldBytes);
      System.arraycopy(ECUtil.unsigned(s, fieldBytes), 0, out, fieldBytes, fieldBytes);
      return out;
    }
    DerOutputStream body = new DerOutputStream();
    body.putInteger(r);
    body.putInteger(s);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  @Override
  protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
    ECParameterSpec params = publicKey.getParams();
    ECOperations ops = new ECOperations(params);
    BigInteger n = ops.order();
    int fieldBytes = (n.bitLength() + 7) >> 3;
    BigInteger r;
    BigInteger s;
    if (p1363) {
      if (sigBytes.length != 2 * fieldBytes) {
        digest();
        return false;
      }
      r = new BigInteger(1, java.util.Arrays.copyOfRange(sigBytes, 0, fieldBytes));
      s = new BigInteger(1, java.util.Arrays.copyOfRange(sigBytes, fieldBytes, sigBytes.length));
    } else {
      try {
        DerValue seq = new DerValue(sigBytes);
        if (seq.getTag() != DerValue.tag_Sequence) {
          throw new IOException("bad tag");
        }
        DerInputStream in = seq.data();
        r = in.getBigInteger();
        s = in.getBigInteger();
        if (in.available() != 0) {
          throw new IOException("extra data");
        }
      } catch (IOException ex) {
        throw new SignatureException("Invalid encoding for signature", ex);
      }
    }
    byte[] digest = digest();
    if (r.signum() <= 0 || s.signum() <= 0 || r.compareTo(n) >= 0 || s.compareTo(n) >= 0) {
      return false;
    }
    BigInteger e = hashToInt(digest, n.bitLength());
    BigInteger w = s.modInverse(n);
    BigInteger u1 = e.multiply(w).mod(n);
    BigInteger u2 = r.multiply(w).mod(n);
    ECPoint pt = ops.multiplyAdd(u1, publicKey.getW(), u2);
    if (pt == ECPoint.POINT_INFINITY) {
      return false;
    }
    return pt.getAffineX().mod(n).equals(r);
  }

  @Override
  @Deprecated
  protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    throw new UnsupportedOperationException("setParameter() not supported");
  }

  @Override
  protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    if (params != null && !(params instanceof ECParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Parameters must be of type ECParameterSpec");
    }
  }

  @Override
  @Deprecated
  protected Object engineGetParameter(String param) throws InvalidParameterException {
    throw new UnsupportedOperationException("getParameter() not supported");
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
  }

  public static final class SHA1withECDSA extends ECDSASignature {
    public SHA1withECDSA() {
      super("SHA-1", false);
    }
  }

  public static final class SHA1withECDSAinP1363Format extends ECDSASignature {
    public SHA1withECDSAinP1363Format() {
      super("SHA-1", true);
    }
  }

  public static final class SHA224withECDSA extends ECDSASignature {
    public SHA224withECDSA() {
      super("SHA-224", false);
    }
  }

  public static final class SHA224withECDSAinP1363Format extends ECDSASignature {
    public SHA224withECDSAinP1363Format() {
      super("SHA-224", true);
    }
  }

  public static final class SHA256withECDSA extends ECDSASignature {
    public SHA256withECDSA() {
      super("SHA-256", false);
    }
  }

  public static final class SHA256withECDSAinP1363Format extends ECDSASignature {
    public SHA256withECDSAinP1363Format() {
      super("SHA-256", true);
    }
  }

  public static final class SHA384withECDSA extends ECDSASignature {
    public SHA384withECDSA() {
      super("SHA-384", false);
    }
  }

  public static final class SHA384withECDSAinP1363Format extends ECDSASignature {
    public SHA384withECDSAinP1363Format() {
      super("SHA-384", true);
    }
  }

  public static final class SHA512withECDSA extends ECDSASignature {
    public SHA512withECDSA() {
      super("SHA-512", false);
    }
  }

  public static final class SHA512withECDSAinP1363Format extends ECDSASignature {
    public SHA512withECDSAinP1363Format() {
      super("SHA-512", true);
    }
  }

  public static final class SHA3_224withECDSA extends ECDSASignature {
    public SHA3_224withECDSA() {
      super("SHA3-224", false);
    }
  }

  public static final class SHA3_224withECDSAinP1363Format extends ECDSASignature {
    public SHA3_224withECDSAinP1363Format() {
      super("SHA3-224", true);
    }
  }

  public static final class SHA3_256withECDSA extends ECDSASignature {
    public SHA3_256withECDSA() {
      super("SHA3-256", false);
    }
  }

  public static final class SHA3_256withECDSAinP1363Format extends ECDSASignature {
    public SHA3_256withECDSAinP1363Format() {
      super("SHA3-256", true);
    }
  }

  public static final class SHA3_384withECDSA extends ECDSASignature {
    public SHA3_384withECDSA() {
      super("SHA3-384", false);
    }
  }

  public static final class SHA3_384withECDSAinP1363Format extends ECDSASignature {
    public SHA3_384withECDSAinP1363Format() {
      super("SHA3-384", true);
    }
  }

  public static final class SHA3_512withECDSA extends ECDSASignature {
    public SHA3_512withECDSA() {
      super("SHA3-512", false);
    }
  }

  public static final class SHA3_512withECDSAinP1363Format extends ECDSASignature {
    public SHA3_512withECDSAinP1363Format() {
      super("SHA3-512", true);
    }
  }

  public static final class RawECDSA extends ECDSASignature {
    public RawECDSA() {
      super(null, false);
    }
  }

  public static final class RawECDSAinP1363Format extends ECDSASignature {
    public RawECDSAinP1363Format() {
      super(null, true);
    }
  }
}
