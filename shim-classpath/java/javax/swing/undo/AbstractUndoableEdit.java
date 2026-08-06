/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

import java.io.Serializable;

public class AbstractUndoableEdit implements UndoableEdit, Serializable {
  protected static final String UndoName = "Undo";
  protected static final String RedoName = "Redo";

  boolean hasBeenDone = true;
  boolean alive = true;

  public AbstractUndoableEdit() { }

  public void die() { alive = false; }

  public void undo() throws CannotUndoException {
    if (!canUndo()) {
      throw new CannotUndoException();
    }
    hasBeenDone = false;
  }

  public boolean canUndo() { return alive && hasBeenDone; }

  public void redo() throws CannotRedoException {
    if (!canRedo()) {
      throw new CannotRedoException();
    }
    hasBeenDone = true;
  }

  public boolean canRedo() { return alive && !hasBeenDone; }

  public boolean addEdit(UndoableEdit anEdit) { return false; }

  public boolean replaceEdit(UndoableEdit anEdit) { return false; }

  public boolean isSignificant() { return true; }

  public String getPresentationName() { return ""; }

  private String labelled(String prefix) {
    String n = getPresentationName();
    return n.isEmpty() ? prefix : prefix + " " + n;
  }

  public String getUndoPresentationName() { return labelled(UndoName); }

  public String getRedoPresentationName() { return labelled(RedoName); }

  public String toString() { return super.toString() + " hasBeenDone: " + hasBeenDone + " alive: " + alive; }
}
