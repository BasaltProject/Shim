/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*; import javax.crypto.*; import javax.crypto.spec.*; import java.util.*; import java.security.spec.*;
public class JceAesModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage()); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H").replaceAll("t51\\$1", "KeyImpl").replaceAll("JceAesModern\\$1", "KeyImpl")); }
  public static void main(String[] a) throws Exception {
    SecretKeySpec k16 = new SecretKeySpec(new byte[16], "AES");
    p("null key", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, (Key) null); return "ok"; });
    p("bad key len", () -> { Cipher c = Cipher.getInstance("AES"); c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[5], "AES")); return "ok"; });
    p("bad key alg", () -> { Cipher c = Cipher.getInstance("AES"); c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "DES")); return "ok"; });
    p("bad opmode", () -> { Cipher c = Cipher.getInstance("AES"); c.init(9, k16); return "ok"; });
    p("no init update", () -> { Cipher c = Cipher.getInstance("AES"); return c.update(new byte[1]); });
    p("no init dofinal", () -> { Cipher c = Cipher.getInstance("AES"); return c.doFinal(); });
    p("no init getOutputSize", () -> { Cipher c = Cipher.getInstance("AES"); return c.getOutputSize(5); });
    p("blocksize iv", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); return c.getBlockSize() + " " + c.getIV() + " " + (c.getParameters() != null) + " " + c.getAlgorithm() + " " + c; });
    p("cbc dec no iv", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.DECRYPT_MODE, k16); return "ok"; });
    p("cbc enc iv", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16); return c.getIV().length + " " + c.getParameters().getAlgorithm() + " " + c + " " + c.getProvider(); });
    p("ecb iv", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16, new IvParameterSpec(new byte[16])); return "ok"; });
    p("ecb iv getIV", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16); return c.getIV() + " " + c.getParameters(); });
    p("wrong iv len", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16, new IvParameterSpec(new byte[8])); return "ok"; });
    p("wrong param type", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16, new GCMParameterSpec(128, new byte[12])); return "ok"; });
    p("wrong param type2", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16, new PBEParameterSpec(new byte[8], 3)); return "ok"; });
    p("unknown mode", () -> Cipher.getInstance("AES/FOO/PKCS5Padding"));
    p("unknown padding", () -> Cipher.getInstance("AES/CBC/FOO"));
    p("unknown alg", () -> Cipher.getInstance("FOO/CBC/PKCS5Padding"));
    p("two tokens", () -> Cipher.getInstance("AES/CBC"));
    p("four tokens", () -> Cipher.getInstance("AES/CBC/PKCS5Padding/X"));
    p("empty", () -> Cipher.getInstance(""));
    p("null", () -> Cipher.getInstance((String) null));
    p("slash alg", () -> Cipher.getInstance("/CBC/NoPadding"));
    p("ctr padding", () -> Cipher.getInstance("AES/CTR/PKCS5Padding"));
    p("cfb8 padding", () -> Cipher.getInstance("AES/CFB8/PKCS5Padding"));
    p("cfb padding", () -> Cipher.getInstance("AES/CFB/PKCS5Padding"));
    p("ofb padding", () -> Cipher.getInstance("AES/OFB/PKCS5Padding"));
    p("cfb3", () -> Cipher.getInstance("AES/CFB3/NoPadding"));
    p("cfb136", () -> Cipher.getInstance("AES/CFB136/NoPadding"));
    p("mode case", () -> Cipher.getInstance("aes/cbc/pkcs5padding").getAlgorithm());
    p("iso", () -> Cipher.getInstance("AES/CBC/ISO10126Padding").getAlgorithm());
    p("provider ok", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE").getProvider());
    p("provider missing", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", "SUN"));
    p("provider unknown", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", "NOPE"));
    p("provider empty", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", ""));
    p("provider obj", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", Security.getProvider("SunJCE")).getProvider());
    p("provider obj missing", () -> Cipher.getInstance("AES/CBC/PKCS5Padding", Security.getProvider("SUN")));
    p("provider obj bad mode", () -> Cipher.getInstance("AES/FOO/PKCS5Padding", Security.getProvider("SunJCE")));
    p("provider obj bad pad", () -> Cipher.getInstance("AES/CBC/FOO", Security.getProvider("SunJCE")));
    p("enc size", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, k16); return c.getOutputSize(0) + " " + c.getOutputSize(15) + " " + c.getOutputSize(16) + " " + c.getOutputSize(17); });
    p("dec size", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(Cipher.DECRYPT_MODE, k16, new IvParameterSpec(new byte[16])); return c.getOutputSize(0) + " " + c.getOutputSize(15) + " " + c.getOutputSize(32); });
    p("neg size", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.getOutputSize(-1); });
    p("nopad bad len", () -> { Cipher c = Cipher.getInstance("AES/ECB/NoPadding"); c.init(1, k16); return c.doFinal(new byte[15]); });
    p("pad dec bad len", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(2, k16); return c.doFinal(new byte[15]); });
    p("pad dec bad pad", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(2, k16); return c.doFinal(new byte[16]); });
    p("pad dec empty", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(2, k16); return c.doFinal(new byte[0]).length; });
    p("nopad empty", () -> { Cipher c = Cipher.getInstance("AES/ECB/NoPadding"); c.init(1, k16); return c.doFinal().length; });
    p("pad empty", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.doFinal().length; });
    p("update null", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.update((byte[]) null); });
    p("update bad range", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.update(new byte[4], 2, 5); });
    p("update empty", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.update(new byte[0]); });
    p("update small", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.update(new byte[3]); });
    p("update 16", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.update(new byte[16]).length; });
    p("update 17", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.update(new byte[17]).length; });
    p("update 32 dec", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(2, k16); return c.update(new byte[32]).length; });
    p("update 16 dec", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(2, k16); return c.update(new byte[16]); });
    p("short out", () -> { Cipher c = Cipher.getInstance("AES/ECB/NoPadding"); c.init(1, k16); return c.update(new byte[32], 0, 32, new byte[10]); });
    p("short out final", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.doFinal(new byte[5], 0, 5, new byte[10], 0); });
    p("short out final dec", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); byte[] e = c.doFinal(new byte[5]); c.init(2, k16); return c.doFinal(e, 0, 16, new byte[3], 0); });
    p("short out final dec2", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); byte[] e = c.doFinal(new byte[5]); c.init(2, k16); return c.doFinal(e, 0, 16, new byte[8], 0); });
    p("out offset", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(1, k16); return c.doFinal(new byte[5], 0, 5, new byte[30], 20); });
    p("wrap state", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.wrap(k16); });
    p("unwrap state", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); return c.unwrap(new byte[16], "AES", 3); });
    p("wrap ok", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.WRAP_MODE, k16); byte[] w = c.wrap(new SecretKeySpec(new byte[20], "HmacSHA1")); c.init(Cipher.UNWRAP_MODE, k16); Key u = c.unwrap(w, "HmacSHA1", Cipher.SECRET_KEY); return w.length + " " + u.getAlgorithm() + " " + u.getEncoded().length + " " + c; });
    p("unwrap bad", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.UNWRAP_MODE, k16); return c.unwrap(new byte[16], "AES", Cipher.SECRET_KEY); });
    p("unwrap bad len", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.UNWRAP_MODE, k16); return c.unwrap(new byte[15], "AES", Cipher.SECRET_KEY); });
    p("unwrap bad type", () -> { Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.UNWRAP_MODE, k16); return c.unwrap(new byte[16], "AES", 7); });
    p("max key", () -> Cipher.getMaxAllowedKeyLength("AES") + " " + Cipher.getMaxAllowedParameterSpec("AES"));
    p("max key bad", () -> Cipher.getMaxAllowedKeyLength("AES/CBC"));
    p("max key null", () -> Cipher.getMaxAllowedKeyLength(null));
    p("update after final", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(1, k16, new IvParameterSpec(new byte[16])); byte[] x = c.doFinal(new byte[20]); byte[] y = c.doFinal(new byte[20]); return Arrays.equals(x, y); });
    p("aad", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(1, k16); c.updateAAD(new byte[2]); return "ok"; });
    p("aad null", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(1, k16); c.updateAAD((byte[]) null); return "ok"; });
    p("aad before init", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.updateAAD(new byte[2]); return "ok"; });
    p("bytebuffers", () -> { Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); c.init(1, k16, new IvParameterSpec(new byte[16])); java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(new byte[40]); java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(64); int n = c.update(in, out); int m = c.doFinal(java.nio.ByteBuffer.allocate(0), out); return n + " " + m + " " + out.position() + " " + in.remaining(); });
    p("bytebuffer same", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, k16); java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(new byte[40]); return c.update(in, in); });
    p("bytebuffer short", () -> { Cipher c = Cipher.getInstance("AES/ECB/NoPadding"); c.init(1, k16); return c.doFinal(java.nio.ByteBuffer.wrap(new byte[32]), java.nio.ByteBuffer.allocate(16)); });
    p("bytebuffer ro", () -> { Cipher c = Cipher.getInstance("AES/ECB/NoPadding"); c.init(1, k16); return c.doFinal(java.nio.ByteBuffer.wrap(new byte[32]), java.nio.ByteBuffer.allocate(64).asReadOnlyBuffer()); });
    p("key sizes", () -> { StringBuilder sb = new StringBuilder(); for (int n : new int[]{15,16,17,23,24,25,31,32,33}) { try { Cipher c = Cipher.getInstance("AES"); c.init(1, new SecretKeySpec(new byte[n], "AES")); sb.append(n).append("ok "); } catch (Exception e) { sb.append(n).append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } } return sb; });
    p("fixed key", () -> { Cipher c = Cipher.getInstance("AES_128/CBC/NoPadding"); c.init(1, new SecretKeySpec(new byte[32], "AES")); return "ok"; });
    p("fixed key ok", () -> { Cipher c = Cipher.getInstance("AES_256/CBC/NoPadding"); c.init(1, new SecretKeySpec(new byte[32], "AES")); return c.getIV().length + " " + c + " " + c.getBlockSize(); });
    p("fixed with extra", () -> Cipher.getInstance("AES_128/CBC/PKCS5Padding"));
    p("rijndael key", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, new SecretKeySpec(new byte[16], "Rijndael")); return "ok"; });
    p("lower key alg", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, new SecretKeySpec(new byte[16], "aes")); return "ok"; });
    p("non-raw key", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, new SecretKey() { public String getAlgorithm() { return "AES"; } public String getFormat() { return "X.509"; } public byte[] getEncoded() { return new byte[16]; } }); return "ok"; });
    p("null encoded key", () -> { Cipher c = Cipher.getInstance("AES"); c.init(1, new SecretKey() { public String getAlgorithm() { return "AES"; } public String getFormat() { return "RAW"; } public byte[] getEncoded() { return null; } }); return "ok"; });
  }
}
