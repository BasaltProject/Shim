/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class DecimalFormatSymbols implements Cloneable, Serializable {
  private static final long serialVersionUID = 5772796143397350380L;

  private char zeroDigit;
  private char groupingSeparator;
  private char decimalSeparator;
  private char perMill;
  private char percent;
  private char digit;
  private char patternSeparator;
  private String infinity;
  private String NaN;
  private char minusSign;
  private String currencySymbol;
  private String intlCurrencySymbol;
  private char monetaryDecimalSeparator;
  private String exponentSeparator;
  private Locale locale;

  public DecimalFormatSymbols() {
    this(Locale.getDefault());
  }

  public DecimalFormatSymbols(Locale locale) {
    this.locale = locale == null ? Locale.getDefault() : locale;
    initialize();
  }

  public static DecimalFormatSymbols getInstance() {
    return new DecimalFormatSymbols();
  }

  public static DecimalFormatSymbols getInstance(Locale locale) {
    return new DecimalFormatSymbols(locale);
  }

  private void initialize() {
    this.zeroDigit = '0';
    this.groupingSeparator = ',';
    this.decimalSeparator = '.';
    this.perMill = '\u2030';
    this.percent = '%';
    this.digit = '#';
    this.patternSeparator = ';';
    this.infinity = "\u221E";
    this.NaN = "NaN";
    this.minusSign = '-';
    this.currencySymbol = "$";
    this.intlCurrencySymbol = "USD";
    this.monetaryDecimalSeparator = '.';
    this.exponentSeparator = "E";
  }

  public char getZeroDigit() {
    return zeroDigit;
  }

  public void setZeroDigit(char zeroDigit) {
    this.zeroDigit = zeroDigit;
  }

  public char getGroupingSeparator() {
    return groupingSeparator;
  }

  public void setGroupingSeparator(char groupingSeparator) {
    this.groupingSeparator = groupingSeparator;
  }

  public char getDecimalSeparator() {
    return decimalSeparator;
  }

  public void setDecimalSeparator(char decimalSeparator) {
    this.decimalSeparator = decimalSeparator;
  }

  public char getPerMill() {
    return perMill;
  }

  public void setPerMill(char perMill) {
    this.perMill = perMill;
  }

  public char getPercent() {
    return percent;
  }

  public void setPercent(char percent) {
    this.percent = percent;
  }

  public char getDigit() {
    return digit;
  }

  public void setDigit(char digit) {
    this.digit = digit;
  }

  public char getPatternSeparator() {
    return patternSeparator;
  }

  public void setPatternSeparator(char patternSeparator) {
    this.patternSeparator = patternSeparator;
  }

  public String getInfinity() {
    return infinity;
  }

  public void setInfinity(String infinity) {
    this.infinity = Objects.requireNonNull(infinity, "infinity");
  }

  public String getNaN() {
    return NaN;
  }

  public void setNaN(String NaN) {
    this.NaN = Objects.requireNonNull(NaN, "NaN");
  }

  public char getMinusSign() {
    return minusSign;
  }

  public void setMinusSign(char minusSign) {
    this.minusSign = minusSign;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol(String currency) {
    this.currencySymbol = Objects.requireNonNull(currency, "currency");
  }

  public String getInternationalCurrencySymbol() {
    return intlCurrencySymbol;
  }

  public void setInternationalCurrencySymbol(String currencyCode) {
    this.intlCurrencySymbol = Objects.requireNonNull(currencyCode, "currencyCode");
  }

  public char getMonetaryDecimalSeparator() {
    return monetaryDecimalSeparator;
  }

  public void setMonetaryDecimalSeparator(char sep) {
    this.monetaryDecimalSeparator = sep;
  }

  public String getExponentSeparator() {
    return exponentSeparator;
  }

  public void setExponentSeparator(String exp) {
    this.exponentSeparator = Objects.requireNonNull(exp, "exp");
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DecimalFormatSymbols)) return false;
    DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
    return zeroDigit == other.zeroDigit
        && groupingSeparator == other.groupingSeparator
        && decimalSeparator == other.decimalSeparator
        && percent == other.percent
        && perMill == other.perMill
        && digit == other.digit
        && minusSign == other.minusSign
        && patternSeparator == other.patternSeparator
        && monetaryDecimalSeparator == other.monetaryDecimalSeparator
        && Objects.equals(NaN, other.NaN)
        && Objects.equals(infinity, other.infinity)
        && Objects.equals(currencySymbol, other.currencySymbol)
        && Objects.equals(intlCurrencySymbol, other.intlCurrencySymbol)
        && Objects.equals(exponentSeparator, other.exponentSeparator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(zeroDigit, groupingSeparator, decimalSeparator, percent,
        perMill, digit, minusSign, patternSeparator, monetaryDecimalSeparator,
        NaN, infinity, currencySymbol, intlCurrencySymbol, exponentSeparator);
  }
}
