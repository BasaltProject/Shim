/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

final class JfrRuntime {
  static final ConcurrentHashMap<Class<?>, EventInfo> BY_CLASS = new ConcurrentHashMap<Class<?>, EventInfo>();
  static final ConcurrentHashMap<String, EventInfo> REGISTERED = new ConcurrentHashMap<String, EventInfo>();
  static final CopyOnWriteArrayList<Recording> ALL = new CopyOnWriteArrayList<Recording>();
  static final CopyOnWriteArrayList<FlightRecorderListener> LISTENERS =
      new CopyOnWriteArrayList<FlightRecorderListener>();
  static final CopyOnWriteArrayList<Hook> HOOKS = new CopyOnWriteArrayList<Hook>();
  static final AtomicLong IDS = new AtomicLong(1);
  static final long BASE_NANOS = System.nanoTime();
  static final long BASE_EPOCH_NANOS = epochNanos();
  static final int MAX_DEPTH = 64;
  static volatile Recording[] running = new Recording[0];
  static volatile boolean initialized;
  private static Thread periodic;

  static {
    BuiltinEvents.init();
  }

  private JfrRuntime() {
  }

  static final class Hook {
    final EventInfo info;
    final Runnable runnable;
    long last;

    Hook(EventInfo info, Runnable runnable) {
      this.info = info;
      this.runnable = runnable;
    }
  }

  static long epochNanos() {
    java.time.Instant i = java.time.Instant.now();
    return i.getEpochSecond() * 1000000000L + i.getNano();
  }

  static long epochOf(long ticks) {
    return BASE_EPOCH_NANOS + (ticks - BASE_NANOS);
  }

  static EventInfo infoFor(Class<? extends Event> cls, boolean register) {
    EventInfo info = BY_CLASS.get(cls);
    if (info == null) {
      if (!Event.class.isAssignableFrom(cls)) {
        throw new IllegalArgumentException("Class " + cls.getName() + " is not an event class");
      }
      EventInfo built = EventInfo.of(cls);
      info = BY_CLASS.putIfAbsent(cls, built);
      if (info == null) {
        info = built;
        if (built.registeredByDefault) {
          registerQuiet(built);
        }
      }
    }
    if (register && !info.registered) {
      register(info);
    }
    return info;
  }

  static EventInfo infoOf(Event e) {
    if (e instanceof DynamicEvent) {
      return ((DynamicEvent) e).info;
    }
    @SuppressWarnings("unchecked")
    Class<? extends Event> c = (Class<? extends Event>) e.getClass();
    return infoFor(c, false);
  }

  private static void registerQuiet(EventInfo info) {
    info.registered = true;
    REGISTERED.put(info.name, info);
  }

  static void register(EventInfo info) {
    registerQuiet(info);
  }

  static void unregister(EventInfo info) {
    info.registered = false;
    REGISTERED.remove(info.name, info);
  }

  static List<EventType> registeredTypes() {
    List<EventInfo> l = new ArrayList<EventInfo>(REGISTERED.values());
    java.util.Collections.sort(l, new java.util.Comparator<EventInfo>() {
      public int compare(EventInfo a, EventInfo b) {
        return a.name.compareTo(b.name);
      }
    });
    List<EventType> t = new ArrayList<EventType>();
    for (EventInfo i : l) {
      t.add(i.type);
    }
    return t;
  }

  static boolean isEnabledAnywhere(EventType type) {
    EventInfo info = REGISTERED.get(type.getName());
    if (info == null) {
      return false;
    }
    for (Recording r : running) {
      if (r.resolve(info).enabled) {
        return true;
      }
    }
    return false;
  }

  static boolean isEnabled(Event e) {
    Recording[] rs = running;
    if (rs.length == 0) {
      return false;
    }
    EventInfo info = infoOf(e);
    if (!info.registered) {
      return false;
    }
    for (Recording r : rs) {
      if (r.resolve(info).enabled) {
        return true;
      }
    }
    return false;
  }

