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

import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

public class BigInteger extends Number implements Serializable, Comparable<BigInteger> {
  private static final long serialVersionUID = -8287574255936472291L;

  private final int sign;
  private final int[] value;

  public static final BigInteger ZERO = new BigInteger(0, new int[0]);
  public static final BigInteger ONE = new BigInteger(1, new int[] {1});
  public static final BigInteger TWO = new BigInteger(1, new int[] {2});
  public static final BigInteger TEN = new BigInteger(1, new int[] {10});

  private BigInteger(int sign, int[] value) {
    this.sign = sign;
    this.value = value;
  }

  public BigInteger(String val, int radix) {
    BigInteger parsed = parseString(val, radix);
    this.sign = parsed.sign;
    this.value = parsed.value;
  }

  public BigInteger(String val) {
    this(val, 10);
  }

  public BigInteger(byte[] val) {
    BigInteger parsed = parseByteArray(val);
    this.sign = parsed.sign;
    this.value = parsed.value;
  }

  public BigInteger(int signum, byte[] magnitude) {
    BigInteger parsed = parseSignMagnitude(signum, magnitude);
    this.sign = parsed.sign;
    this.value = parsed.value;
  }

  public BigInteger(int numBits, Random rnd) {
    if (numBits < 0) {
      throw new IllegalArgumentException("numBits must be non-negative");
    }
    if (numBits == 0) {
      this.sign = 0;
      this.value = new int[0];
    } else {
      int numWords = (numBits + 31) >>> 5;
      int[] mag = new int[numWords];
      for (int i = 0; i < numWords; i++) {
        mag[i] = rnd.nextInt();
      }
      int excess = (numWords << 5) - numBits;
      if (excess > 0) {
        mag[numWords - 1] >>>= excess;
      }
      BigInteger b = make(1, mag);
      this.sign = b.sign;
      this.value = b.value;
    }
  }

  private static BigInteger make(int sign, int[] raw) {
    int len = raw.length;
    while (len > 0 && raw[len - 1] == 0) {
      len--;
    }
    if (len == 0 || sign == 0) {
      return ZERO;
    }
    int[] trimmed = new int[len];
    System.arraycopy(raw, 0, trimmed, 0, len);
    return new BigInteger(sign, trimmed);
  }

  private static BigInteger parseString(String val, int radix) {
    Objects.requireNonNull(val, "val");
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      throw new NumberFormatException("Radix out of range: " + radix);
    }
    int len = val.length();
    if (len == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    int cursor = 0;
    int signum = 1;
    char firstChar = val.charAt(0);
    if (firstChar == '-') {
      signum = -1;
      cursor = 1;
    } else if (firstChar == '+') {
      cursor = 1;
    }
    if (cursor == len) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    while (cursor < len && val.charAt(cursor) == '0') {
      cursor++;
    }
    if (cursor == len) {
      return ZERO;
    }

    BigInteger result = ZERO;
    BigInteger r = valueOf(radix);
    for (int i = cursor; i < len; i++) {
      char c = val.charAt(i);
      int digit = Character.digit(c, radix);
      if (digit < 0) {
        throw new NumberFormatException("Illegal character at " + i + ": " + c);
      }
      result = result.multiply(r).add(valueOf(digit));
    }
    return signum < 0 ? result.negate() : result;
  }

  private static BigInteger parseSignMagnitude(int signum, byte[] magnitude) {
    if (signum < -1 || signum > 1) {
      throw new NumberFormatException("Invalid signum value");
    }
    Objects.requireNonNull(magnitude, "magnitude");
    int byteLen = magnitude.length;
    int firstNonZero = 0;
    while (firstNonZero < byteLen && magnitude[firstNonZero] == 0) {
      firstNonZero++;
    }
    if (firstNonZero == byteLen) {
      if (signum != 0 && byteLen > 0) {
        // Java accepts signum != 0 when byte array is all zeros by normalizing to ZERO
        return ZERO;
      }
      return ZERO;
    }
    if (signum == 0) {
      throw new NumberFormatException("signum is zero with non-zero magnitude");
    }
    int activeBytes = byteLen - firstNonZero;
    int numWords = (activeBytes + 3) / 4;
    int[] raw = new int[numWords];
    for (int i = 0; i < activeBytes; i++) {
      int b = magnitude[byteLen - 1 - i] & 0xFF;
      raw[i / 4] |= (b << ((i % 4) * 8));
    }
    return make(signum, raw);
  }

