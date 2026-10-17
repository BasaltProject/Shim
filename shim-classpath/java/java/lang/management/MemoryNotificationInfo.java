/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import javax.management.openmbean.CompositeData;

public class MemoryNotificationInfo {
  public static final String MEMORY_THRESHOLD_EXCEEDED = "java.management.memory.threshold.exceeded";
  public static final String MEMORY_COLLECTION_THRESHOLD_EXCEEDED =
      "java.management.memory.collection.threshold.exceeded";

  private final String poolName;
  private final MemoryUsage usage;
  private final long count;

  public MemoryNotificationInfo(String poolName, MemoryUsage usage, long count) {
    if (poolName == null) {
      throw new NullPointerException("Null poolName");
    }
    if (usage == null) {
      throw new NullPointerException("Null usage");
    }
    this.poolName = poolName;
    this.usage = usage;
    this.count = count;
  }

  public String getPoolName() {
    return poolName;
  }

  public MemoryUsage getUsage() {
    return usage;
  }

  public long getCount() {
    return count;
  }

  public static MemoryNotificationInfo from(CompositeData cd) {
    if (cd == null) {
      return null;
    }
    return new MemoryNotificationInfo((String) cd.get("poolName"), MemoryUsage.from((CompositeData) cd.get("usage")),
        ((Long) cd.get("count")).longValue());
  }
}
