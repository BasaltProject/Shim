/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text.spi;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class NumberFormatProvider extends LocaleServiceProvider {
  protected NumberFormatProvider() {
  }

  public abstract NumberFormat getCurrencyInstance(Locale locale);

  public abstract NumberFormat getIntegerInstance(Locale locale);

  public abstract NumberFormat getNumberInstance(Locale locale);

  public abstract NumberFormat getPercentInstance(Locale locale);

  public NumberFormat getCompactNumberInstance(Locale locale, NumberFormat.Style formatStyle) {
    throw new UnsupportedOperationException("The " + this.getClass().getName()
        + " should override this method to return compact number format instance of " + locale + " locale and "
        + formatStyle + " style.");
  }
}
