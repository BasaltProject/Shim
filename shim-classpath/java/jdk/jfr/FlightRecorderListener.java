/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public interface FlightRecorderListener {
  public default void recorderInitialized(jdk.jfr.FlightRecorder arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public default void recordingStateChanged(jdk.jfr.Recording arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
