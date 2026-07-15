/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface InputMethodListener extends java.util.EventListener {
  void inputMethodTextChanged(java.awt.event.InputMethodEvent p0);
  void caretPositionChanged(java.awt.event.InputMethodEvent p0);
}
