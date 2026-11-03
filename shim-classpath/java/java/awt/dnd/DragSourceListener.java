/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.dnd;

public interface DragSourceListener extends java.util.EventListener {
  void dragDropEnd(java.awt.dnd.DragSourceDropEvent p0);
  void dragEnter(java.awt.dnd.DragSourceDragEvent p0);
  void dragOver(java.awt.dnd.DragSourceDragEvent p0);
  void dropActionChanged(java.awt.dnd.DragSourceDragEvent p0);
  void dragExit(java.awt.dnd.DragSourceEvent p0);
}
