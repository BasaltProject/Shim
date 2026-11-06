/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

final class DHKeyCodec {
  static final String kOid = "1.2.840.113549.1.3.1";

  private DHKeyCodec() {
  }

  static byte[] algorithmId(BigInteger p, BigInteger g, int l) throws IOException {
    DerOutputStream params = new DerOutputStream();
    params.putInteger(p);
    params.putInteger(g);
    if (l != 0) {
      params.putInteger(l);
    }
    DerOutputStream alg = new DerOutputStream();
    alg.putOID(ObjectIdentifier.of(kOid));
    alg.write(DerValue.tag_Sequence, params);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, alg);
    return out.toByteArray();
  }

  static BigInteger[] readAlgorithmId(DerValue alg, int[] lOut) throws IOException {
    DerInputStream in = alg.data();
    if (!in.getOID().toString().equals(kOid)) {
      throw new IOException("algid parse error, not a DH key");
    }
    DerInputStream ps = in.getDerValue().data();
    BigInteger p = ps.getBigInteger();
    BigInteger g = ps.getBigInteger();
    lOut[0] = ps.available() > 0 ? ps.getInteger() : 0;
    return new BigInteger[] {p, g};
  }

  static String hex(BigInteger v) {
    String h = v.toString(16);
    if ((h.length() & 1) != 0) {
      h = "0" + h;
    }
    StringBuilder sb = new StringBuilder("    ");
    int groups = 0;
    for (int i = 0; i < h.length(); i += 8) {
      if (groups > 0) {
        sb.append(groups % 8 == 0 ? "\n    " : " ");
      }
      sb.append(h, i, Math.min(h.length(), i + 8));
      groups++;
    }
    return sb.toString();
  }
}
