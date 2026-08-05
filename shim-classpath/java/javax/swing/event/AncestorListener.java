/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface AncestorListener extends java.util.EventListener {
  void ancestorAdded(javax.swing.event.AncestorEvent p0);
  void ancestorRemoved(javax.swing.event.AncestorEvent p0);
  void ancestorMoved(javax.swing.event.AncestorEvent p0);
}
