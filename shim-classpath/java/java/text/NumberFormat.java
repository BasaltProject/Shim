/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public abstract class NumberFormat extends Format {
  private static final long serialVersionUID = -2308460120365585849L;

  public static final int INTEGER_FIELD = 0;
  public static final int FRACTION_FIELD = 1;

  private boolean groupingUsed = true;
  private byte maxIntegerDigits = 40;
  private byte minIntegerDigits = 1;
  private byte maxFractionDigits = 3;
  private byte minFractionDigits = 0;
  private boolean parseIntegerOnly = false;
  private RoundingMode roundingMode = RoundingMode.HALF_EVEN;

  protected NumberFormat() { }

  @Override
  public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
    if (number instanceof Long || number instanceof Integer || number instanceof Short
        || number instanceof Byte || number instanceof BigInteger) {
      return format(((Number) number).longValue(), toAppendTo, pos);
    } else if (number instanceof Number) {
      return format(((Number) number).doubleValue(), toAppendTo, pos);
    } else {
      throw new IllegalArgumentException("Cannot format given Object as a Number");
    }
  }

  @Override
  public final Object parseObject(String source, ParsePosition pos) {
    return parse(source, pos);
  }

  public final String format(double number) {
    return format(number, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public final String format(long number) {
    return format(number, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public abstract StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos);

  public abstract StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos);

  public abstract Number parse(String source, ParsePosition parsePosition);

  public Number parse(String source) throws ParseException {
    ParsePosition parsePosition = new ParsePosition(0);
    Number result = parse(source, parsePosition);
    if (parsePosition.getIndex() == 0) {
      throw new ParseException("Unparseable number: \"" + source + "\"", parsePosition.getErrorIndex());
    }
    return result;
  }

  public boolean isParseIntegerOnly() {
    return parseIntegerOnly;
  }

  public void setParseIntegerOnly(boolean value) {
    parseIntegerOnly = value;
  }

  public static NumberFormat getInstance() {
    return getInstance(Locale.getDefault());
  }

  public static NumberFormat getInstance(Locale inLocale) {
    return getNumberInstance(inLocale);
  }

  public static NumberFormat getNumberInstance() {
    return getNumberInstance(Locale.getDefault());
  }

  public static NumberFormat getNumberInstance(Locale inLocale) {
    DecimalFormat df = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(inLocale));
    df.setGroupingUsed(true);
    return df;
  }

  public static NumberFormat getIntegerInstance() {
    return getIntegerInstance(Locale.getDefault());
  }

  public static NumberFormat getIntegerInstance(Locale inLocale) {
    DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(inLocale));
    df.setParseIntegerOnly(true);
    df.setMaximumFractionDigits(0);
    return df;
  }

  public static NumberFormat getCurrencyInstance() {
    return getCurrencyInstance(Locale.getDefault());
  }

  public static NumberFormat getCurrencyInstance(Locale inLocale) {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(inLocale);
    DecimalFormat df = new DecimalFormat("\u00A4#,##0.00", symbols);
    df.setMinimumFractionDigits(2);
    df.setMaximumFractionDigits(2);
    return df;
  }

  public static NumberFormat getPercentInstance() {
    return getPercentInstance(Locale.getDefault());
  }

  public static NumberFormat getPercentInstance(Locale inLocale) {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(inLocale);
    DecimalFormat df = new DecimalFormat("#,##0%", symbols);
    df.setMultiplier(100);
    df.setMinimumFractionDigits(0);
    df.setMaximumFractionDigits(0);
    return df;
  }

  public static Locale[] getAvailableLocales() {
    return new Locale[] {Locale.getDefault(), Locale.ROOT, Locale.US};
  }

  public boolean isGroupingUsed() {
    return groupingUsed;
  }

  public void setGroupingUsed(boolean newValue) {
    groupingUsed = newValue;
  }

  public int getMaximumIntegerDigits() {
    return maxIntegerDigits;
  }

  public void setMaximumIntegerDigits(int newValue) {
    maxIntegerDigits = (byte) Math.max(0, newValue);
    if (minIntegerDigits > maxIntegerDigits) {
      minIntegerDigits = maxIntegerDigits;
    }
  }

  public int getMinimumIntegerDigits() {
    return minIntegerDigits;
  }

  public void setMinimumIntegerDigits(int newValue) {
    minIntegerDigits = (byte) Math.max(0, newValue);
    if (minIntegerDigits > maxIntegerDigits) {
      maxIntegerDigits = minIntegerDigits;
    }
  }

  public int getMaximumFractionDigits() {
    return maxFractionDigits;
  }

  public void setMaximumFractionDigits(int newValue) {
    maxFractionDigits = (byte) Math.max(0, newValue);
    if (minFractionDigits > maxFractionDigits) {
      minFractionDigits = maxFractionDigits;
    }
  }

  public int getMinimumFractionDigits() {
    return minFractionDigits;
  }

  public void setMinimumFractionDigits(int newValue) {
    minFractionDigits = (byte) Math.max(0, newValue);
    if (minFractionDigits > maxFractionDigits) {
      maxFractionDigits = minFractionDigits;
    }
  }

  public RoundingMode getRoundingMode() {
    return roundingMode;
  }

  public void setRoundingMode(RoundingMode roundingMode) {
    this.roundingMode = Objects.requireNonNull(roundingMode, "roundingMode");
  }

  @Override
  public Object clone() {
    return super.clone();
  }
}
