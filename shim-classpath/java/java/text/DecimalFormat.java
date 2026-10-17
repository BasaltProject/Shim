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

public class DecimalFormat extends NumberFormat {
  private static final long serialVersionUID = 864413376551465018L;

  private static final int kMaxInteger = Integer.MAX_VALUE;
  private static final int kDoubleInteger = 309;
  private static final int kDoubleFraction = 340;
  private static final char kQuote = '\'';
  private static final char kCurrencySign = '¤';

  private transient DigitList digitList = new DigitList();
  private String positivePrefix = "";
  private String positiveSuffix = "";
  private String negativePrefix = "-";
  private String negativeSuffix = "";
  private String posPrefixPattern;
  private String posSuffixPattern;
  private String negPrefixPattern;
  private String negSuffixPattern;
  private int multiplier = 1;
  private int groupingSize = 3;
  private boolean decimalSeparatorAlwaysShown = false;
  private boolean parseBigDecimal = false;
  private boolean isCurrencyFormat = false;
  private DecimalFormatSymbols symbols;
  private boolean useExponentialNotation;
  private int minExponentDigits;
  private int maxIntegerDigits = kMaxInteger;
  private int minIntegerDigits = 1;
  private int maxFractionDigits = 3;
  private int minFractionDigits = 0;
  private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
  private boolean strict;
  private Currency currency;
  private transient java.util.List<Object[]> segs;

