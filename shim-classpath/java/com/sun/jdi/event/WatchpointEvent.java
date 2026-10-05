/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.event;

public interface WatchpointEvent extends com.sun.jdi.event.LocatableEvent {
  com.sun.jdi.Field field();
  com.sun.jdi.ObjectReference object();
  com.sun.jdi.Value valueCurrent();
}
