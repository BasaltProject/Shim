/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

import java.util.Vector;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

public class UndoableEditSupport {
  protected int updateLevel;
  protected CompoundEdit compoundEdit;
  protected Vector<UndoableEditListener> listeners;
  protected Object realSource;

  public UndoableEditSupport() {
    this(null);
  }

  public UndoableEditSupport(Object r) {
    realSource = r == null ? this : r;
    updateLevel = 0;
    compoundEdit = null;
    listeners = new Vector<>();
  }

  public synchronized void addUndoableEditListener(UndoableEditListener l) {
    listeners.addElement(l);
  }

  public synchronized void removeUndoableEditListener(UndoableEditListener l) {
    listeners.removeElement(l);
  }

  public synchronized UndoableEditListener[] getUndoableEditListeners() {
    return listeners.toArray(new UndoableEditListener[0]);
  }

  protected void _postEdit(UndoableEdit e) {
    UndoableEditEvent ev = new UndoableEditEvent(realSource, e);
    for (UndoableEditListener l : new Vector<>(listeners)) {
      l.undoableEditHappened(ev);
    }
  }

  public synchronized void postEdit(UndoableEdit e) {
    if (updateLevel == 0) {
      _postEdit(e);
    } else {
      compoundEdit.addEdit(e);
    }
  }

  public int getUpdateLevel() { return updateLevel; }

  public synchronized void beginUpdate() {
    if (updateLevel == 0) {
      compoundEdit = createCompoundEdit();
    }
    updateLevel++;
  }

  protected CompoundEdit createCompoundEdit() { return new CompoundEdit(); }

  public synchronized void endUpdate() {
    updateLevel--;
    if (updateLevel == 0) {
      compoundEdit.end();
      _postEdit(compoundEdit);
      compoundEdit = null;
    }
  }

  public String toString() {
    return super.toString() + " updateLevel: " + updateLevel + " listeners: " + listeners + " compoundEdit: "
        + compoundEdit;
  }
}