  public DecimalFormat() {
    this("#,##0.###", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
  }

  public DecimalFormat(String pattern) {
    this(pattern, DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
  }

  public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
    Objects.requireNonNull(pattern);
    this.symbols = (DecimalFormatSymbols) symbols.clone();
    applyPattern(pattern, false);
  }

  @Override
  public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
    if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte
        || number instanceof AtomicInteger || number instanceof AtomicLong
        || (number instanceof BigInteger && ((BigInteger) number).bitLength() < 64)) {
      return format(((Number) number).longValue(), toAppendTo, pos);
    } else if (number instanceof BigDecimal) {
      return format((BigDecimal) number, toAppendTo, pos);
    } else if (number instanceof BigInteger) {
      return format((BigInteger) number, toAppendTo, pos);
    } else if (number instanceof Number) {
      return format(((Number) number).doubleValue(), toAppendTo, pos);
    } else {
      throw new IllegalArgumentException("Cannot format given Object as a Number");
    }
  }

  @Override
  public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
    fieldPosition.setBeginIndex(0);
    fieldPosition.setEndIndex(0);
    if (Double.isNaN(number)) {
      int start = result.length();
      result.append(symbols.getNaN());
      markField(fieldPosition, INTEGER_FIELD, start, result.length());
      addSeg(NumberFormat.Field.INTEGER, start, result.length());
      return result;
    }
    boolean isNegative = ((number < 0.0) || (number == 0.0 && 1 / number < 0.0)) ^ (multiplier < 0);
    if (multiplier != 1) {
      number *= multiplier;
    }
    if (Double.isInfinite(number)) {
      appendAffix(result, isNegative ? negativePrefix : positivePrefix, isNegative ? negPrefixPattern
          : posPrefixPattern);
      int start = result.length();
      result.append(symbols.getInfinity());
      markField(fieldPosition, INTEGER_FIELD, start, result.length());
      addSeg(NumberFormat.Field.INTEGER, start, result.length());
      appendAffix(result, isNegative ? negativeSuffix : positiveSuffix, isNegative ? negSuffixPattern
          : posSuffixPattern);
      return result;
    }
    if (isNegative) {
      number = -number;
    }
    int maxInt = getMaximumIntegerDigits();
    int minInt = getMinimumIntegerDigits();
    int maxFra = getMaximumFractionDigits();
    int minFra = getMinimumFractionDigits();
    digitList.setRounding(roundingMode, isNegative);
    digitList.setDouble(number, useExponentialNotation ? maxInt + maxFra : maxFra, !useExponentialNotation);
    return subformat(result, fieldPosition, isNegative, false, maxInt, minInt, maxFra, minFra);
  }

  @Override
  public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
    fieldPosition.setBeginIndex(0);
    fieldPosition.setEndIndex(0);
    boolean isNegative = number < 0;
    if (isNegative) {
      number = -number;
    }
    boolean useBig = false;
    if (number < 0) {
      useBig = true;
    } else if (multiplier != 0) {
      long limit = Long.MAX_VALUE / Math.abs((long) multiplier);
      if (number > limit) {
        useBig = true;
      }
    }
    if (useBig) {
      BigInteger big = number < 0 ? BigInteger.valueOf(Long.MIN_VALUE).negate() : BigInteger.valueOf(number);
      return format(isNegative ? big.negate() : big, result, fieldPosition);
    }
    number *= multiplier;
    if (number == 0) {
      isNegative = false;
    } else if (multiplier < 0) {
      number = -number;
      isNegative = !isNegative;
    }
    if (number < 0) {
      number = -number;
    }
    int maxInt = getMaximumIntegerDigits();
    int minInt = getMinimumIntegerDigits();
    int maxFra = getMaximumFractionDigits();
    int minFra = getMinimumFractionDigits();
    digitList.setRounding(roundingMode, isNegative);
    digitList.setLong(number, useExponentialNotation ? maxInt + maxFra : 0);
    return subformat(result, fieldPosition, isNegative, true, maxInt, minInt, maxFra, minFra);
  }

  StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
    fieldPosition.setBeginIndex(0);
    fieldPosition.setEndIndex(0);
    if (multiplier != 1) {
      number = number.multiply(BigDecimal.valueOf(multiplier));
    }
    boolean isNegative = number.signum() == -1;
    if (isNegative) {
      number = number.negate();
    }
    int maxInt = getMaximumIntegerDigits();
    int minInt = getMinimumIntegerDigits();
    int maxFra = getMaximumFractionDigits();
    int minFra = getMinimumFractionDigits();
    int maximumDigits = maxInt + maxFra;
    digitList.setRounding(roundingMode, isNegative);
    digitList.setBigDecimal(number, useExponentialNotation ? (maximumDigits < 0 ? Integer.MAX_VALUE : maximumDigits)
        : maxFra, !useExponentialNotation);
    return subformat(result, fieldPosition, isNegative, false, maxInt, minInt, maxFra, minFra);
  }

  StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
    fieldPosition.setBeginIndex(0);
    fieldPosition.setEndIndex(0);
    if (multiplier != 1) {
      number = number.multiply(BigInteger.valueOf(multiplier));
    }
    boolean isNegative = number.signum() == -1;
    if (isNegative) {
      number = number.negate();
    }
    int maxInt = getMaximumIntegerDigits();
    int minInt = getMinimumIntegerDigits();
    int maxFra = getMaximumFractionDigits();
    int minFra = getMinimumFractionDigits();
    int maximumDigits = maxInt + maxFra;
    digitList.setRounding(roundingMode, isNegative);
    digitList.setBigInteger(number, useExponentialNotation ? (maximumDigits < 0 ? Integer.MAX_VALUE : maximumDigits)
        : 0);
    return subformat(result, fieldPosition, isNegative, true, maxInt, minInt, maxFra, minFra);
  }

  private static void markField(FieldPosition pos, int field, int begin, int end) {
    if (pos.getField() == field && pos.getBeginIndex() == 0 && pos.getEndIndex() == 0) {
      pos.setBeginIndex(begin);
      pos.setEndIndex(end);
    }
  }

  private StringBuffer subformat(StringBuffer result, FieldPosition fieldPosition, boolean isNegative,
      boolean isInteger, int maxIntDigits, int minIntDigits, int maxFraDigits, int minFraDigits) {
    char zero = symbols.getZeroDigit();
    int zeroDelta = zero - '0';
    char grouping = symbols.getGroupingSeparator();
    char decimal = isCurrencyFormat ? symbols.getMonetaryDecimalSeparator() : symbols.getDecimalSeparator();
    if (digitList.isZero()) {
      digitList.decimalAt = 0;
    }
    appendAffix(result, isNegative ? negativePrefix : positivePrefix, isNegative ? negPrefixPattern : posPrefixPattern);
    int intStart = result.length();
    int intEnd;
    int fracStart;
    if (useExponentialNotation) {
      int exponent = digitList.decimalAt;
      int repeat = maxIntDigits;
      int minimumIntegerDigits = minIntDigits;
      if (repeat > 1 && repeat > minIntDigits) {
        if (exponent >= 1) {
          exponent = ((exponent - 1) / repeat) * repeat;
        } else {
          exponent = ((exponent - repeat) / repeat) * repeat;
        }
        minimumIntegerDigits = 1;
      } else {
        exponent -= minimumIntegerDigits;
      }
      int minimumDigits = minIntDigits + minFraDigits;
      if (minimumDigits < 0) {
        minimumDigits = Integer.MAX_VALUE;
      }
      int integerDigits = digitList.isZero() ? minimumIntegerDigits : digitList.decimalAt - exponent;
      if (minimumDigits < integerDigits) {
        minimumDigits = integerDigits;
      }
      int totalDigits = digitList.count;
      if (minimumDigits > totalDigits) {
        totalDigits = minimumDigits;
      }
      boolean addedDecimalSeparator = false;
      intEnd = -1;
      fracStart = -1;
      for (int i = 0; i < totalDigits; ++i) {
        if (i == integerDigits) {
          intEnd = result.length();
          result.append(decimal);
          addedDecimalSeparator = true;
          fracStart = result.length();
        }
        result.append(i < digitList.count ? (char) (digitList.digits[i] + zeroDelta) : zero);
      }
      if (intEnd < 0) {
        intEnd = result.length();
      }
      if (decimalSeparatorAlwaysShown && totalDigits == integerDigits) {
        intEnd = result.length();
        result.append(decimal);
        addedDecimalSeparator = true;
        fracStart = result.length();
      }
      if (fracStart < 0) {
        fracStart = result.length();
      }
      markField(fieldPosition, INTEGER_FIELD, intStart, intEnd);
      markField(fieldPosition, FRACTION_FIELD, fracStart, result.length());
      addSeg(NumberFormat.Field.INTEGER, intStart, intEnd);
      if (addedDecimalSeparator) {
        addSeg(NumberFormat.Field.DECIMAL_SEPARATOR, intEnd, fracStart);
      }
      addSeg(NumberFormat.Field.FRACTION, fracStart, result.length());
      int expSymbolStart = result.length();
      result.append(symbols.getExponentSeparator());
      addSeg(NumberFormat.Field.EXPONENT_SYMBOL, expSymbolStart, result.length());
      if (digitList.isZero()) {
        exponent = 0;
      }
      boolean negativeExponent = exponent < 0;
      if (negativeExponent) {
        exponent = -exponent;
        int signStart = result.length();
        result.append(symbols.getMinusSign());
        addSeg(NumberFormat.Field.EXPONENT_SIGN, signStart, result.length());
      }
      int expStart = result.length();
      String exponentDigits = Integer.toString(exponent);
      for (int i = exponentDigits.length(); i < minExponentDigits; ++i) {
        result.append(zero);
      }
      for (int i = 0; i < exponentDigits.length(); ++i) {
        result.append((char) (exponentDigits.charAt(i) + zeroDelta));
      }
      addSeg(NumberFormat.Field.EXPONENT, expStart, result.length());
    } else {
      int count = minIntDigits;
      int digitIndex = 0;
      if (digitList.decimalAt > 0 && count < digitList.decimalAt) {
        count = digitList.decimalAt;
      }
      if (count > maxIntDigits) {
        count = maxIntDigits;
        digitIndex = digitList.decimalAt - count;
      }
      int sizeBeforeIntegerPart = result.length();
      for (int i = count - 1; i >= 0; --i) {
        if (i < digitList.decimalAt && digitIndex < digitList.count) {
          result.append((char) (digitList.digits[digitIndex++] + zeroDelta));
        } else {
          result.append(zero);
        }
        if (isGroupingUsed() && i > 0 && (groupingSize != 0) && (i % groupingSize == 0)) {
          addSeg(NumberFormat.Field.GROUPING_SEPARATOR, result.length(), result.length() + 1);
          result.append(grouping);
        }
      }
      boolean fractionPresent = (minFraDigits > 0) || (!isInteger && digitIndex < digitList.count);
      if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
        result.append(zero);
      }
      intEnd = result.length();
      if (decimalSeparatorAlwaysShown || fractionPresent) {
        result.append(decimal);
      }
      fracStart = result.length();
      for (int i = 0; i < maxFraDigits; ++i) {
        if (i >= minFraDigits && (isInteger || digitIndex >= digitList.count)) {
          break;
        }
        if (-1 - i > (digitList.decimalAt - 1)) {
          result.append(zero);
          continue;
        }
        if (!isInteger && digitIndex < digitList.count) {
          result.append((char) (digitList.digits[digitIndex++] + zeroDelta));
        } else {
          result.append(zero);
        }
      }
      markField(fieldPosition, INTEGER_FIELD, intStart, intEnd);
      markField(fieldPosition, FRACTION_FIELD, fracStart, result.length());
      addSeg(NumberFormat.Field.INTEGER, intStart, intEnd);
      if (fracStart > intEnd) {
        addSeg(NumberFormat.Field.DECIMAL_SEPARATOR, intEnd, fracStart);
      }
      addSeg(NumberFormat.Field.FRACTION, fracStart, result.length());
    }
    appendAffix(result, isNegative ? negativeSuffix : positiveSuffix, isNegative ? negSuffixPattern : posSuffixPattern);
    return result;
  }

  private void addSeg(NumberFormat.Field field, int begin, int end) {
    if (segs != null && end > begin) {
      segs.add(new Object[] {field, begin, end});
    }
  }

  private void appendAffix(StringBuffer result, String expanded, String pattern) {
    int base = result.length();
    result.append(expanded);
    if (segs == null || pattern == null) {
      return;
    }
    int p = 0;
    for (int i = 0; i < pattern.length();) {
      char c = pattern.charAt(i++);
      if (c == kQuote) {
        c = pattern.charAt(i++);
        switch (c) {
          case kCurrencySign: {
            String text;
            if (i < pattern.length() && pattern.charAt(i) == kCurrencySign) {
              ++i;
              text = symbols.getInternationalCurrencySymbol();
            } else {
              text = symbols.getCurrencySymbol();
            }
            addSeg(NumberFormat.Field.CURRENCY, base + p, base + p + text.length());
            p += text.length();
            continue;
          }
          case '%':
            addSeg(NumberFormat.Field.PERCENT, base + p, base + p + 1);
            p++;
            continue;
          case '\u2030':
            addSeg(NumberFormat.Field.PERMILLE, base + p, base + p + 1);
            p++;
            continue;
          case '-':
            addSeg(NumberFormat.Field.SIGN, base + p, base + p + 1);
            p++;
            continue;
          default:
            break;
        }
      }
      p++;
    }
  }

  @Override
  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    if (obj == null) {
      throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
    }
    segs = new java.util.ArrayList<Object[]>();
    try {
      StringBuffer text = new StringBuffer();
      format(obj, text, new FieldPosition(0));
      AttributedString result = new AttributedString(text.toString());
      for (Object[] seg : segs) {
        result.addAttribute((AttributedCharacterIterator.Attribute) seg[0],
            (AttributedCharacterIterator.Attribute) seg[0], (Integer) seg[1], (Integer) seg[2]);
      }
      return result.getIterator();
    } finally {
      segs = null;
    }
  }

  @Override
  public Number parse(String text, ParsePosition pos) {
    if (text.regionMatches(pos.getIndex(), symbols.getNaN(), 0, symbols.getNaN().length())) {
      pos.setIndex(pos.getIndex() + symbols.getNaN().length());
      return Double.valueOf(Double.NaN);
    }
    boolean[] status = new boolean[2];
    digitList.clear();
    if (!subparse(text, pos, positivePrefix, negativePrefix, digitList, false, status)) {
      return null;
    }
    if (status[0]) {
      return Double.valueOf(status[1] ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
    }
    boolean positive = status[1];
    if (multiplier == 0) {
      if (digitList.isZero()) {
        return Double.valueOf(Double.NaN);
      }
      return Double.valueOf(positive ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
    }
    if (isParseBigDecimal()) {
      BigDecimal result = digitList.getBigDecimal();
      if (multiplier != 1) {
        try {
          result = result.divide(BigDecimal.valueOf(multiplier));
        } catch (ArithmeticException e) {
          result = result.divide(BigDecimal.valueOf(multiplier), roundingMode);
        }
      }
      if (!positive) {
        result = result.negate();
      }
      return result;
    }
    boolean gotDouble = true;
    boolean gotLongMinimum = false;
    double doubleResult = 0.0;
    long longResult = 0;
    if (digitList.fitsIntoLong(positive, isParseIntegerOnly())) {
      gotDouble = false;
      longResult = digitList.getLong();
      if (longResult < 0) {
        gotLongMinimum = true;
      }
    } else {
      doubleResult = digitList.getDouble();
    }
    if (multiplier != 1) {
      if (gotDouble) {
        doubleResult /= multiplier;
      } else {
        if (longResult % multiplier == 0) {
          longResult /= multiplier;
        } else {
          doubleResult = ((double) longResult) / multiplier;
          gotDouble = true;
        }
      }
    }
    if (!positive && !gotLongMinimum) {
      doubleResult = -doubleResult;
      longResult = -longResult;
    }
    if (multiplier != 1 && gotDouble) {
      longResult = (long) doubleResult;
      gotDouble = ((doubleResult != (double) longResult) || (doubleResult == 0.0 && 1 / doubleResult < 0.0))
          && !isParseIntegerOnly();
    }
    return gotDouble ? (Number) Double.valueOf(doubleResult) : (Number) Long.valueOf(longResult);
  }

  private boolean subparse(String text, ParsePosition parsePosition, String positivePrefix, String negativePrefix,
      DigitList digits, boolean isExponent, boolean[] status) {
    int position = parsePosition.getIndex();
    int oldStart = position;
    boolean gotPositive = text.regionMatches(position, positivePrefix, 0, positivePrefix.length());
    boolean gotNegative = text.regionMatches(position, negativePrefix, 0, negativePrefix.length());
    if (gotPositive && gotNegative) {
      if (positivePrefix.length() > negativePrefix.length()) {
        gotNegative = false;
      } else if (positivePrefix.length() < negativePrefix.length()) {
        gotPositive = false;
      }
    }
    if (gotPositive) {
      position += positivePrefix.length();
    } else if (gotNegative) {
      position += negativePrefix.length();
    } else {
      parsePosition.setErrorIndex(position);
      return false;
    }
    position = subparseNumber(text, position, digits, status, isExponent);
    if (position < 0) {
      parsePosition.setErrorIndex(oldStart);
      return false;
    }
    if (!isExponent) {
      if (gotPositive) {
        gotPositive = text.regionMatches(position, positiveSuffix, 0, positiveSuffix.length());
      }
      if (gotNegative) {
        gotNegative = text.regionMatches(position, negativeSuffix, 0, negativeSuffix.length());
      }
      if (gotPositive && gotNegative) {
        if (positiveSuffix.length() > negativeSuffix.length()) {
          gotNegative = false;
        } else if (positiveSuffix.length() < negativeSuffix.length()) {
          gotPositive = false;
        }
      }
      if (gotPositive == gotNegative) {
        parsePosition.setErrorIndex(position);
        return false;
      }
      parsePosition.setIndex(position + (gotPositive ? positiveSuffix.length() : negativeSuffix.length()));
    } else {
      parsePosition.setIndex(position);
    }
    status[1] = gotPositive;
    if (parsePosition.getIndex() == oldStart) {
      parsePosition.setErrorIndex(position);
      return false;
    }
    return true;
  }

  private int subparseNumber(String text, int position, DigitList digits, boolean[] status, boolean isExponent) {
    status[0] = false;
    int start = position;
    if (!isExponent && text.regionMatches(position, symbols.getInfinity(), 0, symbols.getInfinity().length())) {
      status[0] = true;
      return position + symbols.getInfinity().length();
    }
    digits.clear();
    char zero = symbols.getZeroDigit();
    char decimal = isCurrencyFormat ? symbols.getMonetaryDecimalSeparator() : symbols.getDecimalSeparator();
    char grouping = symbols.getGroupingSeparator();
    String exponentString = symbols.getExponentSeparator();
    boolean sawDecimal = false;
    boolean sawExponent = false;
    boolean sawDigit = false;
    int exponent = 0;
    int digitCount = 0;
    int backup = -1;
    for (; position < text.length(); ++position) {
      char ch = text.charAt(position);
      int digit = ch - zero;
      if (digit < 0 || digit > 9) {
        digit = Character.digit(ch, 10);
      }
      if (digit == 0) {
        backup = -1;
        sawDigit = true;
        if (digits.count == 0) {
          if (sawDecimal) {
            --digits.decimalAt;
          }
        } else {
          ++digitCount;
          digits.append((char) (digit + '0'));
        }
      } else if (digit > 0 && digit <= 9) {
        sawDigit = true;
        ++digitCount;
        digits.append((char) (digit + '0'));
        backup = -1;
      } else if (!isExponent && ch == decimal) {
        if (isParseIntegerOnly() || sawDecimal) {
          break;
        }
        digits.decimalAt = digitCount;
        sawDecimal = true;
      } else if (!isExponent && ch == grouping && isGroupingUsed()) {
        if (sawDecimal) {
          break;
        }
        backup = position;
      } else if (!isExponent && !sawExponent && text.regionMatches(position, exponentString, 0,
          exponentString.length())) {
        ParsePosition pos = new ParsePosition(position + exponentString.length());
        boolean[] stat = new boolean[2];
        DigitList exponentDigits = new DigitList();
        if (subparse(text, pos, "", String.valueOf(symbols.getMinusSign()), exponentDigits, true, stat)
            && exponentDigits.fitsIntoLong(stat[1], true)) {
          position = pos.getIndex();
          exponent = (int) exponentDigits.getLong();
          if (!stat[1]) {
            exponent = -exponent;
          }
          sawExponent = true;
        }
        break;
      } else {
        break;
      }
    }
    if (backup != -1) {
      position = backup;
    }
    if (!sawDecimal) {
      digits.decimalAt = digitCount;
    }
    digits.decimalAt += exponent;
    if (!sawDigit && digitCount == 0) {
      return -1;
    }
    return position;
  }

  public DecimalFormatSymbols getDecimalFormatSymbols() {
    return (DecimalFormatSymbols) symbols.clone();
  }

  public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
    symbols = (DecimalFormatSymbols) newSymbols.clone();
    expandAffixes();
  }

  public String getPositivePrefix() {
    return positivePrefix;
  }

  public void setPositivePrefix(String newValue) {
    positivePrefix = newValue;
    posPrefixPattern = null;
  }

  public String getNegativePrefix() {
    return negativePrefix;
  }

  public void setNegativePrefix(String newValue) {
    negativePrefix = newValue;
    negPrefixPattern = null;
  }

  public String getPositiveSuffix() {
    return positiveSuffix;
  }

  public void setPositiveSuffix(String newValue) {
    positiveSuffix = newValue;
    posSuffixPattern = null;
  }

  public String getNegativeSuffix() {
    return negativeSuffix;
  }

  public void setNegativeSuffix(String newValue) {
    negativeSuffix = newValue;
    negSuffixPattern = null;
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
    if (newValue < 0 || newValue > 127) {
      throw new IllegalArgumentException("newValue is out of valid range. value: " + newValue);
    }
    groupingSize = newValue;
  }

  public boolean isDecimalSeparatorAlwaysShown() {
    return decimalSeparatorAlwaysShown;
  }

  public void setDecimalSeparatorAlwaysShown(boolean newValue) {
    decimalSeparatorAlwaysShown = newValue;
  }

  public boolean isParseBigDecimal() {
    return parseBigDecimal;
  }

  public void setParseBigDecimal(boolean newValue) {
    parseBigDecimal = newValue;
  }

  @Override
  public boolean isStrict() {
    return strict;
  }

  @Override
  public void setStrict(boolean value) {
    strict = value;
  }

  @Override
  public Currency getCurrency() {
    return symbols.getCurrency();
  }

  @Override
  public void setCurrency(Currency currency) {
    if (currency != symbols.getCurrency()) {
      symbols.setCurrency(currency);
      if (isCurrencyFormat) {
        expandAffixes();
      }
    }
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

  @Override
  public int getMaximumIntegerDigits() {
    return maxIntegerDigits;
  }

  @Override
  public int getMinimumIntegerDigits() {
    return minIntegerDigits;
  }

  @Override
  public int getMaximumFractionDigits() {
    return maxFractionDigits;
  }

  @Override
  public int getMinimumFractionDigits() {
    return minFractionDigits;
  }

  @Override
  public void setMaximumIntegerDigits(int newValue) {
    maxIntegerDigits = Math.min(Math.max(0, newValue), kMaxInteger);
    super.setMaximumIntegerDigits(Math.min(maxIntegerDigits, kDoubleInteger));
    if (minIntegerDigits > maxIntegerDigits) {
      minIntegerDigits = maxIntegerDigits;
      super.setMinimumIntegerDigits(Math.min(minIntegerDigits, kDoubleInteger));
    }
  }

  @Override
  public void setMinimumIntegerDigits(int newValue) {
    minIntegerDigits = Math.min(Math.max(0, newValue), kMaxInteger);
    super.setMinimumIntegerDigits(Math.min(minIntegerDigits, kDoubleInteger));
    if (minIntegerDigits > maxIntegerDigits) {
      maxIntegerDigits = minIntegerDigits;
      super.setMaximumIntegerDigits(Math.min(maxIntegerDigits, kDoubleInteger));
    }
  }

  @Override
  public void setMaximumFractionDigits(int newValue) {
    maxFractionDigits = Math.min(Math.max(0, newValue), kMaxInteger);
    super.setMaximumFractionDigits(Math.min(maxFractionDigits, kDoubleFraction));
    if (minFractionDigits > maxFractionDigits) {
      minFractionDigits = maxFractionDigits;
      super.setMinimumFractionDigits(Math.min(minFractionDigits, kDoubleFraction));
    }
  }

  @Override
  public void setMinimumFractionDigits(int newValue) {
    minFractionDigits = Math.min(Math.max(0, newValue), kMaxInteger);
    super.setMinimumFractionDigits(Math.min(minFractionDigits, kDoubleFraction));
    if (minFractionDigits > maxFractionDigits) {
      maxFractionDigits = minFractionDigits;
      super.setMaximumFractionDigits(Math.min(maxFractionDigits, kDoubleFraction));
    }
  }

  private void expandAffixes() {
    StringBuffer buffer = new StringBuffer();
    if (posPrefixPattern != null) {
      positivePrefix = expandAffix(posPrefixPattern, buffer);
    }
    if (posSuffixPattern != null) {
      positiveSuffix = expandAffix(posSuffixPattern, buffer);
    }
    if (negPrefixPattern != null) {
      negativePrefix = expandAffix(negPrefixPattern, buffer);
    }
    if (negSuffixPattern != null) {
      negativeSuffix = expandAffix(negSuffixPattern, buffer);
    }
  }

  private String expandAffix(String pattern, StringBuffer buffer) {
    buffer.setLength(0);
    for (int i = 0; i < pattern.length();) {
      char c = pattern.charAt(i++);
      if (c == kQuote) {
        c = pattern.charAt(i++);
        switch (c) {
          case kCurrencySign:
            if (i < pattern.length() && pattern.charAt(i) == kCurrencySign) {
              ++i;
              buffer.append(symbols.getInternationalCurrencySymbol());
            } else {
              buffer.append(symbols.getCurrencySymbol());
            }
            continue;
          case '%':
            buffer.append(symbols.getPercent());
            continue;
          case '‰':
            buffer.append(symbols.getPerMill());
            continue;
          case '-':
            buffer.append(symbols.getMinusSign());
            continue;
          default:
            break;
        }
      }
      buffer.append(c);
    }
    return buffer.toString();
  }

  public String toPattern() {
    return toPattern(false);
  }

  public String toLocalizedPattern() {
    return toPattern(true);
  }

  private String toPattern(boolean localized) {
    StringBuffer result = new StringBuffer();
    char zero = localized ? symbols.getZeroDigit() : '0';
    char digit = localized ? symbols.getDigit() : '#';
    char group = localized ? symbols.getGroupingSeparator() : ',';
    char decimal = localized ? symbols.getDecimalSeparator() : '.';
    for (int j = 1; j >= 0; --j) {
      if (j == 1) {
        appendAffix(result, posPrefixPattern, positivePrefix, localized);
      } else {
        appendAffix(result, negPrefixPattern, negativePrefix, localized);
      }
      int digitCount = useExponentialNotation ? getMaximumIntegerDigits()
          : Math.max(groupingSize, getMinimumIntegerDigits()) + 1;
      for (int i = digitCount; i > 0; --i) {
        if (i != digitCount && isGroupingUsed() && groupingSize != 0 && i % groupingSize == 0) {
          result.append(group);
        }
        result.append(i <= getMinimumIntegerDigits() ? zero : digit);
      }
      if (getMaximumFractionDigits() > 0 || decimalSeparatorAlwaysShown) {
        result.append(decimal);
      }
      for (int i = 0; i < getMaximumFractionDigits(); ++i) {
        result.append(i < getMinimumFractionDigits() ? zero : digit);
      }
      if (useExponentialNotation) {
        result.append(localized ? symbols.getExponentSeparator() : "E");
        for (int i = 0; i < minExponentDigits; ++i) {
          result.append(zero);
        }
      }
      if (j == 1) {
        appendAffix(result, posSuffixPattern, positiveSuffix, localized);
        if ((negSuffixPattern == posSuffixPattern && negativeSuffix.equals(positiveSuffix))
            || (negSuffixPattern != null && negSuffixPattern.equals(posSuffixPattern))) {
          if ((negPrefixPattern != null && posPrefixPattern != null
              && negPrefixPattern.equals("'-" + posPrefixPattern))
              || (negPrefixPattern == posPrefixPattern
                  && negativePrefix.equals(symbols.getMinusSign() + positivePrefix))) {
            break;
          }
        }
        result.append(localized ? symbols.getPatternSeparator() : ';');
      } else {
        appendAffix(result, negSuffixPattern, negativeSuffix, localized);
      }
    }
    return result.toString();
  }

  private void appendAffix(StringBuffer buffer, String affixPattern, String expAffix, boolean localized) {
    if (affixPattern == null) {
      appendAffix(buffer, expAffix, localized);
      return;
    }
    int i;
    for (int pos = 0; pos < affixPattern.length(); pos = i) {
      i = affixPattern.indexOf(kQuote, pos);
      if (i < 0) {
        appendAffix(buffer, affixPattern.substring(pos), localized);
        break;
      }
      if (i > pos) {
        appendAffix(buffer, affixPattern.substring(pos, i), localized);
      }
      char c = affixPattern.charAt(++i);
      ++i;
      if (c == kQuote) {
        buffer.append(c);
      } else if (c == kCurrencySign && i < affixPattern.length() && affixPattern.charAt(i) == kCurrencySign) {
        ++i;
        buffer.append(c);
      } else if (localized) {
        switch (c) {
          case '%':
            c = symbols.getPercent();
            break;
          case '‰':
            c = symbols.getPerMill();
            break;
          case '-':
            c = symbols.getMinusSign();
            break;
          default:
            break;
        }
      }
      buffer.append(c);
    }
  }

  private void appendAffix(StringBuffer buffer, String affix, boolean localized) {
    boolean needQuote;
    if (localized) {
      needQuote = affix.indexOf(symbols.getZeroDigit()) >= 0 || affix.indexOf(symbols.getGroupingSeparator()) >= 0
          || affix.indexOf(symbols.getDecimalSeparator()) >= 0 || affix.indexOf(symbols.getPercent()) >= 0
          || affix.indexOf(symbols.getPerMill()) >= 0 || affix.indexOf(symbols.getDigit()) >= 0
          || affix.indexOf(symbols.getPatternSeparator()) >= 0 || affix.indexOf(symbols.getMinusSign()) >= 0
          || affix.indexOf(kCurrencySign) >= 0;
    } else {
      needQuote = affix.indexOf('0') >= 0 || affix.indexOf(',') >= 0 || affix.indexOf('.') >= 0
          || affix.indexOf('%') >= 0 || affix.indexOf('‰') >= 0 || affix.indexOf('#') >= 0
          || affix.indexOf(';') >= 0 || affix.indexOf('-') >= 0 || affix.indexOf(kCurrencySign) >= 0;
    }
    if (needQuote) {
      buffer.append('\'');
    }
    if (affix.indexOf('\'') < 0) {
      buffer.append(affix);
    } else {
      for (int j = 0; j < affix.length(); ++j) {
        char c = affix.charAt(j);
        buffer.append(c);
        if (c == '\'') {
          buffer.append(c);
        }
      }
    }
    if (needQuote) {
      buffer.append('\'');
    }
  }

  public void applyPattern(String pattern) {
    applyPattern(pattern, false);
  }

  public void applyLocalizedPattern(String pattern) {
    applyPattern(pattern, true);
  }

  private void applyPattern(String pattern, boolean localized) {
    char zeroDigit = '0';
    char groupingSeparator = ',';
    char decimalSeparator = '.';
    char percent = '%';
    char perMill = '‰';
    char digit = '#';
    char separator = ';';
    String exponent = "E";
    char minus = '-';
    if (localized) {
      zeroDigit = symbols.getZeroDigit();
      groupingSeparator = symbols.getGroupingSeparator();
      decimalSeparator = symbols.getDecimalSeparator();
      percent = symbols.getPercent();
      perMill = symbols.getPerMill();
      digit = symbols.getDigit();
      separator = symbols.getPatternSeparator();
      exponent = symbols.getExponentSeparator();
      minus = symbols.getMinusSign();
    }
    boolean gotNegative = false;
    decimalSeparatorAlwaysShown = false;
    isCurrencyFormat = false;
    useExponentialNotation = false;
    int start = 0;
    for (int j = 1; j >= 0 && start < pattern.length(); --j) {
      boolean inQuote = false;
      StringBuffer prefix = new StringBuffer();
      StringBuffer suffix = new StringBuffer();
      int decimalPos = -1;
      int multiplier = 1;
      int digitLeftCount = 0;
      int zeroDigitCount = 0;
      int digitRightCount = 0;
      int groupingCount = -1;
      int phase = 0;
      StringBuffer affix = prefix;
      for (int pos = start; pos < pattern.length(); ++pos) {
        char ch = pattern.charAt(pos);
        switch (phase) {
          case 0:
          case 2:
            if (inQuote) {
              if (ch == kQuote) {
                if ((pos + 1) < pattern.length() && pattern.charAt(pos + 1) == kQuote) {
                  ++pos;
                  affix.append("''");
                } else {
                  inQuote = false;
                }
                continue;
              }
            } else {
              if (ch == digit || ch == zeroDigit || ch == groupingSeparator || ch == decimalSeparator) {
                phase = 1;
                --pos;
                continue;
              } else if (ch == kCurrencySign) {
                boolean doubled = (pos + 1) < pattern.length() && pattern.charAt(pos + 1) == kCurrencySign;
                if (doubled) {
                  ++pos;
                }
                isCurrencyFormat = true;
                affix.append(doubled ? "'¤¤" : "'¤");
                continue;
              } else if (ch == kQuote) {
                if (ch == kQuote) {
                  if ((pos + 1) < pattern.length() && pattern.charAt(pos + 1) == kQuote) {
                    ++pos;
                    affix.append("''");
                  } else {
                    inQuote = true;
                  }
                  continue;
                }
              } else if (ch == separator) {
                if (phase == 0 || j == 0) {
                  throw new IllegalArgumentException("Unquoted special character '" + ch + "' in pattern \""
                      + pattern + '"');
                }
                start = pos + 1;
                pos = pattern.length();
                continue;
              } else if (ch == percent) {
                if (multiplier != 1) {
                  throw new IllegalArgumentException("Too many percent/per mille characters in pattern \""
                      + pattern + '"');
                }
                multiplier = 100;
                affix.append("'%");
                continue;
              } else if (ch == perMill) {
                if (multiplier != 1) {
                  throw new IllegalArgumentException("Too many percent/per mille characters in pattern \""
                      + pattern + '"');
                }
                multiplier = 1000;
                affix.append("'‰");
                continue;
              } else if (ch == minus) {
                affix.append("'-");
                continue;
              }
            }
            affix.append(ch);
            break;
          case 1:
            if (j == 0) {
              while (pos < pattern.length()) {
                char negPatternChar = pattern.charAt(pos);
                if (negPatternChar == digit || negPatternChar == zeroDigit || negPatternChar == groupingSeparator
                    || negPatternChar == decimalSeparator) {
                  ++pos;
                } else if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                  pos = pos + exponent.length();
                } else {
                  --pos;
                  phase = 2;
                  affix = suffix;
                  break;
                }
              }
              continue;
            }
            if (ch == digit) {
              if (zeroDigitCount > 0) {
                ++digitRightCount;
              } else {
                ++digitLeftCount;
              }
              if (groupingCount >= 0 && decimalPos < 0) {
                ++groupingCount;
              }
            } else if (ch == zeroDigit) {
              if (digitRightCount > 0) {
                throw new IllegalArgumentException("Unexpected '0' in pattern \"" + pattern + '"');
              }
              ++zeroDigitCount;
              if (groupingCount >= 0 && decimalPos < 0) {
                ++groupingCount;
              }
            } else if (ch == groupingSeparator) {
              groupingCount = 0;
            } else if (ch == decimalSeparator) {
              if (decimalPos >= 0) {
                throw new IllegalArgumentException("Multiple decimal separators in pattern \"" + pattern + '"');
              }
              decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
            } else if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
              if (useExponentialNotation) {
                throw new IllegalArgumentException("Multiple exponential symbols in pattern \"" + pattern + '"');
              }
              useExponentialNotation = true;
              minExponentDigits = 0;
              pos = pos + exponent.length();
              while (pos < pattern.length() && pattern.charAt(pos) == zeroDigit) {
                ++minExponentDigits;
                ++pos;
              }
              if ((digitLeftCount + zeroDigitCount) < 1 || minExponentDigits < 1) {
                throw new IllegalArgumentException("Malformed exponential pattern \"" + pattern + '"');
              }
              phase = 2;
              affix = suffix;
              --pos;
              continue;
            } else {
              phase = 2;
              affix = suffix;
              --pos;
              continue;
            }
            break;
          default:
            break;
        }
      }
      if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
        int n = decimalPos;
        if (n == 0) {
          ++n;
        }
        digitRightCount = digitLeftCount - n;
        digitLeftCount = n - 1;
        zeroDigitCount = 1;
      }
      if ((decimalPos < 0 && digitRightCount > 0)
          || (decimalPos >= 0 && (decimalPos < digitLeftCount || decimalPos > (digitLeftCount + zeroDigitCount)))
          || groupingCount == 0 || inQuote) {
        throw new IllegalArgumentException("Malformed pattern \"" + pattern + '"');
      }
      if (j == 1) {
        posPrefixPattern = prefix.toString();
        posSuffixPattern = suffix.toString();
        negPrefixPattern = posPrefixPattern;
        negSuffixPattern = posSuffixPattern;
        int digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
        int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
        setMinimumIntegerDigits(effectiveDecimalPos - digitLeftCount);
        setMaximumIntegerDigits(useExponentialNotation ? digitLeftCount + getMinimumIntegerDigits() : kMaxInteger);
        setMaximumFractionDigits(decimalPos >= 0 ? (digitTotalCount - decimalPos) : 0);
        setMinimumFractionDigits(decimalPos >= 0 ? (digitLeftCount + zeroDigitCount - decimalPos) : 0);
        setGroupingUsed(groupingCount > 0);
        this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
        this.multiplier = multiplier;
        setDecimalSeparatorAlwaysShown(decimalPos == 0 || decimalPos == digitTotalCount);
      } else {
        negPrefixPattern = prefix.toString();
        negSuffixPattern = suffix.toString();
        gotNegative = true;
      }
    }
    if (pattern.isEmpty()) {
      posPrefixPattern = posSuffixPattern = "";
      setMinimumIntegerDigits(0);
      setMaximumIntegerDigits(kMaxInteger);
      setMinimumFractionDigits(0);
      setMaximumFractionDigits(kMaxInteger);
    }
    if (!gotNegative || (negPrefixPattern.equals(posPrefixPattern) && negSuffixPattern.equals(posSuffixPattern))) {
      negSuffixPattern = posSuffixPattern;
      negPrefixPattern = "'-" + posPrefixPattern;
    }
    expandAffixes();
  }

  @Override
  public String toString() {
    return "DecimalFormat [locale: \"" + symbols.getLocale().getDisplayName() + "\", pattern: \"" + toPattern()
        + "\"]\n";
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 37 + positivePrefix.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    DecimalFormat other = (DecimalFormat) obj;
    return Objects.equals(posPrefixPattern, other.posPrefixPattern)
        && Objects.equals(posSuffixPattern, other.posSuffixPattern)
        && Objects.equals(negPrefixPattern, other.negPrefixPattern)
        && Objects.equals(negSuffixPattern, other.negSuffixPattern)
        && positivePrefix.equals(other.positivePrefix) && positiveSuffix.equals(other.positiveSuffix)
        && negativePrefix.equals(other.negativePrefix) && negativeSuffix.equals(other.negativeSuffix)
        && multiplier == other.multiplier && groupingSize == other.groupingSize
        && decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown
        && parseBigDecimal == other.parseBigDecimal && useExponentialNotation == other.useExponentialNotation
        && (!useExponentialNotation || minExponentDigits == other.minExponentDigits)
        && maxIntegerDigits == other.maxIntegerDigits && minIntegerDigits == other.minIntegerDigits
        && maxFractionDigits == other.maxFractionDigits && minFractionDigits == other.minFractionDigits
        && roundingMode == other.roundingMode && symbols.equals(other.symbols);
  }

  @Override
  public Object clone() {
    DecimalFormat other = (DecimalFormat) super.clone();
    other.symbols = (DecimalFormatSymbols) symbols.clone();
    other.digitList = new DigitList();
    return other;
  }
}
