/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface StepRequest extends com.sun.jdi.request.EventRequest {
  int STEP_INTO = 1;
  int STEP_OVER = 2;
  int STEP_OUT = 3;
  int STEP_MIN = -1;
  int STEP_LINE = -2;

  void addClassExclusionFilter(java.lang.String string);
  void addClassFilter(java.lang.String string);
  void addClassFilter(com.sun.jdi.ReferenceType referenceType);
  void addInstanceFilter(com.sun.jdi.ObjectReference objectReference);
  int depth();
  int size();
  com.sun.jdi.ThreadReference thread();
}
