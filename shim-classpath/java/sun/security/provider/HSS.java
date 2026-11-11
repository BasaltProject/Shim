/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/** Verification of HSS/LMS signatures (RFC 8554) with SHA-256, n = m = 32. */
public final class HSS {
  private static final int kN = 32;
  private static final byte[] kOid = {0x06, 0x0b, 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x0d, 0x01,
      0x09, 0x10, 0x03, 0x11};

  private HSS() {
  }

  private static int lmsHeight(int type) {
    return type >= 5 && type <= 9 ? 5 * (type - 4) : -1;
  }

  private static int otsWidth(int type) {
    return type >= 1 && type <= 4 ? 1 << (type - 1) : -1;
  }

  private static int u32(byte[] b, int off) {
    return (b[off] & 0xff) << 24 | (b[off + 1] & 0xff) << 16 | (b[off + 2] & 0xff) << 8 | (b[off + 3] & 0xff);
  }

  private static void be32(MessageDigest md, long v) {
    md.update((byte) (v >>> 24));
    md.update((byte) (v >>> 16));
    md.update((byte) (v >>> 8));
    md.update((byte) v);
  }

  private static void be16(MessageDigest md, int v) {
    md.update((byte) (v >>> 8));
    md.update((byte) v);
  }

  private static int otsLength(int w) {
    return 4 + kN + otsP(w) * kN;
  }

  private static int otsP(int w) {
    int u = 8 * kN / w;
    int v = (32 - Integer.numberOfLeadingZeros(((1 << w) - 1) * u) + w - 1) / w;
    return u + v;
  }

  private static int coef(byte[] s, int i, int w) {
    return ((1 << w) - 1) & ((s[i * w / 8] & 0xff) >> (8 - (w * (i % (8 / w)) + w)));
  }

  private static MessageDigest sha() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static boolean verifyLms(byte[] msg, byte[] sig, int off, int total, byte[] key) {
    int q = u32(sig, off);
    int w = otsWidth(u32(sig, off + 4));
    int h = lmsHeight(u32(sig, off + 4 + otsLength(w)));
    if (u32(key, 0) != u32(sig, off + 4 + otsLength(w)) || otsWidth(u32(key, 4)) != w) {
      return false;
    }
    if (q < 0 || q >= 1 << h) {
      return false;
    }
    int idOff = 8;
    int rootOff = 24;
    int p = otsP(w);
    int ls = 16 - (p - 8 * kN / w) * w;
    MessageDigest md = sha();
    md.update(key, idOff, 16);
    be32(md, q);
    be16(md, 0x8181);
    md.update(sig, off + 8, kN);
    md.update(msg);
    byte[] qa = Arrays.copyOf(md.digest(), kN + 2);
    int sum = 0;
    for (int i = 0; i < 8 * kN / w; i++) {
      sum += ((1 << w) - 1) - coef(qa, i, w);
    }
    sum <<= ls;
    qa[kN] = (byte) (sum >>> 8);
    qa[kN + 1] = (byte) sum;
    MessageDigest pub = sha();
    pub.update(key, idOff, 16);
    be32(pub, q);
    be16(pub, 0x8080);
    byte[] tmp = new byte[kN];
    for (int i = 0; i < p; i++) {
      System.arraycopy(sig, off + 8 + kN + i * kN, tmp, 0, kN);
      for (int j = coef(qa, i, w); j < (1 << w) - 1; j++) {
        md.update(key, idOff, 16);
        be32(md, q);
        be16(md, i);
        md.update((byte) j);
        md.update(tmp);
        try {
          md.digest(tmp, 0, kN);
        } catch (java.security.DigestException e) {
          throw new IllegalStateException(e);
        }
      }
      pub.update(tmp);
    }
    byte[] kc = pub.digest();
    long node = (1L << h) + q;
    md.update(key, idOff, 16);
    be32(md, node);
    be16(md, 0x8282);
    md.update(kc);
    byte[] cur = md.digest();
    int pathOff = off + 4 + otsLength(w) + 4;
    for (int i = 0; i < h; i++) {
      md.update(key, idOff, 16);
      be32(md, node >> 1);
      be16(md, 0x8383);
      if ((node & 1) == 1) {
        md.update(sig, pathOff + i * kN, kN);
        md.update(cur);
      } else {
        md.update(cur);
        md.update(sig, pathOff + i * kN, kN);
      }
      cur = md.digest();
      node >>= 1;
    }
    return java.security.MessageDigest.isEqual(cur, Arrays.copyOfRange(key, rootOff, rootOff + kN));
  }

