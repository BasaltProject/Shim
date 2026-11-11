/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.nio.charset.StandardCharsets;

final class TlsPrf {
  private TlsPrf() {
  }

  static byte[] prf(String hmac, byte[] secret, String label, byte[] seed, int length) {
    return expand(hmac, secret, concat(label.getBytes(StandardCharsets.US_ASCII), seed), length);
  }

  static byte[] prf(int version, String hmac, byte[] secret, String label, byte[] seed, int length) {
    if (version >= TlsConnection.kTls12) {
      return prf(hmac, secret, label, seed, length);
    }
    int half = (secret.length + 1) / 2;
    byte[] labelSeed = concat(label.getBytes(StandardCharsets.US_ASCII), seed);
    byte[] md5 = expand("HmacMD5", java.util.Arrays.copyOfRange(secret, 0, half), labelSeed, length);
    byte[] sha = expand("HmacSHA1", java.util.Arrays.copyOfRange(secret, secret.length - half, secret.length),
        labelSeed, length);
    for (int i = 0; i < length; i++) {
      md5[i] ^= sha[i];
    }
    return md5;
  }

  private static byte[] expand(String hmac, byte[] secret, byte[] labelSeed, int length) {
    byte[] out = new byte[length];
    byte[] a = labelSeed;
    int pos = 0;
    while (pos < length) {
      a = Hkdf.hmac(hmac, secret, a);
      byte[] block = Hkdf.hmac(hmac, secret, a, labelSeed);
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
