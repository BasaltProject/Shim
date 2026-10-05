/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.event;

public interface ModificationWatchpointEvent extends com.sun.jdi.event.WatchpointEvent {
  com.sun.jdi.Value valueToBe();
}
