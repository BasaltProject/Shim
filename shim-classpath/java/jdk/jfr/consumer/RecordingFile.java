/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import jdk.jfr.EventType;
import jdk.jfr.internal.PrivateAccess;

public final class RecordingFile implements Closeable {
  private final List<Chunk> chunks;
  private final List<RecordedEvent> events = new ArrayList<RecordedEvent>();
  private int next;
  private boolean closed;

  public RecordingFile(Path file) throws IOException {
    java.util.Objects.requireNonNull(file);
    this.chunks = Chunk.parse(Files.readAllBytes(file));
    for (Chunk c : chunks) {
      List<RecordedEvent> l = new ArrayList<RecordedEvent>(c.events);
      Collections.sort(l, new Comparator<RecordedEvent>() {
        public int compare(RecordedEvent a, RecordedEvent b) {
          return a.getEndTime().compareTo(b.getEndTime());
        }
      });
      events.addAll(l);
    }
  }

  public RecordedEvent readEvent() throws IOException {
    if (closed) {
      throw new IOException("Stream Closed");
    }
    if (next >= events.size()) {
      throw new EOFException("No more events");
    }
    return events.get(next++);
  }

  public boolean hasMoreEvents() {
    return !closed && next < events.size();
  }

  public List<EventType> readEventTypes() throws IOException {
    if (closed) {
      throw new IOException("Stream Closed");
    }
    Map<String, EventType> m = new LinkedHashMap<String, EventType>();
    for (Chunk c : chunks) {
      for (Chunk.TypeDef td : c.types.values()) {
        if ("jdk.jfr.Event".equals(td.superType) && !m.containsKey(td.name)) {
          m.put(td.name, c.eventType(td));
        }
      }
    }
    return new ArrayList<EventType>(m.values());
  }

  public static List<RecordedEvent> readAllEvents(Path path) throws IOException {
    RecordingFile f = new RecordingFile(path);
    try {
      List<RecordedEvent> l = new ArrayList<RecordedEvent>();
      while (f.hasMoreEvents()) {
        l.add(f.readEvent());
      }
      return l;
    } finally {
      f.close();
    }
  }

  public void write(Path destination, Predicate<RecordedEvent> filter) throws IOException {
    java.util.Objects.requireNonNull(destination);
    java.util.Objects.requireNonNull(filter);
    List<PrivateAccess.Row> rows = new ArrayList<PrivateAccess.Row>();
    for (RecordedEvent e : events) {
      if (!filter.test(e)) {
        continue;
      }
      PrivateAccess.Row r = new PrivateAccess.Row();
      r.type = e.getEventType();
      r.startEpochNanos = e.getStartTime().getEpochSecond() * 1000000000L + e.getStartTime().getNano();
      r.durationNanos = e.getDuration().toNanos();
      RecordedThread t = e.getThread();
      if (t != null) {
        r.thread = t.getJavaName();
        r.threadId = t.getJavaThreadId();
      }
      RecordedStackTrace st = e.getStackTrace();
      if (st != null) {
        List<RecordedFrame> fr = st.getFrames();
        r.stack = new StackTraceElement[fr.size()];
        for (int i = 0; i < r.stack.length; i++) {
          RecordedMethod m = fr.get(i).getMethod();
          r.stack[i] = new StackTraceElement(m.getType().getName(), m.getName(), null, fr.get(i).getLineNumber());
        }
      }
      for (int i = 4; i < e.td.fields.size(); i++) {
        Object v = e.chunk.resolve(e.vals[i]);
        if (v instanceof RecordedClass) {
          v = ((RecordedClass) v).getName();
        } else if (v instanceof RecordedObject || v instanceof List) {
          v = null;
        }
        r.values.put(e.td.fields.get(i).name, v);
      }
      rows.add(r);
    }
    PrivateAccess.getInstance().writeRecording(destination, rows);
  }

  public void close() throws IOException {
    closed = true;
  }
}
