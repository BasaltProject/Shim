/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class HssLmsModern {
  static final HexFormat hex = HexFormat.of();

  static byte[] sha(byte[]... parts) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    for (byte[] p : parts) md.update(p);
    return md.digest();
  }

  static byte[] u32(long v) { return new byte[] {(byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) v}; }
  static byte[] u16(int v) { return new byte[] {(byte) (v >>> 8), (byte) v}; }
  static byte[] cat(byte[]... parts) {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    for (byte[] p : parts) o.writeBytes(p);
    return o.toByteArray();
  }

  // Signer for LMS_SHA256_M32 with LMOTS_SHA256_N32 (w = 1, 2, 4 or 8)
  static class Lms {
    final int h, w, p, ls, typeCode, otsCode;
    final byte[] id, seed;
    byte[][] nodes;
    Lms(int h, int w, byte[] id, byte[] seed) throws Exception {
      this.h = h; this.w = w; this.id = id; this.seed = seed;
      int u = 256 / w;
      int v = (32 - Integer.numberOfLeadingZeros(((1 << w) - 1) * u) + w - 1) / w;
      this.p = u + v;
      this.ls = 16 - v * w;
      this.typeCode = switch (h) { case 5 -> 5; case 10 -> 6; default -> 7; };
      this.otsCode = switch (w) { case 1 -> 1; case 2 -> 2; case 4 -> 3; default -> 4; };
      nodes = new byte[2 << h][];
      for (int q = 0; q < (1 << h); q++) nodes[(1 << h) + q] = sha(id, u32((1 << h) + q), u16(0x8282), otsPublic(q));
      for (int i = (1 << h) - 1; i >= 1; i--) nodes[i] = sha(id, u32(i), u16(0x8383), nodes[2 * i], nodes[2 * i + 1]);
    }
    byte[] secret(int q, int i) throws Exception {
      return sha(id, u32(q), u16(i), new byte[] {(byte) 0xff}, seed);
    }
    byte[] chain(int q, int i, byte[] start, int from, int to) throws Exception {
      byte[] tmp = start;
      for (int j = from; j < to; j++) tmp = sha(id, u32(q), u16(i), new byte[] {(byte) j}, tmp);
      return tmp;
    }
    byte[] otsPublic(int q) throws Exception {
      byte[][] z = new byte[p][];
      for (int i = 0; i < p; i++) z[i] = chain(q, i, secret(q, i), 0, (1 << w) - 1);
      return sha(id, u32(q), u16(0x8080), cat(z));
    }
    int coef(byte[] s, int i) { return ((1 << w) - 1) & ((s[i * w / 8] & 0xff) >> (8 - (w * (i % (8 / w)) + w))); }
    byte[] qa(byte[] q) {
      int sum = 0;
      for (int i = 0; i < 256 / w; i++) sum += ((1 << w) - 1) - coef(q, i);
      return cat(q, u16(sum << ls));
    }
    byte[] pub() { return cat(u32(typeCode), u32(otsCode), id, nodes[1]); }
    byte[] sign(int q, byte[] msg, byte[] c) throws Exception {
      byte[] d = qa(sha(id, u32(q), u16(0x8181), c, msg));
      byte[][] y = new byte[p][];
      for (int i = 0; i < p; i++) y[i] = chain(q, i, secret(q, i), 0, coef(d, i));
      byte[][] path = new byte[h][];
      for (int i = 0, n = (1 << h) + q; i < h; i++, n >>= 1) path[i] = nodes[n ^ 1];
      return cat(u32(q), u32(otsCode), c, cat(y), u32(typeCode), cat(path));
    }
  }

  static byte[] seq(int n, int s) {
    byte[] b = new byte[n];
    for (int i = 0; i < n; i++) b[i] = (byte) (i * 31 + s);
    return b;
  }

  static byte[] spki(byte[] hss) {
    byte[] oid = hex.parseHex("060b2a864886f70d0109100311");
    byte[] inner = cat(new byte[] {4, (byte) hss.length}, hss);
    byte[] bits = cat(new byte[] {3, (byte) (inner.length + 1), 0}, inner);
    byte[] alg = cat(new byte[] {0x30, (byte) oid.length}, oid);
    byte[] body = cat(alg, bits);
    return cat(new byte[] {0x30, (byte) body.length}, body);
  }

  static void check(String label, Signature s, PublicKey k, byte[] msg, byte[] sig) {
    try {
      s.initVerify(k);
      s.update(msg);
      System.out.println(label + " " + s.verify(sig));
    } catch (Exception e) {
      System.out.println(label + " " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    byte[] msg = "hss test message".getBytes();
    KeyFactory kf = KeyFactory.getInstance("HSS/LMS");
    Signature sig = Signature.getInstance("HSS/LMS");
    for (int w : new int[] {1, 2, 4}) {
      Lms l = new Lms(5, w, seq(16, 7), seq(32, w));
      byte[] pub = cat(u32(1), l.pub());
      PublicKey key = kf.generatePublic(new X509EncodedKeySpec(spki(pub)));
      System.out.println("w" + w + " " + key.getAlgorithm() + " " + key.getFormat() + " "
          + hex.formatHex(key.getEncoded()).equals(hex.formatHex(spki(pub))) + " " + key.equals(
              kf.generatePublic(new X509EncodedKeySpec(spki(pub)))));
      byte[] s3 = cat(u32(0), l.sign(3, msg, seq(32, 9)));
      check("w" + w + " valid", sig, key, msg, s3);
      check("w" + w + " other-msg", sig, key, "other".getBytes(), s3);
      byte[] t = s3.clone();
      t[40] ^= 1;
      check("w" + w + " tampered", sig, key, msg, t);
      check("w" + w + " short", sig, key, msg, Arrays.copyOf(s3, s3.length - 1));
      check("w" + w + " long", sig, key, msg, cat(s3, new byte[1]));
    }
    Lms top = new Lms(5, 2, seq(16, 1), seq(32, 1));
    Lms bottom = new Lms(5, 2, seq(16, 2), seq(32, 2));
    byte[] pub2 = cat(u32(2), top.pub());
    PublicKey key2 = kf.generatePublic(new X509EncodedKeySpec(spki(pub2)));
    byte[] bsig = bottom.sign(17, msg, seq(32, 3));
    byte[] hss2 = cat(u32(1), top.sign(4, bottom.pub(), seq(32, 5)), bottom.pub(), bsig);
    check("hss2 valid", sig, key2, msg, hss2);
    check("hss2 wrong-levels", sig, key2, msg, cat(u32(0), bsig));
    byte[] bad = hss2.clone();
    bad[bad.length - 1] ^= 1;
    check("hss2 bad-last", sig, key2, msg, bad);
    // errors
    try { sig.initSign(null); } catch (Throwable e) { System.out.println("initSign " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.generatePublic(new X509EncodedKeySpec(spki(new byte[10]))); } catch (Throwable e) { System.out.println("short key " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.generatePublic(new X509EncodedKeySpec(spki(cat(u32(0), top.pub())))); } catch (Throwable e) { System.out.println("L0 " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.generatePublic(new X509EncodedKeySpec(spki(cat(u32(9), top.pub())))); } catch (Throwable e) { System.out.println("L9 " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.generatePublic(new X509EncodedKeySpec(spki(cat(u32(1), top.pub(), new byte[1])))); } catch (Throwable e) { System.out.println("trail " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    byte[] badType = cat(u32(1), top.pub());
    badType[7] = 99;
    try { kf.generatePublic(new X509EncodedKeySpec(spki(badType))); } catch (Throwable e) { System.out.println("badtype " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.generatePrivate(new PKCS8EncodedKeySpec(new byte[5])); } catch (Throwable e) { System.out.println("priv " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { KeyPairGenerator.getInstance("HSS/LMS"); } catch (Throwable e) { System.out.println("kpg " + e.getClass().getSimpleName()); }
    try { System.out.println(kf.getKeySpec(key2, X509EncodedKeySpec.class).getFormat()); } catch (Throwable e) { System.out.println("spec " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    try { kf.translateKey(key2); System.out.println("translate ok"); } catch (Throwable e) { System.out.println("translate " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    System.out.println(sig.getProvider().getName() + " " + sig.getAlgorithm());
    try { sig.initVerify(KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic()); } catch (Throwable e) { System.out.println("wrongkey " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
    System.out.println(Signature.getInstance("1.2.840.113549.1.9.16.3.17").getAlgorithm());
  }
}