  private static BigInteger parseByteArray(byte[] val) {
    Objects.requireNonNull(val, "val");
    if (val.length == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    if ((val[0] & 0x80) == 0) {
      return parseSignMagnitude(1, val);
    }
    byte[] inverted = new byte[val.length];
    for (int i = 0; i < val.length; i++) {
      inverted[i] = (byte) (~val[i]);
    }
    int carry = 1;
    for (int i = inverted.length - 1; i >= 0 && carry != 0; i--) {
      int sum = (inverted[i] & 0xFF) + carry;
      inverted[i] = (byte) sum;
      carry = sum >>> 8;
    }
    if (carry != 0) {
      byte[] expanded = new byte[inverted.length + 1];
      expanded[0] = 1;
      System.arraycopy(inverted, 0, expanded, 1, inverted.length);
      inverted = expanded;
    }
    BigInteger pos = parseSignMagnitude(1, inverted);
    return pos.negate();
  }

  private static int compareMagnitude(int[] a, int[] b) {
    if (a.length != b.length) {
      return a.length > b.length ? 1 : -1;
    }
    for (int i = a.length - 1; i >= 0; i--) {
      long av = a[i] & 0xFFFFFFFFL;
      long bv = b[i] & 0xFFFFFFFFL;
      if (av != bv) {
        return av > bv ? 1 : -1;
      }
    }
    return 0;
  }

  public static BigInteger valueOf(long num) {
    int signum = Long.signum(num);
    if (signum == 0) {
      return ZERO;
    }
    long mag = (signum > 0) ? num : -num;
    int upper = (int) (mag >>> 32);
    int lower = (int) mag;
    if (upper == 0) {
      return new BigInteger(signum, new int[] {lower});
    } else {
      return new BigInteger(signum, new int[] {lower, upper});
    }
  }

  public int signum() {
    return sign;
  }

  public BigInteger negate() {
    return sign == 0 ? this : new BigInteger(-sign, value);
  }

  public BigInteger abs() {
    return sign >= 0 ? this : negate();
  }

  public BigInteger shiftLeft(int n) {
    if (sign == 0 || n == 0) {
      return this;
    }
    if (n < 0) {
      return shiftRight(-n);
    }
    int wordShift = n >>> 5;
    int bitShift = n & 31;
    int len = value.length;
    int newLen = len + wordShift + (bitShift != 0 ? 1 : 0);
    int[] newVal = new int[newLen];
    if (bitShift == 0) {
      System.arraycopy(value, 0, newVal, wordShift, len);
    } else {
      int carry = 0;
      for (int i = 0; i < len; i++) {
        int v = value[i];
        newVal[i + wordShift] = (v << bitShift) | carry;
        carry = v >>> (32 - bitShift);
      }
      if (carry != 0) {
        newVal[len + wordShift] = carry;
      }
    }
    return make(sign, newVal);
  }

  public BigInteger shiftRight(int n) {
    if (sign == 0 || n == 0) {
      return this;
    }
    if (n < 0) {
      return shiftLeft(-n);
    }
    int wordShift = n >>> 5;
    int bitShift = n & 31;
    if (wordShift >= value.length) {
      return sign > 0 ? ZERO : valueOf(-1);
    }
    int newLen = value.length - wordShift;
    int[] newVal = new int[newLen];
    if (bitShift == 0) {
      System.arraycopy(value, wordShift, newVal, 0, newLen);
    } else {
      for (int i = 0; i < newLen - 1; i++) {
        newVal[i] = (value[i + wordShift] >>> bitShift) | (value[i + wordShift + 1] << (32 - bitShift));
      }
      newVal[newLen - 1] = value[value.length - 1] >>> bitShift;
    }
    return make(sign, newVal);
  }

  public BigInteger add(BigInteger val) {
    if (val.sign == 0) {
      return this;
    }
    if (this.sign == 0) {
      return val;
    }
    if (this.sign != val.sign) {
      return this.subtract(val.negate());
    }
    int maxLen = Math.max(value.length, val.value.length);
    int[] res = new int[maxLen + 1];
    long carry = 0;
    for (int i = 0; i < maxLen; i++) {
      long av = i < value.length ? (value[i] & 0xFFFFFFFFL) : 0L;
      long bv = i < val.value.length ? (val.value[i] & 0xFFFFFFFFL) : 0L;
      long sum = av + bv + carry;
      res[i] = (int) sum;
      carry = sum >>> 32;
    }
    res[maxLen] = (int) carry;
    return make(sign, res);
  }

  public BigInteger subtract(BigInteger val) {
    if (val.sign == 0) {
      return this;
    }
    if (this.sign == 0) {
      return val.negate();
    }
    if (this.sign != val.sign) {
      return this.add(val.negate());
    }
    int cmp = compareMagnitude(this.value, val.value);
    if (cmp == 0) {
      return ZERO;
    }
    int[] big = (cmp > 0) ? this.value : val.value;
    int[] small = (cmp > 0) ? val.value : this.value;
    int resultSign = (cmp > 0) ? this.sign : -this.sign;

    int[] res = new int[big.length];
    long borrow = 0;
    for (int i = 0; i < big.length; i++) {
      long av = big[i] & 0xFFFFFFFFL;
      long bv = (i < small.length ? (small[i] & 0xFFFFFFFFL) : 0L);
      long diff = av - bv - borrow;
      if (diff < 0) {
        diff += 0x100000000L;
        borrow = 1;
      } else {
        borrow = 0;
      }
      res[i] = (int) diff;
    }
    return make(resultSign, res);
  }

  public BigInteger multiply(BigInteger val) {
    if (this.sign == 0 || val.sign == 0) {
      return ZERO;
    }
    if (this.equals(ONE)) {
      return val;
    }
    if (val.equals(ONE)) {
      return this;
    }
    int resSign = this.sign * val.sign;
    int aLen = this.value.length;
    int bLen = val.value.length;
    long[] prod = new long[aLen + bLen];
    for (int i = 0; i < aLen; i++) {
      long aWord = this.value[i] & 0xFFFFFFFFL;
      long carry = 0;
      for (int j = 0; j < bLen; j++) {
        long bWord = val.value[j] & 0xFFFFFFFFL;
        long sum = aWord * bWord + prod[i + j] + carry;
        prod[i + j] = sum & 0xFFFFFFFFL;
        carry = sum >>> 32;
      }
      prod[i + bLen] += carry;
    }
    int[] res = new int[prod.length];
    for (int i = 0; i < prod.length; i++) {
      res[i] = (int) prod[i];
    }
    return make(resSign, res);
  }

  public BigInteger[] divideAndRemainder(BigInteger val) {
    if (val.sign == 0) {
      throw new ArithmeticException("BigInteger divide by zero");
    }
    if (this.sign == 0) {
      return new BigInteger[] {ZERO, ZERO};
    }
    int cmp = compareMagnitude(this.value, val.value);
    if (cmp < 0) {
      return new BigInteger[] {ZERO, this};
    }
    if (cmp == 0) {
      int qSign = this.sign * val.sign;
      return new BigInteger[] {qSign > 0 ? ONE : ONE.negate(), ZERO};
    }

    BigInteger u = this.abs();
    BigInteger v = val.abs();
    int shift = u.bitLength() - v.bitLength();
    BigInteger shiftedV = v.shiftLeft(shift);
    BigInteger rem = u;
    BigInteger q = ZERO;

    for (int i = shift; i >= 0; i--) {
      if (rem.compareTo(shiftedV) >= 0) {
        rem = rem.subtract(shiftedV);
        q = q.setBit(i);
      }
      shiftedV = shiftedV.shiftRight(1);
    }

    int qSign = this.sign * val.sign;
    int rSign = this.sign;
    BigInteger quotient = (qSign < 0) ? q.negate() : q;
    BigInteger remainder = (rem.signum() == 0 || rSign > 0) ? rem : rem.negate();
    return new BigInteger[] {quotient, remainder};
  }

  public BigInteger divide(BigInteger val) {
    return divideAndRemainder(val)[0];
  }

  public BigInteger remainder(BigInteger val) {
    return divideAndRemainder(val)[1];
  }

  public BigInteger pow(int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("Negative exponent");
    }
    if (exponent == 0) {
      return ONE;
    }
    if (sign == 0) {
      return ZERO;
    }
    BigInteger base = this;
    BigInteger result = ONE;
    int exp = exponent;
    while (exp > 0) {
      if ((exp & 1) != 0) {
        result = result.multiply(base);
      }
      exp >>>= 1;
      if (exp > 0) {
        base = base.multiply(base);
      }
    }
    return result;
  }

