/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*; import java.security.interfaces.*; import java.math.BigInteger; 
public class JceRsaModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H").replaceAll("JceRsaModern\\$\\d", "Anon")); }
  static String hx(byte[] b) { return HexFormat.of().formatHex(b); }
  static Map<String,BigInteger> load(String f) { Map<String,BigInteger> m = new HashMap<>(); m.put("n", new BigInteger("a2551f88394cabed630ee31ea95548ce5497dc58f54e0abb040f1fed6d92a17ade5c0b205fc7f7c2cbf4c839ceffae7b8bc928afbfcca732c68f70f10544393e7e84e44c34bb60bcc454428433cad3a380068c3db90c5774792422daad34a38894d31eb4d16017725822cf297aa98ea8163b5768b6537a76cee4ab03e9c425e7", 16)); m.put("e", new BigInteger("10001", 16)); m.put("d", new BigInteger("1f9661c6dfa37836fe960882d05f6c85ddb71924e442d5dab33d0030f2a7dc2d2cf48b23b70f91ba0176f5b92dfad9a867b87266d4730dbbd720555148fd9007b4eaa2e4811d16f720f3876bed43d98367564329e6bce1dff679817c559601c29a115aa6458116dec3a74bcd4b2229fc610532be61e7a4f3dc771184c21c360d", 16)); m.put("p", new BigInteger("a9c17732e21b50584a78728cacb697948f182c401e168aa3aca2582b0a7095f1a893a9c1af3ea35978fefb91ea77156104c4976b41dd91d944767f4436cd4ec3", 16)); m.put("q", new BigInteger("f4ce309f263562f6ebceefe804301284e6e16ab798f5013e4733f424a378bc9636b1ed6257cae7a38bb81b913db8d1e54672ee35c0389fc3d30fb0cbc805e20d", 16)); m.put("dp", new BigInteger("d0367bfded579334b823045d877a6e71c911dd34c7b2de59e3653cbf8e0e89dbdee93f4e51a482b73bb184df74b69b826456bd62d166f37d8a7bcd9ddd99f5", 16)); m.put("dq", new BigInteger("578ea7b5446533d03ce6f6c77537fc67ac6c534ef75629ebac6bd89682215cd21d1de659962cec4c825c86ff9ba3ceeec213b93f6050e05399d9b2d22fe86ac1", 16)); m.put("qi", new BigInteger("9b59ca7189309d93adb83453f772a6c376a60a739abfbcb7d3a6f94bb3544d51345d3e73b0914c313f7df821724d92820ded1241837dc2478abab6318a44be2", 16)); return m; }
  public static void main(String[] a) throws Exception {
    Map<String,BigInteger> k = load("");
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PrivateKey priv = kf.generatePrivate(new RSAPrivateCrtKeySpec(k.get("n"), k.get("e"), k.get("d"), k.get("p"), k.get("q"), k.get("dp"), k.get("dq"), k.get("qi")));
    PublicKey pub = kf.generatePublic(new RSAPublicKeySpec(k.get("n"), k.get("e")));
    byte[] msg = "hello world".getBytes();
    for (String alg : new String[]{"SHA1withRSA","SHA224withRSA","SHA256withRSA","SHA384withRSA","SHA512withRSA","SHA512/224withRSA","SHA512/256withRSA","MD5withRSA","MD2withRSA","SHA3-256withRSA","SHA3-512withRSA","NONEwithRSA","RSASSA-PSS","SHA256withRSAandMGF1"}) {
      p("sign " + alg, () -> { Signature s = Signature.getInstance(alg); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); Signature v = Signature.getInstance(alg); v.initVerify(pub); v.update(msg); return sig.length + " " + hx(sig).substring(0, 40) + " " + v.verify(sig) + " " + s.getProvider().getName(); });
    }
    p("sign sha256 full", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); return hx(s.sign()); });
    p("sign none", () -> { Signature s = Signature.getInstance("NONEwithRSA"); s.initSign(priv); s.update(new byte[20]); return hx(s.sign()); });
    p("sign none long", () -> { Signature s = Signature.getInstance("NONEwithRSA"); s.initSign(priv); s.update(new byte[120]); return hx(s.sign()); });
    p("verify bad", () -> { Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[128]); });
    p("verify short", () -> { Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[10]); });
    p("verify long", () -> { Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(pub); v.update(msg); return v.verify(new byte[129]); });
    p("verify tampered", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(pub); v.update("hello worle".getBytes()); return v.verify(sig); });
    p("verify other key", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); KeyPairGenerator g = KeyPairGenerator.getInstance("RSA"); g.initialize(1024); Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(g.generateKeyPair().getPublic()); v.update(msg); return v.verify(sig); });
    p("sign null", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(null); return "x"; });
    p("verify null", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initVerify((PublicKey) null); return "x"; });
    p("sig reuse", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); byte[] a1 = s.sign(); s.update(msg); byte[] a2 = s.sign(); return Arrays.equals(a1, a2); });
    p("sig partial", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg, 0, 5); s.update((byte) ' '); s.update("world".getBytes()); byte[] a1 = s.sign(); s.update(msg); return Arrays.equals(a1, s.sign()); });
    p("sig out buf", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); byte[] out = new byte[130]; return s.sign(out, 1, 128) + " " + hx(out).substring(0, 8); });
    p("sig out buf short", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); return s.sign(new byte[130], 1, 100); });
    p("sig verify off", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); byte[] buf = new byte[140]; System.arraycopy(sig, 0, buf, 5, 128); Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(pub); v.update(msg); return v.verify(buf, 5, 128); });
    p("sig bytebuffer", () -> { Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(priv); s.update(java.nio.ByteBuffer.wrap(msg)); return hx(s.sign()).substring(0, 20); });
    p("sig msg too long for none", () -> { Signature s = Signature.getInstance("NONEwithRSA"); s.initSign(priv); s.update(new byte[200]); return s.sign(); });
    p("pss params", () -> { Signature s = Signature.getInstance("RSASSA-PSS"); s.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1)); s.initSign(priv); s.update(msg); byte[] sig = s.sign(); Signature v = Signature.getInstance("RSASSA-PSS"); v.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1)); v.initVerify(pub); v.update(msg); return sig.length + " " + v.verify(sig); });
    p("pss no params", () -> { Signature s = Signature.getInstance("RSASSA-PSS"); s.initSign(priv); s.update(msg); return s.sign(); });
    p("pss bad params", () -> { Signature s = Signature.getInstance("RSASSA-PSS"); s.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 2)); return "x"; });
    p("pss params get", () -> { Signature s = Signature.getInstance("RSASSA-PSS"); s.setParameter(new PSSParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384, 20, 1)); AlgorithmParameters ap = s.getParameters(); return ap.getAlgorithm() + " " + hx(ap.getEncoded()); });
    p("pss large salt", () -> { Signature s = Signature.getInstance("RSASSA-PSS"); s.setParameter(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 100, 1)); s.initSign(priv); return "x"; });
    p("pss verify jdk", () -> { return "skip"; });
    p("sigwith", () -> { Signature s = Signature.getInstance("SHA256withRSAandMGF1"); s.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1)); s.initSign(priv); s.update(msg); return s.sign().length; });
    p("sig alias oid", () -> Signature.getInstance("1.2.840.113549.1.1.11").getAlgorithm());
    p("sig alias2", () -> Signature.getInstance("SHA256WithRSAEncryption").getAlgorithm());
    p("sig alias3", () -> Signature.getInstance("sha256withrsa").getAlgorithm());
    p("rsa toString", () -> { Signature s = Signature.getInstance("SHA256withRSA"); return s.toString(); });
    byte[] data = "secret data".getBytes();
    p("cipher pkcs1", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, priv); return ct.length + " " + new String(d.doFinal(ct)) + " " + c.getBlockSize() + " " + c.getOutputSize(5) + " " + c.getIV() + " " + c.getParameters(); });
    p("cipher default", () -> { Cipher c = Cipher.getInstance("RSA"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA"); d.init(Cipher.DECRYPT_MODE, priv); return ct.length + " " + new String(d.doFinal(ct)); });
    p("cipher nopad", () -> { Cipher c = Cipher.getInstance("RSA/ECB/NoPadding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] in = new byte[128]; in[127] = 5; byte[] ct = c.doFinal(in); Cipher d = Cipher.getInstance("RSA/ECB/NoPadding"); d.init(Cipher.DECRYPT_MODE, priv); byte[] back = d.doFinal(ct); return hx(ct).substring(0, 20) + " " + back.length + " " + back[127]; });
    p("cipher nopad short", () -> { Cipher c = Cipher.getInstance("RSA/ECB/NoPadding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] ct = c.doFinal(new byte[]{5}); Cipher d = Cipher.getInstance("RSA/ECB/NoPadding"); d.init(Cipher.DECRYPT_MODE, priv); byte[] back = d.doFinal(ct); return back.length + " " + back[127]; });
    p("cipher nopad long", () -> { Cipher c = Cipher.getInstance("RSA/ECB/NoPadding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[129]); });
    p("cipher nopad big value", () -> { Cipher c = Cipher.getInstance("RSA/ECB/NoPadding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] in = new byte[128]; Arrays.fill(in, (byte) 0xff); return c.doFinal(in); });
    p("cipher pkcs1 long", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[118]); });
    p("cipher pkcs1 max", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[117]).length; });
    p("cipher pkcs1 empty", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal().length; });
    p("cipher dec bad", () -> { Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, priv); return d.doFinal(new byte[128]); });
    p("cipher dec short", () -> { Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, priv); return d.doFinal(new byte[100]); });
    p("cipher dec long", () -> { Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, priv); return d.doFinal(new byte[129]); });
    p("cipher dec toobig", () -> { Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, priv); byte[] x = new byte[128]; Arrays.fill(x, (byte) 0xff); return d.doFinal(x); });
    p("cipher update", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] r = c.update(data); c.update(data); return r + " " + c.doFinal().length; });
    p("cipher priv enc", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, priv); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.DECRYPT_MODE, pub); return ct.length + " " + hx(ct).substring(0, 20) + " " + new String(d.doFinal(ct)); });
    p("cipher oaep", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); d.init(Cipher.DECRYPT_MODE, priv); return ct.length + " " + new String(d.doFinal(ct)) + " " + c.getParameters().getAlgorithm(); });
    p("cipher oaep default", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding"); c.init(Cipher.ENCRYPT_MODE, pub); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/OAEPPadding"); d.init(Cipher.DECRYPT_MODE, priv); return ct.length + " " + new String(d.doFinal(ct)); });
    p("cipher oaep sha1", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[86]).length; });
    p("cipher oaep sha1 too long", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[87]).length; });
    p("cipher oaep spec", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding"); c.init(Cipher.ENCRYPT_MODE, pub, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/OAEPPadding"); d.init(Cipher.DECRYPT_MODE, priv, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)); return new String(d.doFinal(ct)); });
    p("cipher oaep spec mismatch", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding"); c.init(Cipher.ENCRYPT_MODE, pub, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/OAEPPadding"); d.init(Cipher.DECRYPT_MODE, priv, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)); return new String(d.doFinal(ct)); });
    p("cipher oaep label", () -> { Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding"); OAEPParameterSpec sp = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, new PSource.PSpecified("label".getBytes())); c.init(Cipher.ENCRYPT_MODE, pub, sp); byte[] ct = c.doFinal(data); Cipher d = Cipher.getInstance("RSA/ECB/OAEPPadding"); d.init(Cipher.DECRYPT_MODE, priv, sp); return new String(d.doFinal(ct)); });
    p("cipher bad params", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub, new IvParameterSpec(new byte[8])); return "x"; });
    p("cipher bad key", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES")); return "x"; });
    p("cipher enc with priv nopad dec with pub", () -> { Cipher c = Cipher.getInstance("RSA/ECB/NoPadding"); c.init(Cipher.ENCRYPT_MODE, priv); byte[] in = new byte[128]; in[127] = 9; byte[] ct = c.doFinal(in); Cipher d = Cipher.getInstance("RSA/ECB/NoPadding"); d.init(Cipher.DECRYPT_MODE, pub); return d.doFinal(ct)[127]; });
    p("cipher wrap", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.WRAP_MODE, pub); byte[] w = c.wrap(new SecretKeySpec(new byte[16], "AES")); Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.UNWRAP_MODE, priv); Key u = d.unwrap(w, "AES", Cipher.SECRET_KEY); return w.length + " " + u.getAlgorithm() + " " + u.getEncoded().length; });
    p("cipher wrap private", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.WRAP_MODE, pub); byte[] w = c.wrap(priv); Cipher d = Cipher.getInstance("RSA/ECB/PKCS1Padding"); d.init(Cipher.UNWRAP_MODE, priv); Key u = d.unwrap(w, "RSA", Cipher.PRIVATE_KEY); return w.length + " " + u.getAlgorithm() + " " + u.equals(priv); });
    p("cipher keysize", () -> "n/a");
    p("cipher modes", () -> { StringBuilder sb = new StringBuilder(); for (String t : new String[]{"RSA/ECB/PKCS1Padding", "RSA/None/PKCS1Padding", "RSA/CBC/PKCS1Padding", "RSA/ECB/OAEPWithSHA-512/256AndMGF1Padding", "RSA/ECB/OAEPWithMD5AndMGF1Padding", "RSA/ECB/ISO10126Padding", "RSA//PKCS1Padding", "rsa/ecb/pkcs1padding"}) { try { Cipher.getInstance(t); sb.append("ok "); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(' '); } } return sb; });
    p("cipher blocksize", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); return c.getBlockSize() + " " + c.getOutputSize(1) + " " + c.getOutputSize(500); });
    p("cipher init twice", () -> { Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding"); c.init(Cipher.ENCRYPT_MODE, pub); c.update(new byte[5]); c.init(Cipher.ENCRYPT_MODE, pub); return c.doFinal(new byte[5]).length; });
    p("keyfactory spec", () -> { RSAPublicKeySpec s = kf.getKeySpec(pub, RSAPublicKeySpec.class); RSAPrivateCrtKeySpec s2 = kf.getKeySpec(priv, RSAPrivateCrtKeySpec.class); RSAPrivateKeySpec s3 = kf.getKeySpec(priv, RSAPrivateKeySpec.class); X509EncodedKeySpec s4 = kf.getKeySpec(pub, X509EncodedKeySpec.class); PKCS8EncodedKeySpec s5 = kf.getKeySpec(priv, PKCS8EncodedKeySpec.class); return s.getPublicExponent() + " " + s2.getPrimeP().bitLength() + " " + s3.getPrivateExponent().bitLength() + " " + s4.getFormat() + " " + s5.getFormat() + " " + s4.getEncoded().length + " " + s5.getEncoded().length; });
    p("keyfactory bad spec", () -> kf.getKeySpec(pub, RSAPrivateKeySpec.class));
    p("keyfactory bad spec2", () -> kf.getKeySpec(priv, RSAPublicKeySpec.class));
    p("keyfactory bad spec3", () -> kf.getKeySpec(pub, DESKeySpec.class));
    p("keyfactory gen pub from private spec", () -> kf.generatePublic(new RSAPrivateKeySpec(k.get("n"), k.get("d"))));
    p("keyfactory gen priv from pub spec", () -> kf.generatePrivate(new RSAPublicKeySpec(k.get("n"), k.get("e"))));
    p("keyfactory gen pub x509 garbage", () -> kf.generatePublic(new X509EncodedKeySpec(new byte[10])));
    p("keyfactory gen priv pkcs8 garbage", () -> kf.generatePrivate(new PKCS8EncodedKeySpec(new byte[10])));
    p("keyfactory gen pub null", () -> kf.generatePublic(null));
    p("keyfactory roundtrip", () -> { PublicKey p2 = kf.generatePublic(new X509EncodedKeySpec(pub.getEncoded())); PrivateKey q2 = kf.generatePrivate(new PKCS8EncodedKeySpec(priv.getEncoded())); return p2.equals(pub) + " " + q2.equals(priv) + " " + (p2.hashCode() == pub.hashCode()) + " " + (q2.hashCode() == priv.hashCode()) + " " + hx(p2.getEncoded()).equals(hx(pub.getEncoded())); });
    p("keyfactory noncrt", () -> { PrivateKey q = kf.generatePrivate(new RSAPrivateKeySpec(k.get("n"), k.get("d"))); return q.getClass().getName() + " " + q.getAlgorithm() + " " + hx(q.getEncoded()).substring(0, 60) + " " + ((RSAPrivateKey) q).getPrivateExponent().bitLength(); });
    p("keyfactory translate", () -> { Key t = kf.translateKey(pub); return t == pub; });
    p("keyfactory translate foreign", () -> { RSAPublicKey fk = new RSAPublicKey() { public BigInteger getPublicExponent() { return k.get("e"); } public String getAlgorithm() { return "RSA"; } public String getFormat() { return "X.509"; } public byte[] getEncoded() { return null; } public BigInteger getModulus() { return k.get("n"); } }; Key t = kf.translateKey(fk); return t.getClass().getName() + " " + t.equals(pub); });
    p("keyfactory translate bad", () -> kf.translateKey(new SecretKeySpec(new byte[8], "AES")));
    p("keyfactory translate null", () -> kf.translateKey(null));
    p("keyfactory alg", () -> kf.getAlgorithm() + " " + kf.getProvider().getName());
    p("keyfactory pss", () -> { KeyFactory f = KeyFactory.getInstance("RSASSA-PSS"); PublicKey pk = f.generatePublic(new RSAPublicKeySpec(k.get("n"), k.get("e"), new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))); return pk.getAlgorithm() + " " + ((RSAKey) pk).getParams().getClass().getSimpleName() + " " + hx(pk.getEncoded()).length(); });
    p("keyfactory pss default", () -> { KeyFactory f = KeyFactory.getInstance("RSASSA-PSS"); PublicKey pk = f.generatePublic(new RSAPublicKeySpec(k.get("n"), k.get("e"))); return pk.getAlgorithm() + " " + ((RSAKey) pk).getParams() + " " + hx(pk.getEncoded()).length(); });
    p("keyfactory pss on rsa factory", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n"), k.get("e"), PSSParameterSpec.DEFAULT)));
    p("modulus small", () -> kf.generatePublic(new RSAPublicKeySpec(BigInteger.valueOf(77), BigInteger.valueOf(3))));
    p("modulus even", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n").add(BigInteger.ONE), BigInteger.valueOf(65537))));
    p("exp large", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n"), BigInteger.ONE.shiftLeft(100).add(BigInteger.ONE))));
    p("exp 2", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n"), BigInteger.TWO)));
    p("exp 1", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n"), BigInteger.ONE)));
    p("exp 0", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n"), BigInteger.ZERO)));
    p("neg modulus", () -> kf.generatePublic(new RSAPublicKeySpec(k.get("n").negate(), BigInteger.valueOf(3))));
    p("pub equals other impl", () -> { RSAPublicKey fk = new RSAPublicKey() { public BigInteger getPublicExponent() { return k.get("e"); } public String getAlgorithm() { return "RSA"; } public String getFormat() { return "X.509"; } public byte[] getEncoded() { return pub.getEncoded(); } public BigInteger getModulus() { return k.get("n"); } }; return pub.equals(fk) + " " + fk.equals(pub); });
  }
}
