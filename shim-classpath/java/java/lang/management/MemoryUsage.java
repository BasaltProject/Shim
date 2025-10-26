/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import javax.management.openmbean.CompositeData;

public class MemoryUsage {
  private final long init;
  private final long used;
  private final long committed;
  private final long max;

  public MemoryUsage(long init, long used, long committed, long max) {
    if (init < -1) {
      throw new IllegalArgumentException("init parameter = " + init + " is negative but not -1.");
    }
    if (max < -1) {
      throw new IllegalArgumentException("max parameter = " + max + " is negative but not -1.");
    }
    if (used < 0) {
      throw new IllegalArgumentException("used parameter = " + used + " is negative.");
    }
    if (committed < 0) {
      throw new IllegalArgumentException("committed parameter = " + committed + " is negative.");
    }
    if (used > committed) {
      throw new IllegalArgumentException("used = " + used + " should be <= committed = " + committed);
    }
    if (max >= 0 && committed > max) {
      throw new IllegalArgumentException("committed = " + committed + " should be < max = " + max);
    }
    this.init = init;
    this.used = used;
    this.committed = committed;
    this.max = max;
  }

  public long getInit() {
    return init;
  }

  public long getUsed() {
    return used;
  }

  public long getCommitted() {
    return committed;
  }

  public long getMax() {
    return max;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("init = " + init + "(" + (init >> 10) + "K) ");
    buf.append("used = " + used + "(" + (used >> 10) + "K) ");
    buf.append("committed = " + committed + "(" + (committed >> 10) + "K) ");
    buf.append("max = " + max + "(" + (max >> 10) + "K)");
    return buf.toString();
  }

  public static MemoryUsage from(CompositeData cd) {
    if (cd == null) {
      return null;
    }
    return new MemoryUsage(((Long) cd.get("init")).longValue(), ((Long) cd.get("used")).longValue(),
        ((Long) cd.get("committed")).longValue(), ((Long) cd.get("max")).longValue());
  }
}
