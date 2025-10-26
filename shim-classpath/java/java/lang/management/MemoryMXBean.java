/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface MemoryMXBean extends PlatformManagedObject {
  int getObjectPendingFinalizationCount();

  MemoryUsage getHeapMemoryUsage();

  MemoryUsage getNonHeapMemoryUsage();

  boolean isVerbose();

  void setVerbose(boolean p0);

  void gc();
}
