/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.Locale;

public abstract class LocaleNameProvider extends LocaleServiceProvider {
  protected LocaleNameProvider() {
  }

  public abstract String getDisplayLanguage(String languageCode, Locale locale);

  public String getDisplayScript(String scriptCode, Locale locale) {
    return null;
  }

  public abstract String getDisplayCountry(String countryCode, Locale locale);

  public abstract String getDisplayVariant(String variant, Locale locale);

  public String getDisplayUnicodeExtensionKey(String key, Locale locale) {
    return null;
  }

  public String getDisplayUnicodeExtensionType(String extensionType, String key, Locale locale) {
    return null;
  }
}
