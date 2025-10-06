/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class JceAesVectorsModern {
  static String hex(byte[] b) {
    return HexFormat.of().formatHex(b);
  }

  static String h16(byte[] b) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(b)).substring(0, 16);
  }

  static byte[] bytes(int n, int seed) {
    byte[] b = new byte[n];
    for (int i = 0; i < n; i++) {
      b[i] = (byte) (i * 7 + seed * 13 + (i >> 3));
    }
    return b;
  }

  static byte[] chunked(Cipher c, byte[] in, int chunk) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int i = 0;
    while (i < in.length) {
      int n = Math.min(chunk, in.length - i);
      byte[] part = c.update(in, i, n);
      if (part != null) {
        out.write(part);
      }
      i += n;
    }
    out.write(c.doFinal());
    return out.toByteArray();
  }

  public static void main(String[] args) throws Exception {
    String[] modes = {"ECB", "CBC", "PCBC", "CFB", "CFB8", "CFB16", "CFB64", "OFB", "OFB8", "OFB64", "CTR"};
    String[] pads = {"NoPadding", "PKCS5Padding", "ISO10126Padding"};
    int[] keySizes = {16, 24, 32};
    int[] lengths = {0, 1, 15, 16, 17, 31, 32, 33, 48, 100};
    for (String mode : modes) {
      for (String pad : pads) {
        if (mode.equals("CTR") && !pad.equals("NoPadding")) {
          continue;
        }
        for (int ks : keySizes) {
          StringBuilder sb = new StringBuilder();
          for (int len : lengths) {
            String t = "AES/" + mode + "/" + pad;
            try {
              SecretKeySpec key = new SecretKeySpec(bytes(ks, 3), "AES");
              byte[] data = bytes(len, 5);
              Cipher enc = Cipher.getInstance(t);
              Cipher dec = Cipher.getInstance(t);
              if (mode.equals("ECB")) {
                enc.init(Cipher.ENCRYPT_MODE, key);
                dec.init(Cipher.DECRYPT_MODE, key);
              } else {
                IvParameterSpec iv = new IvParameterSpec(bytes(16, 9));
                enc.init(Cipher.ENCRYPT_MODE, key, iv);
                dec.init(Cipher.DECRYPT_MODE, key, iv);
              }
              byte[] ct;
              try {
                ct = enc.doFinal(data);
              } catch (IllegalBlockSizeException e) {
                sb.append(len).append(":IBSE ");
                continue;
              }
              String ctInfo = pad.equals("ISO10126Padding") ? "len" + ct.length : h16(ct);
              byte[] back = dec.doFinal(ct);
              boolean same = Arrays.equals(back, data);
              byte[] ct2 = chunked(enc, data, 7);
              boolean sameChunk = pad.equals("ISO10126Padding") ? ct2.length == ct.length : Arrays.equals(ct, ct2);
              byte[] back2 = chunked(dec, ct2, 5);
              sb.append(len).append(':').append(ctInfo).append(same ? "" : "!rt").append(sameChunk ? "" : "!chunk")
                  .append(Arrays.equals(back2, data) ? "" : "!rt2").append(' ');
            } catch (Exception e) {
              sb.append(len).append(':').append(e.getClass().getSimpleName()).append(' ');
            }
          }
          System.out.println(mode + "/" + pad + "/" + ks + " " + sb);
        }
      }
    }
    byte[] key = HexFormat.of().parseHex("2b7e151628aed2a6abf7158809cf4f3c");
    byte[] pt = HexFormat.of().parseHex("6bc1bee22e409f96e93d7e117393172a");
    Cipher ecb = Cipher.getInstance("AES/ECB/NoPadding");
    ecb.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    System.out.println("nist ecb " + hex(ecb.doFinal(pt)));
    Cipher dec = Cipher.getInstance("AES/ECB/NoPadding");
    dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
    System.out.println("nist dec " + hex(dec.doFinal(HexFormat.of().parseHex("3ad77bb40d7a3660a89ecaf32466ef97"))));
    byte[] k256 = HexFormat.of().parseHex("603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4");
    Cipher e256 = Cipher.getInstance("AES/ECB/NoPadding");
    e256.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k256, "AES"));
    System.out.println("nist 256 " + hex(e256.doFinal(pt)));
    byte[] k192 = HexFormat.of().parseHex("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b");
    Cipher e192 = Cipher.getInstance("AES/ECB/NoPadding");
    e192.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k192, "AES"));
    System.out.println("nist 192 " + hex(e192.doFinal(pt)));
    Cipher ctr = Cipher.getInstance("AES/CTR/NoPadding");
    ctr.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(HexFormat.of().parseHex("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff")));
    System.out.println("nist ctr " + hex(ctr.doFinal(pt)));
    Cipher ctrWrap = Cipher.getInstance("AES/CTR/NoPadding");
    byte[] ivMax = new byte[16];
    Arrays.fill(ivMax, (byte) 0xff);
    ctrWrap.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(ivMax));
    System.out.println("ctr wrap " + hex(ctrWrap.doFinal(new byte[48])));
  }
}
