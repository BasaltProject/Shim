/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class BigIntegerPrimes {
  static final int kSmallPrimeThreshold = 95;
  static final int kDefaultCertainty = 100;
  private static final int kPrimeSearchBitLengthLimit = 500000000;
  private static final BigInteger kSmallPrimeProduct =
      BigInteger.valueOf(3L * 5 * 7 * 11 * 13 * 17 * 19 * 23 * 29 * 31 * 37 * 41);

  private BigIntegerPrimes() { }

  private static boolean hasSmallFactor(BigInteger p) {
    long r = p.remainder(kSmallPrimeProduct).longValue();
    return (r % 3 == 0) || (r % 5 == 0) || (r % 7 == 0) || (r % 11 == 0) || (r % 13 == 0) || (r % 17 == 0)
        || (r % 19 == 0) || (r % 23 == 0) || (r % 29 == 0) || (r % 31 == 0) || (r % 37 == 0) || (r % 41 == 0);
  }

  static BigInteger probablePrime(int bitLength, int certainty, Random rnd) {
    if (bitLength < 2) {
      throw new ArithmeticException("bitLength < 2");
    }
    return bitLength < kSmallPrimeThreshold ? smallPrime(bitLength, certainty, rnd)
        : largePrime(bitLength, certainty, rnd);
  }

  private static BigInteger smallPrime(int bitLength, int certainty, Random rnd) {
    int magLen = (bitLength + 31) >>> 5;
    int[] temp = new int[magLen];
    int highBit = 1 << ((bitLength + 31) & 0x1f);
    int highMask = (highBit << 1) - 1;
    while (true) {
      for (int i = 0; i < magLen; i++) {
        temp[i] = rnd.nextInt();
      }
      temp[0] = (temp[0] & highMask) | highBit;
      if (bitLength > 2) {
        temp[magLen - 1] |= 1;
      }
      BigInteger p = new BigInteger(temp.clone(), 1);
      if (bitLength > 6 && hasSmallFactor(p)) {
        continue;
      }
      if (bitLength < 4) {
        return p;
      }
      if (primeToCertainty(p, certainty, rnd)) {
        return p;
      }
    }
  }

  private static int searchLength(int bitLength) {
    if (bitLength > kPrimeSearchBitLengthLimit + 1) {
      throw new ArithmeticException("Prime search implementation restriction on bitLength");
    }
    return bitLength / 20 * 64;
  }

  private static BigInteger clearLowBit(BigInteger p) {
    int[] mag = p.mag.clone();
    mag[mag.length - 1] &= 0xfffffffe;
    return new BigInteger(mag, 1);
  }

  private static BigInteger largePrime(int bitLength, int certainty, Random rnd) {
    BigInteger p = clearLowBit(new BigInteger(bitLength, rnd).setBit(bitLength - 1));
    int searchLen = searchLength(bitLength);
    BitSieve searchSieve = new BitSieve(p, searchLen);
    BigInteger candidate = searchSieve.retrieve(p, certainty, rnd);
    while (candidate == null || candidate.bitLength() != bitLength) {
      p = p.add(BigInteger.valueOf(2L * searchLen));
      if (p.bitLength() != bitLength) {
        p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);
      }
      p = clearLowBit(p);
      searchSieve = new BitSieve(p, searchLen);
      candidate = searchSieve.retrieve(p, certainty, rnd);
    }
    return candidate;
  }

  static BigInteger nextProbablePrime(BigInteger start) {
    if (start.signum < 0) {
      throw new ArithmeticException("start < 0: " + start);
    }
    if (start.signum == 0 || start.equals(BigInteger.ONE)) {
      return BigInteger.TWO;
    }
    BigInteger result = start.add(BigInteger.ONE);
    if (result.bitLength() < kSmallPrimeThreshold) {
      if (!result.testBit(0)) {
        result = result.add(BigInteger.ONE);
      }
      while (true) {
        if (result.bitLength() > 6 && hasSmallFactor(result)) {
          result = result.add(BigInteger.TWO);
          continue;
        }
        if (result.bitLength() < 4) {
          return result;
        }
        if (primeToCertainty(result, kDefaultCertainty, null)) {
          return result;
        }
        result = result.add(BigInteger.TWO);
      }
    }
    if (result.testBit(0)) {
      result = result.subtract(BigInteger.ONE);
    }
    int searchLen = searchLength(result.bitLength());
    while (true) {
      BitSieve searchSieve = new BitSieve(result, searchLen);
      BigInteger candidate = searchSieve.retrieve(result, kDefaultCertainty, null);
      if (candidate != null) {
        return candidate;
      }
      result = result.add(BigInteger.valueOf(2L * searchLen));
    }
  }

  static boolean primeToCertainty(BigInteger w, int certainty, Random random) {
    int rounds = 0;
    int n = (Math.min(certainty, Integer.MAX_VALUE - 1) + 1) / 2;
    int sizeInBits = w.bitLength();
    if (sizeInBits < 100) {
      rounds = 50;
      rounds = n < rounds ? n : rounds;
      return passesMillerRabin(w, rounds, random);
    }
    if (sizeInBits < 256) {
      rounds = 27;
    } else if (sizeInBits < 512) {
      rounds = 15;
    } else if (sizeInBits < 768) {
      rounds = 8;
    } else if (sizeInBits < 1024) {
      rounds = 4;
    } else {
      rounds = 2;
    }
    rounds = n < rounds ? n : rounds;
    return passesMillerRabin(w, rounds, random) && passesLucasLehmer(w);
  }

  private static boolean passesMillerRabin(BigInteger w, int iterations, Random rnd) {
    BigInteger thisMinusOne = w.subtract(BigInteger.ONE);
    BigInteger m = thisMinusOne;
    int a = m.getLowestSetBit();
    m = m.shiftRight(a);
    if (rnd == null) {
      rnd = ThreadLocalRandom.current();
    }
    for (int i = 0; i < iterations; i++) {
      BigInteger b;
      do {
        b = new BigInteger(w.bitLength(), rnd);
      } while (b.compareTo(BigInteger.ONE) <= 0 || b.compareTo(w) >= 0);
      int j = 0;
      BigInteger z = b.modPow(m, w);
      while (!((j == 0 && z.equals(BigInteger.ONE)) || z.equals(thisMinusOne))) {
        if (j > 0 && z.equals(BigInteger.ONE) || ++j == a) {
          return false;
        }
        z = z.modPow(BigInteger.TWO, w);
      }
    }
    return true;
  }

  private static boolean passesLucasLehmer(BigInteger w) {
    BigInteger thisPlusOne = w.add(BigInteger.ONE);
    int d = 5;
    while (jacobiSymbol(d, w) != -1) {
      d = (d < 0) ? Math.abs(d) + 2 : -(d + 2);
    }
    BigInteger u = lucasLehmerSequence(d, thisPlusOne, w);
    return u.mod(w).equals(BigInteger.ZERO);
  }

  private static int jacobiSymbol(int p, BigInteger n) {
    if (p == 0) {
      return 0;
    }
    int j = 1;
    int u = n.mag[n.mag.length - 1];
    if (p < 0) {
      p = -p;
      int n8 = u & 7;
      if ((n8 == 3) || (n8 == 7)) {
        j = -j;
      }
    }
    while ((p & 3) == 0) {
      p >>= 2;
    }
    if ((p & 1) == 0) {
      p >>= 1;
      if (((u ^ (u >> 1)) & 2) != 0) {
        j = -j;
      }
    }
    if (p == 1) {
      return j;
    }
    if ((p & u & 2) != 0) {
      j = -j;
    }
    u = n.mod(BigInteger.valueOf(p)).intValue();
    while (u != 0) {
      while ((u & 3) == 0) {
        u >>= 2;
      }
      if ((u & 1) == 0) {
        u >>= 1;
        if (((p ^ (p >> 1)) & 2) != 0) {
          j = -j;
        }
      }
      if (u == 1) {
        return j;
      }
      int t = u;
      u = p;
      p = t;
      if ((u & p & 2) != 0) {
        j = -j;
      }
      u %= p;
    }
    return 0;
  }

  private static BigInteger lucasLehmerSequence(int z, BigInteger k, BigInteger n) {
    BigInteger d = BigInteger.valueOf(z);
    BigInteger u = BigInteger.ONE;
    BigInteger u2;
    BigInteger v = BigInteger.ONE;
    BigInteger v2;
    for (int i = k.bitLength() - 2; i >= 0; i--) {
      u2 = u.multiply(v).mod(n);
      v2 = v.multiply(v).add(d.multiply(u.multiply(u))).mod(n);
      if (v2.testBit(0)) {
        v2 = v2.subtract(n);
      }
      v2 = v2.shiftRight(1);
      u = u2;
      v = v2;
      if (k.testBit(i)) {
        u2 = u.add(v).mod(n);
        if (u2.testBit(0)) {
          u2 = u2.subtract(n);
        }
        u2 = u2.shiftRight(1);
        v2 = v.add(d.multiply(u)).mod(n);
        if (v2.testBit(0)) {
          v2 = v2.subtract(n);
        }
        v2 = v2.shiftRight(1);
        u = u2;
        v = v2;
      }
    }
    return u;
  }

  private static final class BitSieve {
    private static final BitSieve kSmallSieve = new BitSieve();

    private final long[] bits;
    private final int length;

    private BitSieve() {
      length = 150 * 64;
      bits = new long[unitIndex(length - 1) + 1];
      set(0);
      int nextIndex = 1;
      int nextPrime = 3;
      do {
        sieveSingle(length, nextIndex + nextPrime, nextPrime);
        nextIndex = sieveSearch(length, nextIndex + 1);
        nextPrime = 2 * nextIndex + 1;
      } while (nextIndex > 0 && nextPrime < length);
    }

    BitSieve(BigInteger base, int searchLen) {
      bits = new long[unitIndex(searchLen - 1) + 1];
      length = searchLen;
      int start = 0;
      int step = kSmallSieve.sieveSearch(kSmallSieve.length, start);
      int convertedStep = (step * 2) + 1;
      do {
        start = base.remainder(BigInteger.valueOf(convertedStep)).intValue();
        start = convertedStep - start;
        if (start % 2 == 0) {
          start += convertedStep;
        }
        sieveSingle(searchLen, (start - 1) / 2, convertedStep);
        step = kSmallSieve.sieveSearch(kSmallSieve.length, step + 1);
        convertedStep = (step * 2) + 1;
      } while (step > 0);
    }

    private static int unitIndex(int bitIndex) {
      return bitIndex >>> 6;
    }

    private static long bit(int bitIndex) {
      return 1L << (bitIndex & 63);
    }

    private boolean get(int bitIndex) {
      return (bits[unitIndex(bitIndex)] & bit(bitIndex)) != 0;
    }

    private void set(int bitIndex) {
      bits[unitIndex(bitIndex)] |= bit(bitIndex);
    }

    private int sieveSearch(int limit, int start) {
      if (start >= limit) {
        return -1;
      }
      int index = start;
      do {
        if (!get(index)) {
          return index;
        }
        index++;
      } while (index < limit - 1);
      return -1;
    }

    private void sieveSingle(int limit, int start, int step) {
      while (start < limit) {
        set(start);
        start += step;
      }
    }

    BigInteger retrieve(BigInteger initValue, int certainty, Random random) {
      int offset = 1;
      for (int i = 0; i < bits.length; i++) {
        long nextLong = ~bits[i];
        for (int j = 0; j < 64; j++) {
          if ((nextLong & 1) == 1) {
            BigInteger candidate = initValue.add(BigInteger.valueOf(offset));
            if (primeToCertainty(candidate, certainty, random)) {
              return candidate;
            }
          }
          nextLong >>>= 1;
          offset += 2;
        }
      }
      return null;
    }
  }
}
