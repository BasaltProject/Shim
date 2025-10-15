/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;

public final class FormatterSpec {
  public static final int LEFT = 1;
  public static final int UPPER = 2;
  public static final int ALTERNATE = 4;
  public static final int PLUS = 8;
  public static final int SPACE = 16;
  public static final int ZERO = 32;
  public static final int GROUP = 64;
  public static final int PARENTHESES = 128;
  public static final int PREVIOUS = 256;

  private static final String kFlagChars = "-#+ 0,(<";
  private static final int[] kFlagValues = {LEFT, ALTERNATE, PLUS, SPACE, ZERO, GROUP, PARENTHESES, PREVIOUS};

  public int index;
  public int flags;
  public int width = -1;
  public int precision = -1;
  public boolean dateTime;
  public char conversion;
  public char dateTimeConversion;

  private FormatterSpec() { }

  public boolean has(int flag) {
    return (flags & flag) != 0;
  }

  public static String flagsToString(int flags) {
    StringBuilder builder = new StringBuilder();
    if ((flags & LEFT) != 0) builder.append('-');
    if ((flags & UPPER) != 0) builder.append('^');
    if ((flags & ALTERNATE) != 0) builder.append('#');
    if ((flags & PLUS) != 0) builder.append('+');
    if ((flags & SPACE) != 0) builder.append(' ');
    if ((flags & ZERO) != 0) builder.append('0');
    if ((flags & GROUP) != 0) builder.append(',');
    if ((flags & PARENTHESES) != 0) builder.append('(');
    if ((flags & PREVIOUS) != 0) builder.append('<');
    return builder.toString();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder("%");
    builder.append(flagsToString(flags & ~UPPER));
    if (index > 0) builder.append(index).append('$');
    if (width != -1) builder.append(width);
    if (precision != -1) builder.append('.').append(precision);
    if (dateTime) builder.append(has(UPPER) ? 'T' : 't');
    char shown = dateTime ? dateTimeConversion : conversion;
    builder.append(has(UPPER) ? Character.toUpperCase(shown) : shown);
    return builder.toString();
  }

  public static FormatterSpec parse(String format, int start, int[] end) {
    int length = format.length();
    int i = start + 1;
    if (i >= length) throw new UnknownFormatConversionException("%");
    FormatterSpec spec = new FormatterSpec();

    int digits = i;
    while (digits < length && Character.isDigit(format.charAt(digits))) ++digits;
    if (digits > i && digits < length && format.charAt(digits) == '$') {
      spec.index = parseNumber(format.substring(i, digits), true);
      i = digits + 1;
    }

    int seen = 0;
    int flagIndex;
    while (i < length && (flagIndex = kFlagChars.indexOf(format.charAt(i))) >= 0) {
      int value = kFlagValues[flagIndex];
      if ((seen & value) != 0) throw new DuplicateFormatFlagsException(flagsToString(value));
      seen |= value;
      ++i;
    }
    spec.flags = seen;

    int widthStart = i;
    while (i < length && Character.isDigit(format.charAt(i))) ++i;
    if (i > widthStart) spec.width = parseNumber(format.substring(widthStart, i), false);

    if (i < length && format.charAt(i) == '.') {
      int precisionStart = ++i;
      while (i < length && Character.isDigit(format.charAt(i))) ++i;
      if (i == precisionStart) {
        throw new UnknownFormatConversionException(".");
      }
      spec.precision = parseNumber(format.substring(precisionStart, i), false);
    }

    if (i >= length) throw new UnknownFormatConversionException("%");
    char c = format.charAt(i++);
    if (c == 't' || c == 'T') {
      spec.dateTime = true;
      if (c == 'T') spec.flags |= UPPER;
      if (i >= length) throw new UnknownFormatConversionException(String.valueOf(c));
      spec.dateTimeConversion = format.charAt(i++);
      if ("HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc".indexOf(spec.dateTimeConversion) < 0) {
        throw new UnknownFormatConversionException(String.valueOf(c) + spec.dateTimeConversion);
      }
      spec.conversion = 't';
    } else {
      if ("bBhHsScCdoxXeEfgGaAn%".indexOf(c) < 0) throw new UnknownFormatConversionException(String.valueOf(c));
      if (Character.isUpperCase(c)) {
        spec.flags |= UPPER;
        c = Character.toLowerCase(c);
      }
      spec.conversion = c;
    }
    end[0] = i;
    spec.check();
    return spec;
  }

