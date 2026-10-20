/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

public interface ThreadMXBean extends java.lang.management.ThreadMXBean {
  default long[] getThreadCpuTime(long[] ids) {
    long[] out = new long[ids.length];
    for (int i = 0; i < ids.length; i++) {
      out[i] = getThreadCpuTime(ids[i]);
    }
    return out;
  }

  default long[] getThreadUserTime(long[] ids) {
    long[] out = new long[ids.length];
    for (int i = 0; i < ids.length; i++) {
      out[i] = getThreadUserTime(ids[i]);
    }
    return out;
  }

  long getThreadAllocatedBytes(long id);

  long[] getThreadAllocatedBytes(long[] ids);

  boolean isThreadAllocatedMemorySupported();

  boolean isThreadAllocatedMemoryEnabled();

  void setThreadAllocatedMemoryEnabled(boolean enable);

  default long getCurrentThreadAllocatedBytes() {
    return getThreadAllocatedBytes(Thread.currentThread().threadId());
  }
}
