/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.math;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Objects;

public class BigDecimal extends Number implements Comparable<BigDecimal> {
  private static final long serialVersionUID = 6108874887143696463L;

  private final BigInteger intVal;
  private final int scale;
  private transient int precision;

  public static final int ROUND_UP = 0;
  public static final int ROUND_DOWN = 1;
  public static final int ROUND_CEILING = 2;
  public static final int ROUND_FLOOR = 3;
  public static final int ROUND_HALF_UP = 4;
  public static final int ROUND_HALF_DOWN = 5;
  public static final int ROUND_HALF_EVEN = 6;
  public static final int ROUND_UNNECESSARY = 7;

  private static final BigDecimal[] kZeroThroughTen = new BigDecimal[11];

  static {
    for (int i = 0; i <= 10; i++) {
      kZeroThroughTen[i] = new BigDecimal(BigInteger.valueOf(i), 0, i == 0 ? 1 : (i == 10 ? 2 : 1));
    }
  }

  public static final BigDecimal ZERO = kZeroThroughTen[0];
  public static final BigDecimal ONE = kZeroThroughTen[1];
  public static final BigDecimal TWO = kZeroThroughTen[2];
  public static final BigDecimal TEN = kZeroThroughTen[10];

  private static final BigDecimal kOneHalf = valueOf(5, 1);
  private static final BigDecimal kOneTenth = valueOf(1, 1);

  private static final BigInteger[] kTenPowers = new BigInteger[64];

  private static BigInteger tenPow(long n) {
    if (n < kTenPowers.length) {
      BigInteger v = kTenPowers[(int) n];
      if (v == null) {
        v = BigInteger.TEN.pow((int) n);
        kTenPowers[(int) n] = v;
      }
      return v;
    }
    return BigInteger.TEN.pow((int) n);
  }

  BigDecimal(BigInteger intVal, int scale, int prec) {
    this.intVal = intVal;
    this.scale = scale;
    this.precision = prec;
  }

  private static BigDecimal zeroValueOf(int scale) {
    if (scale >= 0 && scale < 0) {
      return ZERO;
    }
    return new BigDecimal(BigInteger.ZERO, scale, 1);
  }

  public BigDecimal(char[] in, int offset, int len) {
    this(in, offset, len, MathContext.UNLIMITED);
  }

