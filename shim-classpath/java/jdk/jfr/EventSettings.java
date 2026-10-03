/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.time.Duration;

public abstract class EventSettings {
  EventSettings() {
  }

  public abstract EventSettings with(String name, String value);

  public final EventSettings withStackTrace() {
    return with(StackTrace.NAME, "true");
  }

  public final EventSettings withoutStackTrace() {
    return with(StackTrace.NAME, "false");
  }

  public final EventSettings withPeriod(Duration duration) {
    java.util.Objects.requireNonNull(duration);
    return with(Period.NAME, JfrRuntime.formatTimespan(duration));
  }

  public final EventSettings withThreshold(Duration duration) {
    java.util.Objects.requireNonNull(duration);
    return with(Threshold.NAME, JfrRuntime.formatTimespan(duration));
  }

  public final EventSettings withoutThreshold() {
    return with(Threshold.NAME, "0 ns");
  }
}
