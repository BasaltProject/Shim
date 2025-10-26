/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface MemoryPoolMXBean extends PlatformManagedObject {
  String getName();

  MemoryType getType();

  MemoryUsage getUsage();

  MemoryUsage getPeakUsage();

  void resetPeakUsage();

  boolean isValid();

  String[] getMemoryManagerNames();

  long getUsageThreshold();

  void setUsageThreshold(long p0);

  boolean isUsageThresholdExceeded();

  long getUsageThresholdCount();

  boolean isUsageThresholdSupported();

  long getCollectionUsageThreshold();

  void setCollectionUsageThreshold(long p0);

  boolean isCollectionUsageThresholdExceeded();

  long getCollectionUsageThresholdCount();

  MemoryUsage getCollectionUsage();

  boolean isCollectionUsageThresholdSupported();
}
