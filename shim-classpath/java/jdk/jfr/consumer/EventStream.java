/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public interface EventStream extends java.lang.AutoCloseable {
  public boolean remove(java.lang.Object arg0);
  public void start();
  public void close();
  public void onClose(java.lang.Runnable arg0);
  public void awaitTermination() throws java.lang.InterruptedException;
  public void awaitTermination(java.time.Duration arg0) throws java.lang.InterruptedException;
  public void setStartTime(java.time.Instant arg0);
  public static jdk.jfr.consumer.EventStream openRepository() throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static jdk.jfr.consumer.EventStream openRepository(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public static jdk.jfr.consumer.EventStream openFile(java.nio.file.Path arg0) throws java.io.IOException { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public default void onMetadata(java.util.function.Consumer<jdk.jfr.consumer.MetadataEvent> arg0) { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public void onEvent(java.util.function.Consumer<jdk.jfr.consumer.RecordedEvent> arg0);
  public void onEvent(java.lang.String arg0, java.util.function.Consumer<jdk.jfr.consumer.RecordedEvent> arg1);
  public void onFlush(java.lang.Runnable arg0);
  public void onError(java.util.function.Consumer<java.lang.Throwable> arg0);
  public void setReuse(boolean arg0);
  public void setOrdered(boolean arg0);
  public void setEndTime(java.time.Instant arg0);
  public void startAsync();
}
