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
import java.util.Map;
import java.util.function.Consumer;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.EventSettings;
import jdk.jfr.Recording;

public final class RecordingStream implements AutoCloseable, EventStream {
  private final Recording recording;
  private final Streams.RecordingSource source;

  public RecordingStream() {
    this.recording = new Recording();
    this.source = new Streams.RecordingSource(recording);
  }

  public RecordingStream(Configuration configuration) {
    this.recording = new Recording(configuration);
    this.source = new Streams.RecordingSource(recording);
  }

  public void setSettings(Map<String, String> settings) {
    recording.setSettings(settings);
  }

  public EventSettings enable(String name) {
    return recording.enable(name);
  }

  public EventSettings disable(String name) {
    return recording.disable(name);
  }

  public EventSettings enable(Class<? extends Event> eventClass) {
    return recording.enable(eventClass);
  }

  public EventSettings disable(Class<? extends Event> eventClass) {
    return recording.disable(eventClass);
  }

  public void setMaxAge(Duration maxAge) {
    recording.setMaxAge(maxAge);
  }

  public void setMaxSize(long maxSize) {
    recording.setMaxSize(maxSize);
  }

  public void onMetadata(Consumer<MetadataEvent> handler) {
    source.onMetadata(handler);
  }

  public void setReuse(boolean reuse) {
    source.setReuse(reuse);
  }

  public void setOrdered(boolean ordered) {
    source.setOrdered(ordered);
  }

  public void setStartTime(Instant startTime) {
    source.setStartTime(startTime);
  }

  public void setEndTime(Instant endTime) {
    source.setEndTime(endTime);
  }

  public void onEvent(String eventName, Consumer<RecordedEvent> handler) {
    source.onEvent(eventName, handler);
  }

  public void onEvent(Consumer<RecordedEvent> handler) {
    source.onEvent(handler);
  }

  public void onFlush(Runnable handler) {
    source.onFlush(handler);
  }

  public void onClose(Runnable handler) {
    source.onClose(handler);
  }

  public void onError(Consumer<Throwable> handler) {
    source.onError(handler);
  }

  public boolean remove(Object handler) {
    return source.remove(handler);
  }

  public void close() {
    source.close();
    recording.close();
  }

  public void start() {
    source.start();
  }

  public void startAsync() {
    source.startAsync();
  }

  public void awaitTermination(Duration timeout) throws InterruptedException {
    source.awaitTermination(timeout);
  }

  public void awaitTermination() throws InterruptedException {
    source.awaitTermination();
  }

  public boolean stop() {
    try {
      return recording.stop();
    } catch (IllegalStateException e) {
      return false;
    }
  }

  public void dump(Path destination) throws IOException {
    recording.dump(destination);
  }
}
