/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.pkcs12;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

final class PKCS12Crypto {
  static final String kPbes2Oid = "1.2.840.113549.1.5.13";
  static final String kPbkdf2Oid = "1.2.840.113549.1.5.12";
  static final int kDefaultIterations = 10000;
  static final int kSaltLength = 20;

  private PKCS12Crypto() {
  }

  static byte[] bmpPassword(char[] password) {
    byte[] b = new byte[(password.length + 1) * 2];
    for (int i = 0; i < password.length; i++) {
      b[2 * i] = (byte) (password[i] >> 8);
      b[2 * i + 1] = (byte) password[i];
    }
    return b;
  }

  private static byte[] expand(byte[] in, int v) {
    if (in.length == 0) {
      return in;
    }
    byte[] out = new byte[v * ((in.length + v - 1) / v)];
    for (int i = 0; i < out.length; i++) {
      out[i] = in[i % in.length];
    }
    return out;
  }

  static byte[] kdf(byte[] password, byte[] salt, int iterations, int id, String hash, int outLen)
      throws GeneralSecurityException {
    MessageDigest md = MessageDigest.getInstance(hash);
    int u = md.getDigestLength();
    int v = hash.equals("SHA-384") || hash.startsWith("SHA-512") ? 128 : 64;
    byte[] d = new byte[v];
    Arrays.fill(d, (byte) id);
    byte[] s = expand(salt, v);
    byte[] p = expand(password, v);
    byte[] block = new byte[s.length + p.length];
    System.arraycopy(s, 0, block, 0, s.length);
    System.arraycopy(p, 0, block, s.length, p.length);
    int c = (outLen + u - 1) / u;
    byte[] out = new byte[outLen];
    for (int i = 0; i < c; i++) {
      md.update(d);
      md.update(block);
      byte[] a = md.digest();
      for (int r = 1; r < iterations; r++) {
        a = md.digest(a);
      }
      int n = Math.min(u, outLen - i * u);
      System.arraycopy(a, 0, out, i * u, n);
      if (i < c - 1) {
        byte[] b = new byte[v];
        for (int k = 0; k < v; k++) {
          b[k] = a[k % u];
        }
        for (int j = 0; j < block.length; j += v) {
          int carry = 1;
          for (int k = v - 1; k >= 0; k--) {
            int sum = (block[j + k] & 0xff) + (b[k] & 0xff) + carry;
            block[j + k] = (byte) sum;
            carry = sum >>> 8;
          }
        }
      }
    }
    return out;
  }

  static String macHash(String macOid) {
    switch (macOid) {
    case "1.3.14.3.2.26":
      return "SHA-1";
    case "2.16.840.1.101.3.4.2.4":
      return "SHA-224";
    case "2.16.840.1.101.3.4.2.1":
      return "SHA-256";
    case "2.16.840.1.101.3.4.2.2":
      return "SHA-384";
    case "2.16.840.1.101.3.4.2.3":
      return "SHA-512";
    case "2.16.840.1.101.3.4.2.5":
      return "SHA-512/224";
    case "2.16.840.1.101.3.4.2.6":
      return "SHA-512/256";
    default:
      return null;
    }
  }

  static String macOid(String hash) {
    switch (hash) {
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
    default:
      throw new IllegalArgumentException(hash);
    }
  }

  static byte[] mac(String hash, byte[] password, byte[] salt, int iterations, byte[] data)
      throws GeneralSecurityException {
    int len = MessageDigest.getInstance(hash).getDigestLength();
    byte[] key = kdf(password, salt, iterations, 3, hash, len);
    String hmac = "Hmac" + hash.replace("-", "");
    Mac mac = Mac.getInstance(hmac);
    mac.init(new SecretKeySpec(key, hmac));
    return mac.doFinal(data);
  }

  private static String prfName(String oid) {
    switch (oid) {
    case "1.2.840.113549.2.7":
      return "HmacSHA1";
    case "1.2.840.113549.2.8":
      return "HmacSHA224";
    case "1.2.840.113549.2.9":
      return "HmacSHA256";
    case "1.2.840.113549.2.10":
      return "HmacSHA384";
    case "1.2.840.113549.2.11":
      return "HmacSHA512";
    default:
      return null;
    }
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBytes, String prf)
      throws GeneralSecurityException {
    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2With" + prf);
    return f.generateSecret(new PBEKeySpec(password, salt, iterations, keyBytes * 8)).getEncoded();
  }

