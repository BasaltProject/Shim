/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface PlatformLoggingMXBean extends PlatformManagedObject {
  java.util.List<String> getLoggerNames();

  String getLoggerLevel(String p0);

  void setLoggerLevel(String p0, String p1);

  String getParentLoggerName(String p0);
}
