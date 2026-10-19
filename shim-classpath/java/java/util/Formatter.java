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

package java.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

import shim.FormatterDates;
import shim.FormatterNumbers;
import shim.FormatterSpec;

public final class Formatter implements Closeable, Flushable {
  public enum BigDecimalLayoutForm { SCIENTIFIC, DECIMAL_FLOAT }

  private Appendable a;

  private final Locale l;
  private Locale callLocale;

  private IOException lastException;

  public Formatter() {
    this(new StringBuilder(), Locale.getDefault());
  }

  public Formatter(Appendable a) {
    this(a == null ? new StringBuilder() : a, Locale.getDefault());
  }

  public Formatter(Locale l) {
    this(new StringBuilder(), l);
  }

  public Formatter(Appendable a, Locale l) {
    this.a = a == null ? new StringBuilder() : a;
    this.l = l;
  }

  public Formatter(String fileName) throws FileNotFoundException {
    this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
  }

  public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
    this(fileName, csn, Locale.getDefault());
  }

  public Formatter(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
    this(new File(fileName), csn, l);
  }

  public Formatter(String fileName, Charset charset, Locale l) throws IOException {
    this(new File(fileName), charset, l);
  }

  public Formatter(File file) throws FileNotFoundException {
    this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
  }

  public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
    this(file, csn, Locale.getDefault());
  }

  public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
    this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn)), l);
  }

  public Formatter(File file, Charset charset, Locale l) throws IOException {
    this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)), l);
  }

  public Formatter(PrintStream ps) {
    this((Appendable) Objects.requireNonNull(ps));
  }

  public Formatter(OutputStream os) {
    this(new BufferedWriter(new OutputStreamWriter(os)));
  }

  public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
    this(os, csn, Locale.getDefault());
  }

  public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
    this(new BufferedWriter(new OutputStreamWriter(os, csn)), l);
  }

  public Formatter(OutputStream os, Charset charset, Locale l) {
    this(new BufferedWriter(new OutputStreamWriter(os, charset)), l);
  }

  private void ensureOpen() {
    if (a == null) {
      throw new FormatterClosedException();
    }
  }

  public Locale locale() {
    ensureOpen();
    return l;
  }

  public Appendable out() {
    ensureOpen();
    return a;
  }

  public String toString() {
    ensureOpen();
    return a.toString();
  }

  public void flush() {
    ensureOpen();
    if (a instanceof Flushable) {
      try {
        ((Flushable) a).flush();
      } catch (IOException ioe) {
        lastException = ioe;
      }
    }
  }

  public void close() {
    if (a == null) {
      return;
    }
    try {
      if (a instanceof Closeable) {
        ((Closeable) a).close();
      }
    } catch (IOException ioe) {
      lastException = ioe;
    } finally {
      a = null;
    }
  }

  public IOException ioException() {
    return lastException;
  }

  public Formatter format(String format, Object... args) {
    return format(l, format, args);
  }

  public Formatter format(Locale l, String format, Object... args) {
    ensureOpen();
    int length = format.length();
    int[] end = new int[1];
    int i = 0;
    List<Object> pieces = new ArrayList<Object>();
    while (i < length) {
      int percent = format.indexOf('%', i);
      int stop = percent < 0 ? length : percent;
      if (stop > i) {
        pieces.add(format.substring(i, stop));
      }
      if (percent < 0) {
        break;
      }
      pieces.add(FormatterSpec.parse(format, percent, end));
      i = end[0];
    }
    Locale previous = callLocale;
    callLocale = l;
    try {
      int lastOrdinary = -1;
      int last = -1;
      for (Object piece : pieces) {
        if (piece instanceof String) {
          append((String) piece);
          continue;
        }
        FormatterSpec spec = (FormatterSpec) piece;
        if (spec.conversion == 'n') {
          append(System.lineSeparator());
          continue;
        }
        if (spec.conversion == '%') {
          justify("%", spec);
          continue;
        }
        int argument;
        if (spec.has(FormatterSpec.PREVIOUS)) {
          if (last < 0 || (args != null && last > args.length - 1)) {
            throw new MissingFormatArgumentException(spec.toString());
          }
          argument = last;
        } else if (spec.index == 0) {
          argument = ++lastOrdinary;
          last = argument;
          if (args != null && argument > args.length - 1) {
            throw new MissingFormatArgumentException(spec.toString());
          }
        } else {
          argument = spec.index - 1;
          last = argument;
          if (args != null && argument > args.length - 1) {
            throw new MissingFormatArgumentException(spec.toString());
          }
        }
        print(spec, args == null ? null : args[argument]);
      }
    } finally {
      callLocale = previous;
    }
    return this;
  }

  private void append(CharSequence text) {
    try {
      a.append(text);
    } catch (IOException ioe) {
      lastException = ioe;
    }
  }

  private void justify(String text, FormatterSpec spec) {
    if (spec.width == -1) {
      append(text);
      return;
    }
    StringBuilder sb = new StringBuilder();
    boolean padRight = spec.has(FormatterSpec.LEFT);
    int padding = spec.width - text.length();
    if (padRight) sb.append(text);
    for (int i = 0; i < padding; ++i) sb.append(' ');
    if (!padRight) sb.append(text);
    append(sb);
  }

  private void printString(FormatterSpec spec, String text) {
    if (spec.precision != -1 && spec.precision < text.length()) {
      text = text.substring(0, spec.precision);
    }
    if (spec.has(FormatterSpec.UPPER)) {
      text = text.toUpperCase();
    }
    justify(text, spec);
  }

  private void failConversion(FormatterSpec spec, Object arg) {
    throw new IllegalFormatConversionException(spec.dateTime ? spec.dateTimeConversion : spec.conversion,
        arg.getClass());
  }

  private void print(FormatterSpec spec, Object arg) {
    switch (spec.conversion) {
    case 'b':
      printString(spec, arg == null ? "false" : (arg instanceof Boolean ? arg.toString() : "true"));
      break;
    case 'h':
      printString(spec, arg == null ? "null" : Integer.toHexString(arg.hashCode()));
      break;
    case 's':
      if (arg instanceof Formattable) {
        int flags = (spec.has(FormatterSpec.UPPER) ? FormattableFlags.UPPERCASE : 0)
            | (spec.has(FormatterSpec.ALTERNATE) ? FormattableFlags.ALTERNATE : 0)
            | (spec.has(FormatterSpec.LEFT) ? FormattableFlags.LEFT_JUSTIFY : 0);
        ((Formattable) arg).formatTo(this, flags, spec.width, spec.precision);
      } else {
        if (spec.has(FormatterSpec.ALTERNATE)) {
          throw new FormatFlagsConversionMismatchException("#", 's');
        }
        printString(spec, String.valueOf(arg));
      }
      break;
    case 'c':
      printCharacter(spec, arg);
      break;
    case 'd':
    case 'o':
    case 'x':
      printInteger(spec, arg);
      break;
    case 'e':
    case 'f':
    case 'g':
    case 'a':
      printFloat(spec, arg);
      break;
    case 't':
      printDateTime(spec, arg);
      break;
    default:
      printString(spec, String.valueOf(arg));
      break;
    }
  }

  private void printDateTime(FormatterSpec spec, Object arg) {
    if (arg == null) {
      printString(spec, "null");
      return;
    }
    if (!(arg instanceof Long || arg instanceof Date || arg instanceof Calendar
        || arg instanceof java.time.temporal.TemporalAccessor)) {
      failConversion(spec, arg);
      return;
    }
    String text;
    try {
      text = FormatterDates.format(spec.dateTimeConversion, arg, callLocale);
    } catch (IllegalFormatConversionException e) {
      throw e;
    }
    justify(spec.has(FormatterSpec.UPPER) ? text.toUpperCase(callLocale == null ? Locale.US : callLocale) : text,
        spec);
  }

  private void printCharacter(FormatterSpec spec, Object arg) {
    if (arg == null) {
      printString(spec, "null");
      return;
    }
    String text;
    if (arg instanceof Character) {
      text = arg.toString();
    } else if (arg instanceof Byte || arg instanceof Short || arg instanceof Integer) {
      int codePoint = ((Number) arg).intValue();
      if (!Character.isValidCodePoint(codePoint)) {
        throw new IllegalFormatCodePointException(codePoint);
      }
      text = new String(Character.toChars(codePoint));
    } else {
      failConversion(spec, arg);
      return;
    }
    printString(spec, text);
  }

  private static String regroup(String text, int size) {
    int start = 0;
    while (start < text.length() && !(text.charAt(start) >= '0' && text.charAt(start) <= '9')) {
      start++;
    }
    int end = start;
    while (end < text.length() && ((text.charAt(end) >= '0' && text.charAt(end) <= '9') || text.charAt(end) == ',')) {
      end++;
    }
    StringBuilder digits = new StringBuilder();
    for (int i = start; i < end; i++) {
      if (text.charAt(i) != ',') {
        digits.append(text.charAt(i));
      }
    }
    StringBuilder grouped = new StringBuilder();
    int first = digits.length() % size;
    for (int i = 0; i < digits.length(); i++) {
      if (i > 0 && (i - first) % size == 0) {
        grouped.append(',');
      }
      grouped.append(digits.charAt(i));
    }
    return text.substring(0, start) + grouped + text.substring(end);
  }

  private String localize(String text, FormatterSpec spec) {
    char conversion = spec.conversion;
    if (conversion == 'o' || conversion == 'x' || conversion == 'X' || conversion == 'a' || conversion == 'A') {
      return text;
    }
    Locale locale = callLocale;
    if (locale == null) {
      return text;
    }
    java.text.DecimalFormatSymbols symbols = java.text.DecimalFormatSymbols.getInstance(locale);
    char zero = symbols.getZeroDigit();
    char grouping = symbols.getGroupingSeparator();
    char decimal = symbols.getDecimalSeparator();
    int groupSize = 3;
    if (text.indexOf(',') >= 0) {
      java.text.NumberFormat integers = java.text.NumberFormat.getIntegerInstance(locale);
      if (integers instanceof java.text.DecimalFormat) {
        groupSize = ((java.text.DecimalFormat) integers).getGroupingSize();
      }
      if (groupSize != 3 && groupSize > 0) {
        text = regroup(text, groupSize);
      }
    }
    if (zero == '0' && grouping == ',' && decimal == '.') {
      return text;
    }
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c >= '0' && c <= '9') {
        sb.append((char) (zero + (c - '0')));
      } else if (c == ',') {
        sb.append(grouping);
      } else if (c == '.') {
        sb.append(decimal);
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private void printInteger(FormatterSpec spec, Object arg) {
    if (arg == null) {
      printString(spec, "null");
      return;
    }
    String text;
    boolean unsigned = spec.conversion != 'd';
    if (arg instanceof Byte) {
      long v = ((Byte) arg).byteValue();
      text = FormatterNumbers.formatLong(spec, v < 0 && unsigned ? v + (1L << 8) : v);
    } else if (arg instanceof Short) {
      long v = ((Short) arg).shortValue();
      text = FormatterNumbers.formatLong(spec, v < 0 && unsigned ? v + (1L << 16) : v);
    } else if (arg instanceof Integer) {
      long v = ((Integer) arg).intValue();
      text = FormatterNumbers.formatLong(spec, v < 0 && unsigned ? v + (1L << 32) : v);
    } else if (arg instanceof Long) {
      text = FormatterNumbers.formatLong(spec, ((Long) arg).longValue());
    } else if (arg instanceof BigInteger) {
      text = FormatterNumbers.formatBigInteger(spec, (BigInteger) arg);
    } else {
      failConversion(spec, arg);
      return;
    }
    justify(localize(text, spec), spec);
  }

  private void printFloat(FormatterSpec spec, Object arg) {
    if (arg == null) {
      printString(spec, "null");
      return;
    }
    String text;
    if (arg instanceof Float) {
      text = FormatterNumbers.formatDouble(spec, ((Float) arg).doubleValue());
    } else if (arg instanceof Double) {
      text = FormatterNumbers.formatDouble(spec, ((Double) arg).doubleValue());
    } else if (arg instanceof BigDecimal) {
      text = FormatterNumbers.formatBigDecimal(spec, (BigDecimal) arg);
    } else {
      failConversion(spec, arg);
      return;
    }
    justify(localize(text, spec), spec);
  }
}
