/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class Hkdf {
  private Hkdf() {
  }

  static byte[] hmac(String algorithm, byte[] key, byte[]... data) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(key.length == 0 ? new byte[1] : key, algorithm));
      for (byte[] d : data) {
        mac.update(d);
      }
      return mac.doFinal();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  static int hashLength(String algorithm) {
    return algorithm.equals("HmacSHA384") ? 48 : 32;
  }

  static byte[] extract(String algorithm, byte[] salt, byte[] ikm) {
    byte[] s = salt == null || salt.length == 0 ? new byte[hashLength(algorithm)] : salt;
    return hmac(algorithm, s, ikm);
  }

  static byte[] expand(String algorithm, byte[] prk, byte[] info, int length) {
    int hashLen = hashLength(algorithm);
    byte[] out = new byte[length];
    byte[] t = new byte[0];
    int pos = 0;
    for (int i = 1; pos < length; i++) {
      t = hmac(algorithm, prk, t, info, new byte[] {(byte) i});
      int n = Math.min(hashLen, length - pos);
      System.arraycopy(t, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  static byte[] expandLabel(String algorithm, byte[] secret, String label, byte[] context, int length) {
    byte[] fullLabel = ("tls13 " + label).getBytes(StandardCharsets.US_ASCII);
    ByteWriter w = new ByteWriter();
    w.u16(length);
    w.u8(fullLabel.length);
    w.bytes(fullLabel);
    w.u8(context.length);
    w.bytes(context);
    return expand(algorithm, secret, w.toByteArray(), length);
  }
}
