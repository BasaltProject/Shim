/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.dnd;

public interface DropTargetListener extends java.util.EventListener {
  void dragEnter(java.awt.dnd.DropTargetDragEvent p0);
  void dragOver(java.awt.dnd.DropTargetDragEvent p0);
  void dropActionChanged(java.awt.dnd.DropTargetDragEvent p0);
  void dragExit(java.awt.dnd.DropTargetEvent p0);
  void drop(java.awt.dnd.DropTargetDropEvent p0);
}
