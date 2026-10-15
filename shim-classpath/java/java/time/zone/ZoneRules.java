/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ZoneRules implements Serializable {
  private final ZoneOffset standardOffset;

  private ZoneRules(ZoneOffset offset) {
    this.standardOffset = offset;
  }

  public static ZoneRules of(ZoneOffset offset) {
    Objects.requireNonNull(offset, "offset");
    return new ZoneRules(offset);
  }

  public boolean isFixedOffset() {
    return true;
  }

  public ZoneOffset getOffset(Instant instant) {
    return standardOffset;
  }

  public ZoneOffset getOffset(LocalDateTime localDateTime) {
    return standardOffset;
  }

  public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
    return Collections.singletonList(standardOffset);
  }

  public ZoneOffset getStandardOffset(Instant instant) {
    return standardOffset;
  }

  public Duration getDaylightSavings(Instant instant) {
    return Duration.ZERO;
  }

  public boolean isDaylightSavings(Instant instant) {
    return false;
  }

  public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
    return standardOffset.equals(offset);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof ZoneRules) {
      return standardOffset.equals(((ZoneRules) other).standardOffset);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return standardOffset.hashCode();
  }

  @Override
  public String toString() {
    return "ZoneRules[currentStandardOffset=" + standardOffset + "]";
  }
}
