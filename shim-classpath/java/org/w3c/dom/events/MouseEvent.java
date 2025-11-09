/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.events;

public interface MouseEvent extends UIEvent {
  int getScreenX();

  int getScreenY();

  int getClientX();

  int getClientY();

  boolean getCtrlKey();

  boolean getShiftKey();

  boolean getAltKey();

  boolean getMetaKey();

  short getButton();

  EventTarget getRelatedTarget();

  void initMouseEvent(String p0, boolean p1, boolean p2, org.w3c.dom.views.AbstractView p3, int p4, int p5, int p6,
      int p7, int p8, boolean p9, boolean p10, boolean p11, boolean p12, short p13, EventTarget p14);
}
