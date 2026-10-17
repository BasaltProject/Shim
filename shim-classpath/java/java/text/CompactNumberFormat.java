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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompactNumberFormat extends NumberFormat {
  private static final long serialVersionUID = 7128367218649234678L;

  private static final class Pattern {
    final Map<String, String> byPlural = new LinkedHashMap<String, String>();
    final String raw;

    Pattern(String raw) {
      this.raw = raw;
      if (raw.startsWith("{") && raw.endsWith("}")) {
        String body = raw.substring(1, raw.length() - 1);
        int i = 0;
        while (i < body.length()) {
          int colon = body.indexOf(':', i);
          if (colon < 0) {
            break;
          }
          String key = body.substring(i, colon).trim();
          int end = colon + 1;
          boolean quote = false;
          while (end < body.length() && (quote || body.charAt(end) != ' ')) {
            if (body.charAt(end) == '\'') {
              quote = !quote;
            }
            end++;
          }
          byPlural.put(key, body.substring(colon + 1, end));
          i = end + 1;
        }
      } else if (!raw.isEmpty()) {
        byPlural.put("other", raw);
      }
    }

    boolean isEmpty() {
      return byPlural.isEmpty();
    }

    String forPlural(String form) {
      String p = byPlural.get(form);
      return p != null ? p : byPlural.get("other");
    }
  }

  private static final class Affixes {
    String positivePrefix = "";
    String positiveSuffix = "";
    String negativePrefix;
    String negativeSuffix;
    int zeros;
  }

  private final String decimalPattern;
  private final DecimalFormatSymbols symbols;
  private final String[] compactPatterns;
  private final String pluralRules;
  private final Pattern[] patterns;
  private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
  private boolean parseBigDecimal;
  private boolean strict;
  private int groupingSize;

  public CompactNumberFormat(String decimalPattern, DecimalFormatSymbols symbols, String[] compactPatterns) {
    this(decimalPattern, symbols, compactPatterns, "");
  }

  public CompactNumberFormat(String decimalPattern, DecimalFormatSymbols symbols, String[] compactPatterns,
      String pluralRules) {
    Objects.requireNonNull(decimalPattern, "decimalPattern");
    Objects.requireNonNull(symbols, "symbols");
    Objects.requireNonNull(compactPatterns, "compactPatterns");
    Objects.requireNonNull(pluralRules, "pluralRules");
    this.decimalPattern = decimalPattern;
    this.symbols = (DecimalFormatSymbols) symbols.clone();
    this.compactPatterns = compactPatterns.clone();
    this.pluralRules = pluralRules;
    if (!pluralRules.isEmpty() && !pluralRules.matches("\\s*\\w+:[^{}]+(;\\s*\\w+:[^{}]+)*\\s*")) {
      throw new IllegalArgumentException("Invalid plural rules syntax: " + pluralRules);
    }
    this.patterns = new Pattern[compactPatterns.length];
    for (int i = 0; i < patterns.length; i++) {
      patterns[i] = new Pattern(compactPatterns[i]);
    }
    super.setMaximumFractionDigits(0);
    super.setMinimumFractionDigits(0);
    super.setMaximumIntegerDigits(309);
    super.setMinimumIntegerDigits(1);
    super.setGroupingUsed(false);
  }

  private String pluralForm(BigDecimal scaled) {
    if (pluralRules.isEmpty()) {
      return "other";
    }
    BigDecimal stripped = scaled.stripTrailingZeros();
    if (pluralRules.contains("one:") && stripped.compareTo(BigDecimal.ONE) == 0) {
      return "one";
    }
    return "other";
  }

  private Affixes affixesOf(String text, String form) {
    Affixes a = new Affixes();
    String positive = text;
    String negative = null;
    boolean quote = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\'') {
        quote = !quote;
      } else if (c == ';' && !quote) {
        positive = text.substring(0, i);
        negative = text.substring(i + 1);
        break;
      }
    }
    String[] pos = split(positive);
    a.positivePrefix = pos[0];
    a.positiveSuffix = pos[1];
    a.zeros = Integer.parseInt(pos[2]);
    if (negative != null) {
      String[] neg = split(negative);
      a.negativePrefix = neg[0];
      a.negativeSuffix = neg[1];
    }
    return a;
  }

  private static String[] split(String sub) {
    int first = -1;
    int last = -1;
    int zeros = 0;
    boolean quote = false;
    for (int i = 0; i < sub.length(); i++) {
      char c = sub.charAt(i);
      if (c == '\'') {
        quote = !quote;
      } else if (c == '0' && !quote) {
        if (first < 0) {
          first = i;
        }
        last = i;
        zeros++;
      }
    }
    if (first < 0) {
      return new String[] {unquote(sub), "", "0"};
    }
    return new String[] {unquote(sub.substring(0, first)), unquote(sub.substring(last + 1)), String.valueOf(zeros)};
  }

  private static String unquote(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\'') {
        if (i + 1 < s.length() && s.charAt(i + 1) == '\'') {
          sb.append('\'');
          i++;
        }
        continue;
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private DecimalFormat decimal() {
    DecimalFormat df = new DecimalFormat(decimalPattern, symbols);
    df.setMaximumFractionDigits(getMaximumFractionDigits());
    df.setMinimumFractionDigits(getMinimumFractionDigits());
    df.setMaximumIntegerDigits(getMaximumIntegerDigits());
    df.setMinimumIntegerDigits(getMinimumIntegerDigits());
    boolean grouping = isGroupingUsed() && groupingSize > 0;
    df.setGroupingUsed(grouping);
    if (grouping) {
      df.setGroupingSize(groupingSize);
    }
    df.setRoundingMode(roundingMode);
    df.setParseBigDecimal(true);
    df.setDecimalSeparatorAlwaysShown(false);
    return df;
  }

  private static BigDecimal bd(double d) {
    return new BigDecimal(d);
  }

  private static final BigDecimal kThousand = BigDecimal.valueOf(1000);

  private boolean reaches(BigDecimal scaled, int decimals, boolean strict) {
    BigDecimal rounded = scaled.setScale(decimals, roundingMode);
    if (rounded.compareTo(kThousand) < 0) {
      return false;
    }
    if (strict && scaled.compareTo(new BigDecimal("999.5")) == 0) {
      return false;
    }
    return true;
  }

  private int select(BigDecimal abs) {
    if (abs.signum() == 0) {
      return -1;
    }
    int idx = Math.max(-1, abs.precision() - abs.scale() - 1);
    if (idx <= 2) {
      if (idx >= 0 && idx < patterns.length && !patterns[idx].isEmpty()) {
        return idx;
      }
      if (reaches(abs, 0, false)) {
        idx = 3;
      } else {
        return -1;
      }
    } else if (idx == 5 && reaches(abs.scaleByPowerOfTen(-3), 1, false)) {
      idx = 6;
    } else if (idx == 8 && reaches(abs.scaleByPowerOfTen(-6), 0, true)) {
      idx = 9;
    } else if (idx == 11 && reaches(abs.scaleByPowerOfTen(-9), 0, true)) {
      idx = 12;
    }
    if (idx >= patterns.length) {
      idx = patterns.length - 1;
    }
    return idx;
  }

  private StringBuffer formatDecimal(BigDecimal value, boolean negative, StringBuffer result, FieldPosition fp,
      boolean binary) {
    BigDecimal abs = value.abs();
    int idx = select(abs);
    String prefix = "";
    String suffix = "";
    String number;
    String sign = negative ? String.valueOf(symbols.getMinusSign()) : "";
    if (idx >= 0 && idx < patterns.length && !patterns[idx].isEmpty()) {
      Pattern p = patterns[idx];
      Affixes aff = affixesOf(p.byPlural.values().iterator().next(), "other");
      int exp = Math.max(0, idx - aff.zeros + 1);
      BigDecimal scaled = scale(abs, exp, binary);
      String form = pluralForm(scaled.setScale(getMaximumFractionDigits(), roundingMode));
      aff = affixesOf(p.forPlural(form), form);
      exp = Math.max(0, idx - aff.zeros + 1);
      scaled = scale(abs, exp, binary);
      prefix = aff.positivePrefix;
      suffix = aff.positiveSuffix;
      if (negative && aff.negativePrefix != null) {
        prefix = aff.negativePrefix;
        suffix = aff.negativeSuffix;
        sign = "";
      }
      number = decimal().format(scaled);
    } else {
      DecimalFormat plain = new DecimalFormat(decimalPattern, symbols);
      plain.setMaximumFractionDigits(0);
      plain.setMinimumFractionDigits(0);
      plain.setDecimalSeparatorAlwaysShown(false);
      number = plain.format(abs);
    }
    int start = result.length();
    result.append(sign).append(prefix);
    int numberStart = result.length();
    result.append(number);
    int numberEnd = result.length();
    result.append(suffix);
    if (fp != null) {
      int field = fp.getField();
      Format.Field attr = fp.getFieldAttribute();
      int dot = number.indexOf(symbols.getDecimalSeparator());
      int intEnd = dot < 0 ? number.length() : dot;
      if (field == INTEGER_FIELD || attr == Field.INTEGER) {
        fp.setBeginIndex(numberStart);
        fp.setEndIndex(numberStart + intEnd);
      } else if (field == FRACTION_FIELD || attr == Field.FRACTION) {
        if (dot >= 0) {
          fp.setBeginIndex(numberStart + dot + 1);
          fp.setEndIndex(numberEnd);
        }
      } else if (attr == Field.SUFFIX && !suffix.isEmpty()) {
        fp.setBeginIndex(numberEnd);
        fp.setEndIndex(numberEnd + suffix.length());
      } else if (attr == Field.PREFIX && !(sign + prefix).isEmpty()) {
        fp.setBeginIndex(start);
        fp.setEndIndex(numberStart);
      }
    }
    return result;
  }

  private static BigDecimal scale(BigDecimal abs, int exp, boolean binary) {
    if (!binary) {
      return abs.scaleByPowerOfTen(-exp);
    }
    double q = abs.doubleValue() / Math.pow(10, exp);
    return q >= 1e15 ? BigDecimal.valueOf(q) : new BigDecimal(q);
  }

  @Override
  public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
    if (Double.isNaN(number)) {
      return result.append(symbols.getNaN());
    }
    if (Double.isInfinite(number)) {
      if (number < 0) {
        result.append(symbols.getMinusSign());
      }
      return result.append(symbols.getInfinity());
    }
    boolean negative = number < 0 || (number == 0 && 1 / number < 0);
    return formatDecimal(bd(Math.abs(number)), negative, result, fieldPosition, true);
  }

  @Override
  public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
    return formatDecimal(new BigDecimal(number), number < 0, result, fieldPosition, true);
  }

  @Override
  public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte
        || number instanceof java.util.concurrent.atomic.AtomicInteger
        || number instanceof java.util.concurrent.atomic.AtomicLong) {
      return format(((Number) number).longValue(), toAppendTo, fieldPosition);
    }
    if (number instanceof BigDecimal) {
      BigDecimal b = (BigDecimal) number;
      return formatDecimal(b, b.signum() < 0, toAppendTo, fieldPosition, false);
    }
    if (number instanceof BigInteger) {
      BigDecimal b = new BigDecimal((BigInteger) number);
      return formatDecimal(b, b.signum() < 0, toAppendTo, fieldPosition, false);
    }
    if (number instanceof Number) {
      return format(((Number) number).doubleValue(), toAppendTo, fieldPosition);
    }
    throw new IllegalArgumentException("Cannot format " + number.getClass().getName() + " as a number");
  }

  @Override
  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    String text = format(obj, new StringBuffer(), new FieldPosition(0)).toString();
    AttributedString as = new AttributedString(text);
    FieldPosition fp = new FieldPosition(Field.INTEGER);
    format(obj, new StringBuffer(), fp);
    if (fp.getEndIndex() > fp.getBeginIndex()) {
      as.addAttribute(Field.INTEGER, Field.INTEGER, fp.getBeginIndex(), fp.getEndIndex());
    }
    FieldPosition fraction = new FieldPosition(Field.FRACTION);
    format(obj, new StringBuffer(), fraction);
    if (fraction.getEndIndex() > fraction.getBeginIndex()) {
      as.addAttribute(Field.FRACTION, Field.FRACTION, fraction.getBeginIndex(), fraction.getEndIndex());
      as.addAttribute(Field.DECIMAL_SEPARATOR, Field.DECIMAL_SEPARATOR, fraction.getBeginIndex() - 1,
          fraction.getBeginIndex());
    }
    FieldPosition suffix = new FieldPosition(Field.SUFFIX);
    format(obj, new StringBuffer(), suffix);
    if (suffix.getEndIndex() > suffix.getBeginIndex()) {
      as.addAttribute(Field.SUFFIX, Field.SUFFIX, suffix.getBeginIndex(), suffix.getEndIndex());
    }
    FieldPosition prefix = new FieldPosition(Field.PREFIX);
    format(obj, new StringBuffer(), prefix);
    if (prefix.getEndIndex() > prefix.getBeginIndex()) {
      as.addAttribute(Field.PREFIX, Field.PREFIX, prefix.getBeginIndex(), prefix.getEndIndex());
    }
    return as.getIterator();
  }

  @Override
  public Number parse(String text, ParsePosition pos) {
    int start = pos.getIndex();
    DecimalFormat df = decimal();
    df.setParseBigDecimal(true);
    df.setGroupingUsed(isGroupingUsed());
    ParsePosition pp = new ParsePosition(start);
    Number parsed = df.parse(text, pp);
    if (parsed == null) {
      pos.setErrorIndex(start);
      return null;
    }
    BigDecimal value = (BigDecimal) parsed;
    if (isParseIntegerOnly()) {
      value = value.setScale(0, RoundingMode.DOWN);
    }
    int end = pp.getIndex();
    int bestLen = 0;
    int bestIndex = -1;
    Affixes bestAff = null;
    for (int i = 0; i < patterns.length; i++) {
      for (String raw : patterns[i].byPlural.values()) {
        Affixes a = affixesOf(raw, "other");
        String s = value.signum() < 0 && a.negativeSuffix != null ? a.negativeSuffix : a.positiveSuffix;
        if (!s.isEmpty() && text.startsWith(s, end) && s.length() > bestLen) {
          bestLen = s.length();
          bestIndex = i;
          bestAff = a;
        }
      }
    }
    if (bestIndex >= 0) {
      int exp = Math.max(0, bestIndex - bestAff.zeros + 1);
      value = value.multiply(BigDecimal.TEN.pow(exp));
      end += bestLen;
    }
    if (strict && end != text.length() && bestIndex < 0 && end < text.length()) {
      pos.setErrorIndex(start);
      return null;
    }
    if (strict && bestIndex >= 0 && end < text.length()) {
      pos.setErrorIndex(start);
      return null;
    }
    pos.setIndex(end);
    if (parseBigDecimal) {
      return value;
    }
    BigDecimal stripped = value.stripTrailingZeros();
    if (stripped.scale() <= 0 && stripped.compareTo(new BigDecimal(Long.MAX_VALUE)) <= 0
        && stripped.compareTo(new BigDecimal(Long.MIN_VALUE)) >= 0) {
      return Long.valueOf(value.longValue());
    }
    return Double.valueOf(value.doubleValue());
  }

  @Override
  public boolean isStrict() {
    return strict;
  }

  @Override
  public void setStrict(boolean value) {
    strict = value;
  }

  public boolean isParseBigDecimal() {
    return parseBigDecimal;
  }

  public void setParseBigDecimal(boolean newValue) {
    parseBigDecimal = newValue;
  }

  @Override
  public RoundingMode getRoundingMode() {
    return roundingMode;
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    if (roundingMode == null) {
      throw new NullPointerException();
    }
    this.roundingMode = roundingMode;
  }

  public int getGroupingSize() {
    return groupingSize;
  }

  public void setGroupingSize(int newValue) {
    if (newValue < 0) {
      throw new IllegalArgumentException("newValue is negative");
    }
    groupingSize = newValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CompactNumberFormat other = (CompactNumberFormat) obj;
    return super.equals(obj) && decimalPattern.equals(other.decimalPattern)
        && symbols.equals(other.symbols) && Arrays.equals(compactPatterns, other.compactPatterns)
        && pluralRules.equals(other.pluralRules) && roundingMode == other.roundingMode
        && groupingSize == other.groupingSize && parseBigDecimal == other.parseBigDecimal
        && strict == other.strict;
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + Objects.hash(decimalPattern, symbols, pluralRules, roundingMode)
        + Arrays.hashCode(compactPatterns);
  }

  @Override
  public CompactNumberFormat clone() {
    return (CompactNumberFormat) super.clone();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CompactNumberFormat [decimal pattern: \"").append(decimalPattern)
        .append("\", compact patterns: \"").append(Arrays.toString(compactPatterns)).append("\"]\n");
    return sb.toString();
  }
}
