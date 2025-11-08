/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.Locale;

public abstract class LocaleServiceProvider {
  protected LocaleServiceProvider() {
  }

  public abstract Locale[] getAvailableLocales();

  public boolean isSupportedLocale(Locale locale) {
    locale = locale.stripExtensions();
    for (Locale available : getAvailableLocales()) {
      if (locale.equals(available.stripExtensions())) {
        return true;
      }
    }
    return false;
  }
}
