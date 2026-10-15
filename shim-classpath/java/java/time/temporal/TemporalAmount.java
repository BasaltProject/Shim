/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.util.List;

public interface TemporalAmount {
  long get(TemporalUnit unit);

  List<TemporalUnit> getUnits();

  Temporal addTo(Temporal temporal);

  Temporal subtractFrom(Temporal temporal);
}
