/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

import java.util.Vector;

public class CompoundEdit extends AbstractUndoableEdit {
  boolean inProgress = true;
  protected Vector<UndoableEdit> edits = new Vector<>();

  public CompoundEdit() { }

  protected UndoableEdit lastEdit() { return edits.isEmpty() ? null : edits.lastElement(); }

  public void undo() throws CannotUndoException {
    super.undo();
    for (int i = edits.size() - 1; i >= 0; i--) {
      edits.get(i).undo();
    }
  }

  public void redo() throws CannotRedoException {
    super.redo();
    for (UndoableEdit e : edits) {
      e.redo();
    }
  }

  public void die() {
    for (int i = edits.size() - 1; i >= 0; i--) {
      edits.get(i).die();
    }
    super.die();
  }

  public boolean addEdit(UndoableEdit anEdit) {
    if (!inProgress) {
      return false;
    }
    UndoableEdit last = lastEdit();
    if (last == null) {
      edits.add(anEdit);
    } else if (!last.addEdit(anEdit)) {
      if (anEdit.replaceEdit(last)) {
        edits.remove(edits.size() - 1);
      }
      edits.add(anEdit);
    }
    return true;
  }

  public void end() { inProgress = false; }

  public boolean canUndo() { return !inProgress && super.canUndo(); }

  public boolean canRedo() { return !inProgress && super.canRedo(); }

  public boolean isInProgress() { return inProgress; }

  public boolean isSignificant() {
    for (UndoableEdit e : edits) {
      if (e.isSignificant()) {
        return true;
      }
    }
    return false;
  }

  public String getPresentationName() {
    UndoableEdit l = lastEdit();
    return l != null ? l.getPresentationName() : super.getPresentationName();
  }

  public String getUndoPresentationName() {
    UndoableEdit l = lastEdit();
    return l != null ? l.getUndoPresentationName() : super.getUndoPresentationName();
  }

  public String getRedoPresentationName() {
    UndoableEdit l = lastEdit();
    return l != null ? l.getRedoPresentationName() : super.getRedoPresentationName();
  }

  public String toString() { return super.toString() + " inProgress: " + inProgress + " edits: " + edits; }
}
