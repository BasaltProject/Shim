/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Recording implements Closeable {
  private static boolean exitHook;

  static final class Resolved {
    final boolean enabled;
    final boolean stack;
    final long thresholdNs;
    final long periodMs;
    final String period;

    Resolved(boolean enabled, boolean stack, long thresholdNs, long periodMs, String period) {
      this.enabled = enabled;
      this.stack = stack;
      this.thresholdNs = thresholdNs;
      this.periodMs = periodMs;
      this.period = period;
    }
  }

  private final long id;
  private String name;
  private volatile RecordingState state = RecordingState.NEW;
  private final Map<String, String> settings = new LinkedHashMap<String, String>();
  private final ConcurrentHashMap<EventInfo, Resolved> cache = new ConcurrentHashMap<EventInfo, Resolved>();
  private final List<EventRecord> events = new ArrayList<EventRecord>();
  private boolean toDisk = true;
  private Duration maxAge;
  private long maxSize;
  private boolean dumpOnExit;
  private Path destination;
  private Duration duration;
  private long startTicks;
  private long stopTicks;
  private Instant startTime;
  private Instant stopTime;
  private Thread timer;
  private long approxSize;

  public Recording(Map<String, String> settings) {
    this();
    java.util.Objects.requireNonNull(settings);
    this.settings.putAll(settings);
  }

  public Recording() {
    this.id = JfrRuntime.IDS.getAndIncrement();
    this.name = String.valueOf(id);
    JfrRuntime.initialized = true;
    JfrRuntime.ALL.add(this);
  }

  public Recording(Configuration configuration) {
    this(configuration.getSettings());
  }

  Resolved resolve(EventInfo info) {
    Resolved r = cache.get(info);
    if (r == null) {
      String en = lookup(info, "enabled");
      String st = lookup(info, "stackTrace");
      String th = lookup(info, "threshold");
      String pe = lookup(info, "period");
      String period = pe != null ? pe : (info.defPeriod != null ? info.defPeriod : "everyChunk");
      long periodMs = -1;
      if (!period.equals("everyChunk") && !period.equals("beginChunk") && !period.equals("endChunk")) {
        try {
          periodMs = JfrRuntime.parseTimespanNs(period) / 1000000L;
        } catch (IllegalArgumentException e) {
          periodMs = -1;
        }
      }
      long thr = 0;
      try {
        thr = JfrRuntime.parseTimespanNs(th != null ? th : info.defThreshold);
      } catch (IllegalArgumentException e) {
        thr = 0;
      }
      r = new Resolved(en != null ? Boolean.parseBoolean(en) : info.defEnabled,
          st != null ? Boolean.parseBoolean(st) : info.defStack, thr, periodMs, period);
      cache.put(info, r);
    }
    return r;
  }

  private String lookup(EventInfo info, String key) {
    synchronized (settings) {
      String v = settings.get(info.name + "#" + key);
      if (v == null) {
        v = settings.get(info.type.getId() + "#" + key);
      }
      return v;
    }
  }

  void putSetting(String key, String value) {
    synchronized (settings) {
      settings.put(key, value);
    }
    cache.clear();
  }

  void add(EventRecord r) {
    synchronized (events) {
      events.add(r);
      approxSize += 40 + 8L * r.values.length;
      if (maxSize > 0) {
        while (approxSize > maxSize && !events.isEmpty()) {
          EventRecord o = events.remove(0);
          approxSize -= 40 + 8L * o.values.length;
        }
      }
      if (maxAge != null) {
        long limit = System.nanoTime() - maxAge.toNanos();
        while (!events.isEmpty() && events.get(0).start < limit) {
          events.remove(0);
        }
      }
    }
  }

  void loadSnapshot(List<EventRecord> evs, long start) {
    synchronized (events) {
      events.addAll(evs);
    }
    startTicks = start;
    stopTicks = System.nanoTime();
    startTime = Instant.now();
    stopTime = startTime;
    state = RecordingState.STOPPED;
    name = "Snapshot";
  }

  byte[] chunkFrom(int from, int[] next) {
    List<EventRecord> all = snapshot();
    next[0] = all.size();
    if (state == RecordingState.NEW || state == RecordingState.DELAYED) {
      return null;
    }
    List<EventRecord> part = from >= all.size() ? new ArrayList<EventRecord>() : all.subList(from, all.size());
    long end = state == RecordingState.RUNNING ? System.nanoTime() : stopTicks;
    return new ChunkWriter().write(part, startTicks, end);
  }

  List<EventRecord> snapshot() {
    synchronized (events) {
      return new ArrayList<EventRecord>(events);
    }
  }

  public String getName() {
    return name;
  }

  public Recording copy(boolean stop) {
    Recording r = new Recording(getSettings());
    r.setName(name);
    r.toDisk = toDisk;
    r.maxAge = maxAge;
    r.maxSize = maxSize;
    r.dumpOnExit = dumpOnExit;
    r.destination = destination;
    r.duration = duration;
    r.start();
    if (stop) {
      r.stop();
    }
    return r;
  }

  public void start() {
    synchronized (this) {
      if (state != RecordingState.NEW) {
        throw new IllegalStateException("Recording can only be started once.");
      }
      if (startDelayed()) {
        return;
      }
      doStart();
    }
  }

  private boolean startDelayed() {
    return false;
  }

  private void doStart() {
    startTicks = System.nanoTime();
    startTime = Instant.now();
    state = RecordingState.RUNNING;
    JfrRuntime.refreshRunning();
    JfrRuntime.runChunkHooks(this, true);
    JfrRuntime.stateChanged(this);
    if (duration != null) {
      scheduleStop(duration);
    }
    installExitHook();
  }

  private void scheduleStop(final Duration d) {
    Thread t = new Thread("JFR Recording Duration") {
      public void run() {
        try {
          Thread.sleep(d.toMillis());
        } catch (InterruptedException e) {
          return;
        }
        if (state == RecordingState.RUNNING) {
          try {
            Recording.this.stop();
          } catch (IllegalStateException e) {
          }
        }
      }
    };
    t.setDaemon(true);
    timer = t;
    t.start();
  }

  private static synchronized void installExitHook() {
    if (exitHook) {
      return;
    }
    exitHook = true;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        for (Recording r : JfrRuntime.ALL) {
          if (r.dumpOnExit && r.state == RecordingState.RUNNING) {
            try {
              r.stop();
              if (r.destination == null) {
                r.dump(Path.of("hotspot-pid-" + ProcessHandle.current().pid() + "-id-" + r.id + ".jfr"));
              }
            } catch (Exception e) {
            }
          }
        }
      }
    });
  }

  public boolean stop() {
    synchronized (this) {
      if (state == RecordingState.NEW) {
        throw new IllegalStateException("Recording must be started before it can be stopped.");
      }
      if (state != RecordingState.RUNNING) {
        throw new IllegalStateException("Can't stop an already stopped recording.");
      }
      JfrRuntime.runChunkHooks(this, false);
      stopTicks = System.nanoTime();
      stopTime = Instant.now();
      state = RecordingState.STOPPED;
      JfrRuntime.refreshRunning();
      if (timer != null) {
        timer.interrupt();
      }
      if (destination != null) {
        try {
          dump(destination);
        } catch (IOException e) {
          throw new IllegalStateException("Could not write recording to destination: " + e.getMessage(), e);
        }
      }
      JfrRuntime.stateChanged(this);
      return true;
    }
  }

  public void setName(String name) {
    java.util.Objects.requireNonNull(name, "name");
    ensureNotClosed();
    this.name = name;
  }

  private void ensureNotClosed() {
    if (state == RecordingState.CLOSED) {
      throw new IllegalStateException("Cannot access a closed recording");
    }
  }

  public long getId() {
    return id;
  }

  public RecordingState getState() {
    return state;
  }

  public void close() {
    synchronized (this) {
      if (state == RecordingState.CLOSED) {
        return;
      }
      if (state == RecordingState.RUNNING) {
        try {
          stop();
        } catch (IllegalStateException e) {
        }
      }
      state = RecordingState.CLOSED;
      JfrRuntime.ALL.remove(this);
      JfrRuntime.refreshRunning();
      synchronized (events) {
        events.clear();
      }
      JfrRuntime.stateChanged(this);
    }
  }

  public void dump(Path destination) throws IOException {
    java.util.Objects.requireNonNull(destination);
    byte[] data = chunk(null, null);
    if (data == null) {
      throw new IOException("Recording \"" + name + "\" (id=" + id + ") has not started, no content to write");
    }
    Files.write(destination, data);
  }

  private byte[] chunk(Instant from, Instant to) {
    if (state == RecordingState.NEW || state == RecordingState.DELAYED) {
      return null;
    }
    long end = state == RecordingState.RUNNING ? System.nanoTime() : stopTicks;
    List<EventRecord> rs = snapshot();
    if (from != null || to != null) {
      List<EventRecord> f = new ArrayList<EventRecord>();
      for (EventRecord r : rs) {
        long ns = JfrRuntime.epochOf(r.start);
        if (from != null && ns < instantNanos(from)) {
          continue;
        }
        if (to != null && ns > instantNanos(to)) {
          continue;
        }
        f.add(r);
      }
      rs = f;
    }
    return new ChunkWriter().write(rs, startTicks, end);
  }

  private static long instantNanos(Instant i) {
    return i.getEpochSecond() * 1000000000L + i.getNano();
  }

  public long getSize() {
    synchronized (events) {
      return approxSize;
    }
  }

  public EventSettings enable(String name) {
    java.util.Objects.requireNonNull(name);
    ensureNotClosed();
    putSetting(name + "#enabled", "true");
    return new RecordingSettings(this, name);
  }

  public EventSettings enable(Class<? extends Event> eventClass) {
    java.util.Objects.requireNonNull(eventClass);
    return enable(String.valueOf(JfrRuntime.infoFor(eventClass, true).type.getId()));
  }

  public EventSettings disable(String name) {
    java.util.Objects.requireNonNull(name);
    ensureNotClosed();
    putSetting(name + "#enabled", "false");
    return new RecordingSettings(this, name);
  }

  public EventSettings disable(Class<? extends Event> eventClass) {
    java.util.Objects.requireNonNull(eventClass);
    return disable(String.valueOf(JfrRuntime.infoFor(eventClass, true).type.getId()));
  }

  public Duration getDuration() {
    return duration;
  }

  public Map<String, String> getSettings() {
    synchronized (settings) {
      return new LinkedHashMap<String, String>(settings);
    }
  }

  public void setSettings(Map<String, String> settings) {
    java.util.Objects.requireNonNull(settings);
    ensureNotClosed();
    synchronized (this.settings) {
      this.settings.clear();
      this.settings.putAll(settings);
    }
    cache.clear();
  }

  public boolean isToDisk() {
    return toDisk;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getStopTime() {
    return stopTime;
  }

  public Path getDestination() {
    return destination;
  }

  public Duration getMaxAge() {
    return maxAge;
  }

  public long getMaxSize() {
    return maxSize;
  }

  public void setMaxAge(Duration maxAge) {
    if (maxAge != null && maxAge.isNegative()) {
      throw new IllegalArgumentException("Max age of recording can't be negative");
    }
    ensureNotClosed();
    this.maxAge = maxAge;
  }

  public void setMaxSize(long maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size of recording can't be negative");
    }
    ensureNotClosed();
    this.maxSize = maxSize;
  }

  public boolean getDumpOnExit() {
    return dumpOnExit;
  }

  public void setDumpOnExit(boolean dumpOnExit) {
    ensureNotClosed();
    this.dumpOnExit = dumpOnExit;
  }

  public void setToDisk(boolean disk) {
    ensureNotClosed();
    this.toDisk = disk;
  }

  public void scheduleStart(final Duration delay) {
    java.util.Objects.requireNonNull(delay);
    if (delay.isNegative()) {
      throw new IllegalArgumentException("Delay can't be negative");
    }
    synchronized (this) {
      if (state != RecordingState.NEW) {
        throw new IllegalStateException("Only a new recording can be scheduled for start");
      }
      state = RecordingState.DELAYED;
    }
    JfrRuntime.stateChanged(this);
    Thread t = new Thread("JFR Recording Delay") {
      public void run() {
        try {
          Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
          return;
        }
        synchronized (Recording.this) {
          if (state == RecordingState.DELAYED) {
            doStart();
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  public void setDestination(Path destination) throws IOException {
    ensureNotClosed();
    if (state == RecordingState.STOPPED) {
      throw new IllegalStateException("Destination can't be set on a recording that has been stopped");
    }
    if (destination != null) {
      Path abs = destination.toAbsolutePath();
      Path parent = abs.getParent();
      if (parent != null && !Files.isDirectory(parent)) {
        throw new java.nio.file.NoSuchFileException(parent.toString());
      }
      if (Files.isDirectory(abs)) {
        throw new IOException("Destination is a directory: " + abs);
      }
    }
    this.destination = destination;
  }

  public void setDuration(Duration duration) {
    ensureNotClosed();
    if (state == RecordingState.STOPPED) {
      throw new IllegalStateException("Duration can't be set after a recording has been stopped");
    }
    this.duration = duration;
    if (state == RecordingState.RUNNING && duration != null) {
      scheduleStop(duration);
    }
  }

  public InputStream getStream(Instant starttime, Instant endtime) throws IOException {
    if (starttime != null && endtime != null && starttime.isAfter(endtime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    if (state == RecordingState.NEW || state == RecordingState.DELAYED) {
      throw new IOException("Recording must be stopped before it can be read.");
    }
    byte[] d = chunk(starttime, endtime);
    if (d == null || (snapshot().isEmpty())) {
      return null;
    }
    return new ByteArrayInputStream(d);
  }
}
