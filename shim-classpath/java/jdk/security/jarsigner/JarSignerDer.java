/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.security.jarsigner;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class JarSignerDer {
  static final Comparator<byte[]> BYTE_ORDER = new Comparator<byte[]>() {
    public int compare(byte[] a, byte[] b) {
      int n = Math.min(a.length, b.length);
      for (int i = 0; i < n; i++) {
        int d = (a[i] & 0xff) - (b[i] & 0xff);
        if (d != 0) {
          return d;
        }
      }
      return a.length - b.length;
    }
  };

  private JarSignerDer() {
  }

  static byte[] tagged(int tag, byte[] content) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(tag);
    int len = content.length;
    if (len < 0x80) {
      bos.write(len);
    } else if (len < 0x100) {
      bos.write(0x81);
      bos.write(len);
    } else if (len < 0x10000) {
      bos.write(0x82);
      bos.write(len >> 8);
      bos.write(len);
    } else if (len < 0x1000000) {
      bos.write(0x83);
      bos.write(len >> 16);
      bos.write(len >> 8);
      bos.write(len);
    } else {
      bos.write(0x84);
      bos.write(len >> 24);
      bos.write(len >> 16);
      bos.write(len >> 8);
      bos.write(len);
    }
    bos.write(content, 0, content.length);
    return bos.toByteArray();
  }

  static byte[] concat(byte[]... parts) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    for (byte[] p : parts) {
      bos.write(p, 0, p.length);
    }
    return bos.toByteArray();
  }

  static byte[] concat(List<byte[]> parts) {
    return concat(parts.toArray(new byte[0][]));
  }

  static byte[] seq(byte[]... parts) {
    return tagged(0x30, concat(parts));
  }

  static byte[] set(byte[]... parts) {
    return tagged(0x31, concat(parts));
  }

  static byte[] octets(byte[] b) {
    return tagged(0x04, b);
  }

  static byte[] integer(BigInteger v) {
    return tagged(0x02, v.toByteArray());
  }

  static byte[] oid(String dotted) {
    String[] parts = dotted.split("\\.");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]));
    for (int i = 2; i < parts.length; i++) {
      long v = Long.parseLong(parts[i]);
      byte[] tmp = new byte[10];
      int n = 0;
      tmp[n++] = (byte) (v & 0x7f);
      v >>= 7;
      while (v > 0) {
        tmp[n++] = (byte) ((v & 0x7f) | 0x80);
        v >>= 7;
      }
      for (int j = n - 1; j >= 0; j--) {
        bos.write(tmp[j]);
      }
    }
    return tagged(0x06, bos.toByteArray());
  }

  static byte[] algId(String oid, boolean nullParams) {
    return nullParams ? seq(oid(oid), new byte[] {0x05, 0x00}) : seq(oid(oid));
  }

  static byte[] algIdContent(String oid, boolean nullParams) {
    return nullParams ? concat(oid(oid), new byte[] {0x05, 0x00}) : oid(oid);
  }

  static byte[] pssParams(String digestOid, int saltLen) {
    byte[] hash = algId(digestOid, true);
    byte[] mgf = seq(oid("1.2.840.113549.1.1.8"), hash);
    return seq(tagged(0xA0, hash), tagged(0xA1, mgf), tagged(0xA2, integer(java.math.BigInteger.valueOf(saltLen))));
  }

  static byte[] utcNow() {
    java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US);
    f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    return tagged(0x17, f.format(new java.util.Date()).getBytes());
  }

  static String digestName(String sigAlg, String digestAlg) {
    String up = sigAlg.toUpperCase(Locale.ENGLISH);
    int w = up.indexOf("WITH");
    if (w > 0) {
      String d = up.substring(0, w);
      if (d.equals("SHA1")) {
        return "SHA-1";
      } else if (d.startsWith("SHA3-")) {
        return d;
      } else if (d.startsWith("SHA")) {
        return "SHA-" + d.substring(3);
      }
    }
    return digestAlg;
  }

  static String digestOid(String sigAlg, String digestAlg) {
    String n = digestName(sigAlg, digestAlg);
    switch (n) {
      case "SHA-1":
        return "1.3.14.3.2.26";
      case "SHA-224":
        return "2.16.840.1.101.3.4.2.4";
      case "SHA-256":
        return "2.16.840.1.101.3.4.2.1";
      case "SHA-384":
        return "2.16.840.1.101.3.4.2.2";
      case "SHA-512":
        return "2.16.840.1.101.3.4.2.3";
      case "SHA3-256":
        return "2.16.840.1.101.3.4.2.8";
      case "SHA3-384":
        return "2.16.840.1.101.3.4.2.9";
      case "SHA3-512":
        return "2.16.840.1.101.3.4.2.10";
      case "MD5":
        return "1.2.840.113549.2.5";
      default:
        throw new IllegalArgumentException("Unsupported digest " + n);
    }
  }

  static String signatureOid(String sigAlg) {
    String up = sigAlg.toUpperCase(Locale.ENGLISH);
    switch (up) {
      case "SHA1WITHRSA":
        return "1.2.840.113549.1.1.5";
      case "SHA224WITHRSA":
        return "1.2.840.113549.1.1.14";
      case "SHA256WITHRSA":
        return "1.2.840.113549.1.1.11";
      case "SHA384WITHRSA":
        return "1.2.840.113549.1.1.12";
      case "SHA512WITHRSA":
        return "1.2.840.113549.1.1.13";
      case "SHA1WITHDSA":
        return "1.2.840.10040.4.3";
      case "SHA224WITHDSA":
        return "2.16.840.1.101.3.4.3.1";
      case "SHA256WITHDSA":
        return "2.16.840.1.101.3.4.3.2";
      case "SHA1WITHECDSA":
        return "1.2.840.10045.4.1";
      case "SHA224WITHECDSA":
        return "1.2.840.10045.4.3.1";
      case "SHA256WITHECDSA":
        return "1.2.840.10045.4.3.2";
      case "SHA384WITHECDSA":
        return "1.2.840.10045.4.3.3";
      case "SHA512WITHECDSA":
        return "1.2.840.10045.4.3.4";
      case "RSASSA-PSS":
        return "1.2.840.113549.1.1.10";
      case "ED25519":
        return "1.3.101.112";
      case "ED448":
        return "1.3.101.113";
      default:
        throw new IllegalArgumentException("Unsupported signature algorithm " + sigAlg);
    }
  }
}