  public BigInteger gcd(BigInteger val) {
    BigInteger a = this.abs();
    BigInteger b = val.abs();
    while (b.signum() != 0) {
      BigInteger rem = a.remainder(b);
      a = b;
      b = rem;
    }
    return a;
  }

  public BigInteger mod(BigInteger m) {
    if (m.signum() <= 0) {
      throw new ArithmeticException("BigInteger: modulus not positive");
    }
    BigInteger rem = this.remainder(m);
    return rem.signum() < 0 ? rem.add(m) : rem;
  }

  public BigInteger modPow(BigInteger exponent, BigInteger m) {
    if (m.signum() <= 0) {
      throw new ArithmeticException("BigInteger: modulus not positive");
    }
    if (exponent.signum() == 0) {
      return ONE.mod(m);
    }
    BigInteger base = this.mod(m);
    BigInteger result = ONE;
    int bits = exponent.bitLength();
    for (int i = 0; i < bits; i++) {
      if (exponent.testBit(i)) {
        result = result.multiply(base).mod(m);
      }
      base = base.multiply(base).mod(m);
    }
    return result;
  }

  public boolean testBit(int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    if (sign >= 0) {
      int word = n >>> 5;
      if (word >= value.length) {
        return false;
      }
      return (value[word] & (1 << (n & 31))) != 0;
    } else {
      BigInteger posSub1 = this.negate().subtract(ONE);
      int word = n >>> 5;
      if (word >= posSub1.value.length) {
        return true;
      }
      return (posSub1.value[word] & (1 << (n & 31))) == 0;
    }
  }

