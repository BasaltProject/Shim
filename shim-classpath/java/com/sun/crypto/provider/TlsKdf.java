/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class TlsKdf {
  private TlsKdf() {
  }

  static byte[] hmac(MessageDigest md, int block, byte[] key, byte[]... data) {
    if (block <= 0) {
      block = md.getDigestLength() > 48 ? 128 : 64;
    }
    byte[] k = key;
    if (k.length > block) {
      md.reset();
      k = md.digest(k);
    }
    byte[] pad = new byte[block];
    System.arraycopy(k, 0, pad, 0, k.length);
    for (int i = 0; i < block; i++) {
      pad[i] ^= 0x36;
    }
    md.reset();
    md.update(pad);
    for (byte[] d : data) {
      md.update(d);
    }
    byte[] inner = md.digest();
    for (int i = 0; i < block; i++) {
      pad[i] ^= 0x36 ^ 0x5c;
    }
    md.update(pad);
    md.update(inner);
    return md.digest();
  }

  static byte[] pHash(MessageDigest md, int block, byte[] secret, byte[] seed, int length) {
    byte[] out = new byte[length];
    byte[] a = seed;
    int pos = 0;
    while (pos < length) {
      a = hmac(md, block, secret, a);
      byte[] chunk = hmac(md, block, secret, a, seed);
      int n = Math.min(chunk.length, length - pos);
      System.arraycopy(chunk, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  static byte[] prf12(byte[] secret, byte[] labelSeed, int length, String hash, int block)
      throws NoSuchAlgorithmException {
    if (hash == null || hash.isEmpty()) {
      throw new NoSuchAlgorithmException("Unspecified PRF algorithm");
    }
    return pHash(MessageDigest.getInstance(hash), block, secret, labelSeed, length);
  }

  static byte[] prf10(byte[] secret, byte[] labelSeed, int length) throws NoSuchAlgorithmException {
    int half = (secret.length + 1) / 2;
    byte[] s1 = java.util.Arrays.copyOfRange(secret, 0, half);
    byte[] s2 = java.util.Arrays.copyOfRange(secret, secret.length - half, secret.length);
    byte[] md5 = pHash(MessageDigest.getInstance("MD5"), 64, s1, labelSeed, length);
    byte[] sha = pHash(MessageDigest.getInstance("SHA1"), 64, s2, labelSeed, length);
    for (int i = 0; i < length; i++) {
      md5[i] ^= sha[i];
    }
    return md5;
  }

  static byte[] prf(int version, byte[] secret, String label, byte[] seed, int length, String hash, int block)
      throws NoSuchAlgorithmException {
    byte[] labelSeed = concat(label.getBytes(StandardCharsets.UTF_8), seed);
    return version >= 0x0303 ? prf12(secret, labelSeed, length, hash, block) : prf10(secret, labelSeed, length);
  }

  static byte[] ssl3(byte[] secret, byte[] seed, int length) throws GeneralSecurityException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    MessageDigest sha = MessageDigest.getInstance("SHA1");
    byte[] out = new byte[length];
    int pos = 0;
    for (int i = 0; pos < length; i++) {
      byte[] label = new byte[i + 1];
      java.util.Arrays.fill(label, (byte) ('A' + i));
      sha.update(label);
      sha.update(secret);
      sha.update(seed);
      md5.update(secret);
      md5.update(sha.digest());
      byte[] block = md5.digest();
      int n = Math.min(block.length, length - pos);
      System.arraycopy(block, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  static byte[] concat(byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) {
      n += p.length;
    }
    byte[] out = new byte[n];
    int o = 0;
    for (byte[] p : parts) {
      System.arraycopy(p, 0, out, o, p.length);
      o += p.length;
    }
    return out;
  }
}
