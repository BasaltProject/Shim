/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.util.logging.internal;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.LogRecord;

public final class JulSystemLogger implements System.Logger {
  private final java.util.logging.Logger logger;

  public JulSystemLogger(String name) {
    this.logger = java.util.logging.Logger.getLogger(name);
  }

  public static java.util.logging.Level toJul(System.Logger.Level level) {
    switch (level) {
    case ALL: return java.util.logging.Level.ALL;
    case TRACE: return java.util.logging.Level.FINER;
    case DEBUG: return java.util.logging.Level.FINE;
    case INFO: return java.util.logging.Level.INFO;
    case WARNING: return java.util.logging.Level.WARNING;
    case ERROR: return java.util.logging.Level.SEVERE;
    default: return java.util.logging.Level.OFF;
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }

  @Override
  public boolean isLoggable(System.Logger.Level level) {
    Objects.requireNonNull(level);
    return logger.isLoggable(toJul(level));
  }

  @Override
  public void log(System.Logger.Level level, ResourceBundle bundle, String msg, Throwable thrown) {
    Objects.requireNonNull(level);
    java.util.logging.Level julLevel = toJul(level);
    if (!logger.isLoggable(julLevel)) return;
    LogRecord record = new LogRecord(julLevel, msg);
    record.setLoggerName(logger.getName());
    record.setThrown(thrown);
    applyBundle(record, bundle);
    logger.log(record);
  }

  @Override
  public void log(System.Logger.Level level, ResourceBundle bundle, String format, Object... params) {
    Objects.requireNonNull(level);
    java.util.logging.Level julLevel = toJul(level);
    if (!logger.isLoggable(julLevel)) return;
    LogRecord record = new LogRecord(julLevel, format);
    record.setLoggerName(logger.getName());
    if (params != null && params.length > 0) record.setParameters(params);
    applyBundle(record, bundle);
    logger.log(record);
  }

  @Override
  public void log(System.Logger.Level level, Supplier<String> msgSupplier) {
    Objects.requireNonNull(msgSupplier);
    log(level, (ResourceBundle) null, msgSupplier.get(), (Object[]) null);
  }

  private void applyBundle(LogRecord record, ResourceBundle bundle) {
    ResourceBundle effective = bundle != null ? bundle : logger.getResourceBundle();
    if (effective != null) {
      record.setResourceBundle(effective);
      record.setResourceBundleName(logger.getResourceBundleName());
    }
  }
}
