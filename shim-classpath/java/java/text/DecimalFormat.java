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
import java.util.Objects;

public class DecimalFormat extends NumberFormat {
  private static final long serialVersionUID = 8644703817706386145L;

  private DecimalFormatSymbols symbols;
  private String pattern;
  private String positivePrefix = "";
  private String positiveSuffix = "";
  private String negativePrefix = "-";
  private String negativeSuffix = "";
  private int multiplier = 1;
  private int groupingSize = 3;
  private boolean decimalSeparatorAlwaysShown = false;

  public DecimalFormat() {
    this("#,##0.###", DecimalFormatSymbols.getInstance());
  }

  public DecimalFormat(String pattern) {
    this(pattern, DecimalFormatSymbols.getInstance());
  }

  public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
    this.symbols = (DecimalFormatSymbols) Objects.requireNonNull(symbols, "symbols").clone();
    applyPattern(pattern);
  }

  public DecimalFormatSymbols getDecimalFormatSymbols() {
    return (DecimalFormatSymbols) symbols.clone();
  }

  public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
    this.symbols = (DecimalFormatSymbols) Objects.requireNonNull(newSymbols, "newSymbols").clone();
  }

  public String getPositivePrefix() {
    return positivePrefix;
  }

  public void setPositivePrefix(String newValue) {
    positivePrefix = newValue == null ? "" : newValue;
  }

  public String getNegativePrefix() {
    return negativePrefix;
  }

  public void setNegativePrefix(String newValue) {
    negativePrefix = newValue == null ? "" : newValue;
  }

  public String getPositiveSuffix() {
    return positiveSuffix;
  }

  public void setPositiveSuffix(String newValue) {
    positiveSuffix = newValue == null ? "" : newValue;
  }

  public String getNegativeSuffix() {
    return negativeSuffix;
  }

  public void setNegativeSuffix(String newValue) {
    negativeSuffix = newValue == null ? "" : newValue;
  }

  public int getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(int newValue) {
    multiplier = newValue;
  }

  public int getGroupingSize() {
    return groupingSize;
  }

  public void setGroupingSize(int newValue) {
    groupingSize = Math.max(0, newValue);
  }

  public boolean isDecimalSeparatorAlwaysShown() {
    return decimalSeparatorAlwaysShown;
  }

  public void setDecimalSeparatorAlwaysShown(boolean newValue) {
    decimalSeparatorAlwaysShown = newValue;
  }

  public void applyPattern(String pattern) {
    Objects.requireNonNull(pattern, "pattern");
    this.pattern = pattern;

    char patSep = symbols.getPatternSeparator();
    int semi = pattern.indexOf(patSep);
    String posPart = semi >= 0 ? pattern.substring(0, semi) : pattern;
    String negPart = semi >= 0 ? pattern.substring(semi + 1) : null;

    int firstDigit = -1;
    int lastDigit = -1;
    for (int i = 0; i < posPart.length(); i++) {
      char c = posPart.charAt(i);
      if (c == '0' || c == '#' || c == '.' || c == ',') {
        if (firstDigit < 0) firstDigit = i;
        lastDigit = i;
      }
    }

    if (firstDigit >= 0) {
      positivePrefix = posPart.substring(0, firstDigit);
      positiveSuffix = posPart.substring(lastDigit + 1);
    } else {
      positivePrefix = posPart;
      positiveSuffix = "";
    }

    positivePrefix = expandCurrencySymbols(positivePrefix);
    positiveSuffix = expandCurrencySymbols(positiveSuffix);

    if (positivePrefix.indexOf('%') >= 0 || positiveSuffix.indexOf('%') >= 0) {
      multiplier = 100;
    } else if (positivePrefix.indexOf('\u2030') >= 0 || positiveSuffix.indexOf('\u2030') >= 0) {
      multiplier = 1000;
    } else {
      multiplier = 1;
    }

    if (negPart != null) {
      int negFirst = -1;
      int negLast = -1;
      for (int i = 0; i < negPart.length(); i++) {
        char c = negPart.charAt(i);
        if (c == '0' || c == '#' || c == '.' || c == ',') {
          if (negFirst < 0) negFirst = i;
          negLast = i;
        }
      }
      if (negFirst >= 0) {
        negativePrefix = expandCurrencySymbols(negPart.substring(0, negFirst));
        negativeSuffix = expandCurrencySymbols(negPart.substring(negLast + 1));
      } else {
        negativePrefix = expandCurrencySymbols(negPart);
        negativeSuffix = "";
      }
    } else {
      negativePrefix = symbols.getMinusSign() + positivePrefix;
      negativeSuffix = positiveSuffix;
    }

    if (firstDigit >= 0) {
      String digitSpec = posPart.substring(firstDigit, lastDigit + 1);
      int dot = digitSpec.indexOf('.');
      String intPart = dot >= 0 ? digitSpec.substring(0, dot) : digitSpec;
      String fracPart = dot >= 0 ? digitSpec.substring(dot + 1) : "";

      int comma = intPart.lastIndexOf(',');
      if (comma >= 0) {
        setGroupingUsed(true);
        groupingSize = intPart.length() - 1 - comma;
      } else {
        setGroupingUsed(false);
      }

      int minInt = 0;
      int maxInt = 0;
      for (int i = 0; i < intPart.length(); i++) {
        char c = intPart.charAt(i);
        if (c == '0') {
          minInt++;
          maxInt++;
        } else if (c == '#') {
          maxInt++;
        }
      }
      setMinimumIntegerDigits(Math.max(1, minInt));
      setMaximumIntegerDigits(Math.max(minInt, Math.max(maxInt, 40)));

      int minFrac = 0;
      int maxFrac = 0;
      for (int i = 0; i < fracPart.length(); i++) {
        char c = fracPart.charAt(i);
        if (c == '0') {
          minFrac++;
          maxFrac++;
        } else if (c == '#') {
          maxFrac++;
        }
      }
      setMinimumFractionDigits(minFrac);
      setMaximumFractionDigits(maxFrac);
    }
  }

  private String expandCurrencySymbols(String s) {
    if (s.contains("\u00A4\u00A4")) {
      return s.replace("\u00A4\u00A4", symbols.getInternationalCurrencySymbol());
    } else if (s.contains("\u00A4")) {
      return s.replace("\u00A4", symbols.getCurrencySymbol());
    }
    return s;
  }

  public String toPattern() {
    return pattern;
  }

  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
    if (Double.isNaN(number)) {
      toAppendTo.append(symbols.getNaN());
      return toAppendTo;
    }
    if (Double.isInfinite(number)) {
      if (number < 0) {
        toAppendTo.append(negativePrefix);
      } else {
        toAppendTo.append(positivePrefix);
      }
      toAppendTo.append(symbols.getInfinity());
      if (number < 0) {
        toAppendTo.append(negativeSuffix);
      } else {
        toAppendTo.append(positiveSuffix);
      }
      return toAppendTo;
    }

    BigDecimal bd = BigDecimal.valueOf(number);
    return formatBigDecimal(bd, toAppendTo);
  }

  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
    BigDecimal bd = BigDecimal.valueOf(number);
    return formatBigDecimal(bd, toAppendTo);
  }

  private StringBuffer formatBigDecimal(BigDecimal bd, StringBuffer toAppendTo) {
    boolean isNeg = bd.signum() < 0;
    if (isNeg) {
      toAppendTo.append(negativePrefix);
      bd = bd.negate();
    } else {
      toAppendTo.append(positivePrefix);
    }

    if (multiplier != 1) {
      bd = bd.multiply(BigDecimal.valueOf(multiplier));
    }

    int maxFrac = getMaximumFractionDigits();
    int minFrac = getMinimumFractionDigits();
    int minInt = getMinimumIntegerDigits();

    bd = bd.setScale(maxFrac, getRoundingMode());

    BigInteger intPart = bd.toBigInteger();
    BigDecimal fracPart = bd.subtract(new BigDecimal(intPart));

    String intStr = intPart.toString();
    if (intStr.length() < minInt) {
      StringBuilder pad = new StringBuilder();
      for (int i = intStr.length(); i < minInt; i++) {
        pad.append(symbols.getZeroDigit());
      }
      pad.append(intStr);
      intStr = pad.toString();
    }

    if (isGroupingUsed() && groupingSize > 0) {
      StringBuilder grouped = new StringBuilder();
      int len = intStr.length();
      for (int i = 0; i < len; i++) {
        if (i > 0 && (len - i) % groupingSize == 0) {
          grouped.append(symbols.getGroupingSeparator());
        }
        grouped.append(intStr.charAt(i));
      }
      intStr = grouped.toString();
    }
    toAppendTo.append(intStr);

    String fracStr = "";
    if (maxFrac > 0) {
      fracPart = fracPart.movePointRight(maxFrac);
      String rawFrac = fracPart.toBigInteger().abs().toString();
      StringBuilder fullFrac = new StringBuilder();
      for (int i = rawFrac.length(); i < maxFrac; i++) {
        fullFrac.append('0');
      }
      fullFrac.append(rawFrac);
      fracStr = fullFrac.toString();

      while (fracStr.length() > minFrac && fracStr.endsWith("0")) {
        fracStr = fracStr.substring(0, fracStr.length() - 1);
      }
    }

    if (fracStr.length() > 0 || decimalSeparatorAlwaysShown) {
      toAppendTo.append(symbols.getDecimalSeparator());
      toAppendTo.append(fracStr);
    }

    if (isNeg) {
      toAppendTo.append(negativeSuffix);
    } else {
      toAppendTo.append(positiveSuffix);
    }
    return toAppendTo;
  }

  @Override
  public Number parse(String source, ParsePosition parsePosition) {
    int start = parsePosition.getIndex();
    int index = start;

    boolean isNeg = false;
    if (negativePrefix.length() > 0 && source.startsWith(negativePrefix, index)) {
      isNeg = true;
      index += negativePrefix.length();
    } else if (positivePrefix.length() > 0 && source.startsWith(positivePrefix, index)) {
      index += positivePrefix.length();
    }

    StringBuilder digits = new StringBuilder();
    boolean hasDecimal = false;
    char decSep = symbols.getDecimalSeparator();
    char grpSep = symbols.getGroupingSeparator();

    int len = source.length();
    while (index < len) {
      char c = source.charAt(index);
      if (c >= '0' && c <= '9') {
        digits.append(c);
        index++;
      } else if (c == grpSep && isGroupingUsed()) {
        index++;
      } else if (c == decSep && !hasDecimal && !isParseIntegerOnly()) {
        hasDecimal = true;
        digits.append('.');
        index++;
      } else {
        break;
      }
    }

    if (digits.length() == 0) {
      parsePosition.setErrorIndex(start);
      return null;
    }

    if (isNeg && negativeSuffix.length() > 0) {
      if (source.startsWith(negativeSuffix, index)) {
        index += negativeSuffix.length();
      }
    } else if (!isNeg && positiveSuffix.length() > 0) {
      if (source.startsWith(positiveSuffix, index)) {
        index += positiveSuffix.length();
      }
    }

    parsePosition.setIndex(index);

    String numStr = (isNeg ? "-" : "") + digits.toString();
    try {
      if (hasDecimal || multiplier != 1) {
        double d = Double.parseDouble(numStr);
        if (multiplier != 1) {
          d /= multiplier;
        }
        return d;
      } else {
        long l = Long.parseLong(numStr);
        return l;
      }
    } catch (NumberFormatException e) {
      parsePosition.setErrorIndex(start);
      return null;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (!(obj instanceof DecimalFormat)) return false;
    DecimalFormat other = (DecimalFormat) obj;
    return multiplier == other.multiplier
        && groupingSize == other.groupingSize
        && decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown
        && Objects.equals(positivePrefix, other.positivePrefix)
        && Objects.equals(positiveSuffix, other.positiveSuffix)
        && Objects.equals(negativePrefix, other.negativePrefix)
        && Objects.equals(negativeSuffix, other.negativeSuffix)
        && Objects.equals(symbols, other.symbols);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), symbols, positivePrefix, positiveSuffix,
        negativePrefix, negativeSuffix, multiplier, groupingSize, decimalSeparatorAlwaysShown);
  }
}
