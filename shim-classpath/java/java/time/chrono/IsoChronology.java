/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

import java.io.Serializable;

public final class IsoChronology implements Chronology, Serializable {
  public static final IsoChronology INSTANCE = new IsoChronology();

  private IsoChronology() {}

  @Override
  public String getId() {
    return "ISO";
  }

  @Override
  public String getCalendarType() {
    return "iso8601";
  }
}
