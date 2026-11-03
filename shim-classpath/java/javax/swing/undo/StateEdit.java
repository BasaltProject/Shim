/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

import java.util.Enumeration;
import java.util.Hashtable;

public class StateEdit extends AbstractUndoableEdit {
  protected static final String RCSID = "$Id: StateEdit.java,v 1.6 1997/10/01 20:05:51 sandipc Exp $";

  protected StateEditable object;
  protected Hashtable<Object, Object> preState;
  protected Hashtable<Object, Object> postState;
  protected String undoRedoName;

  public StateEdit(StateEditable anObject) { super(); init(anObject, null); }

  public StateEdit(StateEditable anObject, String name) { super(); init(anObject, name); }

  protected void init(StateEditable anObject, String name) {
    this.object = anObject;
    this.preState = new Hashtable<Object, Object>(11);
    this.postState = null;
    this.undoRedoName = name;
    this.object.storeState(this.preState);
  }

  public void end() {
    this.postState = new Hashtable<Object, Object>(11);
    this.object.storeState(this.postState);
    this.removeRedundantState();
  }

  public void undo() {
    super.undo();
    this.object.restoreState(preState);
  }

  public void redo() {
    super.redo();
    this.object.restoreState(postState);
  }

  public String getPresentationName() { return this.undoRedoName; }

  protected void removeRedundantState() {
    java.util.Vector<Object> uselessKeys = new java.util.Vector<Object>();
    Enumeration<Object> myKeys = preState.keys();
    while (myKeys.hasMoreElements()) {
      Object myKey = myKeys.nextElement();
      if (postState.containsKey(myKey) && postState.get(myKey).equals(preState.get(myKey))) {
        uselessKeys.addElement(myKey);
      }
    }
    for (int i = uselessKeys.size() - 1; i >= 0; i--) {
      Object myKey = uselessKeys.elementAt(i);
      preState.remove(myKey);
      postState.remove(myKey);
    }
  }
}
