/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class FormatterNumbers {
  private FormatterNumbers() { }

  private static int adjustWidth(int width, FormatterSpec f, boolean negative) {
    return width != -1 && negative && f.has(FormatterSpec.PARENTHESES) ? width - 1 : width;
  }

  private static void leadingSign(StringBuilder sb, FormatterSpec f, boolean negative) {
    if (!negative) {
      if (f.has(FormatterSpec.PLUS)) sb.append('+');
      else if (f.has(FormatterSpec.SPACE)) sb.append(' ');
    } else {
      sb.append(f.has(FormatterSpec.PARENTHESES) ? '(' : '-');
    }
  }

  private static void trailingSign(StringBuilder sb, FormatterSpec f, boolean negative) {
    if (negative && f.has(FormatterSpec.PARENTHESES)) sb.append(')');
  }

  private static void zeros(StringBuilder sb, int count) {
    for (int i = 0; i < count; ++i) sb.append('0');
  }

  private static void magnitude(StringBuilder sb, CharSequence value, int offset, FormatterSpec f, int width) {
    int begin = sb.length();
    int length = value.length();
    int dot = length;
    for (int i = offset; i < length; ++i) {
      if (value.charAt(i) == '.') {
        dot = i;
        break;
      }
    }
    char groupSeparator = f.has(FormatterSpec.GROUP) ? ',' : '\0';
    for (int i = offset; i < length; ++i) {
      if (i == dot) {
        sb.append('.');
        groupSeparator = '\0';
        continue;
      }
      sb.append(value.charAt(i));
      if (groupSeparator != '\0' && i != dot - 1 && (dot - i) % 3 == 1) sb.append(groupSeparator);
    }
    if (width != -1 && f.has(FormatterSpec.ZERO)) {
      for (int k = sb.length(); k < width; ++k) sb.insert(begin, '0');
    }
  }

  public static String formatLong(FormatterSpec f, long value) {
    StringBuilder sb = new StringBuilder();
    if (f.conversion == 'd') {
      boolean negative = value < 0;
      leadingSign(sb, f, negative);
      magnitude(sb, Long.toString(value, 10), negative ? 1 : 0, f, adjustWidth(f.width, f, negative));
      trailingSign(sb, f, negative);
    } else if (f.conversion == 'o') {
      f.failBadFlags(FormatterSpec.PARENTHESES, FormatterSpec.SPACE, FormatterSpec.PLUS);
      String s = Long.toOctalString(value);
      int length = f.has(FormatterSpec.ALTERNATE) ? s.length() + 1 : s.length();
      if (f.has(FormatterSpec.ALTERNATE)) sb.append('0');
      if (f.has(FormatterSpec.ZERO)) zeros(sb, f.width - length);
      sb.append(s);
    } else {
      f.failBadFlags(FormatterSpec.PARENTHESES, FormatterSpec.SPACE, FormatterSpec.PLUS);
      String s = Long.toHexString(value);
      int length = f.has(FormatterSpec.ALTERNATE) ? s.length() + 2 : s.length();
      if (f.has(FormatterSpec.ALTERNATE)) sb.append(f.has(FormatterSpec.UPPER) ? "0X" : "0x");
      if (f.has(FormatterSpec.ZERO)) zeros(sb, f.width - length);
      sb.append(f.has(FormatterSpec.UPPER) ? s.toUpperCase() : s);
    }
    return sb.toString();
  }

  public static String formatBigInteger(FormatterSpec f, BigInteger value) {
    StringBuilder sb = new StringBuilder();
    boolean negative = value.signum() == -1;
    BigInteger magnitude = value.abs();
    leadingSign(sb, f, negative);
    if (f.conversion == 'd') {
      magnitude(sb, magnitude.toString(), 0, f, adjustWidth(f.width, f, negative));
    } else if (f.conversion == 'o') {
      String s = magnitude.toString(8);
      int length = s.length() + sb.length();
      if (negative && f.has(FormatterSpec.PARENTHESES)) ++length;
      if (f.has(FormatterSpec.ALTERNATE)) {
        ++length;
        sb.append('0');
      }
      if (f.has(FormatterSpec.ZERO)) zeros(sb, f.width - length);
      sb.append(s);
    } else {
      String s = magnitude.toString(16);
      int length = s.length() + sb.length();
      if (negative && f.has(FormatterSpec.PARENTHESES)) ++length;
      if (f.has(FormatterSpec.ALTERNATE)) {
        length += 2;
        sb.append(f.has(FormatterSpec.UPPER) ? "0X" : "0x");
      }
      if (f.has(FormatterSpec.ZERO)) zeros(sb, f.width - length);
      sb.append(f.has(FormatterSpec.UPPER) ? s.toUpperCase() : s);
    }
    trailingSign(sb, f, negative);
    return sb.toString();
  }

  public static String formatDouble(FormatterSpec f, double value) {
    StringBuilder sb = new StringBuilder();
    boolean upper = f.has(FormatterSpec.UPPER);
    if (Double.isNaN(value)) {
      sb.append(upper ? "NAN" : "NaN");
      return sb.toString();
    }
    boolean negative = Double.compare(value, 0.0d) == -1;
    double magnitude = Math.abs(value);
    leadingSign(sb, f, negative);
    if (Double.isInfinite(magnitude)) {
      sb.append(upper ? "INFINITY" : "Infinity");
    } else if (f.conversion == 'a') {
      appendHex(sb, f, magnitude, negative);
    } else {
      appendDecimal(sb, f, new BigDecimal(Double.toString(magnitude)), magnitude == 0.0d, negative);
    }
    trailingSign(sb, f, negative);
    return sb.toString();
  }

  public static String formatBigDecimal(FormatterSpec f, BigDecimal value) {
    StringBuilder sb = new StringBuilder();
    if (f.conversion == 'a') throw new java.util.IllegalFormatConversionException('a', BigDecimal.class);
    boolean negative = value.signum() == -1;
    BigDecimal magnitude = value.abs();
    leadingSign(sb, f, negative);
    appendDecimal(sb, f, magnitude, magnitude.signum() == 0, negative);
    trailingSign(sb, f, negative);
    return sb.toString();
  }

  private static void addZeros(StringBuilder sb, int precision) {
    int dot = sb.indexOf(".");
    int fraction = dot < 0 ? 0 : sb.length() - dot - 1;
    if (dot < 0 && precision > 0) sb.append('.');
    for (int i = fraction; i < precision; ++i) sb.append('0');
  }

  private static void appendDecimal(StringBuilder sb, FormatterSpec f, BigDecimal value, boolean zero,
                                    boolean negative) {
    boolean upper = f.has(FormatterSpec.UPPER);
    switch (f.conversion) {
    case 'f': {
      int precision = f.precision == -1 ? 6 : f.precision;
      StringBuilder mantissa = new StringBuilder(value.setScale(precision, RoundingMode.HALF_UP).toPlainString());
      addZeros(mantissa, precision);
      if (f.has(FormatterSpec.ALTERNATE) && precision == 0) mantissa.append('.');
      magnitude(sb, mantissa, 0, f, adjustWidth(f.width, f, negative));
      break;
    }
    case 'e': {
      int precision = f.precision == -1 ? 6 : f.precision;
      StringBuilder mantissa = new StringBuilder();
      int exponent = scientific(value, zero, precision, mantissa);
      addZeros(mantissa, precision);
      if (f.has(FormatterSpec.ALTERNATE) && precision == 0) mantissa.append('.');
      String exp = exponentString(exponent);
      int width = f.width == -1 ? -1 : adjustWidth(f.width - exp.length() - 1, f, negative);
      magnitude(sb, mantissa, 0, f, width);
      sb.append(upper ? 'E' : 'e').append(exp);
      break;
    }
    default: {
      int precision = f.precision == -1 ? 6 : (f.precision == 0 ? 1 : f.precision);
      StringBuilder mantissa = new StringBuilder();
      int exponent = 0;
      boolean fixed = true;
      if (zero) {
        mantissa.append('0');
      } else {
        BigDecimal rounded = value.round(new MathContext(precision, RoundingMode.HALF_UP));
        exponent = rounded.precision() - rounded.scale() - 1;
        if (exponent >= -4 && exponent < precision) {
          mantissa.append(rounded.setScale(Math.max(precision - exponent - 1, 0), RoundingMode.HALF_UP)
                              .toPlainString());
        } else {
          fixed = false;
          scientific(value, false, precision - 1, mantissa);
        }
      }
      int decimals = fixed ? precision - exponent - 1 : precision - 1;
      if (zero) decimals = precision - 1;
      addZeros(mantissa, decimals);
      if (f.has(FormatterSpec.ALTERNATE) && decimals == 0) mantissa.append('.');
      if (fixed) {
        magnitude(sb, mantissa, 0, f, adjustWidth(f.width, f, negative));
      } else {
        String exp = exponentString(exponent);
        int width = f.width == -1 ? -1 : adjustWidth(f.width - exp.length() - 1, f, negative);
        magnitude(sb, mantissa, 0, f, width);
        sb.append(upper ? 'E' : 'e').append(exp);
      }
      break;
    }
    }
  }

  private static int scientific(BigDecimal value, boolean zero, int precision, StringBuilder mantissa) {
    if (zero) {
      mantissa.append('0');
      return 0;
    }
    BigDecimal rounded = value.round(new MathContext(precision + 1, RoundingMode.HALF_UP));
    String digits = rounded.unscaledValue().toString();
    int end = digits.length();
    while (end > 1 && digits.charAt(end - 1) == '0') --end;
    mantissa.append(digits.charAt(0));
    if (end > 1) mantissa.append('.').append(digits, 1, end);
    return rounded.precision() - rounded.scale() - 1;
  }

  private static String exponentString(int exponent) {
    StringBuilder sb = new StringBuilder();
    sb.append(exponent < 0 ? '-' : '+');
    int magnitude = Math.abs(exponent);
    if (magnitude < 10) sb.append('0');
    sb.append(magnitude);
    return sb.toString();
  }

  private static void appendHex(StringBuilder sb, FormatterSpec f, double value, boolean negative) {
    String hex = Double.toHexString(value);
    if (f.precision != -1 && value != 0.0d && !Double.isNaN(value)) {
      hex = roundHex(value, f.precision);
    }
    if (f.has(FormatterSpec.UPPER)) hex = hex.toUpperCase();
    int width = f.width == -1 ? -1 : adjustWidth(f.width, f, negative);
    if (width != -1 && f.has(FormatterSpec.ZERO)) {
      int prefix = hex.indexOf('x') + 1;
      StringBuilder padded = new StringBuilder(hex);
      for (int k = sb.length() + hex.length(); k < width; ++k) padded.insert(prefix, '0');
      hex = padded.toString();
    }
    sb.append(hex);
  }

  private static String roundHex(double value, int precision) {
    String hex = Double.toHexString(value);
    int dot = hex.indexOf('.');
    int p = hex.indexOf('p');
    String lead = hex.substring(0, dot < 0 ? p : dot);
    String fraction = dot < 0 ? "" : hex.substring(dot + 1, p);
    int exponent = Integer.parseInt(hex.substring(p + 1));
    if (fraction.length() <= precision) {
      StringBuilder sb = new StringBuilder(lead);
      if (precision > 0) sb.append('.').append(fraction);
      for (int i = fraction.length(); i < precision; ++i) sb.append('0');
      return sb.append('p').append(exponent).toString();
    }
    boolean up = Character.digit(fraction.charAt(precision), 16) >= 8;
    int leadDigit = Character.digit(lead.charAt(lead.length() - 1), 16);
    String kept = fraction.substring(0, precision);
    if (up) {
      if (precision == 0) {
        ++leadDigit;
      } else {
        BigInteger digits = new BigInteger(kept, 16).add(BigInteger.ONE);
        String text = digits.toString(16);
        if (text.length() > precision) {
          ++leadDigit;
          text = text.substring(1);
        }
        StringBuilder padded = new StringBuilder();
        for (int i = text.length(); i < precision; ++i) padded.append('0');
        kept = padded.append(text).toString();
      }
    }
    if (leadDigit == 2) {
      leadDigit = 1;
      ++exponent;
    }
    StringBuilder sb = new StringBuilder(lead.substring(0, lead.length() - 1)).append(leadDigit);
    if (precision > 0) sb.append('.').append(kept);
    return sb.append('p').append(exponent).toString();
  }
}
