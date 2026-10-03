/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import jdk.jfr.internal.PrivateAccess;

final class Streams {
  private Streams() {
  }

  abstract static class Base implements EventStream {
    private static final class Named {
      final String name;
      final Consumer<RecordedEvent> handler;

      Named(String name, Consumer<RecordedEvent> handler) {
        this.name = name;
        this.handler = handler;
      }
    }

    private final CopyOnWriteArrayList<Named> events = new CopyOnWriteArrayList<Named>();
    private final CopyOnWriteArrayList<Runnable> flush = new CopyOnWriteArrayList<Runnable>();
    private final CopyOnWriteArrayList<Runnable> close = new CopyOnWriteArrayList<Runnable>();
    private final CopyOnWriteArrayList<Consumer<Throwable>> errors = new CopyOnWriteArrayList<Consumer<Throwable>>();
    private final CopyOnWriteArrayList<Consumer<MetadataEvent>> metadata =
        new CopyOnWriteArrayList<Consumer<MetadataEvent>>();
    private volatile boolean closed;
    private volatile boolean started;
    private volatile boolean ordered = true;
    private volatile Instant startTime;
    private volatile Instant endTime;
    private Thread thread;
    private boolean metadataSent;

    abstract List<RecordedEvent> poll() throws IOException;

    abstract boolean finished();

    boolean live() {
      return false;
    }

    public void onMetadata(Consumer<MetadataEvent> handler) {
      java.util.Objects.requireNonNull(handler);
      metadata.add(handler);
    }

    public void setReuse(boolean reuse) {
    }

    public void setOrdered(boolean ordered) {
      this.ordered = ordered;
    }

    public void setStartTime(Instant startTime) {
      java.util.Objects.requireNonNull(startTime);
      if (started) {
        throw new IllegalStateException("Stream is already started");
      }
      this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
      java.util.Objects.requireNonNull(endTime);
      if (started) {
        throw new IllegalStateException("Stream is already started");
      }
      this.endTime = endTime;
    }

    public void onEvent(String eventName, Consumer<RecordedEvent> handler) {
      java.util.Objects.requireNonNull(eventName);
      java.util.Objects.requireNonNull(handler);
      events.add(new Named(eventName, handler));
    }

    public void onEvent(Consumer<RecordedEvent> handler) {
      java.util.Objects.requireNonNull(handler);
      events.add(new Named(null, handler));
    }

    public void onFlush(Runnable handler) {
      java.util.Objects.requireNonNull(handler);
      flush.add(handler);
    }

    public void onClose(Runnable handler) {
      java.util.Objects.requireNonNull(handler);
      close.add(handler);
    }

    public void onError(Consumer<Throwable> handler) {
      java.util.Objects.requireNonNull(handler);
      errors.add(handler);
    }

    public boolean remove(Object handler) {
      java.util.Objects.requireNonNull(handler);
      for (Named n : events) {
        if (n.handler == handler) {
          return events.remove(n);
        }
      }
      return flush.remove(handler) || close.remove(handler) || errors.remove(handler) || metadata.remove(handler);
    }

    public void close() {
      boolean first;
      synchronized (this) {
        first = !closed;
        closed = true;
      }
      if (first) {
        shutdown();
        if (thread != null && Thread.currentThread() != thread) {
          thread.interrupt();
        }
        if (!started) {
          fireClose();
        }
      }
    }

    void shutdown() {
    }

    private void fireClose() {
      for (Runnable r : close) {
        try {
          r.run();
        } catch (Throwable t) {
        }
      }
    }

    public void start() {
      begin();
      run();
    }

    public void startAsync() {
      begin();
      Thread t = new Thread(new Runnable() {
        public void run() {
          Base.this.run();
        }
      }, "JFR Event Stream");
      t.setDaemon(true);
      thread = t;
      t.start();
    }

    void begin() {
      synchronized (this) {
        if (closed) {
          throw new IllegalStateException("Stream is closed");
        }
        if (started) {
          throw new IllegalStateException("Event stream can only be started once");
        }
        started = true;
      }
      beforeStart();
    }

    void beforeStart() {
    }

    private void run() {
      try {
        while (!closed) {
          List<RecordedEvent> batch = poll();
          if (batch.isEmpty() && finished()) {
            break;
          }
          dispatch(batch);
          for (Runnable r : flush) {
            r.run();
          }
          if (batch.isEmpty() && live()) {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              break;
            }
          }
        }
      } catch (Throwable t) {
        for (Consumer<Throwable> e : errors) {
          e.accept(t);
        }
      } finally {
        closed = true;
        shutdown();
        fireClose();
      }
    }

