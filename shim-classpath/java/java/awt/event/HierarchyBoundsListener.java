/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

public interface HierarchyBoundsListener extends java.util.EventListener {
  void ancestorMoved(java.awt.event.HierarchyEvent p0);
  void ancestorResized(java.awt.event.HierarchyEvent p0);
}
