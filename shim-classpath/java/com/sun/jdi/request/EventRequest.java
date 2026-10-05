/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface EventRequest extends com.sun.jdi.Mirror {
  int SUSPEND_NONE = 0;
  int SUSPEND_EVENT_THREAD = 1;
  int SUSPEND_ALL = 2;

  void addCountFilter(int i);
  void disable();
  void enable();
  java.lang.Object getProperty(java.lang.Object object);
  boolean isEnabled();
  void putProperty(java.lang.Object object, java.lang.Object object2);
  void setEnabled(boolean flag);
  void setSuspendPolicy(int i);
  int suspendPolicy();
}
