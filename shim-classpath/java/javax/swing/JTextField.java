/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.accessibility.*;

public class JTextField extends javax.swing.text.JTextComponent implements javax.swing.SwingConstants {
  private String command;
  private int columnWidth;
  private PropertyChangeListener actionListener;
  private AccessibleContext accessibleContext;
  private static final Action[] defaultActions = {new NotifyAction()};
  private javax.swing.Action bp_Action;
  private int bp_HorizontalAlignment = LEADING;
  private javax.swing.BoundedRangeModel bp_HorizontalVisibility;
  private int bp_ScrollOffset;
  private int bp_Columns;
  private javax.swing.Action[] bp_Actions;
  private boolean bp_ValidateRoot;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  private java.awt.Dimension bp_PreferredSize;
  private final java.util.concurrent.CopyOnWriteArrayList<java.awt.event.ActionListener> bl_ActionListener = new java.util.concurrent.CopyOnWriteArrayList<>();
  private java.awt.event.ActionListener[] bp_ActionListeners;
  public static final java.lang.String notifyAction = "notify-field-accept";
  public JTextField(javax.swing.text.Document p0, java.lang.String p1, int p2) {
    if (p2 < 0) {
      throw new IllegalArgumentException("columns less than zero.");
    }
    bp_HorizontalVisibility = new DefaultBoundedRangeModel();
    bp_Columns = p2;
    if (p0 == null) {
      p0 = createDefaultModel();
    }
    setDocument(p0);
    if (p1 != null) {
      setText(p1);
    }
  }
  public JTextField(java.lang.String p0, int p1) {
    this(null, p0, p1);
  }
  public JTextField(int p0) {
    this(null, null, p0);
  }
  public JTextField(java.lang.String p0) {
    this(null, p0, 0);
  }
  public JTextField() {
    this(null, null, 0);
  }
  protected java.lang.String paramString() {
    String horizontalAlignmentString;
    if (bp_HorizontalAlignment == LEFT) {
      horizontalAlignmentString = "LEFT";
    } else if (bp_HorizontalAlignment == CENTER) {
      horizontalAlignmentString = "CENTER";
    } else if (bp_HorizontalAlignment == RIGHT) {
      horizontalAlignmentString = "RIGHT";
    } else if (bp_HorizontalAlignment == LEADING) {
      horizontalAlignmentString = "LEADING";
    } else if (bp_HorizontalAlignment == TRAILING) {
      horizontalAlignmentString = "TRAILING";
    } else {
      horizontalAlignmentString = "";
    }
    String commandString = (command != null ? command : "");
    return super.paramString() + ",columns=" + bp_Columns + ",columnWidth=" + columnWidth + ",command=" + commandString
        + ",horizontalAlignment=" + horizontalAlignmentString;
  }
  protected void configurePropertiesFromAction(javax.swing.Action p0) {
    setEnabled(p0 == null || p0.isEnabled());
    setToolTipText(p0 == null ? null : (String) p0.getValue(Action.SHORT_DESCRIPTION));
    setActionCommand(p0 == null ? null : (String) p0.getValue(Action.ACTION_COMMAND_KEY));
  }
  protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action p0) {
    return new PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName() == null || bp_Action != p0) {
          return;
        }
        actionPropertyChanged(p0, e.getPropertyName());
      }
    };
  }
  public void setAction(javax.swing.Action p0) {
    Action oldValue = bp_Action;
    if (bp_Action == null || !bp_Action.equals(p0)) {
      bp_Action = p0;
      if (oldValue != null) {
        removeActionListener(oldValue);
        if (actionListener != null) {
          oldValue.removePropertyChangeListener(actionListener);
        }
        actionListener = null;
      }
      configurePropertiesFromAction(bp_Action);
      if (bp_Action != null) {
        if (!bl_ActionListener.contains(bp_Action)) {
          addActionListener(bp_Action);
        }
        actionListener = createActionPropertyChangeListener(bp_Action);
        bp_Action.addPropertyChangeListener(actionListener);
      }
      firePropertyChange("action", oldValue, bp_Action);
    }
  }
  public int getHorizontalAlignment() { return bp_HorizontalAlignment; }
  public void setHorizontalAlignment(int p0) {
    if (p0 == bp_HorizontalAlignment) {
      return;
    }
    int oldValue = bp_HorizontalAlignment;
    if ((p0 == LEFT) || (p0 == CENTER) || (p0 == RIGHT) || (p0 == LEADING) || (p0 == TRAILING)) {
      bp_HorizontalAlignment = p0;
    } else {
      throw new IllegalArgumentException("horizontalAlignment");
    }
    firePropertyChange("horizontalAlignment", oldValue, bp_HorizontalAlignment);
    invalidate();
    repaint();
  }
  protected void actionPropertyChanged(javax.swing.Action p0, java.lang.String p1) {
    if (p1 == Action.ACTION_COMMAND_KEY) {
      setActionCommand((String) p0.getValue(Action.ACTION_COMMAND_KEY));
    } else if (p1 == "enabled") {
      setEnabled(p0.isEnabled());
    } else if (p1 == Action.SHORT_DESCRIPTION) {
      setToolTipText((String) p0.getValue(Action.SHORT_DESCRIPTION));
    }
  }
  protected void fireActionPerformed() {
    int modifiers = 0;
    AWTEvent currentEvent = EventQueue.getCurrentEvent();
    if (currentEvent instanceof InputEvent) {
      modifiers = ((InputEvent) currentEvent).getModifiers();
    } else if (currentEvent instanceof ActionEvent) {
      modifiers = ((ActionEvent) currentEvent).getModifiers();
    }
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (command != null) ? command : getText(),
        EventQueue.getMostRecentEventTime(), modifiers);
    for (int i = bl_ActionListener.size() - 1; i >= 0; i--) {
      bl_ActionListener.get(i).actionPerformed(e);
    }
  }
  protected javax.swing.text.Document createDefaultModel() {
    return new PlainDocument();
  }
  public void setDocument(javax.swing.text.Document p0) {
    if (p0 != getDocument()) {
      p0.putProperty("filterNewlines", Boolean.TRUE);
    }
    super.setDocument(p0);
  }
  protected int getColumnWidth() {
    if (columnWidth == 0) {
      FontMetrics metrics = getFontMetrics(getFont());
      columnWidth = metrics.charWidth('m');
    }
    return columnWidth;
  }
  public void postActionEvent() {
    fireActionPerformed();
  }
  public javax.swing.BoundedRangeModel getHorizontalVisibility() {
    return bp_HorizontalVisibility;
  }
  public int getScrollOffset() {
    return bp_HorizontalVisibility.getValue();
  }
  public void setScrollOffset(int p0) {
    bp_HorizontalVisibility.setValue(p0);
  }
  public int getColumns() { return bp_Columns; }
  public void setColumns(int p0) {
    int oldVal = bp_Columns;
    if (p0 < 0) {
      throw new IllegalArgumentException("columns less than zero.");
    }
    if (p0 != oldVal) {
      bp_Columns = p0;
      invalidate();
    }
  }
  public javax.swing.Action getAction() { return bp_Action; }
  public javax.swing.Action[] getActions() {
    return TextAction.augmentList(super.getActions(), defaultActions);
  }
  public boolean isValidateRoot() {
    return !(getParent() instanceof JViewport);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTextField();
    }
    return accessibleContext;
  }
  public void setFont(java.awt.Font p0) {
    super.setFont(p0);
    columnWidth = 0;
  }
  public java.awt.Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    if (bp_Columns != 0) {
      Insets insets = getInsets();
      size.width = bp_Columns * getColumnWidth() + insets.left + insets.right;
    }
    return size;
  }
  public void addActionListener(java.awt.event.ActionListener p0) { if (p0 != null) { bl_ActionListener.add(p0); } }
  public void setActionCommand(java.lang.String p0) {
    command = p0;
  }
  public void removeActionListener(java.awt.event.ActionListener p0) { bl_ActionListener.remove(p0); }
  public java.awt.event.ActionListener[] getActionListeners() { return bl_ActionListener.toArray(new java.awt.event.ActionListener[0]); }
  public java.lang.String getUIClassID() { return "TextFieldUI"; }
  public void scrollRectToVisible(java.awt.Rectangle p0) {
    int x = p0.x + p0.width;
    BoundedRangeModel vis = bp_HorizontalVisibility;
    if (x > vis.getValue() + vis.getExtent()) {
      vis.setValue(x - vis.getExtent());
    } else if (p0.x < vis.getValue()) {
      vis.setValue(p0.x);
    }
  }

  private static class NotifyAction extends TextAction {
    NotifyAction() { super(notifyAction); }

    public void actionPerformed(ActionEvent e) {
      JTextComponent target = getFocusedComponent();
      if (target instanceof JTextField) {
        ((JTextField) target).postActionEvent();
      }
    }

    public boolean isEnabled() {
      JTextComponent target = getFocusedComponent();
      if (target instanceof JTextField) {
        return ((JTextField) target).hasActionListener();
      }
      return false;
    }
  }

  boolean hasActionListener() { return !bl_ActionListener.isEmpty(); }

  protected class AccessibleJTextField extends AccessibleJTextComponent {
    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      states.add(AccessibleState.SINGLE_LINE);
      return states;
    }
  }

}
