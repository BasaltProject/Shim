/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.management.MBeanServerConnection;
import jdk.jfr.EventSettings;
import jdk.jfr.Recording;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.MetadataEvent;
import jdk.jfr.consumer.RecordedEvent;

public final class RemoteRecordingStream implements EventStream {
  private final FlightRecorderMXBean remote;
  private final long id;
  private final EventStream stream;
  private final Recording settings = new Recording();
  private boolean started;

  public RemoteRecordingStream(MBeanServerConnection connection) throws IOException {
    this(connection, null);
  }

  public RemoteRecordingStream(MBeanServerConnection connection, Path directory) throws IOException {
    java.util.Objects.requireNonNull(connection);
    if (directory != null && !java.nio.file.Files.isDirectory(directory)) {
      throw new IOException("Not a directory: " + directory);
    }
    this.remote = ManagementFactory.getPlatformMXBean(connection, FlightRecorderMXBean.class);
    this.id = remote.newRecording();
    this.stream = directory == null ? EventStream.openRepository() : EventStream.openRepository(directory);
  }

  private void begin() {
    if (!started) {
      started = true;
      remote.setRecordingSettings(id, settings.getSettings());
      settings.close();
      remote.startRecording(id);
    }
  }

  public void setSettings(Map<String, String> s) {
    settings.setSettings(s);
  }

  public EventSettings enable(String name) {
    return settings.enable(name);
  }

  public EventSettings disable(String name) {
    return settings.disable(name);
  }

  public void setMaxAge(Duration maxAge) {
    Map<String, String> o = new HashMap<String, String>();
    o.put("maxAge", maxAge.toNanos() + " ns");
    remote.setRecordingOptions(id, o);
  }

  public void setMaxSize(long maxSize) {
    Map<String, String> o = new HashMap<String, String>();
    o.put("maxSize", String.valueOf(maxSize));
    remote.setRecordingOptions(id, o);
  }

  public void onMetadata(Consumer<MetadataEvent> handler) {
    stream.onMetadata(handler);
  }

  public void setReuse(boolean reuse) {
    stream.setReuse(reuse);
  }

  public void setOrdered(boolean ordered) {
    stream.setOrdered(ordered);
  }

  public void setStartTime(Instant startTime) {
    stream.setStartTime(startTime);
  }

  public void setEndTime(Instant endTime) {
    stream.setEndTime(endTime);
  }

  public void onEvent(String eventName, Consumer<RecordedEvent> handler) {
    stream.onEvent(eventName, handler);
  }

  public void onEvent(Consumer<RecordedEvent> handler) {
    stream.onEvent(handler);
  }

  public void onFlush(Runnable handler) {
    stream.onFlush(handler);
  }

  public void onClose(Runnable handler) {
    stream.onClose(handler);
  }

  public void onError(Consumer<Throwable> handler) {
    stream.onError(handler);
  }

  public boolean remove(Object handler) {
    return stream.remove(handler);
  }

  public void close() {
    stream.close();
    try {
      remote.closeRecording(id);
    } catch (IOException | IllegalArgumentException e) {
    }
    settings.close();
  }

  public void start() {
    begin();
    stream.start();
  }

  public void startAsync() {
    begin();
    stream.startAsync();
  }

  public boolean stop() {
    try {
      return remote.stopRecording(id);
    } catch (IllegalStateException e) {
      return false;
    }
  }

  public void dump(Path destination) throws IOException {
    remote.copyTo(id, destination.toString());
  }

  public void awaitTermination(Duration timeout) throws InterruptedException {
    stream.awaitTermination(timeout);
  }

  public void awaitTermination() throws InterruptedException {
    stream.awaitTermination();
  }
}