  /** Returns the total LMS signature length at off, or throws for malformed or truncated data. */
  private static int lmsSignatureLength(byte[] sig, int off) throws SignatureException {
    if (sig.length - off < 8) {
      throw new SignatureException("LMS signature is too short");
    }
    int w = otsWidth(u32(sig, off + 4));
    if (w < 0) {
      throw new SignatureException(new IllegalArgumentException("Unsupported or bad OTS Algorithm Identifier."));
    }
    int typeOff = off + 4 + otsLength(w);
    if (sig.length - typeOff < 4) {
      throw new SignatureException("LMS signature is too short");
    }
    int h = lmsHeight(u32(sig, typeOff));
    if (h < 0) {
      throw new SignatureException(new IllegalArgumentException("Unsupported or bad LMS type"));
    }
    return 4 + otsLength(w) + 4 + h * kN;
  }

  static boolean verify(byte[] publicKey, byte[] msg, byte[] sig) throws SignatureException {
    if (sig.length < 4) {
      throw new SignatureException("HSS signature is too short");
    }
    long levels = u32(publicKey, 0) & 0xffffffffL;
    long nspk = u32(sig, 0) & 0xffffffffL;
    if (nspk + 1 != levels) {
      throw new SignatureException("HSS signature and public key have different tree heights");
    }
    byte[] key = Arrays.copyOfRange(publicKey, 4, publicKey.length);
    int off = 4;
    for (long i = 0; i < nspk; i++) {
      int len = lmsSignatureLength(sig, off);
      if (sig.length - off < len + 8 + 16 + kN) {
        throw new SignatureException("LMS signature is too short");
      }
      byte[] next = Arrays.copyOfRange(sig, off + len, off + len + 8 + 16 + kN);
      if (!verifyLms(next, sig, off, len, key)) {
        return false;
      }
      key = next;
      off += len + next.length;
    }
    int len = lmsSignatureLength(sig, off);
    if (sig.length - off != len) {
      throw new SignatureException("LMS signature length is incorrect");
    }
    return verifyLms(msg, sig, off, len, key);
  }

  public static final class HSSPublicKey implements PublicKey {
    private static final long serialVersionUID = 1L;
    final byte[] raw;

    HSSPublicKey(byte[] raw) {
      this.raw = raw;
    }

    @Override
    public String getAlgorithm() {
      return "HSS/LMS";
    }

    @Override
    public String getFormat() {
      return "X.509";
    }

    @Override
    public byte[] getEncoded() {
      byte[] bits = new byte[raw.length + 1];
      System.arraycopy(raw, 0, bits, 1, raw.length);
      return MLDSAKeys.tlv(0x30, MLDSAKeys.tlv(0x30, kOid), MLDSAKeys.tlv(0x03, bits));
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof HSSPublicKey && Arrays.equals(((HSSPublicKey) o).raw, raw);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(raw);
    }

    @Override
    public String toString() {
      return "HSS/LMS public key, " + (raw.length * 8) + " bits";
    }
  }

