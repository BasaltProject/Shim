/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordingFile implements java.io.Closeable {
  public RecordingFile(java.nio.file.Path arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void write(java.nio.file.Path arg0, java.util.function.Predicate<jdk.jfr.consumer.RecordedEvent> arg1) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void close() throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.consumer.RecordedEvent readEvent() throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean hasMoreEvents() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.util.List<jdk.jfr.EventType> readEventTypes() throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static java.util.List<jdk.jfr.consumer.RecordedEvent> readAllEvents(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
