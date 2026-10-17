/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface OperatingSystemMXBean extends PlatformManagedObject {
  String getName();

  String getArch();

  String getVersion();

  int getAvailableProcessors();

  double getSystemLoadAverage();
}
