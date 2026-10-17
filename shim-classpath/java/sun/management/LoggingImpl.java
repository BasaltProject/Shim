/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.management.ObjectName;

public class LoggingImpl implements PlatformLoggingMXBean {
  @Override
  public List<String> getLoggerNames() {
    List<String> names = new ArrayList<String>();
    Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
    while (e.hasMoreElements()) {
      names.add(e.nextElement());
    }
    return Collections.unmodifiableList(names);
  }

  @Override
  public String getLoggerLevel(String loggerName) {
    Logger l = LogManager.getLogManager().getLogger(loggerName);
    if (l == null) {
      return null;
    }
    Level level = l.getLevel();
    return level == null ? "" : level.getName();
  }

  @Override
  public void setLoggerLevel(String loggerName, String levelName) {
    if (loggerName == null) {
      throw new NullPointerException("loggerName is null");
    }
    Logger l = LogManager.getLogManager().getLogger(loggerName);
    if (l == null) {
      throw new IllegalArgumentException("Logger " + loggerName + "does not exist");
    }
    l.setLevel(levelName == null ? null : Level.parse(levelName));
  }

  @Override
  public String getParentLoggerName(String loggerName) {
    Logger l = LogManager.getLogManager().getLogger(loggerName);
    if (l == null) {
      return null;
    }
    Logger p = l.getParent();
    return p == null ? "" : p.getName();
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.util.logging:type=Logging");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
