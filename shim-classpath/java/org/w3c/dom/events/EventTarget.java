/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.events;

public interface EventTarget {
  void addEventListener(String p0, EventListener p1, boolean p2);

  void removeEventListener(String p0, EventListener p1, boolean p2);

  boolean dispatchEvent(Event p0) throws EventException;
}
