/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DecimalStyle {
  public static final DecimalStyle STANDARD = new DecimalStyle('0', '+', '-', '.');

  private static final ConcurrentMap<Locale, DecimalStyle> CACHE = new ConcurrentHashMap<Locale, DecimalStyle>(16, 0.75f, 2);

  private final char zeroDigit;
  private final char positiveSign;
  private final char negativeSign;
  private final char decimalSeparator;

  public static Set<Locale> getAvailableLocales() {
    Set<Locale> locales = new HashSet<Locale>();
    locales.add(Locale.ROOT);
    return locales;
  }

  public static DecimalStyle ofDefaultLocale() {
    return of(Locale.getDefault());
  }

  public static DecimalStyle of(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    DecimalStyle info = CACHE.get(locale);
    if (info == null) {
      info = STANDARD;
      CACHE.putIfAbsent(locale, info);
      info = CACHE.get(locale);
    }
    return info;
  }

  private DecimalStyle(char zeroChar, char positiveSignChar, char negativeSignChar, char decimalPointChar) {
    this.zeroDigit = zeroChar;
    this.positiveSign = positiveSignChar;
    this.negativeSign = negativeSignChar;
    this.decimalSeparator = decimalPointChar;
  }

  public char getZeroDigit() {
    return zeroDigit;
  }

  public DecimalStyle withZeroDigit(char zeroDigit) {
    if (zeroDigit == this.zeroDigit) {
      return this;
    }
    return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
  }

  public char getPositiveSign() {
    return positiveSign;
  }

  public DecimalStyle withPositiveSign(char positiveSign) {
    if (positiveSign == this.positiveSign) {
      return this;
    }
    return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
  }

  public char getNegativeSign() {
    return negativeSign;
  }

  public DecimalStyle withNegativeSign(char negativeSign) {
    if (negativeSign == this.negativeSign) {
      return this;
    }
    return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
  }

  public char getDecimalSeparator() {
    return decimalSeparator;
  }

  public DecimalStyle withDecimalSeparator(char decimalSeparator) {
    if (decimalSeparator == this.decimalSeparator) {
      return this;
    }
    return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
  }

  int convertToDigit(char ch) {
    int val = ch - zeroDigit;
    return (val >= 0 && val <= 9) ? val : -1;
  }

  String convertNumberToI18N(String numericText) {
    if (zeroDigit == '0') {
      return numericText;
    }
    int diff = zeroDigit - '0';
    char[] array = numericText.toCharArray();
    for (int i = 0; i < array.length; i++) {
      array[i] = (char) (array[i] + diff);
    }
    return new String(array);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DecimalStyle) {
      DecimalStyle other = (DecimalStyle) obj;
      return (zeroDigit == other.zeroDigit && positiveSign == other.positiveSign
          && negativeSign == other.negativeSign && decimalSeparator == other.decimalSeparator);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return zeroDigit + positiveSign + negativeSign + decimalSeparator;
  }

  @Override
  public String toString() {
    return "DecimalStyle[" + zeroDigit + positiveSign + negativeSign + decimalSeparator + "]";
  }
}
