/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class InvocationEvent extends java.awt.AWTEvent implements java.awt.ActiveEvent {
  private Exception exception;
  private Throwable throwable;
  private long when;
  private boolean dispatched;
  public static final int INVOCATION_FIRST = 1200;
  public static final int INVOCATION_DEFAULT = 1200;
  public static final int INVOCATION_LAST = 1200;
  protected java.lang.Runnable runnable;
  protected volatile java.lang.Object notifier;
  protected boolean catchExceptions;
  protected InvocationEvent(java.lang.Object p0, int p1, java.lang.Runnable p2, java.lang.Object p3, boolean p4) {
    super(p0, p1);
    this.runnable = p2;
    this.notifier = p3;
    this.catchExceptions = p4;
    this.when = System.currentTimeMillis();
  }
  public InvocationEvent(java.lang.Object p0, java.lang.Runnable p1, java.lang.Runnable p2, boolean p3) {
    super(p0, INVOCATION_DEFAULT);
    this.runnable = p1;
    this.notifier = p2;
    this.catchExceptions = p3;
    this.when = System.currentTimeMillis();
  }
  public InvocationEvent(java.lang.Object p0, java.lang.Runnable p1, java.lang.Object p2, boolean p3) {
    super(p0, INVOCATION_DEFAULT);
    this.runnable = p1;
    this.notifier = p2;
    this.catchExceptions = p3;
    this.when = System.currentTimeMillis();
  }
  public InvocationEvent(java.lang.Object p0, java.lang.Runnable p1) {
    super(p0, INVOCATION_DEFAULT);
    this.runnable = p1;
    this.when = System.currentTimeMillis();
  }
  public void dispatch() {
    try {
      if (catchExceptions) {
        try {
          runnable.run();
        } catch (Throwable t) {
          throwable = t;
          if (t instanceof Exception) {
            exception = (Exception) t;
          }
        }
      } else {
        runnable.run();
      }
    } finally {
      dispatched = true;
      if (notifier != null) {
        synchronized (notifier) {
          notifier.notifyAll();
        }
      }
    }
  }
  public java.lang.Exception getException() {
    return catchExceptions ? exception : null;
  }
  public java.lang.String paramString() {
    return (id == INVOCATION_DEFAULT ? "INVOCATION_DEFAULT" : "unknown type") + ",runnable=" + runnable + ",notifier=" + notifier + ",catchExceptions=" + catchExceptions + ",when=" + when;
  }
  public boolean isDispatched() {
    return dispatched;
  }
  public java.lang.Throwable getThrowable() {
    return catchExceptions ? throwable : null;
  }
  public long getWhen() {
    return when;
  }
}