  static boolean shouldCommit(Event e) {
    Recording[] rs = running;
    if (rs.length == 0) {
      return false;
    }
    EventInfo info = infoOf(e);
    if (!info.registered) {
      return false;
    }
    long dur = durationOf(e);
    for (Recording r : rs) {
      Recording.Resolved s = r.resolve(info);
      if (s.enabled && dur >= s.thresholdNs) {
        return true;
      }
    }
    return false;
  }

  private static long durationOf(Event e) {
    if (e.jfrStart == 0) {
      return 0;
    }
    long end = e.jfrEnd != 0 ? e.jfrEnd : System.nanoTime();
    return Math.max(0, end - e.jfrStart);
  }

  static void commit(Event e) {
    Recording[] rs = running;
    if (rs.length == 0) {
      return;
    }
    EventInfo info = infoOf(e);
    if (!info.registered) {
      return;
    }
    long end = e.jfrEnd != 0 ? e.jfrEnd : System.nanoTime();
    long start = e.jfrStart != 0 ? e.jfrStart : end;
    long dur = Math.max(0, end - start);
    Object[] values = null;
    StackTraceElement[] stack = null;
    ThreadData td = null;
    for (Recording r : rs) {
      Recording.Resolved s = r.resolve(info);
      if (!s.enabled || dur < s.thresholdNs) {
        continue;
      }
      if (values == null) {
        values = info.extract(e);
        td = ThreadData.of(Thread.currentThread());
      }
      StackTraceElement[] st = null;
      if (s.stack) {
        if (stack == null) {
          stack = capture();
        }
        st = stack;
      }
      r.add(new EventRecord(info, start, dur, td, st, values));
    }
  }

  static StackTraceElement[] capture() {
    StackTraceElement[] all = Thread.currentThread().getStackTrace();
    int i = 0;
    while (i < all.length) {
      String c = all[i].getClassName();
      if (c.equals("java.lang.Thread") || c.equals("jdk.jfr.Event") || c.equals("jdk.jfr.JfrRuntime")
          || c.equals("jdk.jfr.BuiltinEvents") || c.startsWith("jdk.jfr.BuiltinEvents$")) {
        i++;
      } else {
        break;
      }
    }
    int n = Math.min(all.length - i, MAX_DEPTH);
    StackTraceElement[] r = new StackTraceElement[n];
    System.arraycopy(all, i, r, 0, n);
    return r;
  }

  static void refreshRunning() {
    List<Recording> l = new ArrayList<Recording>();
    for (Recording r : ALL) {
      if (r.getState() == RecordingState.RUNNING) {
        l.add(r);
      }
    }
    running = l.toArray(new Recording[0]);
    if (!l.isEmpty() && !HOOKS.isEmpty()) {
      startPeriodic();
    }
  }

  static void stateChanged(Recording r) {
    for (FlightRecorderListener l : LISTENERS) {
      try {
        l.recordingStateChanged(r);
      } catch (Throwable t) {
      }
    }
  }

  static void addHook(EventInfo info, Runnable r) {
    HOOKS.add(new Hook(info, r));
    register(info);
    if (running.length > 0) {
      startPeriodic();
    }
  }

  static boolean removeHook(Runnable r) {
    for (Hook h : HOOKS) {
      if (h.runnable == r) {
        return HOOKS.remove(h);
      }
    }
    return false;
  }

  static void runChunkHooks(Recording rec, boolean begin) {
    for (Hook h : HOOKS) {
      Recording.Resolved s = rec.resolve(h.info);
      if (!s.enabled) {
        continue;
      }
      String p = s.period;
      if (p.equals("everyChunk") || p.equals(begin ? "beginChunk" : "endChunk")) {
        runHook(h);
      }
    }
  }

  private static void runHook(Hook h) {
    try {
      h.runnable.run();
    } catch (Throwable t) {
    }
  }

