/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text.spi;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class DecimalFormatSymbolsProvider extends LocaleServiceProvider {
  protected DecimalFormatSymbolsProvider() {
  }

  public abstract DecimalFormatSymbols getInstance(Locale locale);
}
