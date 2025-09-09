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

public abstract class Handler {
  private Formatter formatter;
  private Filter filter;
  private Level level = Level.ALL;
  private ErrorManager errorManager = new ErrorManager();
  private String encoding;

  protected Handler() {}

  public abstract void publish(LogRecord record);

  public abstract void flush();

  public abstract void close();

  public synchronized void setFormatter(Formatter newFormatter) {
    if (newFormatter == null) {
      throw new NullPointerException();
    }
    this.formatter = newFormatter;
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public synchronized void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  public synchronized void setFilter(Filter newFilter) {
    this.filter = newFilter;
  }

  public Filter getFilter() {
    return filter;
  }

  public synchronized void setErrorManager(ErrorManager em) {
    if (em == null) {
      throw new NullPointerException();
    }
    this.errorManager = em;
  }

  public ErrorManager getErrorManager() {
    return errorManager;
  }

  public synchronized void setLevel(Level newLevel) {
    if (newLevel == null) {
      throw new NullPointerException();
    }
    this.level = newLevel;
  }

  public Level getLevel() {
    return level;
  }

  public boolean isLoggable(LogRecord record) {
    int levelValue = getLevel().intValue();
    if (record.getLevel().intValue() < levelValue || levelValue == Level.OFF.intValue()) {
      return false;
    }
    Filter filter = getFilter();
    if (filter == null) {
      return true;
    }
    return filter.isLoggable(record);
  }

  protected void reportError(String msg, Exception ex, int code) {
    try {
      errorManager.error(msg, ex, code);
    } catch (Exception ignored) {
    }
  }
}
