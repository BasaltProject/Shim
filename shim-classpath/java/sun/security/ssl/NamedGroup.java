/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.List;
import javax.crypto.KeyAgreement;

final class NamedGroup {
  static final NamedGroup kX25519 = new NamedGroup(0x001d, "x25519", null, 32);
  static final NamedGroup kSecp256r1 = new NamedGroup(0x0017, "secp256r1", "secp256r1", 32);
  static final NamedGroup kSecp384r1 = new NamedGroup(0x0018, "secp384r1", "secp384r1", 48);
  static final NamedGroup kSecp521r1 = new NamedGroup(0x0019, "secp521r1", "secp521r1", 66);

  private static final NamedGroup[] kAll = {kX25519, kSecp256r1, kSecp384r1, kSecp521r1};

  final int id;
  final String name;
  final String curve;
  final int fieldBytes;

  private NamedGroup(int id, String name, String curve, int fieldBytes) {
    this.id = id;
    this.name = name;
    this.curve = curve;
    this.fieldBytes = fieldBytes;
  }

  static List<NamedGroup> all() {
    return Arrays.asList(kAll);
  }

  static NamedGroup byId(int id) {
    for (NamedGroup g : kAll) {
      if (g.id == id) {
        return g;
      }
    }
    return null;
  }

  static NamedGroup byName(String name) {
    for (NamedGroup g : kAll) {
      if (g.name.equals(name)) {
        return g;
      }
    }
    return null;
  }

  boolean isXdh() {
    return curve == null;
  }

  KeyShareKeys generate(SecureRandom random) throws GeneralSecurityException {
    return new KeyShareKeys(this, random);
  }

  @Override
  public String toString() {
    return name;
  }

  static final class KeyShareKeys {
    private final NamedGroup group;
    private final byte[] xPrivate;
    private final byte[] publicEncoded;
    private PrivateKey ecPrivate;

    KeyShareKeys(NamedGroup group, SecureRandom random) throws GeneralSecurityException {
      this.group = group;
      if (group.isXdh()) {
        xPrivate = new byte[32];
        random.nextBytes(xPrivate);
        publicEncoded = X25519.publicKey(xPrivate);
      } else {
        xPrivate = null;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec(group.curve), random);
        KeyPair kp = kpg.generateKeyPair();
        ecPrivate = kp.getPrivate();
        publicEncoded = encodePoint(((ECPublicKey) kp.getPublic()).getW(), group.fieldBytes);
      }
    }

    byte[] publicBytes() {
      return publicEncoded.clone();
    }

    byte[] deriveSecret(byte[] peer) throws GeneralSecurityException {
      if (group.isXdh()) {
        if (peer.length != 32) {
          throw new InvalidKeyException("Invalid x25519 key share");
        }
        byte[] secret = X25519.scalarMult(xPrivate, peer);
        byte acc = 0;
        for (byte b : secret) {
          acc |= b;
        }
        if (acc == 0) {
          throw new InvalidKeyException("Invalid x25519 key share");
        }
        return secret;
      }
      ECPoint point = decodePoint(peer, group.fieldBytes);
      AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
      ap.init(new ECGenParameterSpec(group.curve));
      ECParameterSpec spec = ap.getParameterSpec(ECParameterSpec.class);
      checkOnCurve(point, spec);
      PublicKey pub = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(point, spec));
      KeyAgreement ka = KeyAgreement.getInstance("ECDH");
      ka.init(ecPrivate);
      ka.doPhase(pub, true);
      return ka.generateSecret();
    }
  }

  static byte[] encodePoint(ECPoint w, int size) {
    byte[] out = new byte[1 + 2 * size];
    out[0] = 4;
    putFixed(w.getAffineX(), out, 1, size);
    putFixed(w.getAffineY(), out, 1 + size, size);
    return out;
  }

  private static void putFixed(BigInteger v, byte[] dst, int off, int size) {
    byte[] b = v.toByteArray();
    int start = b.length > size ? b.length - size : 0;
    int len = b.length - start;
    System.arraycopy(b, start, dst, off + size - len, len);
  }

  static ECPoint decodePoint(byte[] enc, int size) throws InvalidKeyException {
    if (enc.length != 1 + 2 * size || enc[0] != 4) {
      throw new InvalidKeyException("Invalid EC point encoding");
    }
    BigInteger x = new BigInteger(1, Arrays.copyOfRange(enc, 1, 1 + size));
    BigInteger y = new BigInteger(1, Arrays.copyOfRange(enc, 1 + size, 1 + 2 * size));
    return new ECPoint(x, y);
  }

  static void checkOnCurve(ECPoint point, ECParameterSpec spec) throws InvalidKeyException {
    BigInteger p = ((ECFieldFp) spec.getCurve().getField()).getP();
    BigInteger x = point.getAffineX();
    BigInteger y = point.getAffineY();
    if (x.signum() < 0 || x.compareTo(p) >= 0 || y.signum() < 0 || y.compareTo(p) >= 0) {
      throw new InvalidKeyException("EC point out of range");
    }
    BigInteger lhs = y.multiply(y).mod(p);
    BigInteger rhs = x.multiply(x).add(spec.getCurve().getA()).multiply(x).add(spec.getCurve().getB()).mod(p);
    if (!lhs.equals(rhs)) {
      throw new InvalidKeyException("EC point not on curve");
    }
  }
}
