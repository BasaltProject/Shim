/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.event;

public interface EventQueue extends com.sun.jdi.Mirror {
  com.sun.jdi.event.EventSet remove() throws java.lang.InterruptedException;
  com.sun.jdi.event.EventSet remove(long l) throws java.lang.InterruptedException;
}
