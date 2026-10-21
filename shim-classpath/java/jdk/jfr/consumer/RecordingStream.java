/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordingStream implements java.lang.AutoCloseable, jdk.jfr.consumer.EventStream {
  public RecordingStream(jdk.jfr.Configuration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public RecordingStream() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean remove(java.lang.Object arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void start() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean stop() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void close() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void dump(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings enable(java.lang.Class<? extends jdk.jfr.Event> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings enable(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onClose(java.lang.Runnable arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void awaitTermination() throws java.lang.InterruptedException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void awaitTermination(java.time.Duration arg0) throws java.lang.InterruptedException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setSettings(java.util.Map<java.lang.String, java.lang.String> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setStartTime(java.time.Instant arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setMaxAge(java.time.Duration arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setMaxSize(long arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings disable(java.lang.String arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.EventSettings disable(java.lang.Class<? extends jdk.jfr.Event> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onMetadata(java.util.function.Consumer<jdk.jfr.consumer.MetadataEvent> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onEvent(java.util.function.Consumer<jdk.jfr.consumer.RecordedEvent> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onEvent(java.lang.String arg0, java.util.function.Consumer<jdk.jfr.consumer.RecordedEvent> arg1) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onFlush(java.lang.Runnable arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onError(java.util.function.Consumer<java.lang.Throwable> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setReuse(boolean arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setOrdered(boolean arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void setEndTime(java.time.Instant arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void startAsync() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
