/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

public class JToggleButton extends AbstractButton implements Accessible {
  private AccessibleContext accessibleContext;

  public JToggleButton() { this(null, null, false); }

  public JToggleButton(Icon icon) { this(null, icon, false); }

  public JToggleButton(Icon icon, boolean selected) { this(null, icon, selected); }

  public JToggleButton(String text) { this(text, null, false); }

  public JToggleButton(String text, boolean selected) { this(text, null, selected); }

  public JToggleButton(Action a) {
    this();
    setAction(a);
  }

  public JToggleButton(String text, Icon icon) { this(text, icon, false); }

  public JToggleButton(String text, Icon icon, boolean selected) {
    setModel(new ToggleButtonModel());
    model.setSelected(selected);
    init(text, icon);
  }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "ToggleButtonUI"; }

  boolean shouldUpdateSelectedStateFromAction() { return true; }

  public void requestFocus(FocusEvent.Cause cause) { super.requestFocus(cause); }

  public boolean requestFocusInWindow(FocusEvent.Cause cause) { return super.requestFocusInWindow(cause); }

  protected String paramString() { return super.paramString(); }

  public static class ToggleButtonModel extends DefaultButtonModel {
    public ToggleButtonModel() { }

    public boolean isSelected() { return (stateMask & SELECTED) != 0; }

    public void setSelected(boolean b) {
      ButtonGroup group = getGroup();
      if (group != null) {
        group.setSelected(this, b);
        b = group.isSelected(this);
      }
      if (isSelected() == b) {
        return;
      }
      if (b) {
        stateMask |= SELECTED;
      } else {
        stateMask &= ~SELECTED;
      }
      fireStateChanged();
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this,
          b ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
    }

    public void setPressed(boolean b) {
      if ((isPressed() == b) || !isEnabled()) {
        return;
      }
      if (b == false && isArmed()) {
        setSelected(!this.isSelected());
      }
      if (b) {
        stateMask |= PRESSED;
      } else {
        stateMask &= ~PRESSED;
      }
      fireStateChanged();
      if (!isPressed() && isArmed()) {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
          modifiers = ((InputEvent) currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
          modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(),
            EventQueue.getMostRecentEventTime(), modifiers));
      }
    }
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJToggleButton();
    }
    return accessibleContext;
  }

  protected class AccessibleJToggleButton extends AccessibleAbstractButton {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.TOGGLE_BUTTON; }
  }
}
