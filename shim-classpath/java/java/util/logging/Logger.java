/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.logging;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class Logger {
  public static final String GLOBAL_LOGGER_NAME = "global";

  private final String name;
  private final String resourceBundleName;
  private Level levelValue = null;
  private final List<Handler> handlers = new CopyOnWriteArrayList<Handler>();
  private boolean useParentHandlers = true;
  private Filter filter;
  private Logger parent;

  private static final Logger rootLogger;
  private static final Logger globalLogger;

  static {
    rootLogger = new Logger("", null);
    rootLogger.setLevel(Level.INFO);
    rootLogger.addHandler(new ConsoleHandler());
    LogManager.getLogManager().addLogger(rootLogger);

    globalLogger = new Logger(GLOBAL_LOGGER_NAME, null);
    globalLogger.parent = rootLogger;
    LogManager.getLogManager().addLogger(globalLogger);
  }

  public static Logger getGlobal() {
    return globalLogger;
  }

  public static Logger getLogger(String name) {
    return getLogger(name, null);
  }

  public static synchronized Logger getLogger(String name, String resourceBundleName) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.isEmpty()) {
      return rootLogger;
    }
    LogManager manager = LogManager.getLogManager();
    Logger result = manager.getLogger(name);
    if (result == null) {
      result = new Logger(name, resourceBundleName);
      result.parent = rootLogger;
      manager.addLogger(result);
    }
    return result;
  }

  public static Logger getAnonymousLogger() {
    return getAnonymousLogger(null);
  }

  public static Logger getAnonymousLogger(String resourceBundleName) {
    Logger logger = new Logger(null, resourceBundleName);
    logger.parent = rootLogger;
    return logger;
  }

  protected Logger(String name, String resourceBundleName) {
    this.name = name;
    this.resourceBundleName = resourceBundleName;
  }

  public String getName() {
    return name;
  }

  public String getResourceBundleName() {
    return resourceBundleName;
  }

  public Handler[] getHandlers() {
    return handlers.toArray(new Handler[handlers.size()]);
  }

  public void addHandler(Handler handler) {
    if (handler == null) {
      throw new NullPointerException();
    }
    handlers.add(handler);
  }

  public void removeHandler(Handler handler) {
    if (handler != null) {
      handlers.remove(handler);
    }
  }

  public boolean getUseParentHandlers() {
    return useParentHandlers;
  }

  public void setUseParentHandlers(boolean useParentHandlers) {
    this.useParentHandlers = useParentHandlers;
  }

  public Logger getParent() {
    return parent;
  }

  public void setParent(Logger parent) {
    if (parent == null) {
      throw new NullPointerException();
    }
    this.parent = parent;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter newFilter) {
    this.filter = newFilter;
  }

  public Level getLevel() {
    return levelValue;
  }

  public void setLevel(Level level) {
    this.levelValue = level;
  }

  private Level getEffectiveLevel() {
    Logger logger = this;
    while (logger != null) {
      if (logger.levelValue != null) {
        return logger.levelValue;
      }
      logger = logger.parent;
    }
    return Level.INFO;
  }

  public boolean isLoggable(Level level) {
    int effective = getEffectiveLevel().intValue();
    if (level.intValue() < effective || effective == Level.OFF.intValue()) {
      return false;
    }
    return true;
  }

  public void log(LogRecord record) {
    if (!isLoggable(record.getLevel())) {
      return;
    }
    Filter f = this.filter;
    if (f != null && !f.isLoggable(record)) {
      return;
    }
    Logger cur = this;
    while (cur != null) {
      for (Handler h : cur.handlers) {
        h.publish(record);
      }
      if (!cur.useParentHandlers) {
        break;
      }
      cur = cur.parent;
    }
  }

  private void doLog(Level level, String msg, Object[] params, Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(name);
    lr.setParameters(params);
    lr.setThrown(thrown);
    log(lr);
  }

  public void log(Level level, String msg) {
    doLog(level, msg, null, null);
  }

  public void log(Level level, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    doLog(level, msgSupplier != null ? msgSupplier.get() : null, null, null);
  }

  public void log(Level level, String msg, Object param1) {
    doLog(level, msg, new Object[] { param1 }, null);
  }

  public void log(Level level, String msg, Object[] params) {
    doLog(level, msg, params, null);
  }

  public void log(Level level, String msg, Throwable thrown) {
    doLog(level, msg, null, thrown);
  }

  public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    doLog(level, msgSupplier != null ? msgSupplier.get() : null, null, thrown);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(name);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    log(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    logp(level, sourceClass, sourceMethod, msgSupplier != null ? msgSupplier.get() : null);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(name);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setParameters(new Object[] { param1 });
    log(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(name);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setParameters(params);
    log(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setLoggerName(name);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    log(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    logp(level, sourceClass, sourceMethod, msgSupplier != null ? msgSupplier.get() : null, thrown);
  }

  public void entering(String sourceClass, String sourceMethod) {
    logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
  }

  public void entering(String sourceClass, String sourceMethod, Object param1) {
    logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param1);
  }

  public void entering(String sourceClass, String sourceMethod, Object[] params) {
    if (params == null) {
      logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
      return;
    }
    StringBuilder sb = new StringBuilder("ENTRY");
    for (int i = 0; i < params.length; i++) {
      sb.append(" {").append(i).append("}");
    }
    logp(Level.FINER, sourceClass, sourceMethod, sb.toString(), params);
  }

  public void exiting(String sourceClass, String sourceMethod) {
    logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
  }

  public void exiting(String sourceClass, String sourceMethod, Object result) {
    logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
  }

  public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
    logp(Level.FINER, sourceClass, sourceMethod, "THROW", thrown);
  }

  public void severe(String msg) {
    log(Level.SEVERE, msg);
  }

  public void severe(Supplier<String> msgSupplier) {
    log(Level.SEVERE, msgSupplier);
  }

  public void warning(String msg) {
    log(Level.WARNING, msg);
  }

  public void warning(Supplier<String> msgSupplier) {
    log(Level.WARNING, msgSupplier);
  }

  public void info(String msg) {
    log(Level.INFO, msg);
  }

  public void info(Supplier<String> msgSupplier) {
    log(Level.INFO, msgSupplier);
  }

  public void config(String msg) {
    log(Level.CONFIG, msg);
  }

  public void config(Supplier<String> msgSupplier) {
    log(Level.CONFIG, msgSupplier);
  }

  public void fine(String msg) {
    log(Level.FINE, msg);
  }

  public void fine(Supplier<String> msgSupplier) {
    log(Level.FINE, msgSupplier);
  }

  public void finer(String msg) {
    log(Level.FINER, msg);
  }

  public void finer(Supplier<String> msgSupplier) {
    log(Level.FINER, msgSupplier);
  }

  public void finest(String msg) {
    log(Level.FINEST, msg);
  }

  public void finest(Supplier<String> msgSupplier) {
    log(Level.FINEST, msgSupplier);
  }
}
