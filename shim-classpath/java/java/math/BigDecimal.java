/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.math;

import java.io.Serializable;
import java.util.Objects;

public class BigDecimal extends Number implements Comparable<BigDecimal>, Serializable {
  private static final long serialVersionUID = 6108874887143696463L;

  public static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO, 0, 1);
  public static final BigDecimal ONE = new BigDecimal(BigInteger.ONE, 0, 1);
  public static final BigDecimal TWO = new BigDecimal(BigInteger.TWO, 0, 1);
  public static final BigDecimal TEN = new BigDecimal(BigInteger.TEN, 0, 2);

  public static final int ROUND_UP = 0;
  public static final int ROUND_DOWN = 1;
  public static final int ROUND_CEILING = 2;
  public static final int ROUND_FLOOR = 3;
  public static final int ROUND_HALF_UP = 4;
  public static final int ROUND_HALF_DOWN = 5;
  public static final int ROUND_HALF_EVEN = 6;
  public static final int ROUND_UNNECESSARY = 7;

  private final BigInteger intVal;
  private final int scale;
  private transient int precision;

  private BigDecimal(BigInteger intVal, int scale, int precision) {
    this.intVal = intVal;
    this.scale = scale;
    this.precision = precision;
  }

  public BigDecimal(BigInteger intVal, int scale) {
    this.intVal = Objects.requireNonNull(intVal, "intVal");
    this.scale = scale;
    this.precision = 0;
  }

  public BigDecimal(BigInteger intVal, int scale, MathContext mc) {
    this(intVal, scale);
    if (mc != null && mc.getPrecision() > 0) {
      BigDecimal rounded = round(mc);
      this.precision = rounded.precision;
    }
  }

  public BigDecimal(BigInteger val) {
    this(val, 0);
  }

  public BigDecimal(BigInteger val, MathContext mc) {
    this(val, 0, mc);
  }

  public BigDecimal(int val) {
    this(BigInteger.valueOf(val), 0);
  }

  public BigDecimal(int val, MathContext mc) {
    this(BigInteger.valueOf(val), 0, mc);
  }

  public BigDecimal(long val) {
    this(BigInteger.valueOf(val), 0);
  }

  public BigDecimal(long val, MathContext mc) {
    this(BigInteger.valueOf(val), 0, mc);
  }

  public BigDecimal(double val) {
    if (Double.isInfinite(val) || Double.isNaN(val)) {
      throw new NumberFormatException("Infinite or NaN");
    }
    long bits = Double.doubleToLongBits(val);
    int sign = (bits < 0) ? -1 : 1;
    int exp = (int) ((bits >> 52) & 0x7ffL);
    long mantissa = (exp == 0)
        ? (bits & 0x000fffffffffffffL) << 1
        : (bits & 0x000fffffffffffffL) | 0x0010000000000000L;
    if (mantissa == 0) {
      this.intVal = BigInteger.ZERO;
      this.scale = 0;
      this.precision = 1;
      return;
    }
    exp -= 1075;
    BigInteger bi = BigInteger.valueOf(mantissa);
    int s;
    if (exp >= 0) {
      bi = bi.shiftLeft(exp);
      s = 0;
    } else {
      int powerOf5 = -exp;
      bi = bi.multiply(BigInteger.valueOf(5).pow(powerOf5));
      s = powerOf5;
    }
    this.intVal = (sign < 0) ? bi.negate() : bi;
    this.scale = s;
    this.precision = 0;
  }

  public BigDecimal(double val, MathContext mc) {
    this(val);
    if (mc != null && mc.getPrecision() > 0) {
      BigDecimal rounded = round(mc);
      this.precision = rounded.precision;
    }
  }

  public BigDecimal(String val) {
    this(val.toCharArray(), 0, val.length());
  }

  public BigDecimal(String val, MathContext mc) {
    this(val.toCharArray(), 0, val.length(), mc);
  }

  public BigDecimal(char[] in) {
    this(in, 0, in.length);
  }

  public BigDecimal(char[] in, MathContext mc) {
    this(in, 0, in.length, mc);
  }

  public BigDecimal(char[] in, int offset, int len) {
    Objects.requireNonNull(in, "in");
    if (offset < 0 || len < 0 || offset + len > in.length) {
      throw new NumberFormatException("Bad offset or len");
    }
    if (len == 0) {
      throw new NumberFormatException("Zero length BigDecimal");
    }
    int cursor = offset;
    int end = offset + len;
    int signum = 1;
    if (in[cursor] == '-') {
      signum = -1;
      cursor++;
    } else if (in[cursor] == '+') {
      cursor++;
    }
    if (cursor == end) {
      throw new NumberFormatException("Zero length mantissa");
    }

    int dotIndex = -1;
    int expIndex = -1;
    for (int i = cursor; i < end; i++) {
      char c = in[i];
      if (c == '.') {
        if (dotIndex >= 0 || expIndex >= 0) {
          throw new NumberFormatException("Duplicate dot or dot in exponent");
        }
        dotIndex = i;
      } else if (c == 'e' || c == 'E') {
        if (expIndex >= 0) {
          throw new NumberFormatException("Duplicate exponent indicator");
        }
        expIndex = i;
      }
    }

    int mantissaEnd = (expIndex >= 0) ? expIndex : end;
    int expValue = 0;
    if (expIndex >= 0) {
      int expCursor = expIndex + 1;
      int expSign = 1;
      if (expCursor < end && in[expCursor] == '-') {
        expSign = -1;
        expCursor++;
      } else if (expCursor < end && in[expCursor] == '+') {
        expCursor++;
      }
      if (expCursor == end) {
        throw new NumberFormatException("Empty exponent");
      }
      long parsedExp = 0;
      for (int i = expCursor; i < end; i++) {
        char c = in[i];
        if (c < '0' || c > '9') {
          throw new NumberFormatException("Illegal char in exponent: " + c);
        }
        parsedExp = parsedExp * 10 + (c - '0');
        if (parsedExp > Integer.MAX_VALUE) {
          throw new NumberFormatException("Exponent overflow");
        }
      }
      expValue = (int) (expSign * parsedExp);
    }

    int fracDigits = 0;
    StringBuilder digits = new StringBuilder(mantissaEnd - cursor);
    for (int i = cursor; i < mantissaEnd; i++) {
      char c = in[i];
      if (c == '.') {
        continue;
      }
      if (c < '0' || c > '9') {
        throw new NumberFormatException("Illegal char in mantissa: " + c);
      }
      digits.append(c);
      if (dotIndex >= 0 && i > dotIndex) {
        fracDigits++;
      }
    }
    if (digits.length() == 0) {
      throw new NumberFormatException("No digits in mantissa");
    }

    BigInteger unscaled = new BigInteger(digits.toString());
    this.intVal = (signum < 0) ? unscaled.negate() : unscaled;
    this.scale = fracDigits - expValue;
    this.precision = 0;
  }

  public BigDecimal(char[] in, int offset, int len, MathContext mc) {
    this(in, offset, len);
    if (mc != null && mc.getPrecision() > 0) {
      BigDecimal rounded = round(mc);
      this.precision = rounded.precision;
    }
  }

  public static BigDecimal valueOf(long val) {
    if (val == 0) return ZERO;
    if (val == 1) return ONE;
    if (val == 10) return TEN;
    return new BigDecimal(BigInteger.valueOf(val), 0);
  }

  public static BigDecimal valueOf(long unscaledVal, int scale) {
    if (scale == 0) return valueOf(unscaledVal);
    return new BigDecimal(BigInteger.valueOf(unscaledVal), scale);
  }

  public static BigDecimal valueOf(double val) {
    return new BigDecimal(Double.toString(val));
  }

  public BigDecimal add(BigDecimal augend) {
    Objects.requireNonNull(augend, "augend");
    if (this.scale == augend.scale) {
      return new BigDecimal(this.intVal.add(augend.intVal), this.scale);
    }
    if (this.scale > augend.scale) {
      int diff = this.scale - augend.scale;
      BigInteger scaledOther = augend.intVal.multiply(BigInteger.TEN.pow(diff));
      return new BigDecimal(this.intVal.add(scaledOther), this.scale);
    } else {
      int diff = augend.scale - this.scale;
      BigInteger scaledThis = this.intVal.multiply(BigInteger.TEN.pow(diff));
      return new BigDecimal(scaledThis.add(augend.intVal), augend.scale);
    }
  }

  public BigDecimal add(BigDecimal augend, MathContext mc) {
    BigDecimal res = add(augend);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal subtract(BigDecimal subtrahend) {
    Objects.requireNonNull(subtrahend, "subtrahend");
    return add(subtrahend.negate());
  }

  public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
    BigDecimal res = subtract(subtrahend);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal multiply(BigDecimal multiplicand) {
    Objects.requireNonNull(multiplicand, "multiplicand");
    BigInteger prod = this.intVal.multiply(multiplicand.intVal);
    int newScale = this.scale + multiplicand.scale;
    return new BigDecimal(prod, newScale);
  }

  public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
    BigDecimal res = multiply(multiplicand);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
    Objects.requireNonNull(divisor, "divisor");
    Objects.requireNonNull(roundingMode, "roundingMode");
    if (divisor.intVal.signum() == 0) {
      throw new ArithmeticException("Division by zero");
    }

    int shift = scale - (this.scale - divisor.scale);
    BigInteger scaledDividend;
    BigInteger scaledDivisor;

    if (shift >= 0) {
      scaledDividend = this.intVal.multiply(BigInteger.TEN.pow(shift));
      scaledDivisor = divisor.intVal;
    } else {
      scaledDividend = this.intVal;
      scaledDivisor = divisor.intVal.multiply(BigInteger.TEN.pow(-shift));
    }

    BigInteger[] qr = scaledDividend.divideAndRemainder(scaledDivisor);
    BigInteger q = qr[0];
    BigInteger r = qr[1];

    if (r.signum() != 0) {
      boolean increment = false;
      int qSign = q.signum();
      int rSign = r.signum();
      int divSign = scaledDivisor.signum();
      boolean signMatch = (rSign == divSign);

      switch (roundingMode) {
        case UP:
          increment = true;
          break;
        case DOWN:
          increment = false;
          break;
        case CEILING:
          increment = signMatch;
          break;
        case FLOOR:
          increment = !signMatch;
          break;
        case HALF_UP:
        case HALF_DOWN:
        case HALF_EVEN:
          BigInteger twoR = r.abs().shiftLeft(1);
          BigInteger absDiv = scaledDivisor.abs();
          int cmp = twoR.compareTo(absDiv);
          if (cmp > 0) {
            increment = true;
          } else if (cmp == 0) {
            if (roundingMode == RoundingMode.HALF_UP) {
              increment = true;
            } else if (roundingMode == RoundingMode.HALF_EVEN) {
              increment = q.testBit(0);
            }
          }
          break;
        case UNNECESSARY:
          throw new ArithmeticException("Rounding necessary");
      }

      if (increment) {
        BigInteger one = BigInteger.ONE;
        if (qSign == 0) {
          q = signMatch ? one : one.negate();
        } else if (qSign > 0) {
          q = q.add(one);
        } else {
          q = q.subtract(one);
        }
      }
    }

    return new BigDecimal(q, scale);
  }

  public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
    return divide(divisor, scale, RoundingMode.valueOf(roundingMode));
  }

  public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
    return divide(divisor, this.scale, roundingMode);
  }

  public BigDecimal divide(BigDecimal divisor, int roundingMode) {
    return divide(divisor, this.scale, RoundingMode.valueOf(roundingMode));
  }

  public BigDecimal divide(BigDecimal divisor) {
    Objects.requireNonNull(divisor, "divisor");
    if (divisor.intVal.signum() == 0) {
      throw new ArithmeticException("Division by zero");
    }
    if (this.intVal.signum() == 0) {
      return BigDecimal.valueOf(0, Math.max(0, this.scale - divisor.scale));
    }
    BigInteger p = this.intVal.abs();
    BigInteger q = divisor.intVal.abs();
    BigInteger gcd = p.gcd(q);
    p = p.divide(gcd);
    q = q.divide(gcd);

    BigInteger tempQ = q;
    int p2 = 0;
    while (tempQ.testBit(0) == false && tempQ.signum() != 0) {
      tempQ = tempQ.shiftRight(1);
      p2++;
    }
    int p5 = 0;
    while (tempQ.remainder(BigInteger.valueOf(5)).signum() == 0 && tempQ.signum() != 0) {
      tempQ = tempQ.divide(BigInteger.valueOf(5));
      p5++;
    }
    if (!tempQ.equals(BigInteger.ONE)) {
      throw new ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.");
    }

    int extraScale = Math.max(p2, p5);
    int targetScale = Math.max(0, this.scale - divisor.scale + extraScale);
    return divide(divisor, targetScale, RoundingMode.UNNECESSARY);
  }

  public BigDecimal divide(BigDecimal divisor, MathContext mc) {
    if (mc == null || mc.getPrecision() == 0) {
      return divide(divisor);
    }
    int preferredScale = this.scale - divisor.scale;
    int targetScale = preferredScale + mc.getPrecision();
    BigDecimal raw = divide(divisor, Math.max(targetScale, 0), mc.getRoundingMode());
    return raw.round(mc);
  }

  public BigDecimal divideToIntegralValue(BigDecimal divisor) {
    Objects.requireNonNull(divisor, "divisor");
    int targetScale = this.scale - divisor.scale;
    BigDecimal q = divide(divisor, Math.max(0, targetScale), RoundingMode.DOWN);
    return q.setScale(targetScale, RoundingMode.DOWN);
  }

  public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
    BigDecimal res = divideToIntegralValue(divisor);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal remainder(BigDecimal divisor) {
    Objects.requireNonNull(divisor, "divisor");
    BigDecimal intPart = divideToIntegralValue(divisor);
    return this.subtract(intPart.multiply(divisor));
  }

  public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
    BigDecimal res = remainder(divisor);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
    BigDecimal q = divideToIntegralValue(divisor);
    BigDecimal r = this.subtract(q.multiply(divisor));
    return new BigDecimal[] {q, r};
  }

  public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
    BigDecimal[] qr = divideAndRemainder(divisor);
    if (mc != null && mc.getPrecision() > 0) {
      qr[0] = qr[0].round(mc);
      qr[1] = qr[1].round(mc);
    }
    return qr;
  }

  public BigDecimal pow(int n) {
    if (n < 0 || n > 999999999) {
      throw new ArithmeticException("Invalid pow exponent");
    }
    if (n == 0) {
      return ONE;
    }
    if (intVal.signum() == 0) {
      return ZERO;
    }
    BigInteger newInt = intVal.pow(n);
    long newScale = (long) scale * n;
    if (newScale > Integer.MAX_VALUE || newScale < Integer.MIN_VALUE) {
      throw new ArithmeticException("Scale overflow in pow");
    }
    return new BigDecimal(newInt, (int) newScale);
  }

  public BigDecimal pow(int n, MathContext mc) {
    BigDecimal res = pow(n);
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal abs() {
    return intVal.signum() < 0 ? negate() : this;
  }

  public BigDecimal abs(MathContext mc) {
    BigDecimal res = abs();
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal negate() {
    return new BigDecimal(intVal.negate(), scale);
  }

  public BigDecimal negate(MathContext mc) {
    BigDecimal res = negate();
    return (mc != null && mc.getPrecision() > 0) ? res.round(mc) : res;
  }

  public BigDecimal plus() {
    return this;
  }

  public BigDecimal plus(MathContext mc) {
    return (mc != null && mc.getPrecision() > 0) ? round(mc) : this;
  }

  public int signum() {
    return intVal.signum();
  }

  public int scale() {
    return scale;
  }

  public int precision() {
    if (precision == 0) {
      if (intVal.signum() == 0) {
        precision = 1;
      } else {
        precision = intVal.abs().toString().length();
      }
    }
    return precision;
  }

  public BigInteger unscaledValue() {
    return intVal;
  }

  public BigDecimal round(MathContext mc) {
    Objects.requireNonNull(mc, "mc");
    int prec = mc.getPrecision();
    if (prec == 0) {
      return this;
    }
    int p = precision();
    int drop = p - prec;
    if (drop <= 0) {
      return this;
    }
    int newScale = scale - drop;
    return setScale(newScale, mc.getRoundingMode());
  }

  public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
    Objects.requireNonNull(roundingMode, "roundingMode");
    if (newScale == this.scale) {
      return this;
    }
    if (newScale > this.scale) {
      int diff = newScale - this.scale;
      return new BigDecimal(intVal.multiply(BigInteger.TEN.pow(diff)), newScale);
    }
    int diff = this.scale - newScale;
    BigInteger divisor = BigInteger.TEN.pow(diff);
    BigInteger[] qr = intVal.divideAndRemainder(divisor);
    BigInteger q = qr[0];
    BigInteger r = qr[1];

    if (r.signum() != 0) {
      boolean increment = false;
      int qSign = q.signum();
      int rSign = r.signum();
      boolean signMatch = (rSign > 0);

      switch (roundingMode) {
        case UP:
          increment = true;
          break;
        case DOWN:
          increment = false;
          break;
        case CEILING:
          increment = signMatch;
          break;
        case FLOOR:
          increment = !signMatch;
          break;
        case HALF_UP:
        case HALF_DOWN:
        case HALF_EVEN:
          BigInteger twoR = r.abs().shiftLeft(1);
          int cmp = twoR.compareTo(divisor);
          if (cmp > 0) {
            increment = true;
          } else if (cmp == 0) {
            if (roundingMode == RoundingMode.HALF_UP) {
              increment = true;
            } else if (roundingMode == RoundingMode.HALF_EVEN) {
              increment = q.testBit(0);
            }
          }
          break;
        case UNNECESSARY:
          throw new ArithmeticException("Rounding necessary");
      }

      if (increment) {
        BigInteger one = BigInteger.ONE;
        if (qSign == 0) {
          q = signMatch ? one : one.negate();
        } else if (qSign > 0) {
          q = q.add(one);
        } else {
          q = q.subtract(one);
        }
      }
    }
    return new BigDecimal(q, newScale);
  }

  public BigDecimal setScale(int newScale, int roundingMode) {
    return setScale(newScale, RoundingMode.valueOf(roundingMode));
  }

  public BigDecimal setScale(int newScale) {
    return setScale(newScale, RoundingMode.UNNECESSARY);
  }

  public BigDecimal movePointLeft(int n) {
    return new BigDecimal(intVal, scale + n);
  }

  public BigDecimal movePointRight(int n) {
    return new BigDecimal(intVal, scale - n);
  }

  public BigDecimal scaleByPowerOfTen(int n) {
    return new BigDecimal(intVal, scale - n);
  }

  public BigDecimal stripTrailingZeros() {
    if (intVal.signum() == 0) {
      return BigDecimal.ZERO;
    }
    BigInteger cur = intVal;
    int curScale = scale;
    while (cur.remainder(BigInteger.TEN).signum() == 0) {
      cur = cur.divide(BigInteger.TEN);
      curScale--;
    }
    return new BigDecimal(cur, curScale);
  }

  @Override
  public int compareTo(BigDecimal val) {
    Objects.requireNonNull(val, "val");
    if (this == val) {
      return 0;
    }
    int s1 = this.signum();
    int s2 = val.signum();
    if (s1 != s2) {
      return (s1 > s2) ? 1 : -1;
    }
    if (s1 == 0) {
      return 0;
    }
    int diff = this.scale - val.scale;
    if (diff == 0) {
      return this.intVal.compareTo(val.intVal);
    }
    if (diff > 0) {
      return this.intVal.compareTo(val.intVal.multiply(BigInteger.TEN.pow(diff)));
    } else {
      return this.intVal.multiply(BigInteger.TEN.pow(-diff)).compareTo(val.intVal);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BigDecimal)) {
      return false;
    }
    BigDecimal other = (BigDecimal) o;
    return this.scale == other.scale && this.intVal.equals(other.intVal);
  }

  public BigDecimal min(BigDecimal val) {
    return compareTo(val) <= 0 ? this : val;
  }

  public BigDecimal max(BigDecimal val) {
    return compareTo(val) >= 0 ? this : val;
  }

  @Override
  public int hashCode() {
    return intVal.hashCode() * 31 + scale;
  }

  @Override
  public String toString() {
    if (scale == 0) {
      return intVal.toString();
    }
    String coeff = intVal.abs().toString();
    long adjExp = (long) -scale + (coeff.length() - 1);
    if (scale >= 0 && adjExp >= -6) {
      return toPlainString();
    }
    StringBuilder sb = new StringBuilder();
    if (intVal.signum() < 0) {
      sb.append('-');
    }
    sb.append(coeff.charAt(0));
    if (coeff.length() > 1) {
      sb.append('.');
      sb.append(coeff.substring(1));
    }
    sb.append('E');
    if (adjExp >= 0) {
      sb.append('+');
    }
    sb.append(adjExp);
    return sb.toString();
  }

  public String toPlainString() {
    if (scale == 0) {
      return intVal.toString();
    }
    if (scale < 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(intVal.toString());
      for (int i = 0; i < -scale; i++) {
        sb.append('0');
      }
      return sb.toString();
    }
    String s = intVal.abs().toString();
    StringBuilder sb = new StringBuilder();
    if (intVal.signum() < 0) {
      sb.append('-');
    }
    int pad = scale - s.length();
    if (pad >= 0) {
      sb.append("0.");
      for (int i = 0; i < pad; i++) {
        sb.append('0');
      }
      sb.append(s);
    } else {
      int dotPos = s.length() - scale;
      sb.append(s, 0, dotPos);
      sb.append('.');
      sb.append(s, dotPos, s.length());
    }
    return sb.toString();
  }

  public String toEngineeringString() {
    if (scale == 0) {
      return intVal.toString();
    }
    String coeff = intVal.abs().toString();
    long adjExp = (long) -scale + (coeff.length() - 1);
    if (scale >= 0 && adjExp >= -6) {
      return toPlainString();
    }
    StringBuilder sb = new StringBuilder();
    if (intVal.signum() < 0) {
      sb.append('-');
    }
    int mod = (int) (adjExp % 3);
    if (mod < 0) {
      mod += 3;
    }
    adjExp -= mod;
    int digitsBeforeDot = mod + 1;
    if (digitsBeforeDot <= coeff.length()) {
      sb.append(coeff, 0, digitsBeforeDot);
      if (digitsBeforeDot < coeff.length()) {
        sb.append('.');
        sb.append(coeff.substring(digitsBeforeDot));
      }
    } else {
      sb.append(coeff);
      for (int i = 0; i < digitsBeforeDot - coeff.length(); i++) {
        sb.append('0');
      }
    }
    sb.append('E');
    if (adjExp >= 0) {
      sb.append('+');
    }
    sb.append(adjExp);
    return sb.toString();
  }

  public BigInteger toBigInteger() {
    return setScale(0, RoundingMode.DOWN).intVal;
  }

  public BigInteger toBigIntegerExact() {
    return setScale(0, RoundingMode.UNNECESSARY).intVal;
  }

  @Override
  public long longValue() {
    return toBigInteger().longValue();
  }

  public long longValueExact() {
    BigInteger bi = toBigIntegerExact();
    long l = bi.longValue();
    if (!BigInteger.valueOf(l).equals(bi)) {
      throw new ArithmeticException("Overflow");
    }
    return l;
  }

  @Override
  public int intValue() {
    return toBigInteger().intValue();
  }

  public int intValueExact() {
    long l = longValueExact();
    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
      throw new ArithmeticException("Overflow");
    }
    return (int) l;
  }

  public short shortValueExact() {
    int i = intValueExact();
    if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
      throw new ArithmeticException("Overflow");
    }
    return (short) i;
  }

  public byte byteValueExact() {
    int i = intValueExact();
    if (i < Byte.MIN_VALUE || i > Byte.MAX_VALUE) {
      throw new ArithmeticException("Overflow");
    }
    return (byte) i;
  }

  @Override
  public float floatValue() {
    return Float.parseFloat(this.toString());
  }

  @Override
  public double doubleValue() {
    return Double.parseDouble(this.toString());
  }

  public BigDecimal ulp() {
    return BigDecimal.valueOf(1, this.scale);
  }
}
