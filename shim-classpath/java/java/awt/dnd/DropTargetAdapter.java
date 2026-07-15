/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.dnd;

public abstract class DropTargetAdapter implements java.awt.dnd.DropTargetListener {
  protected DropTargetAdapter() {
  }
  public void dragEnter(java.awt.dnd.DropTargetDragEvent p0) { }
  public void dragOver(java.awt.dnd.DropTargetDragEvent p0) { }
  public void dropActionChanged(java.awt.dnd.DropTargetDragEvent p0) { }
  public void dragExit(java.awt.dnd.DropTargetEvent p0) { }
}
