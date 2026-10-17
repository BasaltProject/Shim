/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

final class Logging implements LoggingMXBean {
  private static final LogManager kLogManager = LogManager.getLogManager();
  private static final Logging kInstance = new Logging();

  private Logging() {
  }

  static Logging getInstance() {
    return kInstance;
  }

  @Override
  public List<String> getLoggerNames() {
    Enumeration<String> loggers = kLogManager.getLoggerNames();
    ArrayList<String> array = new ArrayList<String>();
    for (; loggers.hasMoreElements();) {
      array.add(loggers.nextElement());
    }
    return array;
  }

  private static String kEmptyString = "";

  @Override
  public String getLoggerLevel(String loggerName) {
    Logger l = kLogManager.getLogger(loggerName);
    if (l == null) {
      return null;
    }
    Level level = l.getLevel();
    if (level == null) {
      return kEmptyString;
    } else {
      return level.getLevelName();
    }
  }

  @Override
  public void setLoggerLevel(String loggerName, String levelName) {
    if (loggerName == null) {
      throw new NullPointerException("loggerName is null");
    }
    Logger logger = kLogManager.getLogger(loggerName);
    if (logger == null) {
      throw new IllegalArgumentException("Logger " + loggerName + " does not exist");
    }
    Level level = null;
    if (levelName != null) {
      level = Level.findLevel(levelName);
      if (level == null) {
        throw new IllegalArgumentException("Unknown level \"" + levelName + "\"");
      }
    }
    logger.setLevel(level);
  }

  @Override
  public String getParentLoggerName(String loggerName) {
    Logger l = kLogManager.getLogger(loggerName);
    if (l == null) {
      return null;
    }
    Logger p = l.getParent();
    if (p == null) {
      return kEmptyString;
    } else {
      return p.getName();
    }
  }
}
