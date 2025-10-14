/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.security.*; import java.security.cert.*; import java.util.*;
public class KeyStoreJksModern {
  static final String JKS = "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";
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage() + (t.getCause() != null ? " <- " + t.getCause().getClass().getName() + ":" + t.getCause().getMessage() : ""); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static KeyStore load(byte[] data, String pw) throws Exception { KeyStore ks = KeyStore.getInstance("JKS"); ks.load(new ByteArrayInputStream(data), pw == null ? null : pw.toCharArray()); return ks; }
  public static void main(String[] a) throws Exception {
    byte[] data = Base64.getDecoder().decode(JKS);
    p("type", () -> KeyStore.getInstance("JKS").getType() + " " + KeyStore.getInstance("JKS").getProvider().getName());
    p("aliases", () -> { KeyStore ks = load(data, "changeit"); return Collections.list(ks.aliases()).stream().sorted().toList() + " " + ks.size(); });
    p("kinds", () -> { KeyStore ks = load(data, "changeit"); return ks.isKeyEntry("leaf") + " " + ks.isCertificateEntry("leaf") + " " + ks.isKeyEntry("trusted") + " " + ks.isCertificateEntry("trusted") + " " + ks.containsAlias("LEAF") + " " + ks.containsAlias("nope"); });
    p("chain", () -> { KeyStore ks = load(data, "changeit"); java.security.cert.Certificate[] c = ks.getCertificateChain("leaf"); return c.length + " " + ((X509Certificate) c[0]).getSubjectX500Principal() + " " + ks.getCertificate("trusted").getType() + " " + ks.getCertificateChain("trusted"); });
    p("key", () -> { KeyStore ks = load(data, "changeit"); Key k = ks.getKey("leaf", "changeit".toCharArray()); return k.getAlgorithm() + " " + k.getFormat() + " " + (k instanceof PrivateKey); });
    p("sign", () -> { KeyStore ks = load(data, "changeit"); PrivateKey k = (PrivateKey) ks.getKey("leaf", "changeit".toCharArray()); Signature s = Signature.getInstance("SHA256withRSA"); s.initSign(k); s.update(new byte[]{1, 2, 3}); byte[] sig = s.sign(); Signature v = Signature.getInstance("SHA256withRSA"); v.initVerify(ks.getCertificate("leaf")); v.update(new byte[]{1, 2, 3}); return v.verify(sig); });
    p("wrong key pw", () -> load(data, "changeit").getKey("leaf", "nope".toCharArray()));
    p("null key pw", () -> load(data, "changeit").getKey("leaf", null));
    p("key of cert", () -> load(data, "changeit").getKey("trusted", "changeit".toCharArray()));
    p("missing key", () -> load(data, "changeit").getKey("zzz", "changeit".toCharArray()));
    p("wrong store pw", () -> load(data, "nope"));
    p("null store pw", () -> load(data, null).size());
    p("garbage", () -> load(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, "x"));
    p("truncated", () -> load(Arrays.copyOf(data, 100), "changeit"));
    p("empty stream", () -> load(new byte[0], "x"));
    p("tampered", () -> { byte[] d = data.clone(); d[60] ^= 1; return load(d, "changeit"); });
    p("creation date", () -> load(data, "changeit").getCreationDate("leaf") != null);
    p("cert alias", () -> { KeyStore ks = load(data, "changeit"); return ks.getCertificateAlias(ks.getCertificate("trusted")) + " " + ks.getCertificateAlias(ks.getCertificate("leaf")); });
    p("roundtrip", () -> { KeyStore ks = load(data, "changeit"); ByteArrayOutputStream bo = new ByteArrayOutputStream(); ks.store(bo, "other".toCharArray()); KeyStore k2 = load(bo.toByteArray(), "other"); return Collections.list(k2.aliases()).stream().sorted().toList() + " " + k2.getKey("leaf", "changeit".toCharArray()).equals(ks.getKey("leaf", "changeit".toCharArray())) + " " + k2.getCertificate("trusted").equals(ks.getCertificate("trusted")) + " " + (bo.size() > 100); });
    p("set key", () -> { KeyStore ks = load(data, "changeit"); PrivateKey k = (PrivateKey) ks.getKey("leaf", "changeit".toCharArray()); java.security.cert.Certificate[] chain = ks.getCertificateChain("leaf"); ks.setKeyEntry("New", k, "pw".toCharArray(), chain); ByteArrayOutputStream bo = new ByteArrayOutputStream(); ks.store(bo, "s".toCharArray()); KeyStore k2 = load(bo.toByteArray(), "s"); return k2.containsAlias("new") + " " + k2.getKey("NEW", "pw".toCharArray()).equals(k) + " " + Collections.list(k2.aliases()).stream().sorted().toList(); });
    p("set secret", () -> { KeyStore ks = load(data, "changeit"); ks.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"), "p".toCharArray(), null); return "ok"; });
    p("set cert over key", () -> { KeyStore ks = load(data, "changeit"); ks.setCertificateEntry("leaf", ks.getCertificate("trusted")); return "ok"; });
    p("delete", () -> { KeyStore ks = load(data, "changeit"); ks.deleteEntry("leaf"); ks.deleteEntry("zzz"); return ks.size(); });
    p("store null pw", () -> { load(data, "changeit").store(new ByteArrayOutputStream(), null); return "ok"; });
    p("uninitialized", () -> KeyStore.getInstance("JKS").size());
    p("entries", () -> { KeyStore ks = load(data, "changeit"); KeyStore.Entry e = ks.getEntry("leaf", new KeyStore.PasswordProtection("changeit".toCharArray())); KeyStore.Entry t = ks.getEntry("trusted", null); return e.getClass().getSimpleName() + " " + t.getClass().getSimpleName() + " " + ks.entryInstanceOf("leaf", KeyStore.PrivateKeyEntry.class) + " " + ks.entryInstanceOf("trusted", KeyStore.TrustedCertificateEntry.class); });
    p("bad type", () -> KeyStore.getInstance("NOPE"));
    p("default type", () -> KeyStore.getDefaultType());
  }
}
