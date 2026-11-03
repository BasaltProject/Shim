/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface ListDataListener extends java.util.EventListener {
  void contentsChanged(javax.swing.event.ListDataEvent p0);
  void intervalAdded(javax.swing.event.ListDataEvent p0);
  void intervalRemoved(javax.swing.event.ListDataEvent p0);
}
