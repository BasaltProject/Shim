/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.Locale;
import java.util.Map;

public abstract class CalendarNameProvider extends LocaleServiceProvider {
  protected CalendarNameProvider() {
  }

  public abstract String getDisplayName(String calendarType, int field, int value, int style, Locale locale);

  public abstract Map<String, Integer> getDisplayNames(String calendarType, int field, int style, Locale locale);
}
