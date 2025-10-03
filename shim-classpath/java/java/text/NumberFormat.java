/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class NumberFormat extends Format {
  private static final long serialVersionUID = -2308460125733713944L;

  public static final int INTEGER_FIELD = 0;
  public static final int FRACTION_FIELD = 1;

  public static class Field extends Format.Field {
    private static final long serialVersionUID = 7494728892700160890L;

    protected Field(String name) {
      super(name);
    }

    public static final Field INTEGER = new Field("integer");
    public static final Field FRACTION = new Field("fraction");
    public static final Field EXPONENT = new Field("exponent");
    public static final Field DECIMAL_SEPARATOR = new Field("decimal separator");
    public static final Field SIGN = new Field("sign");
    public static final Field GROUPING_SEPARATOR = new Field("grouping separator");
    public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");
    public static final Field PERCENT = new Field("percent");
    public static final Field PERMILLE = new Field("per mille");
    public static final Field CURRENCY = new Field("currency");
    public static final Field EXPONENT_SIGN = new Field("exponent sign");
    public static final Field PREFIX = new Field("prefix");
    public static final Field SUFFIX = new Field("suffix");
  }

  private boolean groupingUsed = true;
  private int maximumIntegerDigits = 40;
  private int minimumIntegerDigits = 1;
  private int maximumFractionDigits = 3;
  private int minimumFractionDigits = 0;
  private boolean parseIntegerOnly = false;

  protected NumberFormat() { }

  @Override
  public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
    if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte
        || number instanceof AtomicInteger || number instanceof AtomicLong
        || (number instanceof BigInteger && ((BigInteger) number).bitLength() < 64)) {
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

  public static final NumberFormat getInstance() {
    return getNumberInstance(Locale.getDefault(Locale.Category.FORMAT));
  }

  public static NumberFormat getInstance(Locale inLocale) {
    return getNumberInstance(inLocale);
  }

  public static final NumberFormat getNumberInstance() {
    return getNumberInstance(Locale.getDefault(Locale.Category.FORMAT));
  }

  public static NumberFormat getNumberInstance(Locale inLocale) {
    return new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(inLocale));
  }

  public static final NumberFormat getIntegerInstance() {
    return getIntegerInstance(Locale.getDefault(Locale.Category.FORMAT));
  }

  public static NumberFormat getIntegerInstance(Locale inLocale) {
    DecimalFormat df = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(inLocale));
    df.setMaximumFractionDigits(0);
    df.setDecimalSeparatorAlwaysShown(false);
    df.setParseIntegerOnly(true);
    return df;
  }

  public static final NumberFormat getCurrencyInstance() {
    return getCurrencyInstance(Locale.getDefault(Locale.Category.FORMAT));
  }

  public static NumberFormat getCurrencyInstance(Locale inLocale) {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(inLocale);
    String language = inLocale.getLanguage();
    boolean suffix = !language.equals("en") && !language.isEmpty() && !language.equals("ja") && !language.equals("zh")
        && !language.equals("ko") && !language.equals("pt") && !language.equals("nl");
    return new DecimalFormat(suffix ? "#,##0.00 ¤" : "¤#,##0.00", symbols);
  }

  public static final NumberFormat getPercentInstance() {
    return getPercentInstance(Locale.getDefault(Locale.Category.FORMAT));
  }

  public static NumberFormat getPercentInstance(Locale inLocale) {
    String language = inLocale.getLanguage();
    boolean spaced = !language.equals("en") && !language.isEmpty() && !language.equals("ja") && !language.equals("zh")
        && !language.equals("ko") && !language.equals("pt") && !language.equals("nl") && !language.equals("it");
    return new DecimalFormat(spaced ? "#,##0 %" : "#,##0%", DecimalFormatSymbols.getInstance(inLocale));
  }

  public static Locale[] getAvailableLocales() {
    return new Locale[] {Locale.ROOT, Locale.US};
  }

  public boolean isGroupingUsed() {
    return groupingUsed;
  }

  public void setGroupingUsed(boolean newValue) {
    groupingUsed = newValue;
  }

  public int getMaximumIntegerDigits() {
    return maximumIntegerDigits;
  }

  public void setMaximumIntegerDigits(int newValue) {
    maximumIntegerDigits = Math.max(0, newValue);
    if (minimumIntegerDigits > maximumIntegerDigits) {
      minimumIntegerDigits = maximumIntegerDigits;
    }
  }

  public int getMinimumIntegerDigits() {
    return minimumIntegerDigits;
  }

  public void setMinimumIntegerDigits(int newValue) {
    minimumIntegerDigits = Math.max(0, newValue);
    if (minimumIntegerDigits > maximumIntegerDigits) {
      maximumIntegerDigits = minimumIntegerDigits;
    }
  }

  public int getMaximumFractionDigits() {
    return maximumFractionDigits;
  }

  public void setMaximumFractionDigits(int newValue) {
    maximumFractionDigits = Math.max(0, newValue);
    if (maximumFractionDigits < minimumFractionDigits) {
      minimumFractionDigits = maximumFractionDigits;
    }
  }

  public int getMinimumFractionDigits() {
    return minimumFractionDigits;
  }

  public void setMinimumFractionDigits(int newValue) {
    minimumFractionDigits = Math.max(0, newValue);
    if (maximumFractionDigits < minimumFractionDigits) {
      maximumFractionDigits = minimumFractionDigits;
    }
  }

  public Currency getCurrency() {
    throw new UnsupportedOperationException();
  }

  public void setCurrency(Currency currency) {
    throw new UnsupportedOperationException();
  }

  public RoundingMode getRoundingMode() {
    throw new UnsupportedOperationException();
  }

  public void setRoundingMode(RoundingMode roundingMode) {
    throw new UnsupportedOperationException();
  }

  public boolean isStrict() {
    throw new UnsupportedOperationException();
  }

  public void setStrict(boolean value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return maximumIntegerDigits * 37 + maximumFractionDigits;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NumberFormat other = (NumberFormat) obj;
    return maximumIntegerDigits == other.maximumIntegerDigits && minimumIntegerDigits == other.minimumIntegerDigits
        && maximumFractionDigits == other.maximumFractionDigits
        && minimumFractionDigits == other.minimumFractionDigits && groupingUsed == other.groupingUsed
        && parseIntegerOnly == other.parseIntegerOnly;
  }

  @Override
  public Object clone() {
    return super.clone();
  }
}
