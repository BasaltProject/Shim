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

import java.io.Serializable;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

public class LogRecord implements Serializable {
  private static final AtomicLong globalSequenceNumber = new AtomicLong(0);

  private Level level;
  private long sequenceNumber;
  private String sourceClassName;
  private String sourceMethodName;
  private String message;
  private int threadID;
  private long longThreadID;
  private Instant instant;
  private Throwable thrown;
  private String loggerName;
  private String resourceBundleName;
  private transient ResourceBundle resourceBundle;
  private transient Object[] parameters;

  public LogRecord(Level level, String msg) {
    if (level == null) {
      throw new NullPointerException();
    }
    this.level = level;
    this.message = msg;
    this.sequenceNumber = globalSequenceNumber.getAndIncrement();
    this.instant = Instant.now();
    long id = Thread.currentThread().threadId();
    this.longThreadID = id;
    this.threadID = (int) id;
  }

  LogRecord(String loggerName, String methodName, Level level, String message,
            Throwable thrown) {
    this(level != null ? level : Level.INFO, message);
    this.loggerName = loggerName;
    this.sourceMethodName = methodName;
    this.thrown = thrown;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public void setLoggerName(String name) {
    this.loggerName = name;
  }

  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public void setResourceBundle(ResourceBundle bundle) {
    this.resourceBundle = bundle;
  }

  public String getResourceBundleName() {
    return resourceBundleName;
  }

  public void setResourceBundleName(String name) {
    this.resourceBundleName = name;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    if (level == null) {
      throw new NullPointerException();
    }
    this.level = level;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long seq) {
    this.sequenceNumber = seq;
  }

  public String getSourceClassName() {
    return sourceClassName;
  }

  public void setSourceClassName(String sourceClassName) {
    this.sourceClassName = sourceClassName;
  }

  public String getSourceMethodName() {
    return sourceMethodName;
  }

  public void setSourceMethodName(String sourceMethodName) {
    this.sourceMethodName = sourceMethodName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object[] getParameters() {
    return parameters;
  }

  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }

  public int getThreadID() {
    return threadID;
  }

  public void setThreadID(int threadID) {
    this.threadID = threadID;
    this.longThreadID = threadID;
  }

  public long getLongThreadID() {
    return longThreadID;
  }

  public LogRecord setLongThreadID(long longThreadID) {
    this.longThreadID = longThreadID;
    this.threadID = (int) longThreadID;
    return this;
  }

  public long getMillis() {
    return instant != null ? instant.toEpochMilli() : 0L;
  }

  public void setMillis(long millis) {
    this.instant = Instant.ofEpochMilli(millis);
  }

  public Instant getInstant() {
    return instant;
  }

  public void setInstant(Instant instant) {
    if (instant == null) {
      throw new NullPointerException();
    }
    this.instant = instant;
  }

  public Throwable getThrown() {
    return thrown;
  }

  public void setThrown(Throwable thrown) {
    this.thrown = thrown;
  }
}
