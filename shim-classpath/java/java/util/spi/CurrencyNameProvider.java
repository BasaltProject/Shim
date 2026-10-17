/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.Locale;

public abstract class CurrencyNameProvider extends LocaleServiceProvider {
  protected CurrencyNameProvider() {
  }

  public abstract String getSymbol(String currencyCode, Locale locale);

  public String getDisplayName(String currencyCode, Locale locale) {
    java.util.Objects.requireNonNull(currencyCode);
    java.util.Objects.requireNonNull(locale);
    java.util.Currency.getInstance(currencyCode);
    if (!isSupportedLocale(locale)) {
      throw new IllegalArgumentException("The locale is not available");
    }
    return null;
  }
}
