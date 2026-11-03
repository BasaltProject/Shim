/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface DocumentListener extends java.util.EventListener {
  void insertUpdate(javax.swing.event.DocumentEvent p0);
  void removeUpdate(javax.swing.event.DocumentEvent p0);
  void changedUpdate(javax.swing.event.DocumentEvent p0);
}
