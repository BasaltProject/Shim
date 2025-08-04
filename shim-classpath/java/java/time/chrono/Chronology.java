/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.chrono;

public interface Chronology extends Comparable<Chronology> {
  String getId();

  String getCalendarType();

  default int compareTo(Chronology other) {
    return getId().compareTo(other.getId());
  }
}
