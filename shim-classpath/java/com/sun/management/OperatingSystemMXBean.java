/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

public interface OperatingSystemMXBean extends java.lang.management.OperatingSystemMXBean {
  long getCommittedVirtualMemorySize();

  long getTotalSwapSpaceSize();

  long getFreeSwapSpaceSize();

  long getProcessCpuTime();

  @Deprecated(since = "14")
  default long getFreePhysicalMemorySize() {
    return getFreeMemorySize();
  }

  default long getFreeMemorySize() {
    throw new UnsupportedOperationException();
  }

  @Deprecated(since = "14")
  default long getTotalPhysicalMemorySize() {
    return getTotalMemorySize();
  }

  default long getTotalMemorySize() {
    throw new UnsupportedOperationException();
  }

  @Deprecated(since = "14")
  default double getSystemCpuLoad() {
    return getCpuLoad();
  }

  default double getCpuLoad() {
    throw new UnsupportedOperationException();
  }

  double getProcessCpuLoad();
}
