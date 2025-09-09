/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class LogManager {
  private static final LogManager manager = new LogManager();

  private final Map<String, Logger> loggers = new HashMap<String, Logger>();

  protected LogManager() {}

  public static LogManager getLogManager() {
    return manager;
  }

  public synchronized boolean addLogger(Logger logger) {
    String name = logger.getName();
    if (name == null) {
      throw new NullPointerException();
    }
    if (loggers.containsKey(name)) {
      return false;
    }
    loggers.put(name, logger);
    return true;
  }

  public synchronized Logger getLogger(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    return loggers.get(name);
  }

  public synchronized Enumeration<String> getLoggerNames() {
    return Collections.enumeration(new HashMap<String, Logger>(loggers).keySet());
  }

  public void reset() {
    synchronized (this) {
      for (Logger logger : loggers.values()) {
        resetLogger(logger);
      }
    }
  }

  private void resetLogger(Logger logger) {
    Handler[] targets = logger.getHandlers();
    for (Handler h : targets) {
      logger.removeHandler(h);
      try {
        h.close();
      } catch (Exception ignored) {
      }
    }
    logger.setLevel(null);
  }

  public void readConfiguration() {
    reset();
  }

  public String getProperty(String name) {
    return System.getProperty(name);
  }
}
