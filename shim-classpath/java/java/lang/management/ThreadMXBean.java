/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface ThreadMXBean extends PlatformManagedObject {
  int getThreadCount();

  int getPeakThreadCount();

  long getTotalStartedThreadCount();

  int getDaemonThreadCount();

  long[] getAllThreadIds();

  ThreadInfo getThreadInfo(long p0);

  ThreadInfo[] getThreadInfo(long[] p0);

  ThreadInfo getThreadInfo(long p0, int p1);

  ThreadInfo[] getThreadInfo(long[] p0, int p1);

  boolean isThreadContentionMonitoringSupported();

  boolean isThreadContentionMonitoringEnabled();

  void setThreadContentionMonitoringEnabled(boolean p0);

  long getCurrentThreadCpuTime();

  long getCurrentThreadUserTime();

  long getThreadCpuTime(long p0);

  long getThreadUserTime(long p0);

  boolean isThreadCpuTimeSupported();

  boolean isCurrentThreadCpuTimeSupported();

  boolean isThreadCpuTimeEnabled();

  void setThreadCpuTimeEnabled(boolean p0);

  long[] findMonitorDeadlockedThreads();

  void resetPeakThreadCount();

  long[] findDeadlockedThreads();

  boolean isObjectMonitorUsageSupported();

  boolean isSynchronizerUsageSupported();

  ThreadInfo[] getThreadInfo(long[] p0, boolean p1, boolean p2);

  default ThreadInfo[] getThreadInfo(long[] p0, boolean p1, boolean p2, int p3) {
    throw new UnsupportedOperationException();
  }

  ThreadInfo[] dumpAllThreads(boolean p0, boolean p1);

  default ThreadInfo[] dumpAllThreads(boolean p0, boolean p1, int p2) {
    throw new UnsupportedOperationException();
  }
}