  public BigDecimal(char[] in, int offset, int len, MathContext mc) {
    try {
      Objects.checkFromIndexSize(offset, len, in.length);
    } catch (IndexOutOfBoundsException e) {
      throw new NumberFormatException("Bad offset or len arguments for char[] input.");
    }
    int prec = 0;
    int scl = 0;
    BigInteger rb;
    try {
      boolean isneg = false;
      if (in[offset] == '-') {
        isneg = true;
        offset++;
        len--;
      } else if (in[offset] == '+') {
        offset++;
        len--;
      }
      boolean dot = false;
      long exp = 0;
      char c;
      StringBuilder digits = new StringBuilder(len);
      int sigStart = -1;
      for (; len > 0; offset++, len--) {
        c = in[offset];
        if (c == '.') {
          if (dot) {
            throw new NumberFormatException("Character array contains more than one decimal point.");
          }
          dot = true;
        } else if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
          int digit = (c >= '0' && c <= '9') ? c - '0' : Character.digit(c, 10);
          if (digit == 0) {
            if (prec == 0) {
              prec = 1;
              digits.append('0');
            } else if (digits.length() > 0 && !(digits.length() == 1 && digits.charAt(0) == '0')) {
              digits.append('0');
              ++prec;
            }
          } else {
            if (prec != 1 || digits.length() != 1 || digits.charAt(0) != '0') {
              ++prec;
            } else {
              digits.setLength(0);
            }
            digits.append((char) ('0' + digit));
          }
          if (dot) {
            ++scl;
          }
        } else if (c == 'e' || c == 'E') {
          exp = parseExp(in, offset, len);
          if ((int) exp != exp) {
            throw new NumberFormatException("Exponent overflow.");
          }
          break;
        } else {
          throw new NumberFormatException("Character " + c + " is neither a decimal digit number, decimal point, nor"
              + " \"e\" notation exponential mark.");
        }
      }
      if (prec == 0) {
        throw new NumberFormatException("No digits found.");
      }
      if (exp != 0) {
        scl = adjustScale(scl, exp);
      }
      rb = new BigInteger(digits.toString());
      if (isneg) {
        rb = rb.negate();
      }
      int mcp = mc.precision;
      int drop = prec - mcp;
      if (mcp > 0 && drop > 0) {
        while (drop > 0) {
          scl = checkScaleNonZero((long) scl - drop);
          rb = divideAndRound(rb, tenPow(drop), mc.roundingMode.oldMode);
          prec = digitLength(rb);
          drop = prec - mcp;
        }
      }
    } catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException e) {
      NumberFormatException nfe = new NumberFormatException();
      nfe.initCause(e);
      throw nfe;
    }
    this.scale = scl;
    this.precision = rb.signum() == 0 ? 1 : prec;
    this.intVal = rb;
  }

  private static int adjustScale(int scl, long exp) {
    long adjustedScale = scl - exp;
    if (adjustedScale > Integer.MAX_VALUE || adjustedScale < Integer.MIN_VALUE) {
      throw new NumberFormatException("Exponent overflow.");
    }
    return (int) adjustedScale;
  }

  private static long parseExp(char[] in, int offset, int len) {
    long exp = 0;
    offset++;
    char c = in[offset];
    len--;
    boolean negexp = (c == '-');
    if (negexp || c == '+') {
      offset++;
      c = in[offset];
      len--;
    }
    if (len <= 0) {
      throw new NumberFormatException("No digits found.");
    }
    while (len > 10 && (c == '0' || (Character.digit(c, 10) == 0))) {
      offset++;
      c = in[offset];
      len--;
    }
    if (len > 10) {
      throw new NumberFormatException("Too many nonzero exponent digits.");
    }
    for (;; len--) {
      int v;
      if (c >= '0' && c <= '9') {
        v = c - '0';
      } else {
        v = Character.digit(c, 10);
        if (v < 0) {
          throw new NumberFormatException("Not a digit.");
        }
      }
      exp = exp * 10 + v;
      if (len == 1) {
        break;
      }
      offset++;
      c = in[offset];
    }
    if (negexp) {
      exp = -exp;
    }
    return exp;
  }

  public BigDecimal(char[] in, MathContext mc) {
    this(in, 0, in.length, mc);
  }

  public BigDecimal(char[] in) {
    this(in, 0, in.length);
  }

  public BigDecimal(String val) {
    this(val.toCharArray(), 0, val.length());
  }

  public BigDecimal(String val, MathContext mc) {
    this(val.toCharArray(), 0, val.length(), mc);
  }

  public BigDecimal(double val, MathContext mc) {
    if (Double.isInfinite(val) || Double.isNaN(val)) {
      throw new NumberFormatException("Infinite or NaN");
    }
    long valBits = Double.doubleToLongBits(val);
    int sign = ((valBits >> 63) == 0 ? 1 : -1);
    int exponent = (int) ((valBits >> 52) & 0x7ffL);
    long significand = (exponent == 0 ? (valBits & ((1L << 52) - 1)) << 1 : (valBits & ((1L << 52) - 1)) | (1L << 52));
    exponent -= 1075;
    if (significand == 0) {
      this.intVal = BigInteger.ZERO;
      this.scale = 0;
      this.precision = 1;
      return;
    }
    while ((significand & 1) == 0) {
      significand >>= 1;
      exponent++;
    }
    int scl = 0;
    BigInteger rb;
    long compactVal = sign * significand;
    if (exponent == 0) {
      rb = BigInteger.valueOf(compactVal);
    } else if (exponent < 0) {
      rb = BigInteger.valueOf(5).pow(-exponent).multiply(BigInteger.valueOf(compactVal));
      scl = -exponent;
    } else {
      rb = BigInteger.TWO.pow(exponent).multiply(BigInteger.valueOf(compactVal));
    }
    int prec = 0;
    int mcp = mc.precision;
    if (mcp > 0) {
      prec = digitLength(rb);
      int drop = prec - mcp;
      while (drop > 0) {
        scl = checkScaleNonZero((long) scl - drop);
        rb = divideAndRound(rb, tenPow(drop), mc.roundingMode.oldMode);
        prec = digitLength(rb);
        drop = prec - mcp;
      }
    }
    this.intVal = rb;
    this.scale = scl;
    this.precision = prec;
  }

  public BigDecimal(double val) {
    this(val, MathContext.UNLIMITED);
  }

  public BigDecimal(BigInteger val) {
    scale = 0;
    intVal = val;
  }

  public BigDecimal(BigInteger val, MathContext mc) {
    this(val, 0, mc);
  }

  public BigDecimal(BigInteger unscaledVal, int scale) {
    this.intVal = unscaledVal;
    this.scale = scale;
  }

  public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
    BigInteger rb = unscaledVal;
    int scl = scale;
    int mcp = mc.precision;
    int prec = 0;
    if (mcp > 0) {
      prec = digitLength(rb);
      int drop = prec - mcp;
      while (drop > 0) {
        scl = checkScaleNonZero((long) scl - drop);
        rb = divideAndRound(rb, tenPow(drop), mc.roundingMode.oldMode);
        prec = digitLength(rb);
        drop = prec - mcp;
      }
    }
    this.intVal = rb;
    this.scale = scl;
    this.precision = prec;
  }

  public BigDecimal(int val) {
    this.intVal = BigInteger.valueOf(val);
    this.scale = 0;
  }

  public BigDecimal(int val, MathContext mc) {
    this(BigInteger.valueOf(val), 0, mc);
  }

  public BigDecimal(long val) {
    this.intVal = BigInteger.valueOf(val);
    this.scale = 0;
  }

  public BigDecimal(long val, MathContext mc) {
    this(BigInteger.valueOf(val), 0, mc);
  }

  public static BigDecimal valueOf(long unscaledVal, int scale) {
    if (scale == 0) {
      return valueOf(unscaledVal);
    } else if (unscaledVal == 0) {
      return zeroValueOf(scale);
    }
    return new BigDecimal(BigInteger.valueOf(unscaledVal), scale);
  }

  public static BigDecimal valueOf(long val) {
    if (val >= 0 && val < kZeroThroughTen.length) {
      return kZeroThroughTen[(int) val];
    }
    return new BigDecimal(BigInteger.valueOf(val), 0);
  }

  public static BigDecimal valueOf(double val) {
    if (Double.isInfinite(val) || Double.isNaN(val)) {
      throw new NumberFormatException("Infinite or NaN");
    }
    return new BigDecimal(Double.toString(val));
  }

  private static BigDecimal valueOf(BigInteger unscaled, int scale) {
    return new BigDecimal(unscaled, scale);
  }

  public BigDecimal add(BigDecimal augend) {
    return addInternal(augend, false);
  }

  private BigDecimal addInternal(BigDecimal augend, boolean negate) {
    BigInteger y = negate ? augend.intVal.negate() : augend.intVal;
    long sdiff = (long) scale - augend.scale;
    if (sdiff == 0) {
      return new BigDecimal(intVal.add(y), scale);
    }
    if (sdiff < 0) {
      int raise = checkScale(intVal, -sdiff);
      BigInteger scaledX = intVal.signum() == 0 ? intVal : intVal.multiply(tenPow(raise));
      return new BigDecimal(scaledX.add(y), augend.scale);
    }
    int raise = checkScale(y, sdiff);
    BigInteger scaledY = y.signum() == 0 ? y : y.multiply(tenPow(raise));
    return new BigDecimal(intVal.add(scaledY), scale);
  }

  public BigDecimal add(BigDecimal augend, MathContext mc) {
    if (mc.precision == 0) {
      return add(augend);
    }
    BigDecimal lhs = this;
    {
      boolean lhsIsZero = lhs.signum() == 0;
      boolean augendIsZero = augend.signum() == 0;
      if (lhsIsZero || augendIsZero) {
        int preferredScale = Math.max(lhs.scale(), augend.scale());
        BigDecimal result;
        if (lhsIsZero && augendIsZero) {
          return zeroValueOf(preferredScale);
        }
        result = lhsIsZero ? doRound(augend, mc) : doRound(lhs, mc);
        if (result.scale() == preferredScale) {
          return result;
        } else if (result.scale() > preferredScale) {
          return stripZerosToMatchScale(result.intVal, result.scale, preferredScale);
        } else {
          int precisionDiff = mc.precision - result.precision();
          int scaleDiff = preferredScale - result.scale();
          if (precisionDiff >= scaleDiff) {
            return result.setScale(preferredScale);
          } else {
            return result.setScale(result.scale() + precisionDiff);
          }
        }
      }
    }
    long padding = (long) lhs.scale - augend.scale;
    if (padding != 0) {
      BigDecimal[] arg = preAlign(lhs, augend, padding, mc);
      matchScale(arg);
      lhs = arg[0];
      augend = arg[1];
    }
    return doRound(lhs.intVal.add(augend.intVal), lhs.scale, mc);
  }

  private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend, long padding, MathContext mc) {
    BigDecimal big;
    BigDecimal small;
    if (padding < 0) {
      big = lhs;
      small = augend;
    } else {
      big = augend;
      small = lhs;
    }
    long estResultUlpScale = (long) big.scale - big.precision() + mc.precision;
    long smallHighDigitPos = (long) small.scale - small.precision() + 1;
    if (smallHighDigitPos > big.scale + 2 && smallHighDigitPos > estResultUlpScale + 2) {
      small = BigDecimal.valueOf(small.signum(), this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
    }
    return new BigDecimal[] {big, small};
  }

  public BigDecimal subtract(BigDecimal subtrahend) {
    return addInternal(subtrahend, true);
  }

  public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
    if (mc.precision == 0) {
      return subtract(subtrahend);
    }
    return add(subtrahend.negate(), mc);
  }

  public BigDecimal multiply(BigDecimal multiplicand) {
    int productScale = checkScale((long) scale + multiplicand.scale);
    return new BigDecimal(intVal.multiply(multiplicand.intVal), productScale);
  }

  public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
    if (mc.precision == 0) {
      return multiply(multiplicand);
    }
    int productScale = checkScale((long) scale + multiplicand.scale);
    return doRound(intVal.multiply(multiplicand.intVal), productScale, mc);
  }

  public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
    if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY) {
      throw new IllegalArgumentException("Invalid rounding mode");
    }
    if (divisor.signum() == 0) {
      throw new ArithmeticException(intVal.bitLength() < 64 ? "/ by zero" : "BigInteger divide by zero");
    }
    return divideScaled(this.intVal, this.scale, divisor.intVal, divisor.scale, scale, roundingMode, scale);
  }

  public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
    return divide(divisor, scale, roundingMode.oldMode);
  }

  public BigDecimal divide(BigDecimal divisor, int roundingMode) {
    return this.divide(divisor, scale, roundingMode);
  }

  public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
    return this.divide(divisor, scale, roundingMode.oldMode);
  }

  public BigDecimal divide(BigDecimal divisor) {
    if (divisor.signum() == 0) {
      if (this.signum() == 0) {
        throw new ArithmeticException("Division undefined");
      }
      throw new ArithmeticException("Division by zero");
    }
    int preferredScale = saturateLong((long) this.scale - divisor.scale);
    if (this.signum() == 0) {
      return zeroValueOf(preferredScale);
    }
    MathContext mc = new MathContext(
        (int) Math.min(this.precision() + (long) Math.ceil(10.0 * divisor.precision() / 3.0), Integer.MAX_VALUE),
        RoundingMode.UNNECESSARY);
    BigDecimal quotient;
    try {
      quotient = this.divide(divisor, mc);
    } catch (ArithmeticException e) {
      throw new ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.");
    }
    int quotientScale = quotient.scale();
    if (preferredScale > quotientScale) {
      return quotient.setScale(preferredScale, ROUND_UNNECESSARY);
    }
    return quotient;
  }

  public BigDecimal divide(BigDecimal divisor, MathContext mc) {
    int mcp = mc.precision;
    if (mcp == 0) {
      return divide(divisor);
    }
    BigDecimal dividend = this;
    long preferredScale = (long) dividend.scale - divisor.scale;
    if (divisor.signum() == 0) {
      if (dividend.signum() == 0) {
        throw new ArithmeticException("Division undefined");
      }
      throw new ArithmeticException("Division by zero");
    }
    if (dividend.signum() == 0) {
      return zeroValueOf(saturateLong(preferredScale));
    }
    int xscale = dividend.precision();
    int yscale = divisor.precision();
    if (compareMagnitudeNormalized(dividend.intVal, xscale, divisor.intVal, yscale) > 0) {
      yscale -= 1;
    }
    int roundingMode = mc.roundingMode.oldMode;
    int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
    BigDecimal quotient = divideScaled(dividend.intVal, dividend.scale, divisor.intVal, divisor.scale, scl,
        roundingMode, checkScaleNonZero(preferredScale));
    return doRound(quotient, mc);
  }

  private static int compareMagnitudeNormalized(BigInteger xs, int xscale, BigInteger ys, int yscale) {
    int sdiff = xscale - yscale;
    BigInteger a = xs.abs();
    BigInteger b = ys.abs();
    if (sdiff != 0) {
      if (sdiff < 0) {
        a = a.multiply(tenPow(-(long) sdiff));
      } else {
        b = b.multiply(tenPow(sdiff));
      }
    }
    return a.compareTo(b);
  }

  private static BigDecimal divideScaled(BigInteger u1, int s1, BigInteger u2, int s2, int scl, int roundingMode,
      long preferredScale) {
    long e = (long) scl - s1 + s2;
    BigInteger num = u1;
    BigInteger den = u2;
    if (e >= 0) {
      if (e > Integer.MAX_VALUE) {
        throw new ArithmeticException("Overflow");
      }
      if (u1.signum() != 0) {
        num = u1.multiply(tenPow(e));
      }
    } else {
      if (-e > Integer.MAX_VALUE) {
        throw new ArithmeticException("Overflow");
      }
      den = u2.multiply(tenPow(-e));
    }
    BigInteger[] qr = num.divideAndRemainder(den);
    if (qr[1].signum() != 0) {
      return new BigDecimal(roundQuotient(qr[0], qr[1], den, num.signum() * den.signum(), roundingMode), scl);
    }
    BigDecimal q = new BigDecimal(qr[0], scl);
    if (preferredScale != scl) {
      return stripZerosToMatchScale(qr[0], scl, preferredScale);
    }
    return q;
  }

  private static BigInteger roundQuotient(BigInteger q, BigInteger r, BigInteger divisor, int qsign, int roundingMode) {
    int cmpFracHalf = r.abs().shiftLeft(1).compareTo(divisor.abs());
    if (needIncrement(roundingMode, qsign, cmpFracHalf, q.testBit(0))) {
      return q.add(BigInteger.valueOf(qsign));
    }
    return q;
  }

  private static boolean needIncrement(int roundingMode, int qsign, int cmpFracHalf, boolean oddQuot) {
    switch (roundingMode) {
      case ROUND_UNNECESSARY:
        throw new ArithmeticException("Rounding necessary");
      case ROUND_UP:
        return true;
      case ROUND_DOWN:
        return false;
      case ROUND_CEILING:
        return qsign > 0;
      case ROUND_FLOOR:
        return qsign < 0;
      default:
        if (cmpFracHalf < 0) {
          return false;
        } else if (cmpFracHalf > 0) {
          return true;
        }
        switch (roundingMode) {
          case ROUND_HALF_DOWN:
            return false;
          case ROUND_HALF_UP:
            return true;
          case ROUND_HALF_EVEN:
            return oddQuot;
          default:
            throw new AssertionError("Unexpected rounding mode" + roundingMode);
        }
    }
  }

  private static BigInteger divideAndRound(BigInteger dividend, BigInteger divisor, int roundingMode) {
    BigInteger[] qr = dividend.divideAndRemainder(divisor);
    if (qr[1].signum() == 0) {
      return qr[0];
    }
    return roundQuotient(qr[0], qr[1], divisor, dividend.signum() * divisor.signum(), roundingMode);
  }

  private static BigDecimal doRound(BigDecimal val, MathContext mc) {
    int mcp = mc.precision;
    boolean wasDivided = false;
    if (mcp > 0) {
      BigInteger intVal = val.intVal;
      int scale = val.scale;
      int prec = val.precision();
      int drop = prec - mcp;
      while (drop > 0) {
        scale = val.checkScaleNonZeroInstance((long) scale - drop);
        intVal = divideAndRound(intVal, tenPow(drop), mc.roundingMode.oldMode);
        wasDivided = true;
        prec = digitLength(intVal);
        drop = prec - mcp;
      }
      if (wasDivided) {
        return new BigDecimal(intVal, scale, prec);
      }
    }
    return val;
  }

  private static BigDecimal doRound(BigInteger intVal, int scale, MathContext mc) {
    int mcp = mc.precision;
    int prec = 0;
    if (mcp > 0) {
      prec = digitLength(intVal);
      int drop = prec - mcp;
      while (drop > 0) {
        scale = checkScaleNonZero((long) scale - drop);
        intVal = divideAndRound(intVal, tenPow(drop), mc.roundingMode.oldMode);
        prec = digitLength(intVal);
        drop = prec - mcp;
      }
    }
    return new BigDecimal(intVal, scale, prec);
  }

  private int checkScaleNonZeroInstance(long val) {
    return checkScaleNonZero(val);
  }

  public BigDecimal divideToIntegralValue(BigDecimal divisor) {
    int preferredScale = saturateLong((long) this.scale - divisor.scale);
    if (this.compareMagnitude(divisor) < 0) {
      return zeroValueOf(preferredScale);
    }
    if (this.signum() == 0 && divisor.signum() != 0) {
      return this.setScale(preferredScale, ROUND_UNNECESSARY);
    }
    int maxDigits = (int) Math.min(this.precision() + (long) Math.ceil(10.0 * divisor.precision() / 3.0)
        + Math.abs((long) this.scale() - divisor.scale()) + 2, Integer.MAX_VALUE);
    BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits, RoundingMode.DOWN));
    if (quotient.scale > 0) {
      quotient = quotient.setScale(0, RoundingMode.DOWN);
      quotient = stripZerosToMatchScale(quotient.intVal, quotient.scale, preferredScale);
    }
    if (quotient.scale < preferredScale) {
      quotient = quotient.setScale(preferredScale, ROUND_UNNECESSARY);
    }
    return quotient;
  }

  public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
    if (mc.precision == 0 || (this.compareMagnitude(divisor) < 0)) {
      return divideToIntegralValue(divisor);
    }
    int preferredScale = saturateLong((long) this.scale - divisor.scale);
    BigDecimal result = this.divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));
    if (result.scale() < 0) {
      BigDecimal product = result.multiply(divisor);
      if (this.subtract(product).compareMagnitude(divisor) >= 0) {
        throw new ArithmeticException("Division impossible");
      }
    } else if (result.scale() > 0) {
      result = result.setScale(0, RoundingMode.DOWN);
    }
    int precisionDiff;
    if ((preferredScale > result.scale()) && (precisionDiff = mc.precision - result.precision()) > 0) {
      return result.setScale(result.scale() + Math.min(precisionDiff, preferredScale - result.scale));
    } else {
      return stripZerosToMatchScale(result.intVal, result.scale, preferredScale);
    }
  }

  public BigDecimal remainder(BigDecimal divisor) {
    BigDecimal[] divrem = this.divideAndRemainder(divisor);
    return divrem[1];
  }

  public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
    BigDecimal[] divrem = this.divideAndRemainder(divisor, mc);
    return divrem[1];
  }

  public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
    BigDecimal[] result = new BigDecimal[2];
    result[0] = this.divideToIntegralValue(divisor);
    result[1] = this.subtract(result[0].multiply(divisor));
    return result;
  }

  public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
    if (mc.precision == 0 || this.compareMagnitude(divisor) < 0) {
      return divideAndRemainder(divisor);
    }
    BigDecimal[] result = new BigDecimal[2];
    result[0] = this.divideToIntegralValue(divisor, mc);
    result[1] = this.subtract(result[0].multiply(divisor));
    return result;
  }

  public BigDecimal sqrt(MathContext mc) {
    int signum = signum();
    if (signum == 1) {
      int preferredScale = this.scale() / 2;
      BigDecimal zeroWithFinalPreferredScale = valueOf(0L, preferredScale);
      BigDecimal stripped = this.stripTrailingZeros();
      int strippedScale = stripped.scale();
      if (stripped.intVal.equals(BigInteger.ONE) && strippedScale % 2 == 0) {
        BigDecimal result = valueOf(1L, strippedScale / 2);
        if (result.scale() != preferredScale) {
          result = result.add(zeroWithFinalPreferredScale, mc);
        }
        return result;
      }
      int scaleAdjust = 0;
      int scale = stripped.scale() - stripped.precision() + 1;
      if (scale % 2 == 0) {
        scaleAdjust = scale;
      } else {
        scaleAdjust = scale - 1;
      }
      BigDecimal working = stripped.scaleByPowerOfTen(scaleAdjust);
      BigDecimal guess = new BigDecimal(Math.sqrt(working.doubleValue()));
      int guessPrecision = 15;
      int originalPrecision = mc.getPrecision();
      int targetPrecision;
      if (originalPrecision == 0) {
        targetPrecision = stripped.precision() / 2 + 1;
      } else {
        switch (mc.getRoundingMode()) {
          case HALF_UP:
          case HALF_DOWN:
          case HALF_EVEN:
            targetPrecision = 2 * originalPrecision;
            if (targetPrecision < 0) {
              targetPrecision = Integer.MAX_VALUE - 2;
            }
            break;
          default:
            targetPrecision = originalPrecision;
            break;
        }
      }
      BigDecimal approx = guess;
      int workingPrecision = working.precision();
      do {
        int tmpPrecision = Math.max(Math.max(guessPrecision, targetPrecision + 2), workingPrecision);
        MathContext mcTmp = new MathContext(tmpPrecision, RoundingMode.HALF_EVEN);
        approx = kOneHalf.multiply(approx.add(working.divide(approx, mcTmp), mcTmp));
        guessPrecision *= 2;
      } while (guessPrecision < targetPrecision + 2);
      BigDecimal result;
      RoundingMode targetRm = mc.getRoundingMode();
      if (targetRm == RoundingMode.UNNECESSARY || originalPrecision == 0) {
        RoundingMode tmpRm = (targetRm == RoundingMode.UNNECESSARY) ? RoundingMode.DOWN : targetRm;
        MathContext mcTmp = new MathContext(targetPrecision, tmpRm);
        result = approx.scaleByPowerOfTen(-scaleAdjust / 2).round(mcTmp);
        if (this.subtract(result.multiply(result)).compareTo(ZERO) != 0) {
          throw new ArithmeticException("Computed square root not exact.");
        }
      } else {
        result = approx.scaleByPowerOfTen(-scaleAdjust / 2).round(mc);
        switch (targetRm) {
          case DOWN:
          case FLOOR:
            if (result.multiply(result).compareTo(this) > 0) {
              BigDecimal ulp = result.ulp();
              if (approx.compareTo(ONE) == 0) {
                ulp = ulp.multiply(kOneTenth);
              }
              result = result.subtract(ulp);
            }
            break;
          case UP:
          case CEILING:
            if (result.multiply(result).compareTo(this) < 0) {
              result = result.add(result.ulp());
            }
            break;
          default:
            break;
        }
      }
      if (result.scale() != preferredScale) {
        result = result.stripTrailingZeros().add(zeroWithFinalPreferredScale,
            new MathContext(originalPrecision, RoundingMode.UNNECESSARY));
      }
      return result;
    } else {
      switch (signum) {
        case -1:
          throw new ArithmeticException("Attempted square root of negative BigDecimal");
        case 0:
          return valueOf(0L, scale() / 2);
        default:
          throw new AssertionError("Bad value from signum");
      }
    }
  }

  public BigDecimal pow(int n) {
    if (n < 0 || n > 999999999) {
      throw new ArithmeticException("Invalid operation");
    }
    int newScale = checkScale((long) scale * n);
    return new BigDecimal(intVal.pow(n), newScale);
  }

  public BigDecimal pow(int n, MathContext mc) {
    if (mc.precision == 0) {
      return pow(n);
    }
    if (n < -999999999 || n > 999999999) {
      throw new ArithmeticException("Invalid operation");
    }
    if (n == 0) {
      return ONE;
    }
    BigDecimal lhs = this;
    MathContext workmc = mc;
    int mag = Math.abs(n);
    if (mc.precision > 0) {
      int elength = Long.toString(mag).length();
      if (elength > mc.precision) {
        throw new ArithmeticException("Invalid operation");
      }
      workmc = new MathContext(mc.precision + elength + 1, mc.roundingMode);
    }
    BigDecimal acc = ONE;
    boolean seenbit = false;
    for (int i = 1;; i++) {
      mag += mag;
      if (mag < 0) {
        seenbit = true;
        acc = acc.multiply(lhs, workmc);
      }
      if (i == 31) {
        break;
      }
      if (seenbit) {
        acc = acc.multiply(acc, workmc);
      }
    }
    if (n < 0) {
      acc = ONE.divide(acc, workmc);
    }
    return doRound(acc, mc);
  }

  public BigDecimal abs() {
    return (signum() < 0 ? negate() : this);
  }

  public BigDecimal abs(MathContext mc) {
    return (signum() < 0 ? negate(mc) : plus(mc));
  }

  public BigDecimal negate() {
    return new BigDecimal(intVal.negate(), scale, precision);
  }

  public BigDecimal negate(MathContext mc) {
    return negate().plus(mc);
  }

  public BigDecimal plus() {
    return this;
  }

  public BigDecimal plus(MathContext mc) {
    if (mc.precision == 0) {
      return this;
    }
    return doRound(this, mc);
  }

  public int signum() {
    return intVal.signum();
  }

  public int scale() {
    return scale;
  }

  public int precision() {
    int result = precision;
    if (result == 0) {
      result = digitLength(intVal);
      precision = result;
    }
    return result;
  }

  public BigInteger unscaledValue() {
    return intVal;
  }

  public BigDecimal round(MathContext mc) {
    return plus(mc);
  }

  public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
    return setScale(newScale, roundingMode.oldMode);
  }

  public BigDecimal setScale(int newScale, int roundingMode) {
    if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY) {
      throw new IllegalArgumentException("Invalid rounding mode");
    }
    int oldScale = this.scale;
    if (newScale == oldScale) {
      return this;
    }
    if (this.signum() == 0) {
      return zeroValueOf(newScale);
    }
    if (newScale > oldScale) {
      int raise = checkScale((long) newScale - oldScale);
      return new BigDecimal(intVal.multiply(tenPow(raise)), newScale);
    } else {
      int drop = checkScale((long) oldScale - newScale);
      if (drop < digitLength(intVal)) {
        return new BigDecimal(divideAndRound(intVal, tenPow(drop), roundingMode), newScale);
      }
      return new BigDecimal(roundToZero(intVal, drop, roundingMode), newScale);
    }
  }

  private static BigInteger roundToZero(BigInteger v, int drop, int roundingMode) {
    return divideAndRound(v, tenPow(drop), roundingMode);
  }

  public BigDecimal setScale(int newScale) {
    return setScale(newScale, ROUND_UNNECESSARY);
  }

  public BigDecimal movePointLeft(int n) {
    int newScale = checkScale((long) scale + n);
    BigDecimal num = new BigDecimal(intVal, newScale);
    return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
  }

  public BigDecimal movePointRight(int n) {
    int newScale = checkScale((long) scale - n);
    BigDecimal num = new BigDecimal(intVal, newScale);
    return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
  }

  public BigDecimal scaleByPowerOfTen(int n) {
    return new BigDecimal(intVal, checkScale((long) scale - n), precision);
  }

  public BigDecimal stripTrailingZeros() {
    if (intVal.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return stripZerosToMatchScale(intVal, scale, Long.MIN_VALUE);
  }

  public int compareTo(BigDecimal val) {
    if (scale == val.scale) {
      return intVal.compareTo(val.intVal);
    }
    int xsign = this.signum();
    int ysign = val.signum();
    if (xsign != ysign) {
      return (xsign > ysign) ? 1 : -1;
    }
    if (xsign == 0) {
      return 0;
    }
    int cmp = compareMagnitude(val);
    return (xsign > 0) ? cmp : -cmp;
  }

  private int compareMagnitude(BigDecimal val) {
    BigInteger xs = this.intVal;
    BigInteger ys = val.intVal;
    if (xs.signum() == 0) {
      return (ys.signum() == 0) ? 0 : -1;
    }
    if (ys.signum() == 0) {
      return 1;
    }
    long sdiff = (long) this.scale - val.scale;
    if (sdiff != 0) {
      long xae = (long) this.precision() - this.scale;
      long yae = (long) val.precision() - val.scale;
      if (xae < yae) {
        return -1;
      }
      if (xae > yae) {
        return 1;
      }
      if (sdiff < 0) {
        xs = xs.abs().multiply(tenPow(-sdiff));
        ys = ys.abs();
      } else {
        ys = ys.abs().multiply(tenPow(sdiff));
        xs = xs.abs();
      }
      return xs.compareTo(ys);
    }
    return xs.abs().compareTo(ys.abs());
  }

  @Override
  public boolean equals(Object x) {
    if (!(x instanceof BigDecimal)) {
      return false;
    }
    BigDecimal xDec = (BigDecimal) x;
    if (x == this) {
      return true;
    }
    if (scale != xDec.scale) {
      return false;
    }
    return intVal.equals(xDec.intVal);
  }

  public BigDecimal min(BigDecimal val) {
    return (compareTo(val) <= 0 ? this : val);
  }

  public BigDecimal max(BigDecimal val) {
    return (compareTo(val) >= 0 ? this : val);
  }

  @Override
  public int hashCode() {
    return 31 * intVal.hashCode() + scale;
  }

  @Override
  public String toString() {
    return BigDecimalText.layout(this, true);
  }

  public String toEngineeringString() {
    return BigDecimalText.layout(this, false);
  }

  public String toPlainString() {
    return BigDecimalText.plain(this);
  }

  public BigInteger toBigInteger() {
    return this.setScale(0, ROUND_DOWN).intVal;
  }

  public BigInteger toBigIntegerExact() {
    return this.setScale(0, ROUND_UNNECESSARY).intVal;
  }

  @Override
  public long longValue() {
    if (scale == 0) {
      return intVal.longValue();
    }
    return toBigInteger().longValue();
  }

  public long longValueExact() {
    if (scale == 0 && intVal.bitLength() <= 63) {
      return intVal.longValue();
    }
    if ((precision() - scale) > 19) {
      throw new ArithmeticException("Overflow");
    }
    if (this.signum() == 0) {
      return 0;
    }
    if ((this.precision() - this.scale) <= 0) {
      throw new ArithmeticException("Rounding necessary");
    }
    BigDecimal num = this.setScale(0, ROUND_UNNECESSARY);
    if (num.intVal.bitLength() > 63) {
      throw new ArithmeticException("Overflow");
    }
    return num.intVal.longValue();
  }

  @Override
  public int intValue() {
    return (scale == 0 && intVal.bitLength() <= 31) ? intVal.intValue() : toBigInteger().intValue();
  }

  public int intValueExact() {
    long num = this.longValueExact();
    if ((int) num != num) {
      throw new ArithmeticException("Overflow");
    }
    return (int) num;
  }

  public short shortValueExact() {
    long num = this.longValueExact();
    if ((short) num != num) {
      throw new ArithmeticException("Overflow");
    }
    return (short) num;
  }

  public byte byteValueExact() {
    long num = this.longValueExact();
    if ((byte) num != num) {
      throw new ArithmeticException("Overflow");
    }
    return (byte) num;
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
    return BigDecimal.valueOf(1, this.scale());
  }

  private static BigDecimal stripZerosToMatchScale(BigInteger intVal, int scale, long preferredScale) {
    BigInteger[] qr;
    while (intVal.compareMagnitudeInternal(BigInteger.TEN) >= 0 && scale > preferredScale) {
      if (intVal.testBit(0)) {
        break;
      }
      qr = intVal.divideAndRemainder(BigInteger.TEN);
      if (qr[1].signum() != 0) {
        break;
      }
      intVal = qr[0];
      scale = checkScaleStatic(intVal, (long) scale - 1);
    }
    return new BigDecimal(intVal, scale);
  }

  private static int checkScaleStatic(BigInteger intVal, long val) {
    int asInt = (int) val;
    if (asInt != val) {
      asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
      if (intVal.signum() != 0) {
        throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
      }
    }
    return asInt;
  }

  private int checkScale(long val) {
    return checkScaleStatic(intVal, val);
  }

  private static int checkScale(BigInteger intVal, long val) {
    return checkScaleStatic(intVal, val);
  }

  private static int checkScaleNonZero(long val) {
    int asInt = (int) val;
    if (asInt != val) {
      throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
    }
    return asInt;
  }

  private static int saturateLong(long s) {
    int i = (int) s;
    return (s == i) ? i : (s < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
  }

  private static void matchScale(BigDecimal[] val) {
    if (val[0].scale < val[1].scale) {
      val[0] = val[0].setScale(val[1].scale, ROUND_UNNECESSARY);
    } else if (val[1].scale < val[0].scale) {
      val[1] = val[1].setScale(val[0].scale, ROUND_UNNECESSARY);
    }
  }

  private static int digitLength(BigInteger b) {
    if (b.signum() == 0) {
      return 1;
    }
    int r = (int) ((((long) b.bitLength() + 1) * 646456993) >>> 31);
    return b.abs().compareTo(tenPow(r)) < 0 ? r : r + 1;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    if (intVal == null) {
      String message = "BigDecimal: null intVal in stream";
      throw new StreamCorruptedException(message);
    }
  }
}
