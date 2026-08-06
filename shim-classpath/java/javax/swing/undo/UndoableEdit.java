/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

public interface UndoableEdit {
  void redo() throws javax.swing.undo.CannotRedoException;
  void undo() throws javax.swing.undo.CannotUndoException;
  java.lang.String getPresentationName();
  boolean isSignificant();
  java.lang.String getUndoPresentationName();
  java.lang.String getRedoPresentationName();
  void die();
  boolean replaceEdit(javax.swing.undo.UndoableEdit p0);
  boolean canUndo();
  boolean canRedo();
  boolean addEdit(javax.swing.undo.UndoableEdit p0);
}
