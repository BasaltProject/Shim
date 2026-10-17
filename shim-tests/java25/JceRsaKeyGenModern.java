/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*; import java.security.interfaces.*; import java.math.BigInteger;
public class JceRsaKeyGenModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static String hx(byte[] b) { return HexFormat.of().formatHex(b); }
  public static void main(String[] a) throws Exception {
    KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
    for (int bits : new int[]{512, 1024}) {
      KeyPairGenerator g2 = KeyPairGenerator.getInstance("RSA"); g2.initialize(bits);
      KeyPair kp = g2.generateKeyPair();
      RSAPrivateCrtKey pr = (RSAPrivateCrtKey) kp.getPrivate();
      RSAPublicKey pu = (RSAPublicKey) kp.getPublic();
      BigInteger one = BigInteger.ONE;
      boolean ok = pu.getModulus().bitLength() == bits && pr.getPrimeP().multiply(pr.getPrimeQ()).equals(pu.getModulus())
          && pr.getPublicExponent().equals(pu.getPublicExponent()) && pr.getPrimeP().isProbablePrime(20) && pr.getPrimeQ().isProbablePrime(20)
          && pr.getPrivateExponent().multiply(pu.getPublicExponent()).mod(pr.getPrimeP().subtract(one)).equals(one)
          && pr.getPrivateExponent().multiply(pu.getPublicExponent()).mod(pr.getPrimeQ().subtract(one)).equals(one)
          && pr.getPrimeExponentP().equals(pr.getPrivateExponent().mod(pr.getPrimeP().subtract(one)))
          && pr.getCrtCoefficient().multiply(pr.getPrimeQ()).mod(pr.getPrimeP()).equals(one);
      System.out.println("KEY" + bits + " " + ok + " " + pu.getPublicExponent() + " " + pu.getEncoded().length + " " + pu.getFormat() + " " + pr.getFormat()
          + " " + pu.getClass().getName() + " " + pr.getClass().getName() + " " + pu.getAlgorithm() + " " + pr.getAlgorithm());
      Signature sg = Signature.getInstance("SHA256withRSA"); sg.initSign(pr); sg.update(new byte[]{1, 2, 3}); byte[] sig = sg.sign();
      Signature vf = Signature.getInstance("SHA256withRSA"); vf.initVerify(pu); vf.update(new byte[]{1, 2, 3});
      System.out.println("SIG" + bits + " " + sig.length + " " + vf.verify(sig));
    }
    KeyPairGenerator g3 = KeyPairGenerator.getInstance("RSA"); 
    p("init 511", () -> { g3.initialize(511); return "x"; });
    p("init 512", () -> { g3.initialize(512); return g3.generateKeyPair().getPublic().toString().length() > 0; });
    p("init 0", () -> { g3.initialize(0); return "x"; });
    p("init -1", () -> { g3.initialize(-1); return "x"; });
    p("init 16385", () -> { g3.initialize(16385); return "x"; });
    p("init 513", () -> { g3.initialize(513); return g3.generateKeyPair().getPublic().toString().length() > 0; });
    p("init spec e=3", () -> { g3.initialize(new RSAKeyGenParameterSpec(512, BigInteger.valueOf(3))); return ((RSAPublicKey) g3.generateKeyPair().getPublic()).getPublicExponent(); });
    p("init spec e=4", () -> { g3.initialize(new RSAKeyGenParameterSpec(512, BigInteger.valueOf(4))); return "x"; });
    p("init spec e=1", () -> { g3.initialize(new RSAKeyGenParameterSpec(512, BigInteger.ONE)); return "x"; });
    p("init spec e big", () -> { g3.initialize(new RSAKeyGenParameterSpec(512, BigInteger.ONE.shiftLeft(512).add(BigInteger.ONE))); return "x"; });
    p("init wrong spec", () -> { g3.initialize(new ECGenParameterSpec("secp256r1")); return "x"; });
    p("init pss spec", () -> { g3.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4, new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))); return "x"; });
    p("kpg pss", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("RSASSA-PSS"); k.initialize(1024); KeyPair kp = k.generateKeyPair(); return kp.getPublic().getAlgorithm() + " " + kp.getPublic().getClass().getName() + " " + ((RSAKey) kp.getPublic()).getParams(); });
    p("kpg pss params", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("RSASSA-PSS"); k.initialize(new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4, new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))); KeyPair kp = k.generateKeyPair(); PSSParameterSpec ps = (PSSParameterSpec) ((RSAKey) kp.getPublic()).getParams(); return ps.getDigestAlgorithm() + " " + ps.getSaltLength() + " " + kp.getPublic().getEncoded().length; });
    p("kpg alias", () -> KeyPairGenerator.getInstance("1.2.840.113549.1.1.1").getAlgorithm());
    p("kpg alias2", () -> KeyPairGenerator.getInstance("OID.1.2.840.113549.1.1.1").getAlgorithm());
    p("kpg provider", () -> KeyPairGenerator.getInstance("RSA").getProvider());
    p("kpg uninit", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("RSA"); KeyPair kp = k.generateKeyPair(); return ((RSAPublicKey) kp.getPublic()).getModulus().bitLength(); });
    p("kpg random", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("RSA"); k.initialize(512, new SecureRandom()); return k.generateKeyPair().getPublic().getFormat(); });
  }
}
