/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import javax.swing.Action;

public abstract class TextAction extends javax.swing.AbstractAction {
  public TextAction(java.lang.String p0) {
    super(p0);
  }
  protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent p0) {
    if (p0 != null) {
      Object o = p0.getSource();
      if (o instanceof JTextComponent) {
        return (JTextComponent) o;
      }
    }
    return getFocusedComponent();
  }
  public static final javax.swing.Action[] augmentList(javax.swing.Action[] p0, javax.swing.Action[] p1) {
    java.util.Hashtable<String, Action> h = new java.util.Hashtable<>();
    for (Action a : p0) {
      String value = (String) a.getValue(Action.NAME);
      h.put((value != null ? value : ""), a);
    }
    for (Action a : p1) {
      String value = (String) a.getValue(Action.NAME);
      h.put((value != null ? value : ""), a);
    }
    Action[] actions = new Action[h.size()];
    int index = 0;
    for (java.util.Enumeration<Action> e = h.elements(); e.hasMoreElements();) {
      actions[index++] = e.nextElement();
    }
    return actions;
  }
  protected final javax.swing.text.JTextComponent getFocusedComponent() {
    return JTextComponent.getFocusedComponent();
  }
}
