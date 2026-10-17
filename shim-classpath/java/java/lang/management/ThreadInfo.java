/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import javax.management.openmbean.CompositeData;

public class ThreadInfo {
  private static final StackTraceElement[] NO_STACK_TRACE = new StackTraceElement[0];
  private static final int kMaxFrames = 8;

  private final String threadName;
  private final long threadId;
  private final long blockedTime;
  private final long blockedCount;
  private final long waitedTime;
  private final long waitedCount;
  private final LockInfo lock;
  private final String lockName;
  private final long lockOwnerId;
  private final String lockOwnerName;
  private final boolean daemon;
  private final boolean inNative;
  private final boolean suspended;
  private final Thread.State threadState;
  private final int priority;
  private final StackTraceElement[] stackTrace;
  private final MonitorInfo[] lockedMonitors;
  private final LockInfo[] lockedSynchronizers;

  ThreadInfo(Thread t, StackTraceElement[] stack) {
    this.threadName = t.getName();
    this.threadId = t.threadId();
    this.blockedTime = -1;
    this.blockedCount = 0;
    this.waitedTime = -1;
    this.waitedCount = 0;
    this.lock = null;
    this.lockName = null;
    this.lockOwnerId = -1;
    this.lockOwnerName = null;
    this.daemon = t.isDaemon();
    this.inNative = false;
    this.suspended = false;
    this.threadState = t.getState();
    this.priority = t.getPriority();
    this.stackTrace = stack == null ? NO_STACK_TRACE : stack;
    this.lockedMonitors = new MonitorInfo[0];
    this.lockedSynchronizers = new LockInfo[0];
  }

  private ThreadInfo(CompositeData cd) {
    this.threadId = ((Long) cd.get("threadId")).longValue();
    this.threadName = (String) cd.get("threadName");
    this.threadState = Thread.State.valueOf((String) cd.get("threadState"));
    this.blockedTime = ((Long) cd.get("blockedTime")).longValue();
    this.blockedCount = ((Long) cd.get("blockedCount")).longValue();
    this.waitedTime = ((Long) cd.get("waitedTime")).longValue();
    this.waitedCount = ((Long) cd.get("waitedCount")).longValue();
    this.lockName = (String) cd.get("lockName");
    this.lockOwnerId = ((Long) cd.get("lockOwnerId")).longValue();
    this.lockOwnerName = (String) cd.get("lockOwnerName");
    this.inNative = ((Boolean) cd.get("inNative")).booleanValue();
    this.suspended = ((Boolean) cd.get("suspended")).booleanValue();
    this.daemon = ((Boolean) cd.get("daemon")).booleanValue();
    this.priority = ((Integer) cd.get("priority")).intValue();
    CompositeData li = (CompositeData) cd.get("lockInfo");
    this.lock = li == null ? null : LockInfo.from(li);
    CompositeData[] frames = (CompositeData[]) cd.get("stackTrace");
    this.stackTrace = new StackTraceElement[frames == null ? 0 : frames.length];
    for (int i = 0; i < stackTrace.length; i++) {
      CompositeData f = frames[i];
      stackTrace[i] = new StackTraceElement((String) f.get("className"), (String) f.get("methodName"),
          (String) f.get("fileName"), ((Integer) f.get("lineNumber")).intValue());
    }
    CompositeData[] monitors = (CompositeData[]) cd.get("lockedMonitors");
    this.lockedMonitors = new MonitorInfo[monitors == null ? 0 : monitors.length];
    for (int i = 0; i < lockedMonitors.length; i++) {
      lockedMonitors[i] = MonitorInfo.from(monitors[i]);
    }
    CompositeData[] syncs = (CompositeData[]) cd.get("lockedSynchronizers");
    this.lockedSynchronizers = new LockInfo[syncs == null ? 0 : syncs.length];
    for (int i = 0; i < lockedSynchronizers.length; i++) {
      lockedSynchronizers[i] = LockInfo.from(syncs[i]);
    }
  }

  public long getThreadId() {
    return threadId;
  }

  public String getThreadName() {
    return threadName;
  }

  public Thread.State getThreadState() {
    return threadState;
  }

  public long getBlockedTime() {
    return blockedTime;
  }

  public long getBlockedCount() {
    return blockedCount;
  }

  public long getWaitedTime() {
    return waitedTime;
  }

  public long getWaitedCount() {
    return waitedCount;
  }

  public LockInfo getLockInfo() {
    return lock;
  }

  public String getLockName() {
    return lockName;
  }

  public long getLockOwnerId() {
    return lockOwnerId;
  }

  public String getLockOwnerName() {
    return lockOwnerName;
  }

  public StackTraceElement[] getStackTrace() {
    return stackTrace.clone();
  }

  public boolean isSuspended() {
    return suspended;
  }

  public boolean isInNative() {
    return inNative;
  }

  public boolean isDaemon() {
    return daemon;
  }

  public int getPriority() {
    return priority;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("\"" + getThreadName() + "\"" + (isDaemon() ? " daemon" : "")
        + " prio=" + getPriority() + " Id=" + getThreadId() + " " + getThreadState());
    if (getLockName() != null) {
      sb.append(" on " + getLockName());
    }
    if (getLockOwnerName() != null) {
      sb.append(" owned by \"" + getLockOwnerName() + "\" Id=" + getLockOwnerId());
    }
    if (isSuspended()) {
      sb.append(" (suspended)");
    }
    if (isInNative()) {
      sb.append(" (in native)");
    }
    sb.append('\n');
    int i = 0;
    for (; i < stackTrace.length && i < kMaxFrames; i++) {
      StackTraceElement ste = stackTrace[i];
      sb.append("\tat " + ste.toString());
      sb.append('\n');
      if (i == 0 && getLockInfo() != null) {
        Thread.State ts = getThreadState();
        switch (ts) {
        case BLOCKED:
          sb.append("\t-  blocked on " + getLockInfo());
          sb.append('\n');
          break;
        case WAITING:
          sb.append("\t-  waiting on " + getLockInfo());
          sb.append('\n');
          break;
        case TIMED_WAITING:
          sb.append("\t-  waiting on " + getLockInfo());
          sb.append('\n');
          break;
        default:
          break;
        }
      }
      for (MonitorInfo mi : lockedMonitors) {
        if (mi.getLockedStackDepth() == i) {
          sb.append("\t-  locked " + mi);
          sb.append('\n');
        }
      }
    }
    if (i < stackTrace.length) {
      sb.append("\t...");
      sb.append('\n');
    }
    LockInfo[] locks = getLockedSynchronizers();
    if (locks.length > 0) {
      sb.append("\n\tNumber of locked synchronizers = " + locks.length);
      sb.append('\n');
      for (LockInfo li : locks) {
        sb.append("\t- " + li);
        sb.append('\n');
      }
    }
    sb.append('\n');
    return sb.toString();
  }

  public static ThreadInfo from(CompositeData cd) {
    if (cd == null) {
      return null;
    }
    return new ThreadInfo(cd);
  }

  public MonitorInfo[] getLockedMonitors() {
    return lockedMonitors.clone();
  }

  public LockInfo[] getLockedSynchronizers() {
    return lockedSynchronizers.clone();
  }
}
