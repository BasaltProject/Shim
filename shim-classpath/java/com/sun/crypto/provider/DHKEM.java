/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;
import javax.crypto.DecapsulateException;
import javax.crypto.KEM;
import javax.crypto.KEMSpi;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.security.ec.ECOperations;
import sun.security.ec.XDH;

public class DHKEM implements KEMSpi {
  private static final byte[] kHpke = {'H', 'P', 'K', 'E', '-', 'v', '1'};

  private enum Kem {
    P256(0x10, "HmacSHA256", 32, 32, 256),
    P384(0x11, "HmacSHA384", 48, 48, 384),
    P521(0x12, "HmacSHA512", 64, 64, 521),
    X25519(0x20, "HmacSHA256", 32, 32, 255),
    X448(0x21, "HmacSHA512", 64, 56, 448);

    final int id;
    final String mac;
    final int secretSize;
    final int fieldBytes;
    final int bits;

    Kem(int id, String mac, int secretSize, int fieldBytes, int bits) {
      this.id = id;
      this.mac = mac;
      this.secretSize = secretSize;
      this.fieldBytes = fieldBytes;
      this.bits = bits;
    }

    boolean isEc() {
      return id < 0x20;
    }

    int encapsulationSize() {
      return isEc() ? 1 + 2 * ((bits + 7) / 8) : fieldBytes;
    }
  }

  private static Kem kemFor(Object key) throws InvalidKeyException {
    if (key instanceof XECPublicKey || key instanceof XECPrivateKey) {
      AlgorithmParameterSpec ps = key instanceof XECPublicKey ? ((XECPublicKey) key).getParams()
          : ((XECPrivateKey) key).getParams();
      if (ps instanceof NamedParameterSpec) {
        String n = ((NamedParameterSpec) ps).getName();
        if (n.equalsIgnoreCase("X25519")) {
          return Kem.X25519;
        }
        if (n.equalsIgnoreCase("X448")) {
          return Kem.X448;
        }
      }
      throw new InvalidKeyException("Unsupported key");
    }
    if (key instanceof ECPublicKey || key instanceof ECPrivateKey) {
      ECParameterSpec ps = key instanceof ECPublicKey ? ((ECPublicKey) key).getParams()
          : ((ECPrivateKey) key).getParams();
      int size = ps.getCurve().getField().getFieldSize();
      if (size == 256) {
        return Kem.P256;
      }
      if (size == 384) {
        return Kem.P384;
      }
      if (size == 521) {
        return Kem.P521;
      }
      throw new InvalidKeyException("Unsupported curve");
    }
    throw new InvalidKeyException("Unsupported key");
  }

