/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*; import java.security.interfaces.*; import java.math.BigInteger;
public class JceEcModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H").replaceAll("JceEcModern\\$\\d", "Anon")); }
  static String hx(byte[] b) { return HexFormat.of().formatHex(b); }
  public static void main(String[] a) throws Exception {
    KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
    p("default size", () -> { KeyPair kp = g.generateKeyPair(); return ((ECPublicKey) kp.getPublic()).getParams().getCurve().getField().getFieldSize(); });
    for (int s : new int[]{128, 192, 224, 255, 256, 384, 521, 512, 0, -1}) {
      p("init " + s, () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(s); return ((ECPublicKey) k.generateKeyPair().getPublic()).getParams().toString(); });
    }
    for (String n : new String[]{"secp256r1", "secp384r1", "secp521r1", "NIST P-256", "prime256v1", "X9.62 prime256v1", "1.2.840.10045.3.1.7", "secp256k1", "secp224r1", "secp192r1", "brainpoolP256r1", "foo", "SECP256R1", "nist p-256", "P-256"}) {
      p("init name " + n, () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(new ECGenParameterSpec(n)); return ((ECPublicKey) k.generateKeyPair().getPublic()).getParams().toString(); });
    }
    p("init bad spec", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(new RSAKeyGenParameterSpec(1024, BigInteger.valueOf(3))); return "x"; });
    p("init null spec", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize((AlgorithmParameterSpec) null); return "x"; });
    p("init paramspec", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(new ECGenParameterSpec("secp384r1")); ECParameterSpec ps = ap.getParameterSpec(ECParameterSpec.class); KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(ps); return ((ECPublicKey) k.generateKeyPair().getPublic()).getParams().toString(); });
    p("init custom paramspec", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(new ECGenParameterSpec("secp256r1")); ECParameterSpec ps0 = ap.getParameterSpec(ECParameterSpec.class); ECParameterSpec ps = new ECParameterSpec(ps0.getCurve(), ps0.getGenerator(), ps0.getOrder(), ps0.getCofactor()); KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(ps); return ((ECPublicKey) k.generateKeyPair().getPublic()).getParams().toString(); });
    p("alg params", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(new ECGenParameterSpec("secp256r1")); ECGenParameterSpec gs = ap.getParameterSpec(ECGenParameterSpec.class); return gs.getName() + " " + ap.getParameterSpec(ECParameterSpec.class).getClass().getName() + " " + ap.getParameterSpec(AlgorithmParameterSpec.class).getClass().getSimpleName() + " " + hx(ap.getEncoded()); });
    p("alg params der", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(HexFormat.of().parseHex("06052b81040022")); return ap.toString() + " " + ap.getParameterSpec(ECGenParameterSpec.class).getName(); });
    p("alg params der bad", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(HexFormat.of().parseHex("06052b81040099")); return ap.toString(); });
    p("alg params bad spec", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(new IvParameterSpec(new byte[8])); return ap.toString(); });
    p("alg params unknown name", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("EC"); ap.init(new ECGenParameterSpec("nope")); return ap.toString(); });
    KeyFactory kf = KeyFactory.getInstance("EC");
    PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(HexFormat.of().parseHex("3041020100301306072a8648ce3d020106082a8648ce3d030107042730250201010420c30bcff4ed5a4461ad0933c6d124aac9f7417a3db5c46af99a4b65c21e83c1e7")));
    PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(HexFormat.of().parseHex("3059301306072a8648ce3d020106082a8648ce3d03010703420004b58dd3904a24e8f83168060956a976263afc05991e338ccf474a4e516396945cd8e1ed9edf43c626fe3f75c93f746a734dd9969ae93ae8fbca53a580838fea04")));
    p("key classes", () -> priv.getClass().getName() + " " + pub.getClass().getName() + " " + priv.getAlgorithm() + " " + priv.getFormat() + " " + pub.getFormat() + " " + hx(priv.getEncoded()).length() + " " + hx(pub.getEncoded()).length());
    p("key str", () -> pub.toString().replace("\n", "|"));
    p("key interfaces", () -> { ECPublicKey ep = (ECPublicKey) pub; ECPrivateKey epr = (ECPrivateKey) priv; return ep.getW().getAffineX().bitLength() + " " + epr.getS().bitLength() + " " + ep.getParams().getOrder().bitLength() + " " + ep.getParams().getCofactor() + " " + ep.getParams().getClass().getName(); });
    p("key equal", () -> { PublicKey p2 = kf.generatePublic(new ECPublicKeySpec(((ECPublicKey) pub).getW(), ((ECPublicKey) pub).getParams())); PrivateKey q2 = kf.generatePrivate(new ECPrivateKeySpec(((ECPrivateKey) priv).getS(), ((ECPrivateKey) priv).getParams())); return p2.equals(pub) + " " + q2.equals(priv) + " " + (p2.hashCode() == pub.hashCode()) + " " + hx(q2.getEncoded()).equals(hx(priv.getEncoded())) + " " + hx(p2.getEncoded()).equals(hx(pub.getEncoded())); });
    p("key specs", () -> { ECPublicKeySpec s = kf.getKeySpec(pub, ECPublicKeySpec.class); ECPrivateKeySpec s2 = kf.getKeySpec(priv, ECPrivateKeySpec.class); X509EncodedKeySpec s3 = kf.getKeySpec(pub, X509EncodedKeySpec.class); PKCS8EncodedKeySpec s4 = kf.getKeySpec(priv, PKCS8EncodedKeySpec.class); return s.getW().getAffineX().bitLength() + " " + s2.getS().bitLength() + " " + s3.getEncoded().length + " " + s4.getEncoded().length; });
    p("key spec bad", () -> kf.getKeySpec(pub, ECPrivateKeySpec.class));
    p("key spec bad2", () -> kf.getKeySpec(priv, ECPublicKeySpec.class));
    p("key spec bad3", () -> kf.getKeySpec(pub, RSAPublicKeySpec.class));
    p("gen pub garbage", () -> kf.generatePublic(new X509EncodedKeySpec(new byte[10])));
    p("gen priv garbage", () -> kf.generatePrivate(new PKCS8EncodedKeySpec(new byte[10])));
    p("gen pub wrong spec", () -> kf.generatePublic(new RSAPublicKeySpec(BigInteger.TEN, BigInteger.TEN)));
    p("gen priv wrong spec", () -> kf.generatePrivate(new RSAPrivateKeySpec(BigInteger.TEN, BigInteger.TEN)));
    p("gen pub off curve", () -> kf.generatePublic(new ECPublicKeySpec(new ECPoint(BigInteger.ONE, BigInteger.ONE), ((ECPublicKey) pub).getParams())));
    p("gen priv zero", () -> kf.generatePrivate(new ECPrivateKeySpec(BigInteger.ZERO, ((ECPublicKey) pub).getParams())));
    p("gen priv large", () -> kf.generatePrivate(new ECPrivateKeySpec(((ECPublicKey) pub).getParams().getOrder(), ((ECPublicKey) pub).getParams())));
    p("gen priv negative", () -> kf.generatePrivate(new ECPrivateKeySpec(BigInteger.valueOf(-5), ((ECPublicKey) pub).getParams())));
    p("translate", () -> kf.translateKey(pub) == pub);
    p("translate foreign", () -> { ECPublicKey f = new ECPublicKey() { public ECPoint getW() { return ((ECPublicKey) pub).getW(); } public ECParameterSpec getParams() { return ((ECPublicKey) pub).getParams(); } public String getAlgorithm() { return "EC"; } public String getFormat() { return "X.509"; } public byte[] getEncoded() { return null; } }; Key t = kf.translateKey(f); return t.getClass().getName() + " " + t.equals(pub); });
    p("translate bad", () -> kf.translateKey(new SecretKeySpec(new byte[8], "AES")));
    p("decode ec with custom params", () -> "skip");
    byte[] msg = "hello ec".getBytes();
    for (String alg : new String[]{"SHA1withECDSA", "SHA224withECDSA", "SHA256withECDSA", "SHA384withECDSA", "SHA512withECDSA", "SHA3-256withECDSA", "NONEwithECDSA", "SHA256withECDSAinP1363Format", "NONEwithECDSAinP1363Format"}) {
      p("sign " + alg, () -> { Signature s = Signature.getInstance(alg); s.initSign(priv); s.update(msg.length > 0 ? (alg.startsWith("NONE") ? new byte[20] : msg) : msg); byte[] sig = s.sign(); Signature v = Signature.getInstance(alg); v.initVerify(pub); v.update(alg.startsWith("NONE") ? new byte[20] : msg); return (sig.length >= 64 && sig.length <= 72) + " " + v.verify(sig) + " " + s.getProvider().getName() + " " + (alg.contains("P1363") ? sig.length : sig[0] & 0xff); });
    }
    p("verify jdk sig", () -> { Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update(msg); return v.verify(HexFormat.of().parseHex("30440220729fc32ea1fa35c262112b95e0d42cc0fa1c561a8398655ce5e188397732935f0220221069301a911203ac217d5cd27d585d280f190dfb210872a914eb677357ce96")); });
    p("verify jdk sig wrong msg", () -> { Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update("other".getBytes()); return v.verify(HexFormat.of().parseHex("30440220729fc32ea1fa35c262112b95e0d42cc0fa1c561a8398655ce5e188397732935f0220221069301a911203ac217d5cd27d585d280f190dfb210872a914eb677357ce96")); });
    p("verify jdk sig p1363", () -> { Signature v = Signature.getInstance("SHA256withECDSAinP1363Format"); v.initVerify(pub); v.update(msg); return v.verify(HexFormat.of().parseHex("729fc32ea1fa35c262112b95e0d42cc0fa1c561a8398655ce5e188397732935f221069301a911203ac217d5cd27d585d280f190dfb210872a914eb677357ce96")); });
    p("verify bad", () -> { Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[70]); });
    p("verify garbage der", () -> { Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[]{0x30, 0x02, 0x02, 0x00}); });
    p("verify empty", () -> { Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[0]); });
    p("verify p1363 wrong len", () -> { Signature v = Signature.getInstance("SHA256withECDSAinP1363Format"); v.initVerify(pub); v.update(msg); return v.verify(new byte[63]); });
    p("verify p1363 zeros", () -> { Signature v = Signature.getInstance("SHA256withECDSAinP1363Format"); v.initVerify(pub); v.update(msg); return v.verify(new byte[64]); });
    p("verify tampered", () -> { Signature s = Signature.getInstance("SHA256withECDSA"); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); sig[10] ^= 1; Signature v = Signature.getInstance("SHA256withECDSA"); v.initVerify(pub); v.update(msg); try { return v.verify(sig); } catch (SignatureException e) { return "SE:" + e.getMessage(); } });
    p("sign rsa key", () -> { KeyPairGenerator r = KeyPairGenerator.getInstance("RSA"); r.initialize(1024); Signature s = Signature.getInstance("SHA256withECDSA"); s.initSign(r.generateKeyPair().getPrivate()); return "x"; });
    p("sign null", () -> { Signature s = Signature.getInstance("SHA256withECDSA"); s.initSign(null); return "x"; });
    p("sign param", () -> { Signature s = Signature.getInstance("SHA256withECDSA"); s.setParameter(new PSSParameterSpec(32)); return "x"; });
    p("sign getparams", () -> { Signature s = Signature.getInstance("SHA256withECDSA"); return s.getParameters(); });
    p("sign alias", () -> Signature.getInstance("1.2.840.10045.4.3.2").getAlgorithm());
    p("sign alias2", () -> Signature.getInstance("SHA256WithECDSA").getAlgorithm());
    p("sign alias3", () -> Signature.getInstance("ECDSA").getAlgorithm());
    p("ecdh", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(256); KeyPair other = k.generateKeyPair(); KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(other.getPublic(), true); byte[] s1 = a1.generateSecret(); KeyAgreement a2 = KeyAgreement.getInstance("ECDH"); a2.init(other.getPrivate()); a2.doPhase(pub, true); byte[] s2 = a2.generateSecret(); return s1.length + " " + Arrays.equals(s1, s2) + " " + a1.getProvider().getName() + " " + a1.getAlgorithm(); });
    p("ecdh jdk fixed", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); return hx(a1.generateSecret()); });
    p("ecdh genSecret alg", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); SecretKey s = a1.generateSecret("AES"); return s.getAlgorithm() + " " + s.getEncoded().length; });
    p("ecdh genSecret des", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); SecretKey s = a1.generateSecret("DES"); return s.getAlgorithm() + " " + s.getEncoded().length; });
    p("ecdh genSecret tls", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); SecretKey s = a1.generateSecret("TlsPremasterSecret"); return s.getAlgorithm() + " " + s.getEncoded().length; });
    p("ecdh genSecret foo", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); SecretKey s = a1.generateSecret("Foo"); return s.getAlgorithm() + " " + s.getEncoded().length; });
    p("ecdh genSecret buf", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); byte[] b = new byte[40]; int n = a1.generateSecret(b, 4); return n + " " + hx(b).substring(0, 12); });
    p("ecdh genSecret buf short", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); return a1.generateSecret(new byte[20], 0); });
    p("ecdh no phase", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); return a1.generateSecret(); });
    p("ecdh no init", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); return a1.doPhase(pub, true); });
    p("ecdh not last", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); return a1.doPhase(pub, false); });
    p("ecdh reuse", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); a1.doPhase(pub, true); a1.generateSecret(); return a1.generateSecret(); });
    p("ecdh init pub", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(pub); return "x"; });
    p("ecdh phase priv", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); return a1.doPhase(priv, true); });
    p("ecdh init params", () -> { KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv, new ECGenParameterSpec("secp256r1")); return "x"; });
    p("ecdh other curve", () -> { KeyPairGenerator k = KeyPairGenerator.getInstance("EC"); k.initialize(384); KeyAgreement a1 = KeyAgreement.getInstance("ECDH"); a1.init(priv); return a1.doPhase(k.generateKeyPair().getPublic(), true); });
    p("ecdh alias", () -> KeyAgreement.getInstance("1.3.132.1.12").getAlgorithm());
  }
}
