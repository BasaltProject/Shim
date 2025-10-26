/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.management.ObjectName;

public class MemoryImpl implements MemoryMXBean {
  private volatile boolean verbose;

  @Override
  public int getObjectPendingFinalizationCount() {
    return 0;
  }

  @Override
  public MemoryUsage getHeapMemoryUsage() {
    Runtime r = Runtime.getRuntime();
    long committed = r.totalMemory();
    long used = Math.min(committed, committed - r.freeMemory());
    long max = r.maxMemory();
    return new MemoryUsage(committed, used, committed, Math.max(max, committed));
  }

  @Override
  public MemoryUsage getNonHeapMemoryUsage() {
    return new MemoryUsage(0, 0, 0, -1);
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public void setVerbose(boolean value) {
    verbose = value;
  }

  @Override
  public void gc() {
    Runtime.getRuntime().gc();
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=Memory");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
