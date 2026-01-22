/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;

public class ThreadImpl implements com.sun.management.ThreadMXBean {
  private boolean allocatedEnabled = true;

  @Override
  public long getThreadAllocatedBytes(long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Invalid thread ID parameter: " + id);
    }
    return allocatedEnabled && Thread.currentThread().threadId() == id ? 0 : -1;
  }

  @Override
  public long[] getThreadAllocatedBytes(long[] ids) {
    long[] out = new long[ids.length];
    for (int i = 0; i < ids.length; i++) {
      out[i] = getThreadAllocatedBytes(ids[i]);
    }
    return out;
  }

  @Override
  public boolean isThreadAllocatedMemorySupported() {
    return true;
  }

  @Override
  public boolean isThreadAllocatedMemoryEnabled() {
    return allocatedEnabled;
  }

  @Override
  public void setThreadAllocatedMemoryEnabled(boolean enable) {
    allocatedEnabled = enable;
  }

  private volatile boolean contentionMonitoring;
  private volatile boolean cpuTime = true;
  private volatile int peak;
  private static final long kStart = System.nanoTime();

  private static long cpuNow() {
    return Math.max(0L, System.nanoTime() - kStart);
  }

  private Thread[] threads() {
    Thread[] all = new Thread[Math.max(16, Thread.activeCount() * 2)];
    int n = Thread.enumerate(all);
    Thread[] out = new Thread[n];
    System.arraycopy(all, 0, out, 0, n);
    return out;
  }

  private Thread find(long id) {
    for (Thread t : threads()) {
      if (t.threadId() == id) {
        return t;
      }
    }
    return null;
  }

  private static void checkId(long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Invalid thread ID parameter: " + id);
    }
  }

  @Override
  public int getThreadCount() {
    int n = threads().length;
    peak = Math.max(peak, n);
    return n;
  }

  @Override
  public int getPeakThreadCount() {
    return Math.max(peak, getThreadCount());
  }

  @Override
  public long getTotalStartedThreadCount() {
    return getPeakThreadCount();
  }

  @Override
  public int getDaemonThreadCount() {
    int n = 0;
    for (Thread t : threads()) {
      if (t.isDaemon()) {
        n++;
      }
    }
    return n;
  }

  @Override
  public long[] getAllThreadIds() {
    Thread[] ts = threads();
    long[] ids = new long[ts.length];
    for (int i = 0; i < ts.length; i++) {
      ids[i] = ts[i].threadId();
    }
    return ids;
  }

  @Override
  public ThreadInfo getThreadInfo(long id) {
    return getThreadInfo(id, 0);
  }

  @Override
  public ThreadInfo[] getThreadInfo(long[] ids) {
    return getThreadInfo(ids, 0);
  }

  @Override
  public ThreadInfo getThreadInfo(long id, int maxDepth) {
    checkId(id);
    if (maxDepth < 0) {
      throw new IllegalArgumentException("Invalid maxDepth parameter: " + maxDepth);
    }
    Thread t = find(id);
    return t == null ? null : info(t, maxDepth);
  }

  @Override
  public ThreadInfo[] getThreadInfo(long[] ids, int maxDepth) {
    if (maxDepth < 0) {
      throw new IllegalArgumentException("Invalid maxDepth parameter: " + maxDepth);
    }
    for (long id : ids) {
      checkId(id);
    }
    ThreadInfo[] out = new ThreadInfo[ids.length];
    for (int i = 0; i < ids.length; i++) {
      Thread t = find(ids[i]);
      out[i] = t == null ? null : info(t, maxDepth);
    }
    return out;
  }

  private static ThreadInfo info(Thread t, int maxDepth) {
    StackTraceElement[] stack = maxDepth == 0 ? new StackTraceElement[0] : t.getStackTrace();
    if (stack.length > maxDepth && maxDepth != Integer.MAX_VALUE) {
      StackTraceElement[] cut = new StackTraceElement[maxDepth];
      System.arraycopy(stack, 0, cut, 0, maxDepth);
      stack = cut;
    }
    return ThreadInfoAccess.make(t, stack);
  }

  @Override
  public boolean isThreadContentionMonitoringSupported() {
    return true;
  }

  @Override
  public boolean isThreadContentionMonitoringEnabled() {
    return contentionMonitoring;
  }

  @Override
  public void setThreadContentionMonitoringEnabled(boolean enable) {
    contentionMonitoring = enable;
  }

  @Override
  public long getCurrentThreadCpuTime() {
    return cpuNow();
  }

  @Override
  public long getCurrentThreadUserTime() {
    return cpuNow();
  }

  @Override
  public long getThreadCpuTime(long id) {
    checkId(id);
    return find(id) == null ? -1 : cpuNow();
  }

  @Override
  public long getThreadUserTime(long id) {
    return getThreadCpuTime(id);
  }

  @Override
  public boolean isThreadCpuTimeSupported() {
    return true;
  }

  @Override
  public boolean isCurrentThreadCpuTimeSupported() {
    return true;
  }

  @Override
  public boolean isThreadCpuTimeEnabled() {
    return cpuTime;
  }

  @Override
  public void setThreadCpuTimeEnabled(boolean enable) {
    cpuTime = enable;
  }

  @Override
  public long[] findMonitorDeadlockedThreads() {
    return null;
  }

  @Override
  public void resetPeakThreadCount() {
    peak = threads().length;
  }

  @Override
  public long[] findDeadlockedThreads() {
    return null;
  }

  @Override
  public boolean isObjectMonitorUsageSupported() {
    return false;
  }

  @Override
  public boolean isSynchronizerUsageSupported() {
    return false;
  }

  @Override
  public ThreadInfo[] getThreadInfo(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
    return getThreadInfo(ids, Integer.MAX_VALUE);
  }

  @Override
  public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers) {
    return dumpAllThreads(lockedMonitors, lockedSynchronizers, Integer.MAX_VALUE);
  }

  @Override
  public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers, int maxDepth) {
    if (maxDepth < 0) {
      throw new IllegalArgumentException("Invalid maxDepth parameter: " + maxDepth);
    }
    List<ThreadInfo> out = new ArrayList<ThreadInfo>();
    for (Thread t : threads()) {
      out.add(info(t, maxDepth));
    }
    return out.toArray(new ThreadInfo[0]);
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=Threading");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