  static HSSPublicKey parse(byte[] encoded) throws InvalidKeyException {
    byte[] raw;
    try {
      int[] top = MLDSAKeys.read(encoded, 0, encoded.length);
      if (top[0] != 0x30 || top[2] != encoded.length) {
        throw new InvalidKeyException("Unable to decode key");
      }
      int[] alg = MLDSAKeys.read(encoded, top[1], top[2]);
      if (alg[0] != 0x30 || !Arrays.equals(Arrays.copyOfRange(encoded, alg[1], alg[2]), kOid)) {
        throw new InvalidKeyException("Unable to decode key");
      }
      int[] bits = MLDSAKeys.read(encoded, alg[2], top[2]);
      if (bits[0] != 0x03 || bits[2] != top[2] || encoded[bits[1]] != 0) {
        throw new InvalidKeyException("Unable to decode key");
      }
      raw = Arrays.copyOfRange(encoded, bits[1] + 1, bits[2]);
      if (raw.length > 2 && raw[0] == 0x04 && raw[1] != 0 && (raw[1] & 0xff) == raw.length - 2) {
        raw = Arrays.copyOfRange(raw, 2, raw.length);
      }
    } catch (InvalidKeyException e) {
      throw new InvalidKeyException("Unable to decode key");
    }
    if (raw.length < 12) {
      throw new InvalidKeyException("LMS public key is too short");
    }
    if (lmsHeight(u32(raw, 4)) < 0) {
      throw new InvalidKeyException("Unsupported or bad LMS type");
    }
    if (otsWidth(u32(raw, 8)) < 0) {
      throw new InvalidKeyException("Unsupported or bad OTS Algorithm Identifier.");
    }
    if (raw.length != 4 + 8 + 16 + kN) {
      throw new InvalidKeyException("Wrong LMS public key length");
    }
    return new HSSPublicKey(raw);
  }

  public static final class KeyFactoryImpl extends KeyFactorySpi {
    @Override
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
      if (!(keySpec instanceof X509EncodedKeySpec)) {
        throw new InvalidKeySpecException("Unrecognized KeySpec");
      }
      try {
        return parse(((X509EncodedKeySpec) keySpec).getEncoded());
      } catch (InvalidKeyException e) {
        throw new InvalidKeySpecException(e);
      }
    }

    @Override
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
      throw new InvalidKeySpecException("Private key generation is not supported");
    }

    @Override
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
      if (key instanceof HSSPublicKey && keySpec.isAssignableFrom(X509EncodedKeySpec.class)) {
        return keySpec.cast(new X509EncodedKeySpec(key.getEncoded()));
      }
      throw new InvalidKeySpecException("keySpec is not an X509EncodedKeySpec");
    }

    @Override
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
      if (key instanceof HSSPublicKey) {
        return key;
      }
      if (key != null && "HSS/LMS".equalsIgnoreCase(key.getAlgorithm()) && "X.509".equals(key.getFormat())) {
        return parse(key.getEncoded());
      }
      throw new InvalidKeyException(new InvalidKeySpecException("Wrong key format or key algorithm"));
    }
  }

  public static final class SignatureImpl extends SignatureSpi {
    private final ByteArrayOutputStream message = new ByteArrayOutputStream();
    private HSSPublicKey pub;

    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
      pub = (HSSPublicKey) new KeyFactoryImpl().engineTranslateKey(publicKey);
      message.reset();
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
      throw new InvalidKeyException("HSS/LMS signing is not supported");
    }

    @Override
    protected void engineUpdate(byte b) {
      message.write(b);
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) {
      message.write(b, off, len);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
      throw new SignatureException("HSS/LMS signing is not supported");
    }

    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
      byte[] msg = message.toByteArray();
      message.reset();
      return verify(pub.raw, msg, sigBytes);
    }

    @Override
    protected void engineSetParameter(java.security.spec.AlgorithmParameterSpec params)
        throws java.security.InvalidAlgorithmParameterException {
      if (params != null) {
        throw new java.security.InvalidAlgorithmParameterException("No parameter accepted");
      }
    }

    @Override
    protected java.security.AlgorithmParameters engineGetParameters() {
      return null;
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
}