  static byte[] decrypt(byte[] algId, char[] password, byte[] data) throws GeneralSecurityException {
    try {
      DerInputStream ai = new DerValue(algId).data();
      String oid = ai.getOID().toString();
      if (oid.equals(kPbes2Oid)) {
        return decryptPbes2(ai.getDerValue(), password, data);
      }
      int keyLen;
      int effective = 0;
      String cipher;
      String keyAlg;
      switch (oid) {
      case "1.2.840.113549.1.12.1.3":
        keyLen = 24;
        cipher = "DESede/CBC/PKCS5Padding";
        keyAlg = "DESede";
        break;
      case "1.2.840.113549.1.12.1.4":
        keyLen = 16;
        cipher = "DESede/CBC/PKCS5Padding";
        keyAlg = "DESede";
        break;
      case "1.2.840.113549.1.12.1.5":
        keyLen = 16;
        effective = 128;
        cipher = "RC2/CBC/PKCS5Padding";
        keyAlg = "RC2";
        break;
      case "1.2.840.113549.1.12.1.6":
        keyLen = 5;
        effective = 40;
        cipher = "RC2/CBC/PKCS5Padding";
        keyAlg = "RC2";
        break;
      default:
        throw new NoSuchAlgorithmException("Unsupported PBE algorithm: " + oid);
      }
      DerInputStream pi = ai.getDerValue().data();
      byte[] salt = pi.getOctetString();
      int iterations = pi.getInteger();
      byte[] pw = bmpPassword(password);
      byte[] key = kdf(pw, salt, iterations, 1, "SHA-1", keyLen);
      byte[] iv = kdf(pw, salt, iterations, 2, "SHA-1", 8);
      if (keyLen == 16 && keyAlg.equals("DESede")) {
        byte[] k24 = new byte[24];
        System.arraycopy(key, 0, k24, 0, 16);
        System.arraycopy(key, 0, k24, 16, 8);
        key = k24;
      }
      Cipher c = Cipher.getInstance(cipher);
      c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, keyAlg), effective != 0
          ? (java.security.spec.AlgorithmParameterSpec) new RC2ParameterSpec(effective, iv) : new IvParameterSpec(iv));
      return c.doFinal(data);
    } catch (IOException e) {
      throw new GeneralSecurityException("Invalid PBE parameters: " + e.getMessage(), e);
    }
  }

  private static byte[] decryptPbes2(DerValue params, char[] password, byte[] data) throws GeneralSecurityException,
      IOException {
    DerInputStream pi = params.data();
    DerInputStream kdf = pi.getDerValue().data();
    DerInputStream scheme = pi.getDerValue().data();
    if (!kdf.getOID().toString().equals(kPbkdf2Oid)) {
      throw new NoSuchAlgorithmException("Unsupported key derivation function");
    }
    DerInputStream kp = kdf.getDerValue().data();
    byte[] salt = kp.getOctetString();
    int iterations = kp.getInteger();
    int keyLen = -1;
    String prf = "HmacSHA1";
    while (kp.available() > 0) {
      DerValue v = kp.getDerValue();
      if (v.getTag() == DerValue.tag_Integer) {
        keyLen = v.getInteger();
      } else if (v.getTag() == DerValue.tag_Sequence) {
        String name = prfName(v.data().getOID().toString());
        if (name == null) {
          throw new NoSuchAlgorithmException("Unsupported PBKDF2 PRF");
        }
        prf = name;
      }
    }
    String encOid = scheme.getOID().toString();
    int need;
    String cipher;
    String keyAlg;
    switch (encOid) {
    case "2.16.840.1.101.3.4.1.2":
      need = 16;
      cipher = "AES/CBC/PKCS5Padding";
      keyAlg = "AES";
      break;
    case "2.16.840.1.101.3.4.1.22":
      need = 24;
      cipher = "AES/CBC/PKCS5Padding";
      keyAlg = "AES";
      break;
    case "2.16.840.1.101.3.4.1.42":
      need = 32;
      cipher = "AES/CBC/PKCS5Padding";
      keyAlg = "AES";
      break;
    case "1.2.840.113549.3.7":
      need = 24;
      cipher = "DESede/CBC/PKCS5Padding";
      keyAlg = "DESede";
      break;
    default:
      throw new NoSuchAlgorithmException("Unsupported encryption scheme: " + encOid);
    }
    if (keyLen != -1 && keyLen != need) {
      throw new InvalidAlgorithmParameterException("Key length does not match the encryption scheme");
    }
    byte[] iv = scheme.getOctetString();
    byte[] key = pbkdf2(password, salt, iterations, need, prf);
    Cipher c = Cipher.getInstance(cipher);
    c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, keyAlg), new IvParameterSpec(iv));
    return c.doFinal(data);
  }

  static byte[][] encryptPbes2(char[] password, byte[] data, SecureRandom random) throws GeneralSecurityException {
    byte[] salt = new byte[kSaltLength];
    byte[] iv = new byte[16];
    random.nextBytes(salt);
    random.nextBytes(iv);
    byte[] key = pbkdf2(password, salt, kDefaultIterations, 32, "HmacSHA256");
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
    byte[] enc = c.doFinal(data);
    try {
      DerOutputStream prf = new DerOutputStream();
      prf.putOID(new ObjectIdentifier("1.2.840.113549.2.9"));
      prf.putNull();
      DerOutputStream kp = new DerOutputStream();
      kp.putOctetString(salt);
      kp.putInteger(kDefaultIterations);
      kp.putInteger(32);
      kp.write(DerValue.tag_Sequence, prf);
      DerOutputStream kdf = new DerOutputStream();
      kdf.putOID(new ObjectIdentifier(kPbkdf2Oid));
      kdf.write(DerValue.tag_Sequence, kp);
      DerOutputStream scheme = new DerOutputStream();
      scheme.putOID(new ObjectIdentifier("2.16.840.1.101.3.4.1.42"));
      scheme.putOctetString(iv);
      DerOutputStream params = new DerOutputStream();
      params.write(DerValue.tag_Sequence, kdf);
      params.write(DerValue.tag_Sequence, scheme);
      DerOutputStream alg = new DerOutputStream();
      alg.putOID(new ObjectIdentifier(kPbes2Oid));
      alg.write(DerValue.tag_Sequence, params);
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, alg);
      return new byte[][] {out.toByteArray(), enc};
    } catch (IOException e) {
      throw new GeneralSecurityException(e);
    }
  }
}
