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

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Throwable implements Serializable {
  private String message;
  private Object trace;
  private Throwable cause;
  private List<Throwable> suppressedExceptions;
  private boolean suppressionEnabled = true;

  public Throwable(String message, Throwable cause) {
    this.message = message;
    this.trace = trace(1);
    this.cause = cause;
  }

  public Throwable(String message) {
    this(message, null);
  }

  public Throwable(Throwable cause) {
    this(cause == null ? null : cause.toString(), cause);
  }

  public Throwable() {
    this(null, null);
  }

  protected Throwable(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
    this.message = message;
    this.cause = cause;
    this.suppressionEnabled = enableSuppression;
    if (writableStackTrace) {
      this.trace = trace(1);
    } else {
      this.trace = null;
    }
  }

  public Throwable getCause() {
    return cause;
  }

  public Throwable initCause(Throwable e) {
    if (cause == null) {
      cause = e;
      return this;
    } else {
      throw new IllegalStateException();
    }
  }

  public String getMessage() {
    return message;
  }

  public String getLocalizedMessage() {
    return getMessage();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getName());
    if (message != null) {
      sb.append(": ").append(message);
    }
    return sb.toString();
  }

  private static native Object trace(int skipCount);

  static native StackTraceElement[] resolveTrace(Object trace);

  private StackTraceElement[] resolveTrace() {
    if (! (trace instanceof StackTraceElement[])) {
      trace = resolveTrace(trace);
    }
    return (StackTraceElement[]) trace;
  }

  public StackTraceElement[] getStackTrace() {
    return resolveTrace();
  }

  public void setStackTrace(StackTraceElement[] trace) {
    this.trace = trace;
  }

  public void printStackTrace(PrintStream out) {
    StringBuilder sb = new StringBuilder();
    printStackTrace(sb, System.getProperty("line.separator"));
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace(PrintWriter out) {
    StringBuilder sb = new StringBuilder();
    printStackTrace(sb, System.getProperty("line.separator"));
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  private void printStackTrace(StringBuilder sb, String nl) {
    sb.append(toString()).append(nl);

    StackTraceElement[] trace = resolveTrace();
    for (int i = 0; i < trace.length; ++i) {
      sb.append("  at ").append(trace[i].toString()).append(nl);
    }

    if (suppressedExceptions != null) {
      for (Throwable se : suppressedExceptions) {
        sb.append("Suppressed: ");
        se.printStackTrace(sb, nl);
      }
    }

    Throwable printCause = getCause();
    if (printCause != null) {
      sb.append("caused by: ");
      printCause.printStackTrace(sb, nl);
    }
  }

  public Throwable fillInStackTrace() {
    trace = trace(0);
    return this;
  }
  
  public synchronized final void addSuppressed(Throwable exception) {
    if (exception == this) {
      throw new IllegalArgumentException("Self-suppression not permitted");
    }
    if (exception == null) {
      throw new NullPointerException("Cannot suppress a null exception");
    }
    if (!suppressionEnabled) {
      return;
    }
    if (suppressedExceptions == null) {
      suppressedExceptions = new ArrayList<Throwable>(2);
    }
    suppressedExceptions.add(exception);
  }

  public synchronized final Throwable[] getSuppressed() {
    if (suppressedExceptions == null || suppressedExceptions.isEmpty()) {
      return new Throwable[0];
    }
    return suppressedExceptions.toArray(new Throwable[suppressedExceptions.size()]);
  }
}
