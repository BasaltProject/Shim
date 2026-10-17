/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.time.Instant;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class LogRecord implements java.io.Serializable {
  private static final AtomicLong globalSequenceNumber = new AtomicLong(0);
  private static final int kMinSequentialThreadId = Integer.MAX_VALUE / 2;
  private static final AtomicInteger nextThreadId = new AtomicInteger(kMinSequentialThreadId);
  private static final ThreadLocal<Integer> threadIds = new ThreadLocal<Integer>();

  private Level level;
  private long sequenceNumber;
  private String sourceClassName;
  private String sourceMethodName;
  private String message;
  private int threadID;
  private long longThreadID;
  private Throwable thrown;
  private String loggerName;
  private String resourceBundleName;
  private Instant instant;

  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("level", Level.class),
    new ObjectStreamField("sequenceNumber", long.class),
    new ObjectStreamField("sourceClassName", String.class),
    new ObjectStreamField("sourceMethodName", String.class),
    new ObjectStreamField("message", String.class),
    new ObjectStreamField("threadID", int.class),
    new ObjectStreamField("millis", long.class),
    new ObjectStreamField("nanoAdjustment", int.class),
    new ObjectStreamField("thrown", Throwable.class),
    new ObjectStreamField("loggerName", String.class),
    new ObjectStreamField("resourceBundleName", String.class),
  };

  private transient boolean needToInferCaller;
  private transient Object[] parameters;
  private transient ResourceBundle resourceBundle;

  private static final long serialVersionUID = 5372048053134512534L;

  private int shortThreadID(long id) {
    if (id >= 0 && id <= kMinSequentialThreadId) {
      return (int) id;
    }
    Integer tid = threadIds.get();
    if (tid == null) {
      tid = nextThreadId.getAndIncrement();
      threadIds.set(tid);
    }
    return tid;
  }

  public LogRecord(Level level, String msg) {
    this.level = Objects.requireNonNull(level);
    message = msg;
    sequenceNumber = globalSequenceNumber.getAndIncrement();
    long id = Thread.currentThread().threadId();
    this.longThreadID = id;
    this.threadID = shortThreadID(id);
    this.instant = Instant.now();
    needToInferCaller = true;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public void setLoggerName(String name) {
    loggerName = name;
  }

  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public void setResourceBundle(ResourceBundle bundle) {
    resourceBundle = bundle;
  }

  public String getResourceBundleName() {
    return resourceBundleName;
  }

  public void setResourceBundleName(String name) {
    resourceBundleName = name;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    if (level == null) {
      throw new NullPointerException();
    }
    this.level = level;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long seq) {
    sequenceNumber = seq;
  }

  public String getSourceClassName() {
    if (needToInferCaller) {
      inferCaller();
    }
    return sourceClassName;
  }

  public void setSourceClassName(String sourceClassName) {
    this.sourceClassName = sourceClassName;
    needToInferCaller = false;
  }

  public String getSourceMethodName() {
    if (needToInferCaller) {
      inferCaller();
    }
    return sourceMethodName;
  }

  public void setSourceMethodName(String sourceMethodName) {
    this.sourceMethodName = sourceMethodName;
    needToInferCaller = false;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object[] getParameters() {
    return parameters;
  }

  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }

  public int getThreadID() {
    return threadID;
  }

  public void setThreadID(int threadID) {
    this.threadID = threadID;
    this.longThreadID = threadID;
  }

  public long getLongThreadID() {
    return longThreadID;
  }

  public LogRecord setLongThreadID(long longThreadID) {
    this.threadID = shortThreadID(longThreadID);
    this.longThreadID = longThreadID;
    return this;
  }

  public long getMillis() {
    return instant.toEpochMilli();
  }

  @Deprecated
  public void setMillis(long millis) {
    this.instant = Instant.ofEpochMilli(millis);
  }

  public Instant getInstant() {
    return instant;
  }

  public void setInstant(Instant instant) {
    this.instant = Objects.requireNonNull(instant);
  }

  public Throwable getThrown() {
    return thrown;
  }

  public void setThrown(Throwable thrown) {
    this.thrown = thrown;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    ObjectOutputStream.PutField pf = out.putFields();
    pf.put("level", level);
    pf.put("sequenceNumber", sequenceNumber);
    pf.put("sourceClassName", sourceClassName);
    pf.put("sourceMethodName", sourceMethodName);
    pf.put("message", message);
    pf.put("threadID", threadID);
    pf.put("millis", instant.toEpochMilli());
    pf.put("nanoAdjustment", instant.getNano() % 1000_000);
    pf.put("thrown", thrown);
    pf.put("loggerName", loggerName);
    pf.put("resourceBundleName", resourceBundleName);
    out.writeFields();
    out.writeByte(1);
    out.writeByte(0);
    if (parameters == null) {
      out.writeInt(-1);
      return;
    }
    out.writeInt(parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i] == null) {
        out.writeObject(null);
      } else {
        out.writeObject(parameters[i].toString());
      }
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField gf = in.readFields();
    level = (Level) gf.get("level", null);
    sequenceNumber = gf.get("sequenceNumber", 0L);
    sourceClassName = (String) gf.get("sourceClassName", null);
    sourceMethodName = (String) gf.get("sourceMethodName", null);
    message = (String) gf.get("message", null);
    threadID = gf.get("threadID", 0);
    long millis = gf.get("millis", 0L);
    int nanoOfMilli = gf.get("nanoAdjustment", 0);
    instant = Instant.ofEpochSecond(Math.floorDiv(millis, 1000L), Math.floorMod(millis, 1000L) * 1000_000L
        + nanoOfMilli);
    thrown = (Throwable) gf.get("thrown", null);
    loggerName = (String) gf.get("loggerName", null);
    resourceBundleName = (String) gf.get("resourceBundleName", null);
    longThreadID = threadID;
    byte major = in.readByte();
    byte minor = in.readByte();
    if (major != 1) {
      throw new IOException("LogRecord: bad version: " + major + "." + minor);
    }
    int len = in.readInt();
    if (len < -1) {
      throw new NegativeArraySizeException();
    } else if (len == -1) {
      parameters = null;
    } else {
      parameters = new Object[len];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = in.readObject();
      }
    }
    if (resourceBundleName != null) {
      try {
        resourceBundle = ResourceBundle.getBundle(resourceBundleName);
      } catch (MissingResourceException ex) {
        resourceBundle = null;
      }
    }
    needToInferCaller = false;
  }

  private void inferCaller() {
    needToInferCaller = false;
    Optional<StackWalker.StackFrame> frame = new CallerFinder().get();
    frame.ifPresent(f -> {
      setSourceClassName(f.getClassName());
      setSourceMethodName(f.getMethodName());
    });
  }

  static final class CallerFinder implements Predicate<StackWalker.StackFrame> {
    private static final StackWalker kWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private boolean lookingForLogger = true;

    Optional<StackWalker.StackFrame> get() {
      return kWalker.walk((s) -> s.filter(this).findFirst());
    }

    @Override
    public boolean test(StackWalker.StackFrame t) {
      final String cname = t.getClassName();
      if (lookingForLogger) {
        lookingForLogger = !isLoggerImplFrame(cname);
        return false;
      }
      return !isFilteredFrame(t);
    }

    private boolean isLoggerImplFrame(String cname) {
      return cname.equals("java.util.logging.Logger") || cname.startsWith("sun.util.logging.PlatformLogger")
          || cname.startsWith("sun.util.logging.internal.");
    }

    private boolean isFilteredFrame(StackWalker.StackFrame st) {
      String cname = st.getClassName();
      if (isLoggerImplFrame(cname)) {
        return true;
      }
      return cname.startsWith("jdk.internal.reflect.") || cname.equals("java.lang.reflect.Method")
          || cname.equals("java.lang.reflect.Constructor") || cname.startsWith("java.lang.invoke.");
    }
  }
}