    private void dispatch(List<RecordedEvent> batch) {
      if (!metadataSent && !metadata.isEmpty() && !batch.isEmpty()) {
        metadataSent = true;
        List<jdk.jfr.EventType> types = FlightRecorder.getFlightRecorder().getEventTypes();
        MetadataEvent m = new MetadataEvent(types, types, new ArrayList<jdk.jfr.EventType>(),
            jdk.jfr.Configuration.getConfigurations());
        for (Consumer<MetadataEvent> c : metadata) {
          c.accept(m);
        }
      }
      List<RecordedEvent> l = batch;
      if (ordered) {
        l = new ArrayList<RecordedEvent>(batch);
        Collections.sort(l, new Comparator<RecordedEvent>() {
          public int compare(RecordedEvent a, RecordedEvent b) {
            return a.getEndTime().compareTo(b.getEndTime());
          }
        });
      }
      for (RecordedEvent e : l) {
        if (closed) {
          return;
        }
        Instant s = e.getStartTime();
        if (startTime != null && e.getEndTime().isBefore(startTime)) {
          continue;
        }
        if (endTime != null && s.isAfter(endTime)) {
          continue;
        }
        String name = e.getEventType().getName();
        for (Named n : events) {
          if (n.name == null || n.name.equals(name)) {
            n.handler.accept(e);
          }
        }
      }
    }

    public void awaitTermination(Duration timeout) throws InterruptedException {
      java.util.Objects.requireNonNull(timeout);
      if (timeout.isNegative()) {
        throw new IllegalArgumentException("timeout is negative");
      }
      Thread t = thread;
      if (t != null) {
        t.join(Math.max(1, timeout.toMillis()));
      }
    }

    public void awaitTermination() throws InterruptedException {
      Thread t = thread;
      if (t != null) {
        t.join();
      }
    }
  }

  static final class FileStream extends Base {
    private final Path file;
    private boolean done;

    FileStream(Path file) throws IOException {
      if (!Files.exists(file)) {
        throw new java.nio.file.NoSuchFileException(file.toString());
      }
      this.file = file;
    }

    List<RecordedEvent> poll() throws IOException {
      if (done) {
        return new ArrayList<RecordedEvent>();
      }
      done = true;
      return RecordingFile.readAllEvents(file);
    }

    boolean finished() {
      return done;
    }
  }

  static final class RepositoryStream extends Base {
    private final Path dir;
    private final java.util.Map<Recording, int[]> positions = new java.util.IdentityHashMap<Recording, int[]>();
    private final Set<String> seen = new HashSet<String>();
    private boolean done;

    RepositoryStream(Path dir) {
      this.dir = dir;
    }

    boolean live() {
      return dir == null;
    }

    List<RecordedEvent> poll() throws IOException {
      List<RecordedEvent> out = new ArrayList<RecordedEvent>();
      if (dir != null) {
        if (done) {
          return out;
        }
        done = true;
        List<Path> files = new ArrayList<Path>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.jfr")) {
          for (Path p : ds) {
            files.add(p);
          }
        }
        Collections.sort(files);
        for (Path p : files) {
          out.addAll(RecordingFile.readAllEvents(p));
        }
        return out;
      }
      for (Recording r : FlightRecorder.getFlightRecorder().getRecordings()) {
        int[] pos = positions.get(r);
        if (pos == null) {
          pos = new int[1];
          positions.put(r, pos);
        }
        int[] next = new int[1];
        byte[] data = PrivateAccess.getInstance().chunkFrom(r, pos[0], next);
        pos[0] = next[0];
        if (data != null) {
          for (Chunk c : Chunk.parse(data)) {
            for (RecordedEvent e : c.events) {
              String key = e.getEventType().getName() + "@" + e.getStartTime() + "+" + e.getDuration();
              if (seen.add(key)) {
                out.add(e);
              }
            }
          }
        }
      }
      return out;
    }

    boolean finished() {
      return false;
    }
  }

  static final class RecordingSource extends Base {
    private final Recording recording;
    private int position;
    private boolean drained;

    RecordingSource(Recording recording) {
      this.recording = recording;
    }

    boolean live() {
      return true;
    }

    List<RecordedEvent> poll() throws IOException {
      List<RecordedEvent> out = new ArrayList<RecordedEvent>();
      RecordingState s = recording.getState();
      int[] next = new int[1];
      byte[] data = PrivateAccess.getInstance().chunkFrom(recording, position, next);
      position = next[0];
      if (data != null) {
        for (Chunk c : Chunk.parse(data)) {
          out.addAll(c.events);
        }
      }
      if (out.isEmpty() && s != RecordingState.RUNNING && s != RecordingState.NEW && s != RecordingState.DELAYED) {
        drained = true;
      }
      return out;
    }

    boolean finished() {
      return drained;
    }

    void beforeStart() {
      if (recording.getState() == RecordingState.NEW) {
        recording.start();
      }
    }

    void shutdown() {
      if (recording.getState() == RecordingState.RUNNING) {
        try {
          recording.stop();
        } catch (IllegalStateException e) {
        }
      }
    }
  }
}