  private static synchronized void startPeriodic() {
    if (periodic != null) {
      return;
    }
    Thread t = new Thread("JFR Periodic Tasks") {
      public void run() {
        while (true) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            return;
          }
          Recording[] rs = running;
          if (rs.length == 0) {
            continue;
          }
          long now = System.nanoTime();
          for (Hook h : HOOKS) {
            long best = Long.MAX_VALUE;
            for (Recording r : rs) {
              Recording.Resolved s = r.resolve(h.info);
              if (s.enabled && s.periodMs > 0) {
                best = Math.min(best, s.periodMs);
              }
            }
            if (best != Long.MAX_VALUE && now - h.last >= best * 1000000L) {
              h.last = now;
              runHook(h);
            }
          }
        }
      }
    };
    t.setDaemon(true);
    periodic = t;
    t.start();
  }

  static void writeRows(java.nio.file.Path dest, List<jdk.jfr.internal.PrivateAccess.Row> rows)
      throws java.io.IOException {
    java.util.Map<String, EventInfo> infos = new java.util.HashMap<String, EventInfo>();
    List<EventRecord> recs = new ArrayList<EventRecord>();
    long minTicks = Long.MAX_VALUE;
    long maxTicks = Long.MIN_VALUE;
    for (jdk.jfr.internal.PrivateAccess.Row row : rows) {
      EventType t = row.type;
      EventInfo info = infos.get(t.getName());
      if (info == null) {
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>(t.getAnnotationElements());
        if (t.find(Name.class.getName()) == null) {
          anns.add(new AnnotationElement(Name.class, t.getName()));
        }
        List<ValueDescriptor> fs = new ArrayList<ValueDescriptor>();
        int idx = 0;
        for (ValueDescriptor v : t.getFields()) {
          String n = v.getName();
          if (idx++ < 4 && (n.equals("startTime") || n.equals("duration") || n.equals("eventThread")
              || n.equals("stackTrace"))) {
            continue;
          }
          if (EventInfo.kindOf(v.getTypeName()) >= 0 && !v.isArray() && !v.isConstantPool()
              || v.getTypeName().equals("java.lang.Class")) {
            fs.add(v);
          }
        }
        info = EventInfo.dynamic(anns, fs);
        infos.put(t.getName(), info);
      }
      Object[] vals = new Object[info.declared.size()];
      for (int i = 0; i < vals.length; i++) {
        vals[i] = row.values.get(info.declared.get(i).getName());
      }
      long start = BASE_NANOS + (row.startEpochNanos - BASE_EPOCH_NANOS);
      ThreadData td = row.thread == null ? null : ThreadData.create(row.thread, row.threadId, false);
      recs.add(new EventRecord(info, start, row.durationNanos, td, row.stack, vals));
      minTicks = Math.min(minTicks, start);
      maxTicks = Math.max(maxTicks, start + row.durationNanos);
    }
    if (recs.isEmpty()) {
      minTicks = System.nanoTime();
      maxTicks = minTicks;
    }
    java.nio.file.Files.write(dest, new ChunkWriter().write(recs, minTicks, maxTicks));
  }

  static String formatTimespan(Duration d) {
    long ns = d.toNanos();
    return ns + " ns";
  }

  static long parseTimespanNs(String s) {
    String v = s.trim();
    if (v.equals("0")) {
      return 0;
    }
    int i = 0;
    while (i < v.length() && Character.isDigit(v.charAt(i))) {
      i++;
    }
    if (i == 0 || i == v.length()) {
      throw new IllegalArgumentException("Not a valid timespan: " + s);
    }
    long n = Long.parseLong(v.substring(0, i));
    String u = v.substring(i).trim();
    switch (u) {
      case "ns":
        return n;
      case "us":
        return n * 1000L;
      case "ms":
        return n * 1000000L;
      case "s":
        return n * 1000000000L;
      case "m":
        return n * 60000000000L;
      case "h":
        return n * 3600000000000L;
      case "d":
        return n * 86400000000000L;
      default:
        throw new IllegalArgumentException("Not a valid timespan: " + s);
    }
  }
}
