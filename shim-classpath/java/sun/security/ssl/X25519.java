/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.math.BigInteger;

final class X25519 {
  private static final BigInteger kP = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19));
  private static final BigInteger kA24 = BigInteger.valueOf(121665);

  private X25519() {
  }

  private static BigInteger decodeLittle(byte[] b, boolean maskHighBit) {
    byte[] be = new byte[b.length];
    for (int i = 0; i < b.length; i++) {
      be[b.length - 1 - i] = b[i];
    }
    if (maskHighBit) {
      be[0] &= 0x7f;
    }
    return new BigInteger(1, be);
  }

  private static byte[] encodeLittle(BigInteger v) {
    byte[] out = new byte[32];
    byte[] be = v.toByteArray();
    for (int i = 0; i < be.length && i < 33; i++) {
      int idx = be.length - 1 - i;
      if (i < 32) {
        out[i] = be[idx];
      }
    }
    return out;
  }

  static byte[] scalarMult(byte[] scalar, byte[] uCoordinate) {
    byte[] k = scalar.clone();
    k[0] &= (byte) 248;
    k[31] &= 127;
    k[31] |= 64;
    BigInteger kInt = decodeLittle(k, false);
    BigInteger x1 = decodeLittle(uCoordinate, true).mod(kP);
    BigInteger x2 = BigInteger.ONE;
    BigInteger z2 = BigInteger.ZERO;
    BigInteger x3 = x1;
    BigInteger z3 = BigInteger.ONE;
    int swap = 0;
    for (int t = 254; t >= 0; t--) {
      int kt = kInt.testBit(t) ? 1 : 0;
      swap ^= kt;
      if (swap == 1) {
        BigInteger tmp = x2;
        x2 = x3;
        x3 = tmp;
        tmp = z2;
        z2 = z3;
        z3 = tmp;
      }
      swap = kt;
      BigInteger a = x2.add(z2).mod(kP);
      BigInteger aa = a.multiply(a).mod(kP);
      BigInteger b = x2.subtract(z2).mod(kP);
      BigInteger bb = b.multiply(b).mod(kP);
      BigInteger e = aa.subtract(bb).mod(kP);
      BigInteger c = x3.add(z3).mod(kP);
      BigInteger d = x3.subtract(z3).mod(kP);
      BigInteger da = d.multiply(a).mod(kP);
      BigInteger cb = c.multiply(b).mod(kP);
      BigInteger s = da.add(cb).mod(kP);
      BigInteger df = da.subtract(cb).mod(kP);
      x3 = s.multiply(s).mod(kP);
      z3 = x1.multiply(df.multiply(df).mod(kP)).mod(kP);
      x2 = aa.multiply(bb).mod(kP);
      z2 = e.multiply(aa.add(kA24.multiply(e)).mod(kP)).mod(kP);
    }
    if (swap == 1) {
      BigInteger tmp = x2;
      x2 = x3;
      x3 = tmp;
      tmp = z2;
      z2 = z3;
      z3 = tmp;
    }
    BigInteger result = z2.signum() == 0 ? BigInteger.ZERO : x2.multiply(z2.modPow(kP.subtract(BigInteger.TWO), kP))
        .mod(kP);
    return encodeLittle(result);
  }

  static byte[] publicKey(byte[] scalar) {
    byte[] nine = new byte[32];
    nine[0] = 9;
    return scalarMult(scalar, nine);
  }
}
