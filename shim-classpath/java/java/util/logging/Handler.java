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

import java.util.Objects;

public abstract class Handler {
  private static final int kOffValue = Level.OFF.intValue();
  private final LogManager manager = LogManager.getLogManager();

  private volatile Filter filter;
  private volatile Formatter formatter;
  private volatile Level logLevel = Level.ALL;
  private volatile ErrorManager errorManager = new ErrorManager();
  private volatile String encoding;

  protected Handler() {
  }

  Handler(Level defaultLevel, Formatter defaultFormatter, Formatter specifiedFormatter) {
    String cname = getClass().getName();
    final Level level = manager.getLevelProperty(cname + ".level", defaultLevel);
    final Filter filter = manager.getFilterProperty(cname + ".filter", null);
    final Formatter formatter = specifiedFormatter == null ? manager.getFormatterProperty(cname + ".formatter",
        defaultFormatter) : specifiedFormatter;
    final String encoding = manager.getStringProperty(cname + ".encoding", null);
    this.logLevel = level;
    this.filter = filter;
    this.formatter = formatter;
    try {
      setEncoding(encoding);
    } catch (Exception ex) {
      try {
        setEncoding(null);
      } catch (Exception ex2) {
        // doing a setEncoding with null should always work
      }
    }
  }

  public abstract void publish(LogRecord record);

  public abstract void flush();

  public abstract void close() throws SecurityException;

  public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
    checkPermission();
    formatter = Objects.requireNonNull(newFormatter);
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public synchronized void setEncoding(String encoding) throws SecurityException, java.io.UnsupportedEncodingException {
    checkPermission();
    if (encoding != null) {
      try {
        if (!java.nio.charset.Charset.isSupported(encoding)) {
          throw new java.io.UnsupportedEncodingException(encoding);
        }
      } catch (java.nio.charset.IllegalCharsetNameException e) {
        throw new java.io.UnsupportedEncodingException(encoding);
      }
    }
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  public synchronized void setFilter(Filter newFilter) throws SecurityException {
    checkPermission();
    filter = newFilter;
  }

  public Filter getFilter() {
    return filter;
  }

  public synchronized void setErrorManager(ErrorManager em) {
    checkPermission();
    if (em == null) {
      throw new NullPointerException();
    }
    errorManager = em;
  }

  public ErrorManager getErrorManager() {
    checkPermission();
    return errorManager;
  }

  protected void reportError(String msg, Exception ex, int code) {
    try {
      errorManager.error(msg, ex, code);
    } catch (Exception ex2) {
      System.err.println("Handler.reportError caught:");
      ex2.printStackTrace();
    }
  }

  public synchronized void setLevel(Level newLevel) throws SecurityException {
    if (newLevel == null) {
      throw new NullPointerException();
    }
    checkPermission();
    logLevel = newLevel;
  }

  public Level getLevel() {
    return logLevel;
  }

  public boolean isLoggable(LogRecord record) {
    final int levelValue = getLevel().intValue();
    if (record == null) {
      return false;
    }
    if (record.getLevel().intValue() < levelValue || levelValue == kOffValue) {
      return false;
    }
    final Filter filter = getFilter();
    if (filter == null) {
      return true;
    }
    return filter.isLoggable(record);
  }

  void checkPermission() throws SecurityException {
    manager.checkPermission();
  }
}
