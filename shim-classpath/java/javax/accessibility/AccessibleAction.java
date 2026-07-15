/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleAction {
  java.lang.String TOGGLE_EXPAND = "toggleexpand";
  java.lang.String INCREMENT = "increment";
  java.lang.String DECREMENT = "decrement";
  java.lang.String CLICK = "click";
  java.lang.String TOGGLE_POPUP = "toggle popup";
  int getAccessibleActionCount();
  java.lang.String getAccessibleActionDescription(int p0);
  boolean doAccessibleAction(int p0);
}
