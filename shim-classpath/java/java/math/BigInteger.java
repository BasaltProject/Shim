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

public class BigInteger implements Serializable, Comparable<BigInteger> {

  private final int   sign;
  private final int[] value;

  public static final BigInteger ZERO = new BigInteger(0, new int[0]);
  public static final BigInteger ONE  = new BigInteger(1, new int[] { 1 });
  public static final BigInteger TWO  = new BigInteger(1, new int[] { 2 });
  public static final BigInteger TEN  = new BigInteger(1, new int[] { 10 });

  private BigInteger(int sign, int[] value) {
    this.sign = sign;
    this.value = value;
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
      return new BigInteger(signum, new int[] { lower });
    } else {
      return new BigInteger(signum, new int[] { lower, upper });
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

  public int intValue() {
    return (int) longValue();
  }

  @Override
  public String toString() {
    return toString(10);
  }

  public String toString(int radix) {
    if (sign == 0 || value.length == 0) {
      return "0";
    }
    if (radix < 2 || radix > 36) {
      radix = 10;
    }
    StringBuilder sb = new StringBuilder();
    BigInteger current = abs();
    BigInteger rad = valueOf(radix);
    while (current.signum() != 0) {
      long rem = current.divideAndRemainderSmall(radix, current);
      sb.append(Character.forDigit((int) rem, radix));
    }
    if (sign < 0) {
      sb.append('-');
    }
    char[] buf = new char[sb.length()];
    for (int i = 0; i < buf.length; i++) {
      buf[i] = sb.charAt(buf.length - 1 - i);
    }
    return new String(buf);
  }

  private long divideAndRemainderSmall(int divisor, BigInteger quotientOut) {
    long rem = 0;
    for (int i = value.length - 1; i >= 0; i--) {
      long cur = (rem << 32) | (value[i] & 0xFFFFFFFFL);
      long q = cur / divisor;
      rem = cur % divisor;
      value[i] = (int) q;
    }
    int len = value.length;
    while (len > 0 && value[len - 1] == 0) {
      len--;
    }
    return rem;
  }
}

