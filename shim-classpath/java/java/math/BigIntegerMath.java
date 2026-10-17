/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.math;

import java.util.Arrays;

final class BigIntegerMath {
  static final long kMask = 0xffffffffL;
  static final int[] kEmpty = new int[0];
  private static final int kKaratsubaThreshold = 40;

  private BigIntegerMath() { }

  static int[] trim(int[] a) {
    int start = 0;
    while (start < a.length && a[start] == 0) {
      start++;
    }
    return start == 0 ? a : (start == a.length ? kEmpty : Arrays.copyOfRange(a, start, a.length));
  }

  static int compare(int[] a, int[] b) {
    if (a.length != b.length) {
      return a.length < b.length ? -1 : 1;
    }
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return (a[i] & kMask) < (b[i] & kMask) ? -1 : 1;
      }
    }
    return 0;
  }

  static int[] add(int[] x, int[] y) {
    if (x.length < y.length) {
      int[] t = x;
      x = y;
      y = t;
    }
    int xi = x.length;
    int yi = y.length;
    int[] r = new int[xi];
    long sum = 0;
    while (yi > 0) {
      xi--;
      yi--;
      sum = (x[xi] & kMask) + (y[yi] & kMask) + (sum >>> 32);
      r[xi] = (int) sum;
    }
    boolean carry = (sum >>> 32) != 0;
    while (xi > 0 && carry) {
      xi--;
      r[xi] = x[xi] + 1;
      carry = r[xi] == 0;
    }
    while (xi > 0) {
      xi--;
      r[xi] = x[xi];
    }
    if (carry) {
      int[] bigger = new int[r.length + 1];
      System.arraycopy(r, 0, bigger, 1, r.length);
      bigger[0] = 1;
      return bigger;
    }
    return r;
  }

  static int[] addWord(int[] x, long w) {
    return add(x, w == 0 ? kEmpty : new int[] {(int) w});
  }

  static int[] subtract(int[] big, int[] little) {
    int bi = big.length;
    int li = little.length;
    int[] r = new int[bi];
    long diff = 0;
    while (li > 0) {
      bi--;
      li--;
      diff = (big[bi] & kMask) - (little[li] & kMask) + (diff >> 32);
      r[bi] = (int) diff;
    }
    boolean borrow = (diff >> 32) != 0;
    while (bi > 0 && borrow) {
      bi--;
      r[bi] = big[bi] - 1;
      borrow = r[bi] == -1;
    }
    while (bi > 0) {
      bi--;
      r[bi] = big[bi];
    }
    return trim(r);
  }

  static int[] multiply(int[] x, int[] y) {
    if (x.length == 0 || y.length == 0) {
      return kEmpty;
    }
    if (x.length < kKaratsubaThreshold || y.length < kKaratsubaThreshold) {
      return multiplySchool(x, y);
    }
    return karatsuba(x, y);
  }

  private static int[] multiplySchool(int[] x, int[] y) {
    int xl = x.length;
    int yl = y.length;
    int[] z = new int[xl + yl];
    for (int j = yl - 1; j >= 0; j--) {
      long carry = 0;
      long yj = y[j] & kMask;
      for (int i = xl - 1, k = i + j + 1; i >= 0; i--, k--) {
        long prod = (x[i] & kMask) * yj + (z[k] & kMask) + carry;
        z[k] = (int) prod;
        carry = prod >>> 32;
      }
      z[j] = (int) carry;
    }
    return trim(z);
  }

  private static int[] lower(int[] a, int count) {
    if (a.length <= count) {
      return a;
    }
    return trim(Arrays.copyOfRange(a, a.length - count, a.length));
  }

  private static int[] upper(int[] a, int count) {
    if (a.length <= count) {
      return kEmpty;
    }
    return Arrays.copyOfRange(a, 0, a.length - count);
  }

  private static int[] shiftWords(int[] a, int words) {
    if (a.length == 0 || words == 0) {
      return a;
    }
    return Arrays.copyOf(a, a.length + words);
  }

  private static int[] karatsuba(int[] x, int[] y) {
    int half = (Math.max(x.length, y.length) + 1) / 2;
    int[] xl = lower(x, half);
    int[] xh = upper(x, half);
    int[] yl = lower(y, half);
    int[] yh = upper(y, half);
    int[] p1 = multiply(xh, yh);
    int[] p2 = multiply(xl, yl);
    int[] p3 = multiply(add(xh, xl), add(yh, yl));
    int[] mid = subtract(subtract(p3, p1), p2);
    return add(add(shiftWords(p1, 2 * half), shiftWords(mid, half)), p2);
  }

  static int[] multiplyWord(int[] x, long w) {
    if (x.length == 0 || w == 0) {
      return kEmpty;
    }
    int[] r = new int[x.length + 1];
    long carry = 0;
    for (int i = x.length - 1; i >= 0; i--) {
      long prod = (x[i] & kMask) * w + carry;
      r[i + 1] = (int) prod;
      carry = prod >>> 32;
    }
    r[0] = (int) carry;
    return trim(r);
  }

  static int[] shiftLeft(int[] a, int n) {
    if (a.length == 0 || n == 0) {
      return a;
    }
    int words = n >>> 5;
    int bits = n & 31;
    int[] r = new int[a.length + words + (bits == 0 ? 0 : 1)];
    if (bits == 0) {
      System.arraycopy(a, 0, r, 0, a.length);
    } else {
      int carry = 0;
      for (int i = a.length - 1; i >= 0; i--) {
        r[i + 1] = (a[i] << bits) | carry;
        carry = a[i] >>> (32 - bits);
      }
      r[0] = carry;
    }
    return trim(r);
  }

  static int[] shiftRight(int[] a, int n) {
    int words = n >>> 5;
    int bits = n & 31;
    if (words >= a.length) {
      return kEmpty;
    }
    int len = a.length - words;
    int[] r = new int[len];
    if (bits == 0) {
      System.arraycopy(a, 0, r, 0, len);
    } else {
      int carry = 0;
      for (int i = 0; i < len; i++) {
        r[i] = (a[i] >>> bits) | carry;
        carry = a[i] << (32 - bits);
      }
    }
    return trim(r);
  }

  static boolean lowBitsNonZero(int[] a, int n) {
    int words = n >>> 5;
    int bits = n & 31;
    for (int i = 0; i < words && i < a.length; i++) {
      if (a[a.length - 1 - i] != 0) {
        return true;
      }
    }
    if (bits != 0 && words < a.length) {
      return (a[a.length - 1 - words] & ((1 << bits) - 1)) != 0;
    }
    return false;
  }

  static int[] reverse(int[] a) {
    int[] r = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      r[i] = a[a.length - 1 - i];
    }
    return r;
  }

  static int[][] divideAndRemainder(int[] a, int[] b) {
    if (compare(a, b) < 0) {
      return new int[][] {kEmpty, a};
    }
    if (b.length == 1) {
      long d = b[0] & kMask;
      int[] q = new int[a.length];
      long rem = 0;
      for (int i = 0; i < a.length; i++) {
        long cur = (rem << 32) | (a[i] & kMask);
        q[i] = (int) Long.divideUnsigned(cur, d);
        rem = Long.remainderUnsigned(cur, d);
      }
      return new int[][] {trim(q), rem == 0 ? kEmpty : new int[] {(int) rem}};
    }
    int[] u0 = reverse(a);
    int[] v0 = reverse(b);
    int m = u0.length;
    int n = v0.length;
    int s = Integer.numberOfLeadingZeros(v0[n - 1]);
    int[] vn = new int[n];
    int[] un = new int[m + 1];
    if (s == 0) {
      System.arraycopy(v0, 0, vn, 0, n);
      System.arraycopy(u0, 0, un, 0, m);
    } else {
      for (int i = n - 1; i > 0; i--) {
        vn[i] = (v0[i] << s) | (v0[i - 1] >>> (32 - s));
      }
      vn[0] = v0[0] << s;
      un[m] = u0[m - 1] >>> (32 - s);
      for (int i = m - 1; i > 0; i--) {
        un[i] = (u0[i] << s) | (u0[i - 1] >>> (32 - s));
      }
      un[0] = u0[0] << s;
    }
    int[] q = new int[m - n + 1];
    long vTop = vn[n - 1] & kMask;
    long vSecond = vn[n - 2] & kMask;
    for (int j = m - n; j >= 0; j--) {
      long num = ((un[j + n] & kMask) << 32) | (un[j + n - 1] & kMask);
      long qhat = Long.divideUnsigned(num, vTop);
      long rhat = Long.remainderUnsigned(num, vTop);
      while (qhat >= (1L << 32) || Long.compareUnsigned(qhat * vSecond, (rhat << 32) | (un[j + n - 2] & kMask)) > 0) {
        qhat--;
        rhat += vTop;
        if (rhat >= (1L << 32)) {
          break;
        }
      }
      long borrow = 0;
      for (int i = 0; i < n; i++) {
        long p = qhat * (vn[i] & kMask);
        long t = (un[i + j] & kMask) - borrow - (p & kMask);
        un[i + j] = (int) t;
        borrow = (p >>> 32) - (t >> 32);
      }
      long t = (un[j + n] & kMask) - borrow;
      un[j + n] = (int) t;
      q[j] = (int) qhat;
      if (t < 0) {
        q[j]--;
        long k = 0;
        for (int i = 0; i < n; i++) {
          long t2 = (un[i + j] & kMask) + (vn[i] & kMask) + k;
          un[i + j] = (int) t2;
          k = t2 >>> 32;
        }
        un[j + n] += (int) k;
      }
    }
    int[] r = new int[n];
    if (s == 0) {
      System.arraycopy(un, 0, r, 0, n);
    } else {
      for (int i = 0; i < n; i++) {
        r[i] = (un[i] >>> s) | (un[i + 1] << (32 - s));
      }
    }
    return new int[][] {trim(reverse(q)), trim(reverse(r))};
  }

  static int[] fromLittleEndian(int[] little, int len) {
    int top = len;
    while (top > 0 && little[top - 1] == 0) {
      top--;
    }
    int[] r = new int[top];
    for (int i = 0; i < top; i++) {
      r[i] = little[top - 1 - i];
    }
    return r;
  }

  static int[] sqrtMagnitude(int[] a) {
    if (a.length == 0) {
      return kEmpty;
    }
    int bitLen = ((a.length - 1) << 5) + (32 - Integer.numberOfLeadingZeros(a[0]));
    int[] x = shiftLeft(new int[] {1}, (bitLen + 1) / 2);
    while (true) {
      int[] y = shiftRight(add(x, divideAndRemainder(a, x)[0]), 1);
      if (compare(y, x) >= 0) {
        return x;
      }
      x = y;
    }
  }
}
