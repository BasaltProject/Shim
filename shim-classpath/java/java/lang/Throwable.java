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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Throwable implements Serializable {
  private static final long serialVersionUID = -3042686055658047285L;
  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("detailMessage", String.class),
    new ObjectStreamField("cause", Throwable.class),
    new ObjectStreamField("stackTrace", StackTraceElement[].class),
    new ObjectStreamField("suppressedExceptions", List.class)
  };
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
    String m = getLocalizedMessage();
    sb.append(getClass().getName());
    if (m != null) {
      sb.append(": ").append(m);
    }
    return sb.toString();
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    StackTraceElement[] st = trace == null ? new StackTraceElement[0] : resolveTrace();
    ObjectOutputStream.PutField fields = s.putFields();
    fields.put("detailMessage", message);
    fields.put("cause", cause == null ? this : cause);
    fields.put("stackTrace", st);
    fields.put("suppressedExceptions",
        suppressedExceptions == null ? java.util.Collections.emptyList() : suppressedExceptions);
    s.writeFields();
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField fields = s.readFields();
    message = (String) fields.get("detailMessage", null);
    Throwable c = (Throwable) fields.get("cause", this);
    cause = c == this ? null : c;
    StackTraceElement[] st = (StackTraceElement[]) fields.get("stackTrace", null);
    trace = st == null ? new StackTraceElement[0] : st.clone();
    List<Throwable> suppressed = (List<Throwable>) fields.get("suppressedExceptions", null);
    suppressedExceptions = suppressed == null || suppressed.isEmpty() ? null : new ArrayList<Throwable>(suppressed);
    suppressionEnabled = true;
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
    printStackTrace(sb, System.lineSeparator());
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace(PrintWriter out) {
    StringBuilder sb = new StringBuilder();
    printStackTrace(sb, System.lineSeparator());
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  private void printStackTrace(StringBuilder sb, String nl) {
    java.util.Set<Throwable> dejaVu = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Throwable,
        Boolean>());
    dejaVu.add(this);
    sb.append(this).append(nl);
    StackTraceElement[] trace = resolveTrace();
    for (StackTraceElement e : trace) {
      sb.append("\tat ").append(e).append(nl);
    }
    for (Throwable se : getSuppressed()) {
      se.printEnclosed(sb, nl, trace, "Suppressed: ", "\t", dejaVu);
    }
    Throwable ourCause = getCause();
    if (ourCause != null) {
      ourCause.printEnclosed(sb, nl, trace, "Caused by: ", "", dejaVu);
    }
  }

  private void printEnclosed(StringBuilder sb, String nl, StackTraceElement[] enclosingTrace, String caption,
                             String prefix, java.util.Set<Throwable> dejaVu) {
    if (dejaVu.contains(this)) {
      sb.append(prefix).append(caption).append("[CIRCULAR REFERENCE: ").append(this).append(']').append(nl);
      return;
    }
    dejaVu.add(this);
    StackTraceElement[] trace = resolveTrace();
    int m = trace.length - 1;
    int n = enclosingTrace.length - 1;
    while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
      m--;
      n--;
    }
    int framesInCommon = trace.length - 1 - m;
    sb.append(prefix).append(caption).append(this).append(nl);
    for (int i = 0; i <= m; i++) {
      sb.append(prefix).append("\tat ").append(trace[i]).append(nl);
    }
    if (framesInCommon != 0) {
      sb.append(prefix).append("\t... ").append(framesInCommon).append(" more").append(nl);
    }
    for (Throwable se : getSuppressed()) {
      se.printEnclosed(sb, nl, trace, "Suppressed: ", prefix + "\t", dejaVu);
    }
    Throwable ourCause = getCause();
    if (ourCause != null) {
      ourCause.printEnclosed(sb, nl, trace, "Caused by: ", prefix, dejaVu);
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
