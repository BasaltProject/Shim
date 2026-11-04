/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

public interface EventStream extends AutoCloseable {
  public static EventStream openRepository() throws IOException {
    return new Streams.RepositoryStream(null);
  }

  public static EventStream openRepository(Path directory) throws IOException {
    java.util.Objects.requireNonNull(directory);
    if (!java.nio.file.Files.isDirectory(directory)) {
      throw new java.nio.file.NoSuchFileException(directory.toString());
    }
    return new Streams.RepositoryStream(directory);
  }

  public static EventStream openFile(Path file) throws IOException {
    java.util.Objects.requireNonNull(file);
    return new Streams.FileStream(file);
  }

  public default void onMetadata(Consumer<MetadataEvent> handler) {
    throw new UnsupportedOperationException("Metadata handlers are not supported by this stream");
  }

  public void setReuse(boolean reuse);

  public void setOrdered(boolean ordered);

  public void setStartTime(Instant startTime);

  public void setEndTime(Instant endTime);

  public void onEvent(String eventName, Consumer<RecordedEvent> handler);

  public void onEvent(Consumer<RecordedEvent> handler);

  public void onFlush(Runnable handler);

  public void onClose(Runnable handler);

  public void onError(Consumer<Throwable> handler);

  public boolean remove(Object handler);

  public void close();

  public void start();

  public void startAsync();

  public void awaitTermination(Duration timeout) throws InterruptedException;

  public void awaitTermination() throws InterruptedException;
}
