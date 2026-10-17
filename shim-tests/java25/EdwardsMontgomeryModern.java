/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.HexFormat;
import javax.crypto.KeyAgreement;

public class EdwardsMontgomeryModern {
  static final HexFormat H = HexFormat.of();

  static void t(String name, java.util.concurrent.Callable<Object> c) {
    try { System.out.println(name + ": " + c.call()); }
    catch (Throwable e) { System.out.println(name + ": !" + e.getClass().getName() + ":" + e.getMessage()); }
  }

  public static void main(String[] a) throws Exception {
    for (String n : new String[] {"Ed25519", "Ed448"}) {
      int len = n.equals("Ed25519") ? 32 : 57;
      byte[] seed = new byte[len];
      for (int i = 0; i < len; i++) seed[i] = (byte) (i * 7 + 3);
      KeyFactory kf = KeyFactory.getInstance(n);
      PrivateKey priv = kf.generatePrivate(new EdECPrivateKeySpec(new NamedParameterSpec(n), seed));
      System.out.println(n + " priv " + priv.getAlgorithm() + " " + priv.getFormat() + " " + H.formatHex(priv.getEncoded()));
      Signature s = Signature.getInstance(n);
      s.initSign(priv);
      s.update("hello world".getBytes());
      byte[] sig = s.sign();
      System.out.println(n + " sig " + H.formatHex(sig));
      PrivateKey p2 = kf.generatePrivate(new PKCS8EncodedKeySpec(priv.getEncoded()));
      System.out.println(n + " priv eq " + p2.equals(priv) + " " + ((EdECPrivateKey) p2).getBytes().isPresent());
      KeyPairGenerator g = KeyPairGenerator.getInstance(n);
      KeyPair kp = g.generateKeyPair();
      Signature s2 = Signature.getInstance("EdDSA");
      s2.initSign(kp.getPrivate());
      s2.update(new byte[] {1, 2, 3});
      byte[] sg = s2.sign();
      s2.initVerify(kp.getPublic());
      s2.update(new byte[] {1, 2, 3});
      System.out.println(n + " verify " + s2.verify(sg));
      s2.initVerify(kp.getPublic());
      s2.update(new byte[] {1, 2, 4});
      System.out.println(n + " verify bad " + s2.verify(sg));
      EdECPublicKey pk = (EdECPublicKey) kp.getPublic();
      PublicKey pk2 = kf.generatePublic(new EdECPublicKeySpec(new NamedParameterSpec(n), pk.getPoint()));
      System.out.println(n + " pub " + pk2.equals(pk) + " " + pk.getAlgorithm() + " " + pk.getFormat() + " " + pk.getParams().getName());
      PublicKey pk3 = kf.generatePublic(new X509EncodedKeySpec(pk.getEncoded()));
      System.out.println(n + " x509 " + pk3.equals(pk) + " " + (pk.getEncoded().length));
      t(n + " wrongsig", () -> { s2.initVerify(kp.getPublic()); return s2.verify(new byte[3]); });
      t(n + " noinit", () -> Signature.getInstance(n).sign());
    }
    t("ed25519 with 448 key", () -> { Signature x = Signature.getInstance("Ed25519"); x.initSign(KeyPairGenerator.getInstance("Ed448").generateKeyPair().getPrivate()); return "ok"; });
    for (String n : new String[] {"X25519", "X448"}) {
      int len = n.equals("X25519") ? 32 : 56;
      byte[] k1 = new byte[len], k2 = new byte[len];
      for (int i = 0; i < len; i++) { k1[i] = (byte) (i * 5 + 1); k2[i] = (byte) (i * 11 + 9); }
      KeyFactory kf = KeyFactory.getInstance("XDH");
      PrivateKey a1 = kf.generatePrivate(new XECPrivateKeySpec(new NamedParameterSpec(n), k1));
      PrivateKey b1 = kf.generatePrivate(new XECPrivateKeySpec(new NamedParameterSpec(n), k2));
      System.out.println(n + " priv " + a1.getAlgorithm() + " " + a1.getFormat() + " " + H.formatHex(a1.getEncoded()));
      KeyPairGenerator g = KeyPairGenerator.getInstance(n);
      KeyPair x = g.generateKeyPair(), y = g.generateKeyPair();
      KeyAgreement ka = KeyAgreement.getInstance(n);
      ka.init(x.getPrivate()); ka.doPhase(y.getPublic(), true);
      byte[] s1 = ka.generateSecret();
      ka.init(y.getPrivate()); ka.doPhase(x.getPublic(), true);
      byte[] s2 = ka.generateSecret();
      System.out.println(n + " agree " + java.util.Arrays.equals(s1, s2) + " " + s1.length);
      XECPublicKey xp = (XECPublicKey) x.getPublic();
      PublicKey rt = kf.generatePublic(new XECPublicKeySpec(new NamedParameterSpec(n), xp.getU()));
      System.out.println(n + " pub " + rt.equals(xp) + " " + xp.getAlgorithm() + " " + xp.getFormat() + " " + H.formatHex(xp.getEncoded()).length());
      // fixed vector from RFC 7748: derive public from k1, secret with k2's public
      KeyAgreement k = KeyAgreement.getInstance("XDH");
      k.init(a1);
      // use base point as peer: u = 9 or 5 gives public key of a1
      k.doPhase(kf.generatePublic(new XECPublicKeySpec(new NamedParameterSpec(n), java.math.BigInteger.valueOf(n.equals("X25519") ? 9 : 5))), true);
      System.out.println(n + " pubfromk1 " + H.formatHex(k.generateSecret()));
      t(n + " small", () -> { KeyAgreement q = KeyAgreement.getInstance(n); q.init(a1); q.doPhase(kf.generatePublic(new XECPublicKeySpec(new NamedParameterSpec(n), java.math.BigInteger.ZERO)), true); return H.formatHex(q.generateSecret()); });
      t(n + " mismatch", () -> { KeyAgreement q = KeyAgreement.getInstance(n); q.init(a1); q.doPhase(KeyPairGenerator.getInstance(n.equals("X25519") ? "X448" : "X25519").generateKeyPair().getPublic(), true); return "ok"; });
    }
  }
}