  private static int parseNumber(String digits, boolean index) {
    try {
      return Integer.parseInt(digits);
    } catch (NumberFormatException overflow) {
      if (index) throw new IllegalFormatWidthException(Integer.MIN_VALUE);
      throw new IllegalFormatWidthException(Integer.MIN_VALUE);
    }
  }

  private void check() {
    switch (conversion) {
    case 'b':
    case 'h':
    case 's':
      checkGeneral();
      break;
    case 'c':
      checkCharacter();
      break;
    case 'd':
    case 'o':
    case 'x':
      checkInteger();
      break;
    case 'e':
    case 'f':
    case 'g':
    case 'a':
      checkFloat();
      break;
    case 't':
      checkDateTime();
      break;
    default:
      checkText();
      break;
    }
  }

  private void failMismatch(int flag) {
    throw new FormatFlagsConversionMismatchException(flagsToString(flag), dateTime ? dateTimeConversion : conversion);
  }

  private void checkBadFlags(int... bad) {
    for (int flag : bad) {
      if (has(flag)) failMismatch(flag);
    }
  }

  private void checkGeneral() {
    if ((conversion == 'b' || conversion == 'h') && has(ALTERNATE)) failMismatch(ALTERNATE);
    if (width == -1 && has(LEFT)) throw new MissingFormatWidthException(toString());
    checkBadFlags(PLUS, SPACE, ZERO, GROUP, PARENTHESES);
  }

  private void checkDateTime() {
    if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    checkBadFlags(ALTERNATE, PLUS, SPACE, ZERO, GROUP, PARENTHESES);
    if (width == -1 && has(LEFT)) throw new MissingFormatWidthException(toString());
  }

  private void checkCharacter() {
    if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    checkBadFlags(ALTERNATE, PLUS, SPACE, ZERO, GROUP, PARENTHESES);
    if (width == -1 && has(LEFT)) throw new MissingFormatWidthException(toString());
  }

  private void checkNumeric() {
    if (width != -1 && width < 0) throw new IllegalFormatWidthException(width);
    if (precision != -1 && precision < 0) throw new IllegalFormatPrecisionException(precision);
    if (width == -1 && (has(LEFT) || has(ZERO))) throw new MissingFormatWidthException(toString());
    if ((has(PLUS) && has(SPACE)) || (has(LEFT) && has(ZERO))) {
      throw new IllegalFormatFlagsException(flagsToString(flags));
    }
  }

  private void checkInteger() {
    checkNumeric();
    if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    if (conversion == 'd') checkBadFlags(ALTERNATE);
    else checkBadFlags(GROUP);
  }

  private void checkFloat() {
    checkNumeric();
    if (conversion == 'a') checkBadFlags(PARENTHESES, GROUP);
    else if (conversion == 'e') checkBadFlags(GROUP);
    else if (conversion == 'g') checkBadFlags(ALTERNATE);
  }

  private void checkText() {
    if (precision != -1) throw new IllegalFormatPrecisionException(precision);
    if (conversion == '%') {
      if (flags != 0 && flags != LEFT) throw new IllegalFormatFlagsException(flagsToString(flags));
      if (width == -1 && has(LEFT)) throw new MissingFormatWidthException(toString());
    } else {
      if (width != -1) throw new IllegalFormatWidthException(width);
      if (flags != 0) throw new IllegalFormatFlagsException(flagsToString(flags));
    }
  }

  public void failBadFlags(int... bad) {
    checkBadFlags(bad);
  }
}
