/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public abstract class EventSettings {
  public abstract jdk.jfr.EventSettings with(java.lang.String arg0, java.lang.String arg1);
  public final jdk.jfr.EventSettings withStackTrace() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public final jdk.jfr.EventSettings withoutStackTrace() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public final jdk.jfr.EventSettings withoutThreshold() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public final jdk.jfr.EventSettings withPeriod(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public final jdk.jfr.EventSettings withThreshold(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
