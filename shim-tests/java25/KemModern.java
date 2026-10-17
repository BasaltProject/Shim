/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*;
import java.security.spec.*;
import java.util.HexFormat;
import javax.crypto.*;

public class KemModern {
  static final HexFormat H = HexFormat.of();

  static class Det extends SecureRandom {
    int n;
    Det(int seed) { n = seed; }
    @Override public void nextBytes(byte[] b) { for (int i = 0; i < b.length; i++) b[i] = (byte) (n++ * 31 + 7); }
  }

  static void t(String name, java.util.concurrent.Callable<Object> c) {
    try { System.out.println(name + ": " + c.call()); }
    catch (Throwable e) { System.out.println(name + ": !" + e.getClass().getName() + ":" + e.getMessage()); }
  }

  public static void main(String[] a) throws Exception {
    KEM kem = KEM.getInstance("DHKEM");
    for (String n : new String[] {"X25519", "X448", "secp256r1", "secp384r1", "secp521r1"}) {
      boolean x = n.startsWith("X");
      KeyPairGenerator g = KeyPairGenerator.getInstance(x ? n : "EC");
      if (!x) g.initialize(new ECGenParameterSpec(n)); 
      KeyPair kp0 = x ? null : g.generateKeyPair();
      if (x) { g.initialize(new NamedParameterSpec(n), new Det(5)); kp0 = g.generateKeyPair(); }
      final KeyPair kp = kp0;
      System.out.println(n + " pub " + kp.getPublic().getAlgorithm() + " " + (x ? H.formatHex(kp.getPublic().getEncoded()) : ""));
      KEM.Encapsulator e = kem.newEncapsulator(kp.getPublic(), null, new Det(100));
      System.out.println(n + " sizes " + e.secretSize() + " " + e.encapsulationSize() + " " + e.providerName());
      KEM.Encapsulated en = e.encapsulate();
      KEM.Decapsulator d = kem.newDecapsulator(kp.getPrivate());
      System.out.println(n + " dsizes " + d.secretSize() + " " + d.encapsulationSize() + " " + d.providerName());
      SecretKey k = d.decapsulate(en.encapsulation());
      System.out.println(n + " same " + java.util.Arrays.equals(k.getEncoded(), en.key().getEncoded()) + " " + k.getAlgorithm() + " " + k.getFormat() + " " + en.params());
      if (x) {
        System.out.println(n + " enc " + H.formatHex(en.encapsulation()));
        System.out.println(n + " key " + H.formatHex(en.key().getEncoded()));
      }
      KEM.Encapsulated part = e.encapsulate(2, 10, "AES");
      System.out.println(n + " part " + part.key().getAlgorithm() + " " + part.key().getEncoded().length + " " + part.encapsulation().length);
      SecretKey pk = d.decapsulate(part.encapsulation(), 2, 10, "AES");
      System.out.println(n + " partsame " + java.util.Arrays.equals(pk.getEncoded(), part.key().getEncoded()));
      byte[] bad = en.encapsulation().clone(); bad[1] ^= 1;
      t(n + " badenc", () -> H.formatHex(d.decapsulate(bad).getEncoded()).length());
      t(n + " short", () -> d.decapsulate(new byte[3]));
      t(n + " nullenc", () -> d.decapsulate(null));
      t(n + " range", () -> e.encapsulate(5, 4, "AES"));
      t(n + " range2", () -> e.encapsulate(0, e.secretSize() + 1, "AES"));
      t(n + " nullalg", () -> e.encapsulate(0, 4, null));
      t(n + " spec", () -> kem.newEncapsulator(kp.getPublic(), new IvParameterSpec0(), null));
      t(n + " privaspub", () -> kem.newEncapsulator((PublicKey) null));
      t(n + " rsa", () -> { KeyPairGenerator r = KeyPairGenerator.getInstance("RSA"); r.initialize(1024); return kem.newEncapsulator(r.generateKeyPair().getPublic()); });
      t(n + " rsapriv", () -> { KeyPairGenerator r = KeyPairGenerator.getInstance("RSA"); r.initialize(1024); return kem.newDecapsulator(r.generateKeyPair().getPrivate()); });
    }
    t("noalg", () -> KEM.getInstance("NOPE"));
    t("nullalg", () -> KEM.getInstance(null));
    t("badprov", () -> KEM.getInstance("DHKEM", "NOPE"));
    t("sunjce", () -> KEM.getInstance("DHKEM", "SunJCE").newEncapsulator(KeyPairGenerator.getInstance("X25519").generateKeyPair().getPublic()).providerName());
    t("DHKEM-X25519", () -> KEM.getInstance("DHKEM-X25519"));
  }
  static class IvParameterSpec0 implements AlgorithmParameterSpec {}
}
