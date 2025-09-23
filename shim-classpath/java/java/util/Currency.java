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

  private static final String[][] kTable = {
      {"USD", "840", "2", "$", "US Dollar", "US"},
      {"EUR", "978", "2", "€", "Euro", ""},
      {"GBP", "826", "2", "£", "British Pound", "GB"},
      {"JPY", "392", "0", "¥", "Japanese Yen", "JP"},
      {"CNY", "156", "2", "CN¥", "Chinese Yuan", "CN"},
      {"CHF", "756", "2", "CHF", "Swiss Franc", "CH"},
      {"CAD", "124", "2", "CA$", "Canadian Dollar", "CA"},
      {"AUD", "36", "2", "A$", "Australian Dollar", "AU"},
      {"NZD", "554", "2", "NZ$", "New Zealand Dollar", "NZ"},
      {"INR", "356", "2", "₹", "Indian Rupee", "IN"},
      {"KRW", "410", "0", "₩", "South Korean Won", "KR"},
      {"BRL", "986", "2", "R$", "Brazilian Real", "BR"},
      {"MXN", "484", "2", "MX$", "Mexican Peso", "MX"},
      {"RUB", "643", "2", "RUB", "Russian Ruble", "RU"},
      {"SEK", "752", "2", "SEK", "Swedish Krona", "SE"},
      {"NOK", "578", "2", "NOK", "Norwegian Krone", "NO"},
      {"DKK", "208", "2", "DKK", "Danish Krone", "DK"},
      {"PLN", "985", "2", "PLN", "Polish Zloty", "PL"},
      {"CZK", "203", "2", "CZK", "Czech Koruna", "CZ"},
      {"HUF", "348", "2", "HUF", "Hungarian Forint", "HU"},
      {"TRY", "949", "2", "TRY", "Turkish Lira", "TR"},
      {"ZAR", "710", "2", "ZAR", "South African Rand", "ZA"},
      {"SGD", "702", "2", "SGD", "Singapore Dollar", "SG"},
      {"HKD", "344", "2", "HK$", "Hong Kong Dollar", "HK"},
      {"ILS", "376", "2", "₪", "Israeli New Shekel", "IL"},
      {"THB", "764", "2", "THB", "Thai Baht", "TH"},
      {"IDR", "360", "2", "IDR", "Indonesian Rupiah", "ID"},
      {"ARS", "32", "2", "ARS", "Argentine Peso", "AR"},
      {"CLP", "152", "0", "CLP", "Chilean Peso", "CL"},
      {"COP", "170", "2", "COP", "Colombian Peso", "CO"},
      {"EGP", "818", "2", "EGP", "Egyptian Pound", "EG"},
      {"SAR", "682", "2", "SAR", "Saudi Riyal", "SA"},
      {"AED", "784", "2", "AED", "United Arab Emirates Dirham", "AE"},
      {"KWD", "414", "3", "KWD", "Kuwaiti Dinar", "KW"},
      {"BHD", "48", "3", "BHD", "Bahraini Dinar", "BH"},
      {"VND", "704", "0", "₫", "Vietnamese Dong", "VN"},
      {"UAH", "980", "2", "UAH", "Ukrainian Hryvnia", "UA"},
      {"RON", "946", "2", "RON", "Romanian Leu", "RO"},
      {"BGN", "975", "2", "BGN", "Bulgarian Lev", "BG"},
      {"ISK", "352", "0", "ISK", "Icelandic Króna", "IS"},
      {"PHP", "608", "2", "₱", "Philippine Peso", "PH"},
      {"MYR", "458", "2", "MYR", "Malaysian Ringgit", "MY"},
      {"XXX", "999", "-1", "¤", "Unknown Currency", ""}};
  private static final Map<String, Currency> kInstances = new HashMap<String, Currency>();

  private final String currencyCode;
  private final int defaultFractionDigits;
  private final int numericCode;
  private final String symbol;
  private final String displayName;

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
    synchronized (kInstances) {
      Currency existing = kInstances.get(currencyCode);
      if (existing != null) {
        return existing;
      }
      for (String[] row : kTable) {
        if (row[0].equals(currencyCode)) {
          Currency created = new Currency(row);
          kInstances.put(currencyCode, created);
          return created;
        }
      }
    }
    throw new IllegalArgumentException();
  }

  public static Currency getInstance(Locale locale) {
    String country = locale.getCountry();
    if (country == null || country.length() != 2) {
      throw new IllegalArgumentException();
    }
    String[] euro = {"AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "HR", "IE", "IT", "LT", "LU", "LV", "MT",
        "NL", "PT", "SI", "SK"};
    for (String code : euro) {
      if (code.equals(country)) {
        return getInstance("EUR");
      }
    }
    for (String[] row : kTable) {
      if (row[5].equals(country)) {
        return getInstance(row[0]);
      }
    }
    return null;
  }

  public static Set<Currency> getAvailableCurrencies() {
    Set<Currency> all = new HashSet<Currency>();
    for (String[] row : kTable) {
      if (!row[0].equals("XXX")) {
        all.add(getInstance(row[0]));
      }
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
    if (currencyCode.equals("USD") && !"US".equals(locale.getCountry()) && !locale.getCountry().isEmpty()
        && !locale.getLanguage().equals("en")) {
      return "$";
    }
    return symbol;
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
    return displayName;
  }

  public String getDisplayName(Locale locale) {
    return displayName;
  }

  @Override
  public String toString() {
    return currencyCode;
  }
}
