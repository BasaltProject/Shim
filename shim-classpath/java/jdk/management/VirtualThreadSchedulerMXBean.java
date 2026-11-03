/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management;

public interface VirtualThreadSchedulerMXBean extends java.lang.management.PlatformManagedObject {
  int getMountedVirtualThreadCount();
  int getParallelism();
  int getPoolSize();
  long getQueuedVirtualThreadCount();
  void setParallelism(int i);
}
