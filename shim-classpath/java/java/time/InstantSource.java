/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.util.Objects;

public interface InstantSource {
  static InstantSource system() {
    return Clock.systemUTC();
  }

  static InstantSource tick(InstantSource baseSource, Duration tickDuration) {
    if (baseSource instanceof Clock) {
      return Clock.tick((Clock) baseSource, tickDuration);
    }
    Objects.requireNonNull(baseSource, "baseSource");
    return Clock.tick(baseSource.withZone(ZoneOffset.UTC), tickDuration);
  }

  static InstantSource fixed(Instant fixedInstant) {
    return Clock.fixed(fixedInstant, ZoneOffset.UTC);
  }

  static InstantSource offset(InstantSource baseSource, Duration offsetDuration) {
    if (baseSource instanceof Clock) {
      return Clock.offset((Clock) baseSource, offsetDuration);
    }
    Objects.requireNonNull(baseSource, "baseSource");
    return Clock.offset(baseSource.withZone(ZoneOffset.UTC), offsetDuration);
  }

  Instant instant();

  default long millis() {
    return instant().toEpochMilli();
  }

  default Clock withZone(final ZoneId zone) {
    return Clock.of(this, zone);
  }
}
