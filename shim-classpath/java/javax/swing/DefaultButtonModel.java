/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultButtonModel implements ButtonModel, Serializable {
  public static final int ARMED = 1 << 0;
  public static final int SELECTED = 1 << 1;
  public static final int PRESSED = 1 << 2;
  public static final int ENABLED = 1 << 3;
  public static final int ROLLOVER = 1 << 4;

  protected int stateMask = ENABLED;
  protected String actionCommand = null;
  protected ButtonGroup group = null;
  protected int mnemonic = 0;
  protected transient ChangeEvent changeEvent = null;
  protected EventListenerList listenerList = new EventListenerList();

  public DefaultButtonModel() { }

  private boolean has(int bit) { return (stateMask & bit) != 0; }

  private void put(int bit, boolean on) {
    stateMask = on ? (stateMask | bit) : (stateMask & ~bit);
  }

  public boolean isArmed() { return has(ARMED); }

  public boolean isSelected() { return has(SELECTED); }

  public boolean isEnabled() { return has(ENABLED); }

  public boolean isPressed() { return has(PRESSED); }

  public boolean isRollover() { return has(ROLLOVER); }

  public void setArmed(boolean b) {
    if (isArmed() == b || !isEnabled()) {
      return;
    }
    put(ARMED, b);
    fireStateChanged();
  }

  public void setEnabled(boolean b) {
    if (isEnabled() == b) {
      return;
    }
    put(ENABLED, b);
    if (!b) {
      put(ARMED, false);
      put(PRESSED, false);
    }
    fireStateChanged();
  }

  public void setSelected(boolean b) {
    if (isSelected() == b) {
      return;
    }
    put(SELECTED, b);
    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this,
        b ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
    fireStateChanged();
  }

  public void setPressed(boolean b) {
    if (isPressed() == b || !isEnabled()) {
      return;
    }
    put(PRESSED, b);
    if (!isPressed() && isArmed()) {
      int modifiers = 0;
      fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(),
          System.currentTimeMillis(), modifiers));
    }
    fireStateChanged();
  }

  public void setRollover(boolean b) {
    if (isRollover() == b || !isEnabled()) {
      return;
    }
    put(ROLLOVER, b);
    fireStateChanged();
  }

  public void setMnemonic(int key) {
    mnemonic = key;
    fireStateChanged();
  }

  public int getMnemonic() { return mnemonic; }

  public void setActionCommand(String s) { actionCommand = s; }

  public String getActionCommand() { return actionCommand; }

  public void setGroup(ButtonGroup group) { this.group = group; }

  public ButtonGroup getGroup() { return group; }

  public Object[] getSelectedObjects() { return null; }

  public void addActionListener(ActionListener l) { listenerList.add(ActionListener.class, l); }

  public void removeActionListener(ActionListener l) { listenerList.remove(ActionListener.class, l); }

  public ActionListener[] getActionListeners() { return listenerList.getListeners(ActionListener.class); }

  public void addItemListener(ItemListener l) { listenerList.add(ItemListener.class, l); }

  public void removeItemListener(ItemListener l) { listenerList.remove(ItemListener.class, l); }

  public ItemListener[] getItemListeners() { return listenerList.getListeners(ItemListener.class); }

  public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

  public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    if (changeEvent == null) {
      changeEvent = new ChangeEvent(this);
    }
    for (ChangeListener l : getChangeListeners()) {
      l.stateChanged(changeEvent);
    }
  }

  protected void fireItemStateChanged(ItemEvent e) {
    for (ItemListener l : getItemListeners()) {
      l.itemStateChanged(e);
    }
  }

  protected void fireActionPerformed(ActionEvent e) {
    for (ActionListener l : getActionListeners()) {
      l.actionPerformed(e);
    }
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }
}
