/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface FocusListener extends java.util.EventListener {
  void focusGained(java.awt.event.FocusEvent p0);
  void focusLost(java.awt.event.FocusEvent p0);
}
