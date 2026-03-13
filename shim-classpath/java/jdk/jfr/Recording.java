/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

public final class Recording implements java.io.Closeable {
  public Recording(jdk.jfr.Configuration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public Recording() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public Recording(java.util.Map<java.lang.String, java.lang.String> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getName() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.Recording copy(boolean arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void start() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean stop() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setName(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.RecordingState getState() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void close() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void dump(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getSize() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings enable(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings enable(java.lang.Class<? extends jdk.jfr.Event> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Duration getDuration() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.Map<java.lang.String, java.lang.String> getSettings() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setSettings(java.util.Map<java.lang.String, java.lang.String> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean isToDisk() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Instant getStartTime() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Instant getStopTime() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.nio.file.Path getDestination() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.time.Duration getMaxAge() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getMaxSize() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setMaxAge(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setMaxSize(long arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean getDumpOnExit() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setDumpOnExit(boolean arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setToDisk(boolean arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void scheduleStart(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setDestination(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setDuration(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.io.InputStream getStream(java.time.Instant arg0, java.time.Instant arg1) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings disable(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings disable(java.lang.Class<? extends jdk.jfr.Event> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
