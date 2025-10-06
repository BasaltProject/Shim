/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*;
public class JceDesModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static String hx(byte[] b) { return HexFormat.of().formatHex(b); }
  public static void main(String[] a) throws Exception {
    byte[] k8 = HexFormat.of().parseHex("133457799BBCDFF1");
    byte[] pt = HexFormat.of().parseHex("0123456789ABCDEF");
    p("des kat", () -> { Cipher c = Cipher.getInstance("DES/ECB/NoPadding"); c.init(1, new SecretKeySpec(k8, "DES")); return hx(c.doFinal(pt)) + " " + c.getBlockSize(); });
    p("des cbc", () -> { Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello world".getBytes())) + " " + c.getBlockSize() + " " + c.getIV().length; });
    p("des default", () -> { Cipher c = Cipher.getInstance("DES"); c.init(1, new SecretKeySpec(k8, "DES")); return hx(c.doFinal("hello world".getBytes())) + " " + c.getIV(); });
    p("des bad len", () -> { Cipher c = Cipher.getInstance("DES"); c.init(1, new SecretKeySpec(new byte[7], "DES")); return "x"; });
    p("des long key", () -> { Cipher c = Cipher.getInstance("DES"); c.init(1, new SecretKeySpec(new byte[9], "DES")); return "x"; });
    p("des bad alg", () -> { Cipher c = Cipher.getInstance("DES"); c.init(1, new SecretKeySpec(new byte[8], "AES")); return "x"; });
    p("des ofb", () -> { Cipher c = Cipher.getInstance("DES/OFB/NoPadding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello w".getBytes())); });
    p("des cfb8", () -> { Cipher c = Cipher.getInstance("DES/CFB8/NoPadding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello w".getBytes())); });
    p("des ctr", () -> { Cipher c = Cipher.getInstance("DES/CTR/NoPadding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello w".getBytes())); });
    p("des pcbc", () -> { Cipher c = Cipher.getInstance("DES/PCBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello w".getBytes())); });
    p("des iv len", () -> { Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[16])); return "x"; });
    p("des toString", () -> { Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k8, "DES")); return c.toString() + " " + c.getParameters().getAlgorithm() + " " + c.getIV().length; });
    byte[] k24 = HexFormat.of().parseHex("0123456789abcdef23456789abcdef01456789abcdef0123");
    p("3des kat", () -> { Cipher c = Cipher.getInstance("DESede/ECB/NoPadding"); c.init(1, new SecretKeySpec(k24, "DESede")); return hx(c.doFinal(pt)); });
    p("3des cbc", () -> { Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k24, "DESede"), new IvParameterSpec(new byte[8])); return hx(c.doFinal("hello world, this is 3des".getBytes())); });
    p("3des alias", () -> { Cipher c = Cipher.getInstance("TripleDES/ECB/NoPadding"); c.init(1, new SecretKeySpec(k24, "TripleDES")); return hx(c.doFinal(pt)) + " " + c.getAlgorithm(); });
    p("3des key16", () -> { Cipher c = Cipher.getInstance("DESede"); c.init(1, new SecretKeySpec(new byte[16], "DESede")); return "x"; });
    p("3des key8", () -> { Cipher c = Cipher.getInstance("DESede"); c.init(1, new SecretKeySpec(new byte[8], "DESede")); return "x"; });
    p("3des key32", () -> { Cipher c = Cipher.getInstance("DESede"); c.init(1, new SecretKeySpec(new byte[32], "DESede")); return hx(c.doFinal(pt)); });
    p("3des wrong alg", () -> { Cipher c = Cipher.getInstance("DESede"); c.init(1, new SecretKeySpec(new byte[24], "DES")); return "x"; });
    p("3des keyalg DESede", () -> { Cipher c = Cipher.getInstance("DESede"); c.init(1, new SecretKeySpec(k24, "DESede")); return hx(c.doFinal(pt)); });
    p("kg des", () -> { KeyGenerator g = KeyGenerator.getInstance("DES"); SecretKey k = g.generateKey(); byte[] e = k.getEncoded(); boolean par = DESKeySpec.isParityAdjusted(e, 0); return e.length + " " + k.getAlgorithm() + " " + k.getFormat() + " " + par + " " + DESKeySpec.isWeak(e, 0); });
    p("kg des size", () -> { KeyGenerator g = KeyGenerator.getInstance("DES"); g.init(64); return "x"; });
    p("kg des size 56", () -> { KeyGenerator g = KeyGenerator.getInstance("DES"); g.init(56); return g.generateKey().getEncoded().length; });
    p("kg des size 128", () -> { KeyGenerator g = KeyGenerator.getInstance("DES"); g.init(128); return "x"; });
    p("kg des params", () -> { KeyGenerator g = KeyGenerator.getInstance("DES"); g.init(new IvParameterSpec(new byte[1])); return "x"; });
    p("kg 3des", () -> { KeyGenerator g = KeyGenerator.getInstance("DESede"); SecretKey k = g.generateKey(); byte[] e = k.getEncoded(); return e.length + " " + k.getAlgorithm() + " " + DESedeKeySpec.isParityAdjusted(e, 0); });
    p("kg 3des 112", () -> { KeyGenerator g = KeyGenerator.getInstance("DESede"); g.init(112); return g.generateKey().getEncoded().length; });
    p("kg 3des 168", () -> { KeyGenerator g = KeyGenerator.getInstance("DESede"); g.init(168); return g.generateKey().getEncoded().length; });
    p("kg 3des 100", () -> { KeyGenerator g = KeyGenerator.getInstance("DESede"); g.init(100); return "x"; });
    p("skf des", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.generateSecret(new DESKeySpec(k8)); return hx(k.getEncoded()) + " " + k.getAlgorithm() + " " + k.getFormat() + " " + f.getAlgorithm() + " " + f.getProvider().getName(); });
    p("skf des keyspec", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.generateSecret(new SecretKeySpec(k8, "DES")); return hx(k.getEncoded()); });
    p("skf des bad spec", () -> SecretKeyFactory.getInstance("DES").generateSecret(new PBEKeySpec("p".toCharArray())));
    p("skf des bad keyspec", () -> SecretKeyFactory.getInstance("DES").generateSecret(new SecretKeySpec(new byte[4], "DES")));
    p("skf des spec back", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.generateSecret(new DESKeySpec(k8)); DESKeySpec s = (DESKeySpec) f.getKeySpec(k, DESKeySpec.class); return hx(s.getKey()); });
    p("skf des spec back bad", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.generateSecret(new DESKeySpec(k8)); return f.getKeySpec(k, PBEKeySpec.class); });
    p("skf des spec back secretkeyspec", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.generateSecret(new DESKeySpec(k8)); return f.getKeySpec(k, SecretKeySpec.class).getClass().getSimpleName(); });
    p("skf des translate", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DES"); SecretKey k = f.translateKey(new SecretKeySpec(k8, "DES")); return k.getClass().getSimpleName() + " " + hx(k.getEncoded()); });
    p("skf des translate bad", () -> SecretKeyFactory.getInstance("DES").translateKey(new SecretKeySpec(k8, "AES")));
    p("skf 3des", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DESede"); SecretKey k = f.generateSecret(new DESedeKeySpec(k24)); return hx(k.getEncoded()) + " " + k.getAlgorithm(); });
    p("skf 3des alias", () -> SecretKeyFactory.getInstance("TripleDES").getAlgorithm());
    p("skf 3des 16", () -> { SecretKeyFactory f = SecretKeyFactory.getInstance("DESede"); return f.generateSecret(new SecretKeySpec(new byte[16], "DESede")); });
    p("des specs", () -> { StringBuilder sb = new StringBuilder(); for (String h : new String[]{"0101010101010101", "fefefefefefefefe", "133457799bbcdff1", "01fe01fe01fe01fe", "1f1f1f1f0e0e0e0e", "1234567812345678"}) { byte[] kb = HexFormat.of().parseHex(h); sb.append(DESKeySpec.isWeak(kb, 0)).append(DESKeySpec.isParityAdjusted(kb, 0)).append(' '); } return sb; });
    p("des spec short", () -> new DESKeySpec(new byte[7]));
    p("des spec offset", () -> hx(new DESKeySpec(new byte[16], 8).getKey()));
    p("des spec isweak short", () -> DESKeySpec.isWeak(new byte[7], 0));
    p("des spec isweak null", () -> DESKeySpec.isWeak(null, 0));
    p("3des spec short", () -> new DESedeKeySpec(new byte[23]));
    p("des params", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("DES"); ap.init(new IvParameterSpec(new byte[8])); return ap.toString().replace("\n", "|"); });
    p("des wrap", () -> { Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding"); c.init(Cipher.WRAP_MODE, new SecretKeySpec(k8, "DES")); return c.wrap(new SecretKeySpec(new byte[16], "AES")).length; });
    p("des big", () -> { Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding"); c.init(1, new SecretKeySpec(k8, "DES"), new IvParameterSpec(new byte[8])); byte[] d = new byte[1000]; for (int i = 0; i < d.length; i++) d[i] = (byte) i; return hx(MessageDigest.getInstance("SHA-256").digest(c.doFinal(d))); });
  }
}
