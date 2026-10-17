/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.util.List;

public interface LoggingMXBean {
  List<String> getLoggerNames();

  String getLoggerLevel(String loggerName);

  void setLoggerLevel(String loggerName, String levelName);

  String getParentLoggerName(String loggerName);
}
