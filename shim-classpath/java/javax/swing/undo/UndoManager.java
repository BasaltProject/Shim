/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

public class UndoManager extends CompoundEdit implements UndoableEditListener {
  // edits[0, cursor) are done, edits[cursor, size) are undone
  private int cursor;
  private int limit = 100;

  public UndoManager() { }

  public synchronized int getLimit() { return limit; }

  public synchronized void setLimit(int l) {
    if (!isInProgress()) {
      throw new RuntimeException("Attempt to call UndoManager.setLimit() after UndoManager.end() has been called");
    }
    limit = l;
    trimForLimit();
  }

  public synchronized void discardAllEdits() {
    for (UndoableEdit e : edits) {
      e.die();
    }
    edits.clear();
    cursor = 0;
  }

  protected void trimForLimit() {
    if (limit < 0) {
      return;
    }
    while (edits.size() > limit) {
      int dropFront = cursor - 1;
      int dropBack = edits.size() - 1 - cursor;
      if (dropFront >= dropBack) {
        trimEdits(0, 0);
      } else {
        trimEdits(edits.size() - 1, edits.size() - 1);
      }
    }
  }

  protected void trimEdits(int from, int to) {
    if (from > to) {
      return;
    }
    for (int i = to; i >= from; i--) {
      edits.get(i).die();
      edits.remove(i);
    }
    if (cursor > to) {
      cursor -= to - from + 1;
    } else if (cursor >= from) {
      cursor = from;
    }
  }

  protected UndoableEdit editToBeUndone() {
    for (int i = cursor - 1; i >= 0; i--) {
      if (edits.get(i).isSignificant()) {
        return edits.get(i);
      }
    }
    return null;
  }

  protected UndoableEdit editToBeRedone() {
    for (int i = cursor; i < edits.size(); i++) {
      if (edits.get(i).isSignificant()) {
        return edits.get(i);
      }
    }
    return null;
  }

  protected void undoTo(UndoableEdit edit) throws CannotUndoException {
    UndoableEdit e;
    do {
      e = edits.get(--cursor);
      e.undo();
    } while (e != edit);
  }

  protected void redoTo(UndoableEdit edit) throws CannotRedoException {
    UndoableEdit e;
    do {
      e = edits.get(cursor++);
      e.redo();
    } while (e != edit);
  }

  public synchronized void undoOrRedo() throws CannotRedoException, CannotUndoException {
    if (cursor == edits.size()) {
      undo();
    } else {
      redo();
    }
  }

  public synchronized boolean canUndoOrRedo() { return cursor == edits.size() ? canUndo() : canRedo(); }

  public synchronized void undo() throws CannotUndoException {
    if (!isInProgress()) {
      super.undo();
      return;
    }
    UndoableEdit e = editToBeUndone();
    if (e == null) {
      throw new CannotUndoException();
    }
    undoTo(e);
  }

  public synchronized boolean canUndo() {
    if (!isInProgress()) {
      return super.canUndo();
    }
    UndoableEdit e = editToBeUndone();
    return e != null && e.canUndo();
  }

  public synchronized void redo() throws CannotRedoException {
    if (!isInProgress()) {
      super.redo();
      return;
    }
    UndoableEdit e = editToBeRedone();
    if (e == null) {
      throw new CannotRedoException();
    }
    redoTo(e);
  }

  public synchronized boolean canRedo() {
    if (!isInProgress()) {
      return super.canRedo();
    }
    UndoableEdit e = editToBeRedone();
    return e != null && e.canRedo();
  }

  public synchronized boolean addEdit(UndoableEdit anEdit) {
    trimEdits(cursor, edits.size() - 1);
    boolean accepted = super.addEdit(anEdit);
    if (isInProgress()) {
      accepted = true;
    }
    cursor = edits.size();
    trimForLimit();
    return accepted;
  }

  public synchronized void end() {
    super.end();
    trimEdits(cursor, edits.size() - 1);
  }

  public synchronized String getUndoOrRedoPresentationName() {
    return cursor == edits.size() ? getUndoPresentationName() : getRedoPresentationName();
  }

  public synchronized String getUndoPresentationName() {
    if (!isInProgress()) {
      return super.getUndoPresentationName();
    }
    return canUndo() ? editToBeUndone().getUndoPresentationName() : AbstractUndoableEdit.UndoName;
  }

  public synchronized String getRedoPresentationName() {
    if (!isInProgress()) {
      return super.getRedoPresentationName();
    }
    return canRedo() ? editToBeRedone().getRedoPresentationName() : AbstractUndoableEdit.RedoName;
  }

  public void undoableEditHappened(UndoableEditEvent e) { addEdit(e.getEdit()); }

  public String toString() { return super.toString() + " limit: " + limit + " indexOfNextAdd: " + cursor; }
}
