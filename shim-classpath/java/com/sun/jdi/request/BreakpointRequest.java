/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface BreakpointRequest extends com.sun.jdi.request.EventRequest, com.sun.jdi.Locatable {
  void addInstanceFilter(com.sun.jdi.ObjectReference objectReference);
  void addThreadFilter(com.sun.jdi.ThreadReference threadReference);
  com.sun.jdi.Location location();
}
