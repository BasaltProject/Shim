/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public final class FlightRecorder {
  public static void register(java.lang.Class<? extends jdk.jfr.Event> arg0) { }
  public static void unregister(java.lang.Class<? extends jdk.jfr.Event> arg0) { }
  public static boolean isAvailable() { return false; }
  public static jdk.jfr.FlightRecorder getFlightRecorder() throws java.lang.IllegalStateException { throw new java.lang.IllegalStateException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.Recording> getRecordings() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static void addListener(jdk.jfr.FlightRecorderListener arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static boolean removeListener(jdk.jfr.FlightRecorderListener arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.Recording takeSnapshot() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static void addPeriodicEvent(java.lang.Class<? extends jdk.jfr.Event> arg0, java.lang.Runnable arg1) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static boolean removePeriodicEvent(java.lang.Runnable arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.EventType> getEventTypes() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static boolean isInitialized() { return false; }
}