  public BigInteger setBit(int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return this.or(ONE.shiftLeft(n));
  }

  public BigInteger clearBit(int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return this.andNot(ONE.shiftLeft(n));
  }

  public BigInteger flipBit(int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return this.xor(ONE.shiftLeft(n));
  }

  public int getLowestSetBit() {
    if (sign == 0) {
      return -1;
    }
    int bits = bitLength() + 1;
    for (int i = 0; i <= bits; i++) {
      if (testBit(i)) {
        return i;
      }
    }
    return -1;
  }

  public int bitLength() {
    if (sign == 0 || value.length == 0) {
      return 0;
    }
    if (sign > 0) {
      int highest = value[value.length - 1];
      int bitsInHighest = 32 - Integer.numberOfLeadingZeros(highest);
      return (value.length - 1) * 32 + bitsInHighest;
    } else {
      return this.negate().subtract(ONE).bitLength();
    }
  }

  public int bitCount() {
    if (sign == 0) {
      return 0;
    }
    if (sign > 0) {
      int count = 0;
      for (int v : value) {
        count += Integer.bitCount(v);
      }
      return count;
    } else {
      return this.negate().subtract(ONE).bitCount();
    }
  }

  public byte[] toByteArray() {
    if (sign == 0) {
      return new byte[] {0};
    }
    int nBytes = (bitLength() + 8) / 8;
    byte[] bytes = new byte[nBytes];
    for (int k = 0; k < nBytes; k++) {
      int b = 0;
      int bitOffset = (nBytes - 1 - k) * 8;
      for (int j = 0; j < 8; j++) {
        if (testBit(bitOffset + j)) {
          b |= (1 << j);
        }
      }
      bytes[k] = (byte) b;
    }
    return bytes;
  }

  public BigInteger and(BigInteger val) {
    byte[] a = this.toByteArray();
    byte[] b = val.toByteArray();
    int len = Math.max(a.length, b.length);
    byte[] res = new byte[len];
    for (int i = 0; i < len; i++) {
      byte b1 = i < a.length ? a[a.length - 1 - i] : (this.sign < 0 ? (byte) -1 : 0);
      byte b2 = i < b.length ? b[b.length - 1 - i] : (val.sign < 0 ? (byte) -1 : 0);
      res[len - 1 - i] = (byte) (b1 & b2);
    }
    return new BigInteger(res);
  }

  public BigInteger or(BigInteger val) {
    byte[] a = this.toByteArray();
    byte[] b = val.toByteArray();
    int len = Math.max(a.length, b.length);
    byte[] res = new byte[len];
    for (int i = 0; i < len; i++) {
      byte b1 = i < a.length ? a[a.length - 1 - i] : (this.sign < 0 ? (byte) -1 : 0);
      byte b2 = i < b.length ? b[b.length - 1 - i] : (val.sign < 0 ? (byte) -1 : 0);
      res[len - 1 - i] = (byte) (b1 | b2);
    }
    return new BigInteger(res);
  }

