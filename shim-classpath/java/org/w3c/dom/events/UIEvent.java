/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.events;

public interface UIEvent extends Event {
  org.w3c.dom.views.AbstractView getView();

  int getDetail();

  void initUIEvent(String p0, boolean p1, boolean p2, org.w3c.dom.views.AbstractView p3, int p4);
}
