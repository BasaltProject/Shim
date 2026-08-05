/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface TreeExpansionListener extends java.util.EventListener {
  void treeExpanded(javax.swing.event.TreeExpansionEvent p0);
  void treeCollapsed(javax.swing.event.TreeExpansionEvent p0);
}
