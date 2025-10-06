/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Arrays;
import sun.security.util.CurveDB;
import sun.security.util.NamedCurve;

final class ECUtil {
  private ECUtil() {
  }

  static NamedCurve namedCurve(ECParameterSpec params) throws InvalidKeyException {
    NamedCurve nc = CurveDB.lookup(params);
    if (nc == null) {
      throw new InvalidKeyException("Unsupported curve: " + params);
    }
    return nc;
  }

  static byte[] unsigned(BigInteger v, int len) {
    byte[] b = v.toByteArray();
    if (b.length == len) {
      return b;
    }
    byte[] out = new byte[len];
    if (b.length > len) {
      System.arraycopy(b, b.length - len, out, 0, len);
    } else {
      System.arraycopy(b, 0, out, len - b.length, b.length);
    }
    return out;
  }

  static byte[] encodePoint(ECPoint w, int fieldBytes) {
    byte[] out = new byte[1 + 2 * fieldBytes];
    out[0] = 4;
    byte[] x = unsigned(w.getAffineX(), fieldBytes);
    byte[] y = unsigned(w.getAffineY(), fieldBytes);
    System.arraycopy(x, 0, out, 1, fieldBytes);
    System.arraycopy(y, 0, out, 1 + fieldBytes, fieldBytes);
    return out;
  }

  static ECPoint decodePoint(byte[] data, int fieldBytes) throws IOException {
    if (data.length != 1 + 2 * fieldBytes || data[0] != 4) {
      throw new IOException("Only uncompressed point format supported");
    }
    byte[] x = Arrays.copyOfRange(data, 1, 1 + fieldBytes);
    byte[] y = Arrays.copyOfRange(data, 1 + fieldBytes, data.length);
    return new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
  }

  static final String kOidEc = "1.2.840.10045.2.1";
}
