/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.util.Locale;

public abstract class CalendarDataProvider extends LocaleServiceProvider {
  protected CalendarDataProvider() {
  }

  public abstract int getFirstDayOfWeek(Locale locale);

  public abstract int getMinimalDaysInFirstWeek(Locale locale);
}