  @Override
  public EncapsulatorSpi engineNewEncapsulator(PublicKey publicKey, AlgorithmParameterSpec spec, SecureRandom random)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    if (publicKey == null) {
      throw new InvalidKeyException("input key is null");
    }
    if (spec != null) {
      throw new InvalidAlgorithmParameterException("no spec needed");
    }
    return new Encap(publicKey, kemFor(publicKey), random);
  }

  @Override
  public DecapsulatorSpi engineNewDecapsulator(PrivateKey privateKey, AlgorithmParameterSpec spec)
      throws InvalidAlgorithmParameterException, InvalidKeyException {
    if (privateKey == null) {
      throw new InvalidKeyException("input key is null");
    }
    if (spec != null) {
      throw new InvalidAlgorithmParameterException("no spec needed");
    }
    return new Decap(privateKey, kemFor(privateKey));
  }

  private static byte[] ecEncode(Kem kem, ECPoint w) {
    int n = (kem.bits + 7) / 8;
    byte[] out = new byte[1 + 2 * n];
    out[0] = 4;
    copyPadded(w.getAffineX(), out, 1, n);
    copyPadded(w.getAffineY(), out, 1 + n, n);
    return out;
  }

  private static void copyPadded(BigInteger v, byte[] out, int off, int n) {
    byte[] b = v.toByteArray();
    int skip = b.length > n ? b.length - n : 0;
    System.arraycopy(b, skip, out, off + n - (b.length - skip), b.length - skip);
  }

  private static byte[] xEncode(Kem kem, BigInteger u) {
    byte[] be = u.toByteArray();
    byte[] out = new byte[kem.fieldBytes];
    for (int i = 0; i < out.length && i < be.length; i++) {
      out[i] = be[be.length - 1 - i];
    }
    return out;
  }

  private static byte[] serialize(Kem kem, PublicKey pk) {
    if (kem.isEc()) {
      return ecEncode(kem, ((ECPublicKey) pk).getW());
    }
    return xEncode(kem, ((XECPublicKey) pk).getU());
  }

  private static byte[] publicOf(Kem kem, PrivateKey sk) throws InvalidKeyException {
    if (kem.isEc()) {
      ECPrivateKey ec = (ECPrivateKey) sk;
      return ecEncode(kem, new ECOperations(ec.getParams()).multiplyG(ec.getS()));
    }
    byte[] scalar = ((XECPrivateKey) sk).getScalar().orElseThrow(() -> new InvalidKeyException("No scalar"));
    return XDH.publicKey(kem == Kem.X25519 ? "X25519" : "X448", scalar);
  }

  private static byte[] dh(Kem kem, PrivateKey sk, PublicKey pk) throws GeneralSecurityException {
    KeyAgreement ka = KeyAgreement.getInstance(kem.isEc() ? "ECDH" : "XDH");
    ka.init(sk);
    ka.doPhase(pk, true);
    return ka.generateSecret();
  }

  private static byte[] concat(byte[]... parts) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (byte[] p : parts) {
      out.write(p, 0, p.length);
    }
    return out.toByteArray();
  }

  private static byte[] hmac(Kem kem, byte[] key, byte[] data) {
    try {
      Mac mac = Mac.getInstance(kem.mac);
      mac.init(new SecretKeySpec(key.length == 0 ? new byte[mac.getMacLength()] : key, kem.mac));
      return mac.doFinal(data);
    } catch (GeneralSecurityException e) {
      throw new ProviderException(e);
    }
  }

  private static byte[] suiteId(Kem kem) {
    return new byte[] {'K', 'E', 'M', (byte) (kem.id >> 8), (byte) kem.id};
  }

  private static byte[] sharedSecret(Kem kem, byte[] dh, byte[] context) {
    byte[] suite = suiteId(kem);
    byte[] prk = hmac(kem, new byte[0], concat(kHpke, suite, "eae_prk".getBytes(), dh));
    byte[] info = concat(new byte[] {(byte) (kem.secretSize >> 8), (byte) kem.secretSize}, kHpke, suite,
        "shared_secret".getBytes(), context);
    byte[] out = new byte[kem.secretSize];
    byte[] t = new byte[0];
    int pos = 0;
    for (int i = 1; pos < out.length; i++) {
      t = hmac(kem, prk, concat(t, info, new byte[] {(byte) i}));
      int n = Math.min(t.length, out.length - pos);
      System.arraycopy(t, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  private static final class Encap implements EncapsulatorSpi {
    private final PublicKey pk;
    private final Kem kem;
    private final SecureRandom random;

    Encap(PublicKey pk, Kem kem, SecureRandom random) {
      this.pk = pk;
      this.kem = kem;
      this.random = random;
    }

    @Override
    public KEM.Encapsulated engineEncapsulate(int from, int to, String algorithm) {
      try {
        KeyPairGenerator g = KeyPairGenerator.getInstance(kem.isEc() ? "EC" : "XDH");
        g.initialize(kem.isEc() ? (AlgorithmParameterSpec) ((ECPublicKey) pk).getParams()
            : ((XECPublicKey) pk).getParams(), random);
        KeyPair e = g.generateKeyPair();
        byte[] enc = serialize(kem, e.getPublic());
        byte[] secret = sharedSecret(kem, dh(kem, e.getPrivate(), pk), concat(enc, serialize(kem, pk)));
        return new KEM.Encapsulated(new SecretKeySpec(secret, from, to - from, algorithm), enc, null);
      } catch (GeneralSecurityException e) {
        throw new ProviderException("internal error", e);
      }
    }

    @Override
    public int engineSecretSize() {
      return kem.secretSize;
    }

    @Override
    public int engineEncapsulationSize() {
      return kem.encapsulationSize();
    }
  }

  private static final class Decap implements DecapsulatorSpi {
    private final PrivateKey sk;
    private final Kem kem;

    Decap(PrivateKey sk, Kem kem) {
      this.sk = sk;
      this.kem = kem;
    }

    @Override
    public SecretKey engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm)
        throws DecapsulateException {
      if (encapsulation.length != kem.encapsulationSize()) {
        throw new DecapsulateException("incorrect encapsulation size");
      }
      try {
        PublicKey pkE;
        if (kem.isEc()) {
          if (encapsulation[0] != 4) {
            throw new InvalidKeyException("Unsupported point format");
          }
          int n = (kem.bits + 7) / 8;
          ECPoint w = new ECPoint(new BigInteger(1, Arrays.copyOfRange(encapsulation, 1, 1 + n)),
              new BigInteger(1, Arrays.copyOfRange(encapsulation, 1 + n, 1 + 2 * n)));
          pkE = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(w, ((ECPrivateKey) sk).getParams()));
        } else {
          byte[] be = new byte[encapsulation.length];
          for (int i = 0; i < be.length; i++) {
            be[i] = encapsulation[be.length - 1 - i];
          }
          pkE = KeyFactory.getInstance("XDH").generatePublic(new XECPublicKeySpec(
              ((XECPrivateKey) sk).getParams(), new BigInteger(1, be)));
        }
        byte[] pkR = publicOf(kem, sk);
        byte[] secret = sharedSecret(kem, dh(kem, sk, pkE), concat(encapsulation, pkR));
        return new SecretKeySpec(secret, from, to - from, algorithm);
      } catch (GeneralSecurityException | RuntimeException e) {
        throw new DecapsulateException("Cannot decapsulate", e);
      }
    }

    @Override
    public int engineSecretSize() {
      return kem.secretSize;
    }

    @Override
    public int engineEncapsulationSize() {
      return kem.encapsulationSize();
    }
  }
}
