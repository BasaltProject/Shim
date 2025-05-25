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

public class LogRecord {
  private final String loggerName;
  private final String message;
  private final Throwable thrown;
  private final Level level;
  private final String methodName;

  LogRecord(String loggerName, String methodName, Level level, String message,
            Throwable thrown) {
    this.loggerName = loggerName;
    this.message = message;
    this.thrown = thrown;
    this.level = level;
    this.methodName = methodName;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public String getMessage() {
    return message;
  }

  public Throwable getThrown() {
    return thrown;
  }

  public Level getLevel() {
    return level;
  }

  public String getSourceMethodName() {
    return methodName;
  }
}
