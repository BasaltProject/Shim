/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.util.ArrayList;
import java.util.List;

public final class FlightRecorder {
  private static final FlightRecorder INSTANCE = new FlightRecorder();
  private static boolean notified;

  private FlightRecorder() {
  }

  public static FlightRecorder getFlightRecorder() throws IllegalStateException {
    JfrRuntime.initialized = true;
    notifyInitialized();
    return INSTANCE;
  }

  private static synchronized void notifyInitialized() {
    if (notified) {
      return;
    }
    notified = true;
    for (FlightRecorderListener l : JfrRuntime.LISTENERS) {
      l.recorderInitialized(INSTANCE);
    }
  }

  public List<Recording> getRecordings() {
    return new ArrayList<Recording>(JfrRuntime.ALL);
  }

  public Recording takeSnapshot() {
    Recording r = new Recording();
    List<EventRecord> all = new ArrayList<EventRecord>();
    long start = Long.MAX_VALUE;
    java.util.IdentityHashMap<EventRecord, Boolean> seen = new java.util.IdentityHashMap<EventRecord, Boolean>();
    for (Recording o : JfrRuntime.ALL) {
      if (o == r) {
        continue;
      }
      for (EventRecord e : o.snapshot()) {
        if (seen.put(e, Boolean.TRUE) == null) {
          all.add(e);
          start = Math.min(start, e.start);
        }
      }
    }
    r.loadSnapshot(all, start == Long.MAX_VALUE ? System.nanoTime() : start);
    return r;
  }

  public static void addListener(FlightRecorderListener changeListener) {
    java.util.Objects.requireNonNull(changeListener);
    JfrRuntime.LISTENERS.add(changeListener);
    if (notified) {
      changeListener.recorderInitialized(INSTANCE);
    }
  }

  public static boolean removeListener(FlightRecorderListener changeListener) {
    return JfrRuntime.LISTENERS.remove(changeListener);
  }

  public static void register(Class<? extends Event> eventClass) {
    java.util.Objects.requireNonNull(eventClass);
    JfrRuntime.infoFor(eventClass, true);
  }

  public static void unregister(Class<? extends Event> eventClass) {
    java.util.Objects.requireNonNull(eventClass);
    JfrRuntime.unregister(JfrRuntime.infoFor(eventClass, false));
  }

  public static boolean isAvailable() {
    return true;
  }

  public static boolean isInitialized() {
    return JfrRuntime.initialized;
  }

  public static void addPeriodicEvent(Class<? extends Event> eventClass, Runnable hook) {
    java.util.Objects.requireNonNull(eventClass);
    java.util.Objects.requireNonNull(hook);
    JfrRuntime.addHook(JfrRuntime.infoFor(eventClass, true), hook);
  }

  public static boolean removePeriodicEvent(Runnable hook) {
    java.util.Objects.requireNonNull(hook);
    return JfrRuntime.removeHook(hook);
  }

  public List<EventType> getEventTypes() {
    return JfrRuntime.registeredTypes();
  }
}
