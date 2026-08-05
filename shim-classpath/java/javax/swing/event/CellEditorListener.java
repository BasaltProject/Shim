/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface CellEditorListener extends java.util.EventListener {
  void editingStopped(javax.swing.event.ChangeEvent p0);
  void editingCanceled(javax.swing.event.ChangeEvent p0);
}
