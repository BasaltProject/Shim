/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import javax.management.openmbean.CompositeData;

public class MonitorInfo extends LockInfo {
  private int stackDepth;
  private StackTraceElement stackFrame;

  public MonitorInfo(String className, int identityHashCode, int stackDepth, StackTraceElement stackFrame) {
    super(className, identityHashCode);
    if (stackDepth >= 0 && stackFrame == null) {
      throw new IllegalArgumentException("Parameter stackDepth is " + stackDepth + " but stackFrame is null");
    }
    if (stackDepth < 0 && stackFrame != null) {
      throw new IllegalArgumentException("Parameter stackDepth is " + stackDepth + " but stackFrame is not null");
    }
    this.stackDepth = stackDepth;
    this.stackFrame = stackFrame;
  }

  public int getLockedStackDepth() {
    return stackDepth;
  }

  public StackTraceElement getLockedStackFrame() {
    return stackFrame;
  }

  public static MonitorInfo from(CompositeData cd) {
    if (cd == null) {
      return null;
    }
    CompositeData frame = (CompositeData) cd.get("lockedStackFrame");
    StackTraceElement ste = null;
    if (frame != null) {
      ste = new StackTraceElement((String) frame.get("className"), (String) frame.get("methodName"),
          (String) frame.get("fileName"), ((Integer) frame.get("lineNumber")).intValue());
    }
    return new MonitorInfo((String) cd.get("className"), ((Integer) cd.get("identityHashCode")).intValue(),
        ((Integer) cd.get("lockedStackDepth")).intValue(), ste);
  }
}
