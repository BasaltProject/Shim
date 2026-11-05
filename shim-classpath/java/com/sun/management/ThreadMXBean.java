/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

public interface ThreadMXBean extends java.lang.management.ThreadMXBean {
  long[] getThreadCpuTime(long[] ids);

  long[] getThreadUserTime(long[] ids);

  long getThreadAllocatedBytes(long id);

  long[] getThreadAllocatedBytes(long[] ids);

  boolean isThreadAllocatedMemorySupported();

  boolean isThreadAllocatedMemoryEnabled();

  void setThreadAllocatedMemoryEnabled(boolean enable);

  default long getCurrentThreadAllocatedBytes() {
    return getThreadAllocatedBytes(Thread.currentThread().threadId());
  }

  default long getTotalThreadAllocatedBytes() {
    throw new UnsupportedOperationException();
  }
}
