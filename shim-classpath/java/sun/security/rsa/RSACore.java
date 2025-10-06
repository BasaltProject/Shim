/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.math.BigInteger;
import java.security.ProviderException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.BadPaddingException;

public final class RSACore {
  private RSACore() {
  }

  public static int getByteLength(BigInteger b) {
    int n = b.bitLength();
    return (n + 7) >> 3;
  }

  public static int getByteLength(RSAKey key) {
    return getByteLength(key.getModulus());
  }

  public static byte[] convert(byte[] b, int ofs, int len) {
    if ((ofs == 0) && (len == b.length)) {
      return b;
    }
    byte[] t = new byte[len];
    System.arraycopy(b, ofs, t, 0, len);
    return t;
  }

  public static byte[] rsa(byte[] msg, RSAPublicKey key) throws BadPaddingException {
    return crypt(msg, key.getModulus(), key.getPublicExponent());
  }

  public static byte[] rsa(byte[] msg, RSAPrivateKey key, boolean verify) throws BadPaddingException {
    if (key instanceof RSAPrivateCrtKey) {
      return crtCrypt(msg, (RSAPrivateCrtKey) key, verify);
    }
    return crypt(msg, key.getModulus(), key.getPrivateExponent());
  }

  private static byte[] crypt(byte[] msg, BigInteger n, BigInteger exp) throws BadPaddingException {
    int outLen = getByteLength(n);
    BigInteger m = parseMsg(msg, n);
    BigInteger c = m.modPow(exp, n);
    return toByteArray(c, outLen);
  }

  private static byte[] crtCrypt(byte[] msg, RSAPrivateCrtKey key, boolean verify) throws BadPaddingException {
    BigInteger n = key.getModulus();
    int outLen = getByteLength(n);
    BigInteger m = parseMsg(msg, n);
    BigInteger p = key.getPrimeP();
    BigInteger q = key.getPrimeQ();
    BigInteger dp = key.getPrimeExponentP();
    BigInteger dq = key.getPrimeExponentQ();
    BigInteger qInv = key.getCrtCoefficient();
    BigInteger m1 = m.mod(p).modPow(dp, p);
    BigInteger m2 = m.mod(q).modPow(dq, q);
    BigInteger h = qInv.multiply(m1.subtract(m2)).mod(p);
    BigInteger c = m2.add(h.multiply(q));
    if (verify) {
      BigInteger check = c.modPow(key.getPublicExponent(), n);
      if (!check.equals(m)) {
        throw new BadPaddingException("RSA private key operation failed");
      }
    }
    return toByteArray(c, outLen);
  }

  private static BigInteger parseMsg(byte[] msg, BigInteger n) throws BadPaddingException {
    BigInteger m = new BigInteger(1, msg);
    if (m.compareTo(n) >= 0) {
      throw new BadPaddingException("Message is larger than modulus");
    }
    return m;
  }

  private static byte[] toByteArray(BigInteger bi, int len) {
    byte[] b = bi.toByteArray();
    int n = b.length;
    if (n == len) {
      return b;
    }
    if ((n == len + 1) && (b[0] == 0)) {
      byte[] t = new byte[len];
      System.arraycopy(b, 1, t, 0, len);
      java.util.Arrays.fill(b, (byte) 0);
      return t;
    }
    byte[] t = new byte[len];
    if (n > len) {
      throw new ProviderException("Internal error: result too long");
    }
    System.arraycopy(b, 0, t, len - n, n);
    java.util.Arrays.fill(b, (byte) 0);
    return t;
  }
}
