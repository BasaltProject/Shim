/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text.spi;

import java.text.DateFormat;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public abstract class DateFormatProvider extends LocaleServiceProvider {
  protected DateFormatProvider() {
  }

  public abstract DateFormat getTimeInstance(int style, Locale locale);

  public abstract DateFormat getDateInstance(int style, Locale locale);

  public abstract DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale);
}
