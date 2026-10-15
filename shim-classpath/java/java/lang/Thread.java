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

package java.lang;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Thread implements Runnable {
  // set and accessed from within LockSupport
  protected volatile Object parkBlocker;
  
  private long peer;
  private volatile boolean interrupted;
  private volatile boolean unparked;
  private boolean daemon;
  private byte state;
  private byte priority;
  private final Runnable task;
  private Map<ThreadLocal, Object> locals;
  private Object sleepLock;
  private Object interruptLock;
  private ClassLoader classLoader;
  private UncaughtExceptionHandler exceptionHandler;
  private String name;
  private ThreadGroup group;

  private static final AtomicLong nextThreadId = new AtomicLong(1);
  private final long tid;
  private final boolean isVirtual;

  private static UncaughtExceptionHandler defaultExceptionHandler;

  public static final int MIN_PRIORITY = 1;
  public static final int NORM_PRIORITY = 5;
  public static final int MAX_PRIORITY = 10;

  public Thread(ThreadGroup group, Runnable task, String name, long stackSize, boolean isVirtual) {
    this.isVirtual = isVirtual;
    this.tid = nextThreadId.getAndIncrement();
    this.group = (group == null ? (Thread.currentThread() != null ? Thread.currentThread().group : null) : group);
    this.task = task;
    this.name = (name != null) ? name : (isVirtual ? "" : "Thread-" + tid);
    if (isVirtual) {
      this.daemon = true;
      this.priority = (byte) NORM_PRIORITY;
    } else {
      this.priority = (byte) NORM_PRIORITY;
    }

    Thread current = currentThread();
    if (current != null) {
      Map<ThreadLocal, Object> map = current.locals;
      if (map != null) {
        for (Map.Entry<ThreadLocal, Object> e: map.entrySet()) {
          if (e.getKey() instanceof InheritableThreadLocal) {
            InheritableThreadLocal itl = (InheritableThreadLocal) e.getKey();
            locals().put(itl, itl.childValue(e.getValue()));
          }
        }
      }
      classLoader = current.classLoader;
    }
  }

  public Thread(ThreadGroup group, Runnable task, String name, long stackSize) {
    this(group, task, name, stackSize, false);
  }

  public Thread(ThreadGroup group, Runnable task, String name) {
    this(group, task, name, 0, false);
  }

  public Thread(ThreadGroup group, String name) {
    this(group, null, name, 0, false);
  }

  public Thread(Runnable task, String name) {
    this(null, task, name, 0, false);
  }

  public Thread(Runnable task) {
    this(null, task, null, 0, false);
  }

  public Thread(String name) {
    this(null, null, name, 0, false);
  }

  public Thread() {
    this((Runnable) null);
  }

  public static void onSpinWait() {
    // Hint to CPU
  }

  public final boolean isVirtual() {
    return isVirtual;
  }

  public final long threadId() {
    return tid != 0 ? tid : 1;
  }

  public long getId() {
    return threadId();
  }

  public static Builder.OfPlatform ofPlatform() {
    return new PlatformThreadBuilder();
  }

  public static Builder.OfVirtual ofVirtual() {
    return new VirtualThreadBuilder();
  }

  public static Thread startVirtualThread(Runnable task) {
    Objects.requireNonNull(task);
    return ofVirtual().start(task);
  }

  public static void sleep(java.time.Duration duration) throws InterruptedException {
    Objects.requireNonNull(duration);
    if (duration.isNegative()) {
      return;
    }
    long millis = duration.toMillis();
    int nanos = duration.toNanosPart();
    sleep(millis, nanos);
  }

  public final boolean join(java.time.Duration duration) throws InterruptedException {
    Objects.requireNonNull(duration);
    if (duration.isNegative()) {
      return getState() == State.TERMINATED;
    }
    long millis = duration.toMillis();
    int nanos = duration.toNanosPart();
    join(millis, nanos);
    return getState() == State.TERMINATED;
  }

  public synchronized void start() {
    if (peer != 0) {
      throw new IllegalStateException("thread already started");
    }

    state = (byte) State.RUNNABLE.ordinal();

    peer = doStart();
    if (peer == 0) {
      state = (byte) State.NEW.ordinal();
      throw new RuntimeException("unable to start native thread");
    }
  }

  private native long doStart();

  private static void run(Thread t) throws Throwable {
    try {
      t.run();
    } catch (Throwable e) {
      UncaughtExceptionHandler eh = t.exceptionHandler;
      UncaughtExceptionHandler deh = defaultExceptionHandler;
      if (eh != null) {
        eh.uncaughtException(t, e);
      } else if (deh != null) {
        deh.uncaughtException(t, e);
      } else {
        throw e;
      }
    } finally {
      synchronized (t) {
        t.state = (byte) State.TERMINATED.ordinal();
        t.notifyAll();
      }
    }
  }

  public void run() {
    if (task != null) {
      task.run();
    }
  }

  public ClassLoader getContextClassLoader() {
    return classLoader;
  }

  public void setContextClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  Map<ThreadLocal, Object> locals() {
    if (locals == null) {
      locals = new WeakHashMap();
    }
    return locals;
  }

  public static native Thread currentThread();

  public void interrupt() {
    interrupt(peer);
  }

  private static native boolean interrupt(long peer);

  public static boolean interrupted() {
    return interrupted(currentThread().peer);
  }

  private static native boolean interrupted(long peer);

  public boolean isInterrupted() {
    return interrupted;
  }

  public static void sleep(long milliseconds) throws InterruptedException {
    if (milliseconds <= 0) {
      milliseconds = 1;
    }

    Thread t = currentThread();
    if (t.sleepLock == null) {
      t.sleepLock = new Object();
    }
    synchronized (t.sleepLock) {
      t.sleepLock.wait(milliseconds);
    }
  }

  public static void sleep(long milliseconds, int nanoseconds)
    throws InterruptedException
  {
    if (nanoseconds > 0) {
      ++ milliseconds;
    }

    sleep(milliseconds);
  }

  public StackTraceElement[] getStackTrace() {
    long p = peer;
    if (p == 0) {
      return new StackTraceElement[0];
    }
    return Throwable.resolveTrace(getStackTrace(p));
  }

  private static native Object getStackTrace(long peer);

  public static native int activeCount();

  public static native int enumerate(Thread[] array);
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UncaughtExceptionHandler getUncaughtExceptionHandler() {
    UncaughtExceptionHandler eh = exceptionHandler;
    return (eh == null ? group : eh);
  }

  public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
    return defaultExceptionHandler;
  }

  public void setUncaughtExceptionHandler(UncaughtExceptionHandler h) {
    exceptionHandler = h;
  }

  public static void setDefaultUncaughtExceptionHandler
    (UncaughtExceptionHandler h)
  {
    defaultExceptionHandler = h;
  }

  public State getState() {
    return State.values()[state];
  }

  public boolean isAlive() {
    switch (getState()) {
    case NEW:
    case TERMINATED:
      return false;

    default:
      return true;
    }
  }

  public int getPriority() {
    return isVirtual() ? NORM_PRIORITY : priority;
  }

  public void setPriority(int priority) {
    if (isVirtual()) {
      return;
    }
    if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
      throw new IllegalArgumentException();
    }
    this.priority = (byte) priority;
  }

  public boolean isDaemon() {
    return isVirtual() || daemon;
  }

  public synchronized void setDaemon(boolean v) {
    if (isVirtual()) {
      if (!v) {
        throw new IllegalArgumentException("Virtual threads are always daemon");
      }
      return;
    }
    if (getState() != State.NEW) {
      throw new IllegalStateException();
    }

    daemon = v;
  }

  public static native void yield();

  public synchronized void join() throws InterruptedException {
    while (getState() != State.TERMINATED) {
      wait();
    }
  }

  public synchronized void join(long milliseconds) throws InterruptedException
  {
    long then = System.currentTimeMillis();
    long remaining = milliseconds;
    while (remaining > 0 && getState() != State.TERMINATED) {
      wait(remaining);

      remaining = milliseconds - (System.currentTimeMillis() - then);
    }
  }

  public void join(long milliseconds, int nanoseconds)
    throws InterruptedException
  {
    if (nanoseconds > 0) {
      ++ milliseconds;
    }

    join(milliseconds);
  }

  public ThreadGroup getThreadGroup() {
    return group;
  }

  public static native boolean holdsLock(Object o);

  public interface UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e);
  }

  public enum State {
    NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
  }

  public interface Builder {
    Builder name(String name);
    Builder name(String prefix, long start);
    Builder inheritInheritableThreadLocals(boolean inheritInheritableThreadLocals);
    Builder uncaughtExceptionHandler(UncaughtExceptionHandler ueh);
    Thread unstarted(Runnable task);
    Thread start(Runnable task);
    java.util.concurrent.ThreadFactory factory();

    public interface OfPlatform extends Builder {
      @Override OfPlatform name(String name);
      @Override OfPlatform name(String prefix, long start);
      @Override OfPlatform inheritInheritableThreadLocals(boolean inheritInheritableThreadLocals);
      @Override OfPlatform uncaughtExceptionHandler(UncaughtExceptionHandler ueh);
      OfPlatform group(ThreadGroup group);
      OfPlatform daemon(boolean on);
      default OfPlatform daemon() { return daemon(true); }
      OfPlatform priority(int priority);
      OfPlatform stackSize(long stackSize);
    }

    public interface OfVirtual extends Builder {
      @Override OfVirtual name(String name);
      @Override OfVirtual name(String prefix, long start);
      @Override OfVirtual inheritInheritableThreadLocals(boolean inheritInheritableThreadLocals);
      @Override OfVirtual uncaughtExceptionHandler(UncaughtExceptionHandler ueh);
    }
  }

  private static class PlatformThreadBuilder implements Builder.OfPlatform {
    private String name;
    private String prefix;
    private long counter = -1;
    private boolean inheritLocals = true;
    private UncaughtExceptionHandler ueh;
    private ThreadGroup group;
    private boolean daemon = false;
    private int priority = NORM_PRIORITY;
    private long stackSize = 0;

    @Override
    public Builder.OfPlatform name(String name) {
      this.name = Objects.requireNonNull(name);
      this.prefix = null;
      return this;
    }

    @Override
    public Builder.OfPlatform name(String prefix, long start) {
      this.prefix = Objects.requireNonNull(prefix);
      if (start < 0) throw new IllegalArgumentException("start < 0");
      this.counter = start;
      this.name = null;
      return this;
    }

    @Override
    public Builder.OfPlatform inheritInheritableThreadLocals(boolean inherit) {
      this.inheritLocals = inherit;
      return this;
    }

    @Override
    public Builder.OfPlatform uncaughtExceptionHandler(UncaughtExceptionHandler ueh) {
      this.ueh = Objects.requireNonNull(ueh);
      return this;
    }

    @Override
    public Builder.OfPlatform group(ThreadGroup group) {
      this.group = Objects.requireNonNull(group);
      return this;
    }

    @Override
    public Builder.OfPlatform daemon(boolean on) {
      this.daemon = on;
      return this;
    }

    @Override
    public Builder.OfPlatform priority(int priority) {
      if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
        throw new IllegalArgumentException();
      }
      this.priority = priority;
      return this;
    }

    @Override
    public Builder.OfPlatform stackSize(long stackSize) {
      if (stackSize < 0) throw new IllegalArgumentException();
      this.stackSize = stackSize;
      return this;
    }

    private String nextName() {
      if (prefix != null) {
        return prefix + (counter++);
      }
      return name;
    }

    @Override
    public Thread unstarted(Runnable task) {
      Objects.requireNonNull(task);
      Thread t = new Thread(group, task, nextName(), stackSize, false);
      t.daemon = daemon;
      t.priority = (byte) priority;
      if (ueh != null) {
        t.setUncaughtExceptionHandler(ueh);
      }
      return t;
    }

    @Override
    public Thread start(Runnable task) {
      Thread t = unstarted(task);
      t.start();
      return t;
    }

    @Override
    public java.util.concurrent.ThreadFactory factory() {
      return new java.util.concurrent.ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return unstarted(r);
        }
      };
    }
  }

  private static class VirtualThreadBuilder implements Builder.OfVirtual {
    private String name;
    private String prefix;
    private long counter = -1;
    private boolean inheritLocals = true;
    private UncaughtExceptionHandler ueh;

    @Override
    public Builder.OfVirtual name(String name) {
      this.name = Objects.requireNonNull(name);
      this.prefix = null;
      return this;
    }

    @Override
    public Builder.OfVirtual name(String prefix, long start) {
      this.prefix = Objects.requireNonNull(prefix);
      if (start < 0) throw new IllegalArgumentException("start < 0");
      this.counter = start;
      this.name = null;
      return this;
    }

    @Override
    public Builder.OfVirtual inheritInheritableThreadLocals(boolean inherit) {
      this.inheritLocals = inherit;
      return this;
    }

    @Override
    public Builder.OfVirtual uncaughtExceptionHandler(UncaughtExceptionHandler ueh) {
      this.ueh = Objects.requireNonNull(ueh);
      return this;
    }

    private String nextName() {
      if (prefix != null) {
        return prefix + (counter++);
      }
      return (name != null) ? name : "";
    }

    @Override
    public Thread unstarted(Runnable task) {
      Objects.requireNonNull(task);
      Thread t = new Thread(null, task, nextName(), 0, true);
      if (ueh != null) {
        t.setUncaughtExceptionHandler(ueh);
      }
      return t;
    }

    @Override
    public Thread start(Runnable task) {
      Thread t = unstarted(task);
      t.start();
      return t;
    }

    @Override
    public java.util.concurrent.ThreadFactory factory() {
      return new java.util.concurrent.ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          return unstarted(r);
        }
      };
    }
  }
}
