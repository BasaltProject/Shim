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
    byte[] labelSeed = concat(label.getBytes(StandardCharsets.US_ASCII), seed);
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
