/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;

public final class Currency implements Serializable {
  private static final long serialVersionUID = -158308464356906721L;

  private static final Map<String, Currency> kInstances = new HashMap<String, Currency>();

  private final String currencyCode;
  private final transient int defaultFractionDigits;
  private final transient int numericCode;
  private final transient String symbol;
  private final transient String displayName;

  private Object readResolve() {
    return getInstance(currencyCode);
  }

  private Currency(String[] row) {
    this.currencyCode = row[0];
    this.numericCode = Integer.parseInt(row[1]);
    this.defaultFractionDigits = Integer.parseInt(row[2]);
    this.symbol = row[3];
    this.displayName = row[4];
  }

  public static Currency getInstance(String currencyCode) {
    if (currencyCode == null) {
      throw new NullPointerException();
    }
    if (currencyCode.length() != 3) {
      throw new IllegalArgumentException("The input currency code: \"" + currencyCode
          + "\" must have a length of 3 characters");
    }
    char first = currencyCode.charAt(0);
    char second = currencyCode.charAt(1);
    if (first < 'A' || first > 'Z' || second < 'A' || second > 'Z') {
      throw new IllegalArgumentException("The country code: \"" + currencyCode.substring(0, 2)
          + "\" is not a valid ISO 3166 code");
    }
    synchronized (kInstances) {
      Currency existing = kInstances.get(currencyCode);
      if (existing != null) {
        return existing;
      }
      for (String[] row : CurrencyData.kRows) {
        if (row[0].equals(currencyCode)) {
          Currency created = new Currency(row);
          kInstances.put(currencyCode, created);
          return created;
        }
      }
    }
    throw new IllegalArgumentException("The input currency code: \"" + currencyCode
        + "\" is not a valid ISO 4217 code");
  }

  public static Currency getInstance(Locale locale) {
    String country = locale.getCountry();
    if ("XK".equals(country)) {
      return getInstance("EUR");
    }
    if (country == null || !country.matches("^[a-zA-Z]{2}$")
        || !Arrays.asList(Locale.getISOCountries()).contains(country.toUpperCase(Locale.ROOT))) {
      throw new IllegalArgumentException("The country of the input locale: \"" + locale
          + "\" is not a valid ISO 3166 country code");
    }
    country = country.toUpperCase(Locale.ROOT);
    for (String pair : CurrencyData.kCountries) {
      if (pair.startsWith(country)) {
        return getInstance(pair.substring(2));
      }
    }
    return null;
  }

  public static Set<Currency> getAvailableCurrencies() {
    Set<Currency> all = new HashSet<Currency>();
    for (String[] row : CurrencyData.kRows) {
      all.add(getInstance(row[0]));
    }
    return all;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public String getSymbol() {
    return getSymbol(Locale.getDefault(Locale.Category.DISPLAY));
  }

  public String getSymbol(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    String[] entry = shim.LocaleDb.currency().list(locale, "c." + currencyCode);
    return entry != null ? entry[0] : currencyCode;
  }

  public int getDefaultFractionDigits() {
    return defaultFractionDigits;
  }

  public int getNumericCode() {
    return numericCode;
  }

  public String getNumericCodeAsString() {
    if (numericCode < 100) {
      StringBuilder sb = new StringBuilder();
      sb.append('0');
      if (numericCode < 10) {
        sb.append('0');
      }
      return sb.append(numericCode).toString();
    }
    return Integer.toString(numericCode);
  }

  public String getDisplayName() {
    return getDisplayName(Locale.getDefault(Locale.Category.DISPLAY));
  }

  public String getDisplayName(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    String[] entry = shim.LocaleDb.currency().list(locale, "c." + currencyCode);
    return entry != null ? entry[1] : currencyCode;
  }

  @Override
  public String toString() {
    return currencyCode;
  }

  public static java.util.stream.Stream<Currency> availableCurrencies() {
    return getAvailableCurrencies().stream();
  }
}
