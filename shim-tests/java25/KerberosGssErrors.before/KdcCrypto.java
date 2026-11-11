/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class KdcCrypto {
  public static final int DES_CBC_CRC = 1;
  public static final int DES_CBC_MD5 = 3;
  public static final int DES3_CBC_SHA1 = 16;
  public static final int AES128 = 17;
  public static final int AES256 = 18;
  public static final int AES128_SHA2 = 19;
  public static final int AES256_SHA2 = 20;
  public static final int RC4_HMAC = 23;

  public static final int CKSUM_HMAC_SHA1_DES3 = 12;
  public static final int CKSUM_HMAC_SHA1_AES128 = 15;
  public static final int CKSUM_HMAC_SHA1_AES256 = 16;
  public static final int CKSUM_HMAC_MD5 = -138;
  public static final int CKSUM_HMAC_SHA256_AES128 = 19;
  public static final int CKSUM_HMAC_SHA384_AES256 = 20;

  private static final SecureRandom kRandom = new SecureRandom();

  private KdcCrypto() {
  }

  public static String description(int etype) {
    switch (etype) {
      case 1:
        return "DES CBC mode with CRC-32";
      case 3:
        return "DES CBC mode with MD5";
      case 16:
        return "Triple DES CBC mode with HMAC/sha1";
      case 17:
        return "AES128 CTS mode with HMAC SHA1-96";
      case 18:
        return "AES256 CTS mode with HMAC SHA1-96";
      case 19:
        return "AES128 CTS mode with HMAC SHA256";
      case 20:
        return "AES256 CTS mode with HMAC SHA384";
      case 23:
        return "RC4 with HMAC";
      default:
        return "Unknown (" + etype + ")";
    }
  }

  public static String name(int etype) {
    switch (etype) {
      case DES_CBC_CRC:
        return "des-cbc-crc";
      case DES_CBC_MD5:
        return "des-cbc-md5";
      case DES3_CBC_SHA1:
        return "des3-cbc-sha1-kd";
      case AES128:
        return "aes128-cts-hmac-sha1-96";
      case AES256:
        return "aes256-cts-hmac-sha1-96";
      case AES128_SHA2:
        return "aes128-cts-hmac-sha256-128";
      case AES256_SHA2:
        return "aes256-cts-hmac-sha384-192";
      case RC4_HMAC:
        return "rc4-hmac";
      default:
        return etype < 0 ? "private" : "unknown";
    }
  }

  public static boolean isSupported(int etype) {
    return etype == AES128 || etype == AES256 || etype == AES128_SHA2 || etype == AES256_SHA2 || etype == RC4_HMAC
        || etype == DES3_CBC_SHA1;
  }

  public static int keyBytes(int etype) {
    switch (etype) {
      case DES_CBC_CRC:
      case DES_CBC_MD5:
        return 8;
      case DES3_CBC_SHA1:
        return 24;
      case AES128:
      case AES128_SHA2:
      case RC4_HMAC:
        return 16;
      case AES256:
      case AES256_SHA2:
        return 32;
      default:
        throw new IllegalArgumentException("Unsupported encryption type: " + etype);
    }
  }

  public static byte[] randomKey(int etype) {
    byte[] k = new byte[keyBytes(etype)];
    kRandom.nextBytes(k);
    if (etype == DES3_CBC_SHA1) {
      for (int i = 0; i < k.length; i++) {
        k[i] = parity(k[i]);
      }
    }
    return k;
  }

  public static byte[] random(int n) {
    byte[] b = new byte[n];
    kRandom.nextBytes(b);
    return b;
  }

  public static int defaultChecksum(int etype) {
    switch (etype) {
      case AES128:
        return CKSUM_HMAC_SHA1_AES128;
      case AES256:
        return CKSUM_HMAC_SHA1_AES256;
      case AES128_SHA2:
        return CKSUM_HMAC_SHA256_AES128;
      case AES256_SHA2:
        return CKSUM_HMAC_SHA384_AES256;
      case RC4_HMAC:
        return CKSUM_HMAC_MD5;
      case DES3_CBC_SHA1:
        return CKSUM_HMAC_SHA1_DES3;
      default:
        throw new IllegalArgumentException("Unsupported encryption type: " + etype);
    }
  }

  public static int checksumSize(int type) {
    switch (type) {
      case CKSUM_HMAC_SHA1_AES128:
      case CKSUM_HMAC_SHA1_AES256:
        return 12;
      case CKSUM_HMAC_SHA256_AES128:
        return 16;
      case CKSUM_HMAC_SHA384_AES256:
        return 24;
      case CKSUM_HMAC_MD5:
        return 16;
      case CKSUM_HMAC_SHA1_DES3:
        return 20;
      default:
        throw new IllegalArgumentException("Unsupported checksum type: " + type);
    }
  }

  public static byte[] defaultString2KeyParams(int etype) {
    switch (etype) {
      case AES128:
      case AES256:
        return new byte[] {0, 0, 0x10, 0};
      case AES128_SHA2:
      case AES256_SHA2:
        return new byte[] {0, 0, (byte) 0x80, 0};
      default:
        return null;
    }
  }

  public static byte[] string2key(int etype, char[] password, String salt, byte[] params) {
    try {
      switch (etype) {
        case RC4_HMAC:
          return md4(new String(password).getBytes(StandardCharsets.UTF_16LE));
        case AES128:
        case AES256: {
          int len = keyBytes(etype);
          int iter = iterations(params, 4096);
          byte[] tkey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(password,
              salt.getBytes(StandardCharsets.UTF_8), iter, len * 8)).getEncoded();
          return dk(tkey, "kerberos".getBytes(StandardCharsets.US_ASCII));
        }
        case AES128_SHA2:
        case AES256_SHA2: {
          int len = keyBytes(etype);
          int iter = iterations(params, 32768);
          byte[] saltp = KdcAsn1.cat(name(etype).getBytes(StandardCharsets.US_ASCII), new byte[] {0},
              salt.getBytes(StandardCharsets.UTF_8));
          String prf = etype == AES128_SHA2 ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA384";
          byte[] tkey = SecretKeyFactory.getInstance(prf).generateSecret(new PBEKeySpec(password, saltp, iter,
              len * 8)).getEncoded();
          return kdfSha2(etype, tkey, "kerberos".getBytes(StandardCharsets.US_ASCII), len * 8);
        }
        case DES3_CBC_SHA1:
          return des3String2Key(password, salt);
        case DES_CBC_CRC:
        case DES_CBC_MD5:
          return desString2Key(password, salt);
        default:
          throw new IllegalArgumentException("Unsupported encryption type: " + etype);
      }
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static int iterations(byte[] params, int dflt) {
    if (params == null || params.length != 4) {
      return dflt;
    }
    return ((params[0] & 0xff) << 24) | ((params[1] & 0xff) << 16) | ((params[2] & 0xff) << 8) | (params[3] & 0xff);
  }

  public static byte[] encrypt(int etype, byte[] key, int usage, byte[] plain) throws GeneralSecurityException {
    switch (etype) {
      case AES128:
      case AES256:
        return aesSha1Encrypt(key, usage, plain);
      case AES128_SHA2:
      case AES256_SHA2:
        return aesSha2Encrypt(etype, key, usage, plain);
      case RC4_HMAC:
        return rc4Encrypt(key, usage, plain);
      case DES3_CBC_SHA1:
        return des3Encrypt(key, usage, plain);
      default:
        throw new GeneralSecurityException("Unsupported encryption type: " + etype);
    }
  }

  public static byte[] decrypt(int etype, byte[] key, int usage, byte[] cipher) throws GeneralSecurityException {
    switch (etype) {
      case AES128:
      case AES256:
        return aesSha1Decrypt(key, usage, cipher);
      case AES128_SHA2:
      case AES256_SHA2:
        return aesSha2Decrypt(etype, key, usage, cipher);
      case RC4_HMAC:
        return rc4Decrypt(key, usage, cipher);
      case DES3_CBC_SHA1:
        return des3Decrypt(key, usage, cipher);
      default:
        throw new GeneralSecurityException("Unsupported encryption type: " + etype);
    }
  }

  public static byte[] checksum(int type, byte[] key, int usage, byte[] data) throws GeneralSecurityException {
    switch (type) {
      case CKSUM_HMAC_SHA1_AES128:
      case CKSUM_HMAC_SHA1_AES256:
        return Arrays.copyOf(hmac("HmacSHA1", dk(key, usageConstant(usage, 0x99)), data), 12);
      case CKSUM_HMAC_SHA256_AES128:
      case CKSUM_HMAC_SHA384_AES256: {
        int etype = type == CKSUM_HMAC_SHA256_AES128 ? AES128_SHA2 : AES256_SHA2;
        byte[] kc = kdfSha2(etype, key, usageConstant(usage, 0x99), etype == AES128_SHA2 ? 128 : 192);
        return Arrays.copyOf(hmac(etype == AES128_SHA2 ? "HmacSHA256" : "HmacSHA384", kc, data),
            checksumSize(type));
      }
      case CKSUM_HMAC_MD5: {
        byte[] ksign = hmac("HmacMD5", key, "signaturekey\0".getBytes(StandardCharsets.US_ASCII));
        byte[] md5 = MessageDigest.getInstance("MD5").digest(KdcAsn1.cat(le32(rc4Usage(usage)), data));
        return hmac("HmacMD5", ksign, md5);
      }
      case CKSUM_HMAC_SHA1_DES3:
        return hmac("HmacSHA1", derive3Des(key, usageConstant(usage, 0x99)), data);
      default:
        throw new GeneralSecurityException("Unsupported checksum type: " + type);
    }
  }

  static byte[] usageConstant(int usage, int tag) {
    return new byte[] {(byte) (usage >>> 24), (byte) (usage >>> 16), (byte) (usage >>> 8), (byte) usage, (byte) tag};
  }

  static byte[] hmac(String alg, byte[] key, byte[]... data) throws GeneralSecurityException {
    Mac m = Mac.getInstance(alg);
    m.init(new SecretKeySpec(key, alg));
    for (byte[] d : data) {
      m.update(d);
    }
    return m.doFinal();
  }

  static byte[] le32(int v) {
    return new byte[] {(byte) v, (byte) (v >>> 8), (byte) (v >>> 16), (byte) (v >>> 24)};
  }

  private static byte[] dk(byte[] key, byte[] constant) throws GeneralSecurityException {
    byte[] block = nFold(constant, 16);
    byte[] out = new byte[key.length];
    Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    for (int pos = 0; pos < out.length;) {
      block = c.doFinal(block);
      int n = Math.min(block.length, out.length - pos);
      System.arraycopy(block, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  private static byte[] kdfSha2(int etype, byte[] key, byte[] label, int bits) throws GeneralSecurityException {
    String alg = etype == AES128_SHA2 ? "HmacSHA256" : "HmacSHA384";
    byte[] input = KdcAsn1.cat(new byte[] {0, 0, 0, 1}, label, new byte[] {0},
        new byte[] {(byte) (bits >>> 24), (byte) (bits >>> 16), (byte) (bits >>> 8), (byte) bits});
    return Arrays.copyOf(hmac(alg, key, input), bits / 8);
  }

  private static byte[] cts(boolean encrypt, byte[] key, byte[] data) throws GeneralSecurityException {
    Cipher c = Cipher.getInstance("AES/CTS/NoPadding");
    c.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
        new IvParameterSpec(new byte[16]));
    return c.doFinal(data);
  }

  private static byte[] aesSha1Encrypt(byte[] key, int usage, byte[] data) throws GeneralSecurityException {
    byte[] ke = dk(key, usageConstant(usage, 0xaa));
    byte[] ki = dk(key, usageConstant(usage, 0x55));
    byte[] plain = KdcAsn1.cat(random(16), data);
    byte[] c = cts(true, ke, plain);
    byte[] h = Arrays.copyOf(hmac("HmacSHA1", ki, plain), 12);
    return KdcAsn1.cat(c, h);
  }

  private static byte[] aesSha1Decrypt(byte[] key, int usage, byte[] cipher) throws GeneralSecurityException {
    if (cipher.length < 16 + 12) {
      throw new GeneralSecurityException("Insufficient data for decryption");
    }
    byte[] ke = dk(key, usageConstant(usage, 0xaa));
    byte[] ki = dk(key, usageConstant(usage, 0x55));
    byte[] plain = cts(false, ke, Arrays.copyOf(cipher, cipher.length - 12));
    byte[] h = Arrays.copyOf(hmac("HmacSHA1", ki, plain), 12);
    if (!MessageDigest.isEqual(h, Arrays.copyOfRange(cipher, cipher.length - 12, cipher.length))) {
      throw new GeneralSecurityException("Checksum failed");
    }
    return Arrays.copyOfRange(plain, 16, plain.length);
  }

  private static byte[] aesSha2Encrypt(int etype, byte[] key, int usage, byte[] data) throws GeneralSecurityException {
    boolean small = etype == AES128_SHA2;
    byte[] ke = kdfSha2(etype, key, usageConstant(usage, 0xaa), small ? 128 : 256);
    byte[] ki = kdfSha2(etype, key, usageConstant(usage, 0x55), small ? 128 : 192);
    byte[] c = cts(true, ke, KdcAsn1.cat(random(16), data));
    byte[] h = Arrays.copyOf(hmac(small ? "HmacSHA256" : "HmacSHA384", ki, new byte[16], c), small ? 16 : 24);
    return KdcAsn1.cat(c, h);
  }

  private static byte[] aesSha2Decrypt(int etype, byte[] key, int usage, byte[] cipher)
      throws GeneralSecurityException {
    boolean small = etype == AES128_SHA2;
    int mac = small ? 16 : 24;
    if (cipher.length < 16 + mac) {
      throw new GeneralSecurityException("Insufficient data for decryption");
    }
    byte[] ke = kdfSha2(etype, key, usageConstant(usage, 0xaa), small ? 128 : 256);
    byte[] ki = kdfSha2(etype, key, usageConstant(usage, 0x55), small ? 128 : 192);
    byte[] c = Arrays.copyOf(cipher, cipher.length - mac);
    byte[] h = Arrays.copyOf(hmac(small ? "HmacSHA256" : "HmacSHA384", ki, new byte[16], c), mac);
    if (!MessageDigest.isEqual(h, Arrays.copyOfRange(cipher, cipher.length - mac, cipher.length))) {
      throw new GeneralSecurityException("Checksum failed");
    }
    byte[] plain = cts(false, ke, c);
    return Arrays.copyOfRange(plain, 16, plain.length);
  }

  static int rc4Usage(int usage) {
    return usage == 3 || usage == 9 ? 8 : usage == 23 ? 13 : usage;
  }

  private static byte[] rc4(byte[] key, byte[] data) throws GeneralSecurityException {
    Cipher c = Cipher.getInstance("ARCFOUR");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "ARCFOUR"));
    return c.doFinal(data);
  }

  private static byte[] rc4Encrypt(byte[] key, int usage, byte[] data) throws GeneralSecurityException {
    byte[] k1 = hmac("HmacMD5", key, le32(rc4Usage(usage)));
    byte[] plain = KdcAsn1.cat(random(8), data);
    byte[] chk = hmac("HmacMD5", k1, plain);
    byte[] k3 = hmac("HmacMD5", k1, chk);
    return KdcAsn1.cat(chk, rc4(k3, plain));
  }

  private static byte[] rc4Decrypt(byte[] key, int usage, byte[] cipher) throws GeneralSecurityException {
    if (cipher.length < 24) {
      throw new GeneralSecurityException("Insufficient data for decryption");
    }
    byte[] k1 = hmac("HmacMD5", key, le32(rc4Usage(usage)));
    byte[] chk = Arrays.copyOf(cipher, 16);
    byte[] k3 = hmac("HmacMD5", k1, chk);
    byte[] plain = rc4(k3, Arrays.copyOfRange(cipher, 16, cipher.length));
    if (!MessageDigest.isEqual(hmac("HmacMD5", k1, plain), chk)) {
      throw new GeneralSecurityException("Checksum failed");
    }
    return Arrays.copyOfRange(plain, 8, plain.length);
  }

  public static byte[] nFold(byte[] in, int outLen) {
    int inLen = in.length;
    int lcm = lcm(inLen, outLen);
    byte[] rotated = new byte[lcm];
    for (int i = 0; i < lcm / inLen; i++) {
      byte[] r = rotateRight(in, 13 * i);
      System.arraycopy(r, 0, rotated, i * inLen, inLen);
    }
    byte[] sum = new byte[outLen];
    for (int chunk = 0; chunk < lcm / outLen; chunk++) {
      int c = 0;
      byte[] next = new byte[outLen];
      for (int i = outLen - 1; i >= 0; i--) {
        int v = (sum[i] & 0xff) + (rotated[chunk * outLen + i] & 0xff) + c;
        next[i] = (byte) v;
        c = v >> 8;
      }
      for (int i = outLen - 1; c != 0 && i >= 0; i--) {
        int v = (next[i] & 0xff) + c;
        next[i] = (byte) v;
        c = v >> 8;
      }
      sum = next;
    }
    return sum;
  }

  private static byte[] rotateRight(byte[] in, int bits) {
    int n = in.length * 8;
    bits %= n;
    byte[] out = new byte[in.length];
    for (int i = 0; i < n; i++) {
      int dst = (i + bits) % n;
      int bit = (in[i / 8] >> (7 - i % 8)) & 1;
      if (bit != 0) {
        out[dst / 8] |= (byte) (1 << (7 - dst % 8));
      }
    }
    return out;
  }

  private static int lcm(int a, int b) {
    int x = a;
    int y = b;
    while (y != 0) {
      int t = x % y;
      x = y;
      y = t;
    }
    return a / x * b;
  }

  static byte parity(byte b) {
    int v = b & 0xfe;
    return (byte) (Integer.bitCount(v) % 2 == 0 ? v | 1 : v);
  }

  private static byte[] derive3Des(byte[] key, byte[] constant) throws GeneralSecurityException {
    byte[] block = nFold(constant, 8);
    byte[] raw = new byte[21];
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    byte[] out = new byte[0];
    while (out.length < 21) {
      c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DESede"), new IvParameterSpec(new byte[8]));
      block = c.doFinal(block);
      out = KdcAsn1.cat(out, block);
    }
    System.arraycopy(out, 0, raw, 0, 21);
    return expand3DesKey(raw);
  }

  private static byte[] expand3DesKey(byte[] raw) {
    byte[] key = new byte[24];
    for (int i = 0; i < 3; i++) {
      int low = 0;
      for (int j = 0; j < 7; j++) {
        key[i * 8 + j] = raw[i * 7 + j];
        low |= (raw[i * 7 + j] & 1) << (j + 1);
      }
      key[i * 8 + 7] = (byte) low;
      for (int j = 0; j < 8; j++) {
        key[i * 8 + j] = parity(key[i * 8 + j]);
      }
    }
    return key;
  }

  private static byte[] des3String2Key(char[] password, String salt) throws GeneralSecurityException {
    byte[] s = KdcAsn1.cat(new String(password).getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8));
    byte[] raw = nFold(s, 21);
    return derive3Des(expand3DesKey(raw), "kerberos".getBytes(StandardCharsets.US_ASCII));
  }

  private static byte[] des3Encrypt(byte[] key, int usage, byte[] data) throws GeneralSecurityException {
    byte[] ke = derive3Des(key, usageConstant(usage, 0xaa));
    byte[] ki = derive3Des(key, usageConstant(usage, 0x55));
    byte[] plain = KdcAsn1.cat(random(8), data);
    int pad = (8 - plain.length % 8) % 8;
    byte[] padded = Arrays.copyOf(plain, plain.length + pad);
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ke, "DESede"), new IvParameterSpec(new byte[8]));
    return KdcAsn1.cat(c.doFinal(padded), hmac("HmacSHA1", ki, padded));
  }

  private static byte[] des3Decrypt(byte[] key, int usage, byte[] cipher) throws GeneralSecurityException {
    if (cipher.length < 8 + 20) {
      throw new GeneralSecurityException("Insufficient data for decryption");
    }
    byte[] ke = derive3Des(key, usageConstant(usage, 0xaa));
    byte[] ki = derive3Des(key, usageConstant(usage, 0x55));
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ke, "DESede"), new IvParameterSpec(new byte[8]));
    byte[] plain = c.doFinal(Arrays.copyOf(cipher, cipher.length - 20));
    if (!MessageDigest.isEqual(hmac("HmacSHA1", ki, plain), Arrays.copyOfRange(cipher, cipher.length - 20,
        cipher.length))) {
      throw new GeneralSecurityException("Checksum failed");
    }
    return Arrays.copyOfRange(plain, 8, plain.length);
  }

  private static final long[] kWeak = {0x0101010101010101L, 0xfefefefefefefefeL, 0x1f1f1f1f0e0e0e0eL,
      0xe0e0e0e0f1f1f1f1L, 0x01fe01fe01fe01feL, 0xfe01fe01fe01fe01L, 0x1fe01fe00ef10ef1L, 0xe01fe01ff10ef10eL,
      0x01e001e001f101f1L, 0xe001e001f101f101L, 0x1ffe1ffe0efe0efeL, 0xfe1ffe1ffe0efe0eL, 0x011f011f010e010eL,
      0x1f011f010e010e01L, 0xe0fee0fef1fef1feL, 0xfee0fee0fef1fef1L};

  private static boolean isWeak(byte[] k) {
    long v = 0;
    for (int i = 0; i < 8; i++) {
      v = (v << 8) | (k[i] & 0xff);
    }
    for (long w : kWeak) {
      if (w == v) {
        return true;
      }
    }
    return false;
  }

  private static byte[] desString2Key(char[] password, String salt) throws GeneralSecurityException {
    byte[] s = KdcAsn1.cat(new String(password).getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8));
    int padded = (s.length + 7) / 8 * 8;
    if (padded == 0) {
      padded = 8;
    }
    s = Arrays.copyOf(s, padded);
    byte[] key = new byte[8];
    for (int i = 0; i < s.length / 8; i++) {
      byte[] blk = new byte[8];
      for (int j = 0; j < 8; j++) {
        blk[j] = (byte) ((s[i * 8 + j] & 0x7f) << 1);
      }
      if ((i & 1) == 1) {
        byte[] r = new byte[8];
        for (int j = 0; j < 8; j++) {
          r[7 - j] = (byte) (((Integer.reverse(blk[j] & 0xff) >>> 24) << 1) & 0xfe);
        }
        blk = r;
      }
      for (int j = 0; j < 8; j++) {
        key[j] ^= blk[j];
      }
    }
    for (int j = 0; j < 8; j++) {
      key[j] = parity(key[j]);
    }
    if (isWeak(key)) {
      key[7] ^= (byte) 0xf0;
    }
    Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DES"), new IvParameterSpec(key));
    byte[] enc = c.doFinal(s);
    byte[] out = Arrays.copyOfRange(enc, enc.length - 8, enc.length);
    for (int j = 0; j < 8; j++) {
      out[j] = parity(out[j]);
    }
    if (isWeak(out)) {
      out[7] ^= (byte) 0xf0;
    }
    return out;
  }

  public static byte[] md4(byte[] in) {
    int[] state = {0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476};
    long bitLen = (long) in.length * 8;
    int padLen = (int) ((56 - (in.length + 1) % 64 + 64) % 64);
    byte[] msg = new byte[in.length + 1 + padLen + 8];
    System.arraycopy(in, 0, msg, 0, in.length);
    msg[in.length] = (byte) 0x80;
    for (int i = 0; i < 8; i++) {
      msg[msg.length - 8 + i] = (byte) (bitLen >>> (8 * i));
    }
    int[] x = new int[16];
    int[] s1 = {3, 7, 11, 19};
    int[] s2 = {3, 5, 9, 13};
    int[] o2 = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
    int[] s3 = {3, 9, 11, 15};
    int[] o3 = {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15};
    for (int off = 0; off < msg.length; off += 64) {
      for (int i = 0; i < 16; i++) {
        x[i] = (msg[off + 4 * i] & 0xff) | ((msg[off + 4 * i + 1] & 0xff) << 8)
            | ((msg[off + 4 * i + 2] & 0xff) << 16) | ((msg[off + 4 * i + 3] & 0xff) << 24);
      }
      int a = state[0];
      int b = state[1];
      int c = state[2];
      int d = state[3];
      for (int i = 0; i < 16; i++) {
        int t = Integer.rotateLeft(a + ((b & c) | (~b & d)) + x[i], s1[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      for (int i = 0; i < 16; i++) {
        int t = Integer.rotateLeft(a + ((b & c) | (b & d) | (c & d)) + x[o2[i]] + 0x5a827999, s2[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      for (int i = 0; i < 16; i++) {
        int t = Integer.rotateLeft(a + (b ^ c ^ d) + x[o3[i]] + 0x6ed9eba1, s3[i % 4]);
        a = d;
        d = c;
        c = b;
        b = t;
      }
      state[0] += a;
      state[1] += b;
      state[2] += c;
      state[3] += d;
    }
    byte[] out = new byte[16];
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out[4 * i + j] = (byte) (state[i] >>> (8 * j));
      }
    }
    return out;
  }
}
