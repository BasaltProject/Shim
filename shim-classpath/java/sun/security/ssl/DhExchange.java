/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

final class DhExchange {
  private static final BigInteger kTwo = BigInteger.valueOf(2);

  final BigInteger p;
  final BigInteger g;
  private final BigInteger x;
  private final BigInteger y;

  DhExchange(BigInteger p, BigInteger g, SecureRandom random) {
    this.p = p;
    this.g = g;
    int bits = Math.min(p.bitLength() - 1, exponentBits(p.bitLength()));
    BigInteger candidate;
    do {
      candidate = new BigInteger(bits, random);
    } while (candidate.compareTo(kTwo) < 0);
    this.x = candidate;
    this.y = g.modPow(x, p);
  }

  private static int exponentBits(int primeBits) {
    return primeBits <= 2048 ? 256 : primeBits <= 3072 ? 320 : primeBits <= 4096 ? 384 : 512;
  }

  static BigInteger ephemeralPrime() {
    String size = System.getProperty("jdk.tls.ephemeralDHKeySize");
    if (size != null) {
      for (NamedGroup g : NamedGroup.all()) {
        if (g.isDh() && Integer.toString(g.dhBits).equals(size)) {
          return g.dhPrime();
        }
      }
    }
    return NamedGroup.kFfdhe2048.dhPrime();
  }

  int primeBytes() {
    return (p.bitLength() + 7) / 8;
  }

  byte[] publicPadded() {
    return toFixed(y, primeBytes());
  }

  byte[] secret(byte[] peer, boolean padded) throws InvalidKeyException {
    BigInteger peerY = new BigInteger(1, peer);
    if (peerY.compareTo(kTwo) < 0 || peerY.compareTo(p.subtract(kTwo)) > 0) {
      throw new InvalidKeyException("Invalid Diffie-Hellman public value");
    }
    BigInteger s = peerY.modPow(x, p);
    return padded ? toFixed(s, primeBytes()) : stripZeros(s.toByteArray());
  }

  static byte[] stripZeros(byte[] b) {
    int i = 0;
    while (i < b.length - 1 && b[i] == 0) {
      i++;
    }
    return i == 0 ? b : Arrays.copyOfRange(b, i, b.length);
  }

  static byte[] toFixed(BigInteger v, int size) {
    byte[] b = v.toByteArray();
    byte[] out = new byte[size];
    int start = b.length > size ? b.length - size : 0;
    int len = b.length - start;
    System.arraycopy(b, start, out, size - len, len);
    return out;
  }
}
