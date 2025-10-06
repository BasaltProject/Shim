/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*;
public class JceGcmModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static byte[] n(Object o) { return (byte[]) o; }
  public static void main(String[] a) throws Exception {
    SecretKeySpec k = new SecretKeySpec(new byte[16], "AES");
    GCMParameterSpec g = new GCMParameterSpec(128, new byte[12]);
    p("sizes enc", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); StringBuilder sb = new StringBuilder(); for (int len : new int[]{1, 15, 16, 17, 31, 33, 0}) { byte[] r = c.update(new byte[len]); sb.append(len).append("->").append(r == null ? "null" : r.length).append(' '); } byte[] f = c.doFinal(); return sb + " final=" + f.length; });
    p("sizes dec", () -> { Cipher e = Cipher.getInstance("AES/GCM/NoPadding"); e.init(1, k, g); byte[] ct = e.doFinal(new byte[40]); Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); StringBuilder sb = new StringBuilder(); int off = 0; for (int len : new int[]{1, 15, 16, 17, 7}) { byte[] r = c.update(ct, off, len); off += len; sb.append(len).append("->").append(r == null ? "null" : r.length).append(' '); } byte[] r = c.update(ct, off, ct.length - off); sb.append("rest->").append(r == null ? "null" : r.length); byte[] f = c.doFinal(); return sb + " final=" + f.length; });
    p("outsize enc", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); return c.getOutputSize(0) + " " + c.getOutputSize(10) + " " + c.getOutputSize(16); });
    p("outsize dec", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); StringBuilder sb = new StringBuilder(); sb.append(c.getOutputSize(0)).append(' ').append(c.getOutputSize(10)).append(' ').append(c.getOutputSize(16)).append(' ').append(c.getOutputSize(40)); c.update(new byte[20]); sb.append(' ').append(c.getOutputSize(0)).append(' ').append(c.getOutputSize(20)); return sb; });
    p("iv params", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k); GCMParameterSpec s = c.getParameters().getParameterSpec(GCMParameterSpec.class); return c.getIV().length + " " + s.getTLen() + " " + s.getIV().length + " " + c.getParameters().getAlgorithm() + " " + c.getBlockSize() + " " + c.getProvider().getName(); });
    p("dec no params", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k); return "ok"; });
    p("wrong type", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new IvParameterSpec(new byte[12])); return "ok"; });
    p("bad tlen", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(64, new byte[12])); return "ok"; });
    p("tlen 96", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(96, new byte[12])); return c.doFinal(new byte[5]).length; });
    p("tlen 120", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(120, new byte[12])); return c.doFinal(new byte[5]).length; });
    p("tlen 0", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(0, new byte[12])); return "ok"; });
    p("empty iv", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(128, new byte[0])); return "ok"; });
    p("iv 1", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(128, new byte[1])); return c.doFinal(new byte[3]).length; });
    p("iv 16 / 100", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(128, new byte[100])); return HexFormat.of().formatHex(c.doFinal(new byte[3])); });
    p("iv 7", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(128, new byte[7])); return HexFormat.of().formatHex(c.doFinal(new byte[20])); });
    p("kat", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.updateAAD(new byte[]{1, 2, 3}); return HexFormat.of().formatHex(c.doFinal(new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9})); });
    p("kat empty", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); return HexFormat.of().formatHex(c.doFinal()); });
    p("reuse iv", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.doFinal(new byte[3]); return c.doFinal(new byte[3]).length; });
    p("reuse iv init", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.doFinal(new byte[3]); c.init(1, k, g); return c.doFinal(new byte[3]).length; });
    p("reuse iv init2", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.doFinal(new byte[3]); c.init(1, k, new GCMParameterSpec(128, new byte[12])); return c.doFinal(new byte[3]).length; });
    p("reuse iv init3", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.doFinal(new byte[3]); c.init(1, new SecretKeySpec(new byte[16], "AES"), new GCMParameterSpec(128, new byte[12])); return c.doFinal(new byte[3]).length; });
    p("reuse iv init4", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.doFinal(new byte[3]); c.init(1, new SecretKeySpec(new byte[16], "AES"), new GCMParameterSpec(128, new byte[12])); c.doFinal(new byte[3]);return c.doFinal(new byte[3]).length; });
    p("reuse dec", () -> { Cipher e = Cipher.getInstance("AES/GCM/NoPadding"); e.init(1, k, g); byte[] ct = e.doFinal(new byte[3]); Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); byte[] x = c.doFinal(ct); byte[] y = c.doFinal(ct); return x.length + " " + y.length; });
    p("tag mismatch", () -> { Cipher e = Cipher.getInstance("AES/GCM/NoPadding"); e.init(1, k, g); byte[] ct = e.doFinal(new byte[3]); ct[ct.length - 1] ^= 1; Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); return c.doFinal(ct); });
    p("tag short", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); return c.doFinal(new byte[10]); });
    p("tag short empty", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); return c.doFinal(); });
    p("aad mismatch", () -> { Cipher e = Cipher.getInstance("AES/GCM/NoPadding"); e.init(1, k, g); e.updateAAD(new byte[]{1}); byte[] ct = e.doFinal(new byte[3]); Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); c.updateAAD(new byte[]{2}); return c.doFinal(ct); });
    p("aad after update", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.update(new byte[3]); c.updateAAD(new byte[]{2}); return "ok"; });
    p("aad after update dec", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); c.update(new byte[3]); c.updateAAD(new byte[]{2}); return "ok"; });
    p("aad bb", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.updateAAD(java.nio.ByteBuffer.wrap(new byte[]{1, 2})); return HexFormat.of().formatHex(c.doFinal(new byte[2])); });
    p("aad split", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.updateAAD(new byte[]{1}); c.updateAAD(new byte[]{2, 3}); return HexFormat.of().formatHex(c.doFinal(new byte[2])); });
    p("aad one", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); c.updateAAD(new byte[]{1, 2, 3}); return HexFormat.of().formatHex(c.doFinal(new byte[2])); });
    p("long msg", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); byte[] pt = new byte[1000]; for (int i = 0; i < pt.length; i++) pt[i] = (byte) i; return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(c.doFinal(pt))); });
    p("chunked eq", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); byte[] pt = new byte[100]; for (int i = 0; i < pt.length; i++) pt[i] = (byte) i; byte[] a1 = c.doFinal(pt); c.init(1, k, new GCMParameterSpec(128, new byte[]{1,2,3,4,5,6,7,8,9,10,11,12})); java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream(); for (int i = 0; i < 100; i += 13) { byte[] x = c.update(pt, i, Math.min(13, 100 - i)); if (x != null) o.write(x); } o.write(c.doFinal()); c.init(1, k, g); return Arrays.equals(a1, c.doFinal(pt)) + " " + o.size(); });
    p("dec out buffer", () -> { Cipher e = Cipher.getInstance("AES/GCM/NoPadding"); e.init(1, k, g); byte[] ct = e.doFinal(new byte[10]); Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(2, k, g); return c.doFinal(ct, 0, ct.length, new byte[5], 0); });
    p("enc out buffer", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); return c.doFinal(new byte[10], 0, 10, new byte[20], 0); });
    p("enc out buffer ok", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); return c.doFinal(new byte[10], 0, 10, new byte[26], 0); });
    p("wrap", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.WRAP_MODE, k, g); return c.wrap(new SecretKeySpec(new byte[16], "AES")).length; });
    p("toString", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, g); return c; });
    p("alg params str", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(g); return ap.toString() + "|" + HexFormat.of().formatHex(ap.getEncoded()); });
    p("alg params spec", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(new GCMParameterSpec(96, new byte[]{1,2,3})); GCMParameterSpec s = ap.getParameterSpec(GCMParameterSpec.class); return s.getTLen() + " " + s.getIV().length; });
    p("alg params dec", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(HexFormat.of().parseHex("300e0403010203020100")); return "x"; });
    p("alg params der", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(HexFormat.of().parseHex("30110404010203040201")); GCMParameterSpec s = ap.getParameterSpec(GCMParameterSpec.class); return s.getTLen() + " " + s.getIV().length; });
    p("alg params der noTag", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(HexFormat.of().parseHex("30050403010203")); return "x"; });
    p("init with params obj", () -> { AlgorithmParameters ap = AlgorithmParameters.getInstance("GCM"); ap.init(g); Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, ap); return HexFormat.of().formatHex(c.doFinal(new byte[1])); });
    p("gcm generic", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k); return "ok"; });
    p("gcm via AES general", () -> Cipher.getInstance("AES/GCM/PKCS5Padding"));
    p("fixed 128 gcm", () -> { Cipher c = Cipher.getInstance("AES_128/GCM/NoPadding"); c.init(1, k, g); return c.doFinal(new byte[1]).length; });
    p("fixed 256 gcm w128", () -> { Cipher c = Cipher.getInstance("AES_256/GCM/NoPadding"); c.init(1, k, g); return "ok"; });
    p("big iv enc size", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, k, new GCMParameterSpec(128, new byte[12])); return c.getOutputSize(Integer.MAX_VALUE - 20); });
    p("key 24", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, new SecretKeySpec(new byte[24], "AES"), g); return HexFormat.of().formatHex(c.doFinal(new byte[5])); });
    p("key 32", () -> { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(1, new SecretKeySpec(new byte[32], "AES"), g); return HexFormat.of().formatHex(c.doFinal(new byte[5])); });
  }
}