  public BigInteger xor(BigInteger val) {
    byte[] a = this.toByteArray();
    byte[] b = val.toByteArray();
    int len = Math.max(a.length, b.length);
    byte[] res = new byte[len];
    for (int i = 0; i < len; i++) {
      byte b1 = i < a.length ? a[a.length - 1 - i] : (this.sign < 0 ? (byte) -1 : 0);
      byte b2 = i < b.length ? b[b.length - 1 - i] : (val.sign < 0 ? (byte) -1 : 0);
      res[len - 1 - i] = (byte) (b1 ^ b2);
    }
    return new BigInteger(res);
  }

  public BigInteger not() {
    return this.negate().subtract(ONE);
  }

  public BigInteger andNot(BigInteger val) {
    return this.and(val.not());
  }

  public BigInteger min(BigInteger val) {
    return compareTo(val) <= 0 ? this : val;
  }

  public BigInteger max(BigInteger val) {
    return compareTo(val) >= 0 ? this : val;
  }

  public BigInteger sqrt() {
    if (this.sign < 0) {
      throw new ArithmeticException("Negative BigInteger");
    }
    if (this.sign == 0) {
      return ZERO;
    }
    BigInteger low = ZERO;
    BigInteger high = ONE.shiftLeft((bitLength() + 1) / 2);
    while (low.compareTo(high) <= 0) {
      BigInteger mid = low.add(high).shiftRight(1);
      BigInteger sq = mid.multiply(mid);
      int cmp = sq.compareTo(this);
      if (cmp == 0) {
        return mid;
      }
      if (cmp < 0) {
        low = mid.add(ONE);
      } else {
        high = mid.subtract(ONE);
      }
    }
    return high;
  }

  public BigInteger[] sqrtAndRemainder() {
    BigInteger s = sqrt();
    return new BigInteger[] {s, this.subtract(s.multiply(s))};
  }

  @Override
  public int compareTo(BigInteger val) {
    if (this == val) {
      return 0;
    }
    if (this.sign != val.sign) {
      return this.sign > val.sign ? 1 : -1;
    }
    if (this.sign == 0) {
      return 0;
    }
    int cmp = compareMagnitude(this.value, val.value);
    return this.sign > 0 ? cmp : -cmp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BigInteger)) {
      return false;
    }
    BigInteger other = (BigInteger) o;
    if (this.sign != other.sign || this.value.length != other.value.length) {
      return false;
    }
    for (int i = 0; i < this.value.length; i++) {
      if (this.value[i] != other.value[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = sign;
    for (int v : value) {
      h = 31 * h + v;
    }
    return h;
  }

  @Override
  public long longValue() {
    if (sign == 0 || value.length == 0) {
      return 0L;
    }
    long v = value[0] & 0xFFFFFFFFL;
    if (value.length > 1) {
      v |= ((long) value[1]) << 32;
    }
    return sign > 0 ? v : -v;
  }

  @Override
  public int intValue() {
    return (int) longValue();
  }

  @Override
  public float floatValue() {
    return (float) doubleValue();
  }

  @Override
  public double doubleValue() {
    if (sign == 0) {
      return 0.0;
    }
    int len = bitLength();
    if (len > 1024) {
      return sign > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    }
    return Double.parseDouble(this.toString());
  }

  @Override
  public byte byteValue() {
    return (byte) intValue();
  }

  @Override
  public short shortValue() {
    return (short) intValue();
  }

  @Override
  public String toString() {
    return toString(10);
  }

  public String toString(int radix) {
    if (sign == 0 || value.length == 0) {
      return "0";
    }
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      radix = 10;
    }
    int[] temp = new int[value.length];
    System.arraycopy(value, 0, temp, 0, value.length);
    int tempLen = temp.length;

    StringBuilder sb = new StringBuilder();
    while (tempLen > 0) {
      long rem = 0;
      for (int i = tempLen - 1; i >= 0; i--) {
        long cur = (rem << 32) | (temp[i] & 0xFFFFFFFFL);
        long q = cur / radix;
        rem = cur % radix;
        temp[i] = (int) q;
      }
      while (tempLen > 0 && temp[tempLen - 1] == 0) {
        tempLen--;
      }
      sb.append(Character.forDigit((int) rem, radix));
    }
    if (sign < 0) {
      sb.append('-');
    }
    sb.reverse();
    return sb.toString();
  }
}
