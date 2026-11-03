/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class ButtonGroup implements Serializable {
  protected Vector<AbstractButton> buttons = new Vector<>();

  private ButtonModel selection = null;

  public ButtonGroup() { }

  public void add(AbstractButton b) {
    if (b == null) {
      return;
    }
    buttons.addElement(b);
    if (b.isSelected()) {
      if (selection == null) {
        selection = b.getModel();
      } else {
        b.setSelected(false);
      }
    }
    b.getModel().setGroup(this);
  }

  public void remove(AbstractButton b) {
    if (b == null) {
      return;
    }
    buttons.removeElement(b);
    if (b.getModel() == selection) {
      selection = null;
    }
    b.getModel().setGroup(null);
  }

  public void clearSelection() {
    if (selection != null) {
      ButtonModel oldSelection = selection;
      selection = null;
      oldSelection.setSelected(false);
    }
  }

  public Enumeration<AbstractButton> getElements() { return buttons.elements(); }

  public ButtonModel getSelection() { return selection; }

  public void setSelected(ButtonModel m, boolean b) {
    if (b && m != null && m != selection) {
      ButtonModel oldSelection = selection;
      selection = m;
      if (oldSelection != null) {
        oldSelection.setSelected(false);
      }
      m.setSelected(true);
    }
  }

  public boolean isSelected(ButtonModel m) { return (m == selection); }

  public int getButtonCount() { return buttons == null ? 0 : buttons.size(); }
}
