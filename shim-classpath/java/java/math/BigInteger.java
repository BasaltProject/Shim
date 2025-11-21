/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class BigInteger extends Number implements Comparable<BigInteger> {
  private static final long serialVersionUID = -8287574255936472291L;
  private static final long kMask = BigIntegerMath.kMask;
  private static final int kMaxConstant = 16;
  private static final int kMaxMagLength = Integer.MAX_VALUE / Integer.SIZE + 1;

  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("signum", Integer.TYPE),
    new ObjectStreamField("magnitude", byte[].class),
    new ObjectStreamField("bitCount", Integer.TYPE),
    new ObjectStreamField("bitLength", Integer.TYPE),
    new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE),
    new ObjectStreamField("lowestSetBit", Integer.TYPE)
  };

  private static final int[] kDigitsPerInt = {0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};
  private static final int[] kIntRadix = new int[Character.MAX_RADIX + 1];

  static {
    for (int r = Character.MIN_RADIX; r <= Character.MAX_RADIX; r++) {
      long v = 1;
      for (int i = 0; i < kDigitsPerInt[r]; i++) {
        v *= r;
      }
      kIntRadix[r] = (int) v;
    }
  }

  transient int signum;
  transient int[] mag;

  private static final BigInteger[] kPosConst = new BigInteger[kMaxConstant + 1];
  private static final BigInteger[] kNegConst = new BigInteger[kMaxConstant + 1];

  static {
    for (int i = 1; i <= kMaxConstant; i++) {
      int[] magnitude = new int[] {i};
      kPosConst[i] = new BigInteger(magnitude, 1);
      kNegConst[i] = new BigInteger(magnitude, -1);
    }
  }

  public static final BigInteger ZERO = new BigInteger(new int[0], 0);
  public static final BigInteger ONE = valueOf(1);
  public static final BigInteger TWO = valueOf(2);
  public static final BigInteger TEN = valueOf(10);

  BigInteger(int[] magnitude, int signum) {
    this.signum = (magnitude.length == 0 ? 0 : signum);
    this.mag = magnitude;
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  private BigInteger(long val) {
    if (val < 0) {
      val = -val;
      signum = -1;
    } else {
      signum = 1;
    }
    int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      mag = new int[] {(int) val};
    } else {
      mag = new int[] {highWord, (int) val};
    }
  }

  private static BigInteger of(int[] magnitude, int signum) {
    magnitude = BigIntegerMath.trim(magnitude);
    return magnitude.length == 0 ? ZERO : new BigInteger(magnitude, signum);
  }

  public BigInteger(byte[] val) {
    if (val.length == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    if (val[0] < 0) {
      mag = makePositive(val);
      signum = -1;
    } else {
      mag = stripLeadingZeroBytes(val, 0, val.length);
      signum = (mag.length == 0 ? 0 : 1);
    }
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  public BigInteger(int signum, byte[] magnitude) {
    this.mag = stripLeadingZeroBytes(magnitude, 0, magnitude.length);
    if (signum < -1 || signum > 1) {
      throw new NumberFormatException("Invalid signum value");
    }
    if (this.mag.length == 0) {
      this.signum = 0;
    } else {
      if (signum == 0) {
        throw new NumberFormatException("signum-magnitude mismatch");
      }
      this.signum = signum;
    }
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  public BigInteger(byte[] val, int off, int len) {
    Objects.checkFromIndexSize(off, len, val.length);
    if (len == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    if (val[off] < 0) {
      mag = makePositive(Arrays.copyOfRange(val, off, off + len));
      signum = -1;
    } else {
      mag = stripLeadingZeroBytes(val, off, len);
      signum = (mag.length == 0 ? 0 : 1);
    }
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  public BigInteger(int signum, byte[] magnitude, int off, int len) {
    if (signum < -1 || signum > 1) {
      throw new NumberFormatException("Invalid signum value");
    }
    Objects.checkFromIndexSize(off, len, magnitude.length);
    this.mag = stripLeadingZeroBytes(magnitude, off, len);
    if (this.mag.length == 0) {
      this.signum = 0;
    } else {
      if (signum == 0) {
        throw new NumberFormatException("signum-magnitude mismatch");
      }
      this.signum = signum;
    }
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  public BigInteger(String val, int radix) {
    int cursor = 0;
    int numDigits;
    final int len = val.length();
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      throw new NumberFormatException("Radix out of range");
    }
    if (len == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    int sign = 1;
    int index1 = val.lastIndexOf('-');
    int index2 = val.lastIndexOf('+');
    if (index1 >= 0) {
      if (index1 != 0 || index2 >= 0) {
        throw new NumberFormatException("Illegal embedded sign character");
      }
      sign = -1;
      cursor = 1;
    } else if (index2 >= 0) {
      if (index2 != 0) {
        throw new NumberFormatException("Illegal embedded sign character");
      }
      cursor = 1;
    }
    if (cursor == len) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    while (cursor < len && Character.digit(val.charAt(cursor), radix) == 0) {
      cursor++;
    }
    if (cursor == len) {
      signum = 0;
      mag = new int[0];
      return;
    }
    numDigits = len - cursor;
    signum = sign;
    long numBits = ((numDigits * kBitsPerDigit[radix]) >>> 10) + 1;
    if (numBits + 31 >= (1L << 32)) {
      reportOverflow();
    }
    int numWords = (int) (numBits + 31) >>> 5;
    int[] magnitude = new int[numWords];
    int firstGroupLen = numDigits % kDigitsPerInt[radix];
    if (firstGroupLen == 0) {
      firstGroupLen = kDigitsPerInt[radix];
    }
    String group = val.substring(cursor, cursor += firstGroupLen);
    magnitude[numWords - 1] = Integer.parseInt(group, radix);
    if (magnitude[numWords - 1] < 0) {
      throw new NumberFormatException("Illegal digit");
    }
    int superRadix = kIntRadix[radix];
    int groupVal = 0;
    while (cursor < len) {
      group = val.substring(cursor, cursor += kDigitsPerInt[radix]);
      groupVal = Integer.parseInt(group, radix);
      if (groupVal < 0) {
        throw new NumberFormatException("Illegal digit");
      }
      destructiveMulAdd(magnitude, superRadix, groupVal);
    }
    mag = BigIntegerMath.trim(magnitude);
    if (mag.length >= kMaxMagLength) {
      reportOverflow();
    }
  }

  private static final long[] kBitsPerDigit = {0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543,
      3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975,
      5025, 5074, 5120, 5166, 5210, 5253, 5295};

  private static void destructiveMulAdd(int[] x, int y, int z) {
    long ylong = y & kMask;
    long zlong = z & kMask;
    int len = x.length;
    long product = 0;
    long carry = 0;
    for (int i = len - 1; i >= 0; i--) {
      product = ylong * (x[i] & kMask) + carry;
      x[i] = (int) product;
      carry = product >>> 32;
    }
    long sum = (x[len - 1] & kMask) + zlong;
    x[len - 1] = (int) sum;
    carry = sum >>> 32;
    for (int i = len - 2; i >= 0; i--) {
      sum = (x[i] & kMask) + carry;
      x[i] = (int) sum;
      carry = sum >>> 32;
    }
  }

  public BigInteger(String val) {
    this(val, 10);
  }

  public BigInteger(int numBits, Random rnd) {
    this(1, randomBits(numBits, rnd));
  }

  private static byte[] randomBits(int numBits, Random rnd) {
    if (numBits < 0) {
      throw new IllegalArgumentException("numBits must be non-negative");
    }
    int numBytes = (int) (((long) numBits + 7) / 8);
    byte[] randomBits = new byte[numBytes];
    if (numBytes > 0) {
      rnd.nextBytes(randomBits);
      int excessBits = 8 * numBytes - numBits;
      randomBits[0] &= (byte) ((1 << (8 - excessBits)) - 1);
    }
    return randomBits;
  }

  public BigInteger(int bitLength, int certainty, Random rnd) {
    BigInteger prime;
    if (bitLength < 2) {
      throw new ArithmeticException("bitLength < 2");
    }
    prime = BigIntegerPrimes.probablePrime(bitLength, certainty, rnd);
    mag = prime.mag;
    signum = prime.signum;
  }

  public static BigInteger probablePrime(int bitLength, Random rnd) {
    if (bitLength < 2) {
      throw new ArithmeticException("bitLength < 2");
    }
    return BigIntegerPrimes.probablePrime(bitLength, BigIntegerPrimes.kDefaultCertainty, rnd);
  }

  public BigInteger nextProbablePrime() {
    return BigIntegerPrimes.nextProbablePrime(this);
  }

  public boolean isProbablePrime(int certainty) {
    if (certainty <= 0) {
      return true;
    }
    BigInteger w = this.abs();
    if (w.equals(TWO)) {
      return true;
    }
    if (!w.testBit(0) || w.equals(ONE)) {
      return false;
    }
    return BigIntegerPrimes.primeToCertainty(w, certainty, null);
  }

  public static BigInteger valueOf(long val) {
    if (val == 0) {
      return ZERO;
    }
    if (val > 0 && val <= kMaxConstant) {
      return kPosConst[(int) val];
    } else if (val < 0 && val >= -kMaxConstant) {
      return kNegConst[(int) -val];
    }
    return new BigInteger(val);
  }

  private static int[] stripLeadingZeroBytes(byte[] a, int off, int len) {
    int indexBound = off + len;
    int keep;
    for (keep = off; keep < indexBound && a[keep] == 0; keep++) {
    }
    int intLength = ((indexBound - keep) + 3) >>> 2;
    int[] result = new int[intLength];
    int b = indexBound - 1;
    for (int i = intLength - 1; i >= 0; i--) {
      result[i] = a[b--] & 0xff;
      int bytesRemaining = b - keep + 1;
      int bytesToTransfer = Math.min(3, bytesRemaining);
      for (int j = 8; j <= (bytesToTransfer << 3); j += 8) {
        result[i] |= ((a[b--] & 0xff) << j);
      }
    }
    return result;
  }

  private static int[] makePositive(byte[] a) {
    int len = a.length;
    int words = (len + 3) >>> 2;
    int[] w = new int[words];
    for (int i = 0; i < words * 4; i++) {
      int b = i < len ? a[len - 1 - i] & 0xff : 0xff;
      w[words - 1 - (i >>> 2)] |= b << (8 * (i & 3));
    }
    for (int i = 0; i < words; i++) {
      w[i] = ~w[i];
    }
    for (int i = words - 1; i >= 0; i--) {
      w[i]++;
      if (w[i] != 0) {
        break;
      }
    }
    return BigIntegerMath.trim(w);
  }

  private static void reportOverflow() {
    throw new ArithmeticException("BigInteger would overflow supported range");
  }

  private static int bitLengthForInt(int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  public BigInteger add(BigInteger val) {
    if (val.signum == 0) {
      return this;
    }
    if (signum == 0) {
      return val;
    }
    if (val.signum == signum) {
      return new BigInteger(BigIntegerMath.add(mag, val.mag), signum);
    }
    int cmp = BigIntegerMath.compare(mag, val.mag);
    if (cmp == 0) {
      return ZERO;
    }
    int[] resultMag = (cmp > 0 ? BigIntegerMath.subtract(mag, val.mag) : BigIntegerMath.subtract(val.mag, mag));
    return new BigInteger(resultMag, cmp == signum ? 1 : -1);
  }

  public BigInteger subtract(BigInteger val) {
    if (val.signum == 0) {
      return this;
    }
    if (signum == 0) {
      return val.negate();
    }
    if (val.signum != signum) {
      return new BigInteger(BigIntegerMath.add(mag, val.mag), signum);
    }
    int cmp = BigIntegerMath.compare(mag, val.mag);
    if (cmp == 0) {
      return ZERO;
    }
    int[] resultMag = (cmp > 0 ? BigIntegerMath.subtract(mag, val.mag) : BigIntegerMath.subtract(val.mag, mag));
    return new BigInteger(resultMag, cmp == signum ? 1 : -1);
  }

  public BigInteger multiply(BigInteger val) {
    if (val.signum == 0 || signum == 0) {
      return ZERO;
    }
    return new BigInteger(BigIntegerMath.multiply(mag, val.mag), signum == val.signum ? 1 : -1);
  }

  public BigInteger parallelMultiply(BigInteger val) {
    return multiply(val);
  }

  public BigInteger divide(BigInteger val) {
    return divideAndRemainder(val)[0];
  }

  public BigInteger[] divideAndRemainder(BigInteger val) {
    if (val.signum == 0) {
      throw new ArithmeticException("BigInteger divide by zero");
    }
    if (signum == 0) {
      return new BigInteger[] {ZERO, ZERO};
    }
    int[][] qr = BigIntegerMath.divideAndRemainder(mag, val.mag);
    BigInteger q = qr[0].length == 0 ? ZERO : new BigInteger(qr[0], signum == val.signum ? 1 : -1);
    BigInteger r = qr[1].length == 0 ? ZERO : new BigInteger(qr[1], signum);
    return new BigInteger[] {q, r};
  }

  public BigInteger remainder(BigInteger val) {
    return divideAndRemainder(val)[1];
  }

  public BigInteger pow(int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("Negative exponent");
    }
    if (signum == 0) {
      return (exponent == 0 ? ONE : this);
    }
    int newSign = (signum < 0 && (exponent & 1) == 1 ? -1 : 1);
    if ((long) bitLength() * exponent / Integer.SIZE > kMaxMagLength) {
      reportOverflow();
    }
    int[] base = mag;
    int[] result = new int[] {1};
    int e = exponent;
    int[] b = base;
    while (e > 0) {
      if ((e & 1) == 1) {
        result = BigIntegerMath.multiply(result, b);
      }
      e >>>= 1;
      if (e > 0) {
        b = BigIntegerMath.multiply(b, b);
      }
    }
    return new BigInteger(result, newSign);
  }

  public BigInteger sqrt() {
    if (this.signum < 0) {
      throw new ArithmeticException("Negative BigInteger");
    }
    return new BigInteger(BigIntegerMath.sqrtMagnitude(mag), 1).orZero();
  }

  private BigInteger orZero() {
    return mag.length == 0 ? ZERO : this;
  }

  public BigInteger[] sqrtAndRemainder() {
    BigInteger s = sqrt();
    BigInteger r = this.subtract(s.multiply(s));
    return new BigInteger[] {s, r};
  }

  public BigInteger gcd(BigInteger val) {
    if (val.signum == 0) {
      return this.abs();
    } else if (this.signum == 0) {
      return val.abs();
    }
    BigInteger a = this.abs();
    BigInteger b = val.abs();
    while (b.signum != 0) {
      BigInteger t = a.remainder(b);
      a = b;
      b = t;
    }
    return a;
  }

  public BigInteger mod(BigInteger m) {
    if (m.signum <= 0) {
      throw new ArithmeticException("BigInteger: modulus not positive");
    }
    BigInteger result = this.remainder(m);
    return (result.signum >= 0 ? result : result.add(m));
  }

  public BigInteger modPow(BigInteger exponent, BigInteger m) {
    if (m.signum <= 0) {
      throw new ArithmeticException("BigInteger: modulus not positive");
    }
    if (exponent.signum == 0) {
      return (m.equals(ONE) ? ZERO : ONE);
    }
    if (this.equals(ONE)) {
      return (m.equals(ONE) ? ZERO : ONE);
    }
    if (this.equals(ZERO) && exponent.signum >= 0) {
      return ZERO;
    }
    if (this.equals(kNegConst[1]) && (!exponent.testBit(0))) {
      return (m.equals(ONE) ? ZERO : ONE);
    }
    boolean invertResult = exponent.signum < 0;
    if (invertResult) {
      exponent = exponent.negate();
    }
    BigInteger base = (this.signum < 0 || BigIntegerMath.compare(mag, m.mag) >= 0 ? this.mod(m) : this);
    BigInteger result = ONE;
    int bits = exponent.bitLength();
    if (m.equals(ONE)) {
      return ZERO;
    }
    for (int i = bits - 1; i >= 0; i--) {
      result = result.multiply(result).remainder(m);
      if (exponent.testBit(i)) {
        result = result.multiply(base).remainder(m);
      }
    }
    return (invertResult ? result.modInverse(m) : result);
  }

  public BigInteger modInverse(BigInteger m) {
    if (m.signum != 1) {
      throw new ArithmeticException("BigInteger: modulus not positive");
    }
    if (m.equals(ONE)) {
      return ZERO;
    }
    BigInteger modVal = this;
    if (signum < 0 || (BigIntegerMath.compare(mag, m.mag) >= 0)) {
      modVal = this.mod(m);
    }
    if (modVal.equals(ONE)) {
      return ONE;
    }
    BigInteger a = m;
    BigInteger b = modVal;
    BigInteger x0 = ZERO;
    BigInteger x1 = ONE;
    while (b.signum != 0) {
      BigInteger[] qr = a.divideAndRemainder(b);
      a = b;
      b = qr[1];
      BigInteger t = x0.subtract(qr[0].multiply(x1));
      x0 = x1;
      x1 = t;
    }
    if (!a.equals(ONE)) {
      throw new ArithmeticException("BigInteger not invertible.");
    }
    return x0.signum < 0 ? x0.add(m) : x0;
  }

  public BigInteger shiftLeft(int n) {
    if (signum == 0) {
      return ZERO;
    }
    if (n > 0) {
      if (n > (long) kMaxMagLength * Integer.SIZE - bitLength()) {
        reportOverflow();
      }
      return new BigInteger(BigIntegerMath.shiftLeft(mag, n), signum);
    } else if (n == 0) {
      return this;
    } else {
      return shiftRightImpl(-n);
    }
  }

  public BigInteger shiftRight(int n) {
    if (signum == 0) {
      return ZERO;
    }
    if (n > 0) {
      return shiftRightImpl(n);
    } else if (n == 0) {
      return this;
    } else {
      if (n == Integer.MIN_VALUE) {
        reportOverflow();
      }
      return shiftLeft(-n);
    }
  }

  private BigInteger shiftRightImpl(int n) {
    int[] q = BigIntegerMath.shiftRight(mag, n);
    if (signum < 0 && BigIntegerMath.lowBitsNonZero(mag, n)) {
      q = BigIntegerMath.addWord(q, 1);
    }
    return q.length == 0 ? ZERO : new BigInteger(q, signum);
  }

  private int getInt(int n) {
    if (n < 0) {
      return 0;
    }
    if (n >= mag.length) {
      return signum < 0 ? -1 : 0;
    }
    int magInt = mag[mag.length - n - 1];
    return (signum >= 0 ? magInt : (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
  }

  private int firstNonzeroIntNum() {
    int i;
    int mlen = mag.length;
    for (i = mlen - 1; i >= 0 && mag[i] == 0; i--) {
    }
    return mlen - i - 1;
  }

  private int intLength() {
    return (bitLength() >>> 5) + 1;
  }

  private static BigInteger fromTwosComplement(int[] val) {
    if (val.length == 0) {
      return ZERO;
    }
    if (val[0] >= 0) {
      return of(val, 1);
    }
    int[] w = val.clone();
    for (int i = 0; i < w.length; i++) {
      w[i] = ~w[i];
    }
    for (int i = w.length - 1; i >= 0; i--) {
      w[i]++;
      if (w[i] != 0) {
        break;
      }
    }
    return of(w, -1);
  }

  private BigInteger bitOp(BigInteger val, int op) {
    int len = Math.max(intLength(), val.intLength()) + 1;
    int[] result = new int[len];
    for (int i = 0; i < len; i++) {
      int a = getInt(len - i - 1);
      int b = val.getInt(len - i - 1);
      switch (op) {
        case 0:
          result[i] = a & b;
          break;
        case 1:
          result[i] = a | b;
          break;
        case 2:
          result[i] = a ^ b;
          break;
        default:
          result[i] = a & ~b;
          break;
      }
    }
    return fromTwosComplement(result);
  }

  public BigInteger and(BigInteger val) {
    return bitOp(val, 0);
  }

  public BigInteger or(BigInteger val) {
    return bitOp(val, 1);
  }

  public BigInteger xor(BigInteger val) {
    return bitOp(val, 2);
  }

  public BigInteger not() {
    int[] result = new int[intLength()];
    for (int i = 0; i < result.length; i++) {
      result[i] = ~getInt(result.length - i - 1);
    }
    return fromTwosComplement(result);
  }

  public BigInteger andNot(BigInteger val) {
    return bitOp(val, 3);
  }

  public boolean testBit(int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
  }

  private BigInteger changeBit(int n, int mode) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    int intNum = n >>> 5;
    if (mode == 1 && !testBit(n)) {
      return this;
    }
    int length = Math.max(intLength(), intNum + 2);
    if (length > (1 << 26)) {
      throw new ArithmeticException("BigInteger would overflow supported range");
    }
    int[] result = new int[length];
    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i);
    }
    int idx = result.length - intNum - 1;
    int bit = 1 << (n & 31);
    if (mode == 0) {
      result[idx] |= bit;
    } else if (mode == 1) {
      result[idx] &= ~bit;
    } else {
      result[idx] ^= bit;
    }
    return fromTwosComplement(result);
  }

  public BigInteger setBit(int n) {
    return changeBit(n, 0);
  }

  public BigInteger clearBit(int n) {
    return changeBit(n, 1);
  }

  public BigInteger flipBit(int n) {
    return changeBit(n, 2);
  }

  public int getLowestSetBit() {
    if (signum == 0) {
      return -1;
    }
    int b = mag.length - 1;
    while (mag[b] == 0) {
      b--;
    }
    return 32 * (mag.length - 1 - b) + Integer.numberOfTrailingZeros(mag[b]);
  }

  public int bitLength() {
    if (mag.length == 0) {
      return 0;
    }
    int magBitLength = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]);
    if (signum < 0) {
      boolean pow2 = (Integer.bitCount(mag[0]) == 1);
      for (int i = 1; i < mag.length && pow2; i++) {
        pow2 = (mag[i] == 0);
      }
      return pow2 ? magBitLength - 1 : magBitLength;
    }
    return magBitLength;
  }

  public int bitCount() {
    int bc = 0;
    for (int i = 0; i < mag.length; i++) {
      bc += Integer.bitCount(mag[i]);
    }
    if (signum < 0) {
      int magTrailingZeroCount = 0;
      int j;
      for (j = mag.length - 1; mag[j] == 0; j--) {
        magTrailingZeroCount += 32;
      }
      magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j]);
      bc += magTrailingZeroCount - 1;
    }
    return bc;
  }

  public int signum() {
    return this.signum;
  }

  public BigInteger negate() {
    return signum == 0 ? this : new BigInteger(mag, -signum);
  }

  public BigInteger abs() {
    return (signum >= 0 ? this : this.negate());
  }

  int compareMagnitudeInternal(BigInteger val) {
    return BigIntegerMath.compare(mag, val.mag);
  }

  public int compareTo(BigInteger val) {
    if (signum == val.signum) {
      switch (signum) {
        case 1:
          return BigIntegerMath.compare(mag, val.mag);
        case -1:
          return BigIntegerMath.compare(val.mag, mag);
        default:
          return 0;
      }
    }
    return signum > val.signum ? 1 : -1;
  }

  public boolean equals(Object x) {
    if (x == this) {
      return true;
    }
    if (!(x instanceof BigInteger)) {
      return false;
    }
    BigInteger xInt = (BigInteger) x;
    if (xInt.signum != signum) {
      return false;
    }
    return Arrays.equals(mag, xInt.mag);
  }

  public BigInteger min(BigInteger val) {
    return (compareTo(val) < 0 ? this : val);
  }

  public BigInteger max(BigInteger val) {
    return (compareTo(val) > 0 ? this : val);
  }

  public int hashCode() {
    int hashCode = 0;
    for (int i = 0; i < mag.length; i++) {
      hashCode = (int) (31 * hashCode + (mag[i] & kMask));
    }
    return hashCode * signum;
  }

  private static final char[] kDigits = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

  public String toString(int radix) {
    if (signum == 0) {
      return "0";
    }
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      radix = 10;
    }
    int groupDigits = kDigitsPerInt[radix];
    long groupRadix = kIntRadix[radix] & kMask;
    int[] work = mag.clone();
    int len = work.length;
    StringBuilder sb = new StringBuilder();
    java.util.ArrayList<String> groups = new java.util.ArrayList<String>();
    while (len > 0) {
      long rem = 0;
      for (int i = 0; i < len; i++) {
        long cur = (rem << 32) | (work[i] & kMask);
        work[i] = (int) Long.divideUnsigned(cur, groupRadix);
        rem = Long.remainderUnsigned(cur, groupRadix);
      }
      int start = 0;
      while (start < len && work[start] == 0) {
        start++;
      }
      if (start > 0) {
        System.arraycopy(work, start, work, 0, len - start);
        len -= start;
      }
      groups.add(Long.toString(rem, radix));
    }
    if (signum < 0) {
      sb.append('-');
    }
    sb.append(groups.get(groups.size() - 1));
    for (int i = groups.size() - 2; i >= 0; i--) {
      String g = groups.get(i);
      for (int pad = g.length(); pad < groupDigits; pad++) {
        sb.append('0');
      }
      sb.append(g);
    }
    return sb.toString();
  }

  public String toString() {
    return toString(10);
  }

  public byte[] toByteArray() {
    int byteLen = bitLength() / 8 + 1;
    byte[] byteArray = new byte[byteLen];
    for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
      if (bytesCopied == 4) {
        nextInt = getInt(intIndex++);
        bytesCopied = 1;
      } else {
        nextInt >>>= 8;
        bytesCopied++;
      }
      byteArray[i] = (byte) nextInt;
    }
    return byteArray;
  }

  public int intValue() {
    return getInt(0);
  }

  public long longValue() {
    long result = 0;
    for (int i = 1; i >= 0; i--) {
      result = (result << 32) + (getInt(i) & kMask);
    }
    return result;
  }

  private double scaledMagnitude(int significantBits) {
    int bl = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]);
    int drop = bl - (significantBits + 1);
    long top = BigIntegerMath.shiftRight(mag, drop).length == 0 ? 0 : new BigInteger(BigIntegerMath.shiftRight(mag,
        drop), 1).longValue();
    boolean sticky = BigIntegerMath.lowBitsNonZero(mag, drop);
    long m = top >>> 1;
    boolean roundBit = (top & 1) != 0;
    if (roundBit && (sticky || (m & 1) != 0)) {
      m++;
    }
    return (double) m;
  }

  public float floatValue() {
    if (signum == 0) {
      return 0.0f;
    }
    int bl = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]);
    if (bl <= 63) {
      return (float) longValue();
    }
    if (bl > Float.MAX_EXPONENT + 1) {
      return signum > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
    }
    float v = Math.scalb((float) scaledMagnitude(24), bl - 24);
    return signum > 0 ? v : -v;
  }

  public double doubleValue() {
    if (signum == 0) {
      return 0.0;
    }
    int bl = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]);
    if (bl <= 63) {
      return (double) longValue();
    }
    if (bl > Double.MAX_EXPONENT + 1) {
      return signum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    }
    double v = Math.scalb(scaledMagnitude(53), bl - 53);
    return signum > 0 ? v : -v;
  }

  public long longValueExact() {
    if (mag.length <= 2 && bitLength() <= 63) {
      return longValue();
    }
    throw new ArithmeticException("BigInteger out of long range");
  }

  public int intValueExact() {
    if (mag.length <= 1 && bitLength() <= 31) {
      return intValue();
    }
    throw new ArithmeticException("BigInteger out of int range");
  }

  public short shortValueExact() {
    if (mag.length <= 1 && bitLength() <= 31) {
      int value = intValue();
      if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
        return shortValue();
      }
    }
    throw new ArithmeticException("BigInteger out of short range");
  }

  public byte byteValueExact() {
    if (mag.length <= 1 && bitLength() <= 31) {
      int value = intValue();
      if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
        return byteValue();
      }
    }
    throw new ArithmeticException("BigInteger out of byte range");
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    ObjectOutputStream.PutField fields = s.putFields();
    fields.put("signum", signum);
    fields.put("magnitude", magSerializedForm());
    fields.put("bitCount", -1);
    fields.put("bitLength", -1);
    fields.put("lowestSetBit", -2);
    fields.put("firstNonzeroByteNum", -2);
    s.writeFields();
  }

  private byte[] magSerializedForm() {
    int len = mag.length;
    int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
    int byteLen = (bitLen + 7) >>> 3;
    byte[] result = new byte[byteLen];
    for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0; i >= 0; i--) {
      if (bytesCopied == 4) {
        nextInt = mag[intIndex--];
        bytesCopied = 1;
      } else {
        nextInt >>>= 8;
        bytesCopied++;
      }
      result[i] = (byte) nextInt;
    }
    return result;
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField fields = s.readFields();
    int sign = fields.get("signum", -2);
    byte[] magnitude = (byte[]) fields.get("magnitude", null);
    if (sign < -1 || sign > 1) {
      String message = "BigInteger: Invalid signum value";
      if (fields.defaulted("signum")) {
        message = "BigInteger: Signum not present in stream";
      }
      throw new StreamCorruptedException(message);
    }
    int[] m = stripLeadingZeroBytes(magnitude, 0, magnitude.length);
    if ((m.length == 0) != (sign == 0)) {
      String message = "BigInteger: signum-magnitude mismatch";
      if (fields.defaulted("magnitude")) {
        message = "BigInteger: Magnitude not present in stream";
      }
      throw new StreamCorruptedException(message);
    }
    this.signum = sign;
    this.mag = m;
    if (m.length >= kMaxMagLength) {
      throw new StreamCorruptedException("BigInteger: Out of the supported range");
    }
  }
}
