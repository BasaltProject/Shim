/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

import java.awt.AWTEvent;
import java.util.EventListenerProxy;

public class AWTEventListenerProxy extends EventListenerProxy<AWTEventListener> implements AWTEventListener {
  private final long eventMask;

  public AWTEventListenerProxy(long eventMask, AWTEventListener listener) {
    super(listener);
    this.eventMask = eventMask;
  }

  public void eventDispatched(AWTEvent event) { getListener().eventDispatched(event); }

  public long getEventMask() { return eventMask; }
}
