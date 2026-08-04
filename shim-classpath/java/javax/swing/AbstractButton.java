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
import java.beans.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

public abstract class AbstractButton extends javax.swing.JComponent implements java.awt.ItemSelectable, javax.swing.SwingConstants {
  private String text = "";
  private Icon icon;
  private Icon pressedIcon;
  private Icon selectedIcon;
  private Icon disabledIcon;
  private Icon disabledSelectedIcon;
  private Icon rolloverIcon;
  private Icon rolloverSelectedIcon;
  private Insets margin;
  private int mnemonicKey;
  private int mnemonicIndex = -1;
  private boolean borderPaintedFlag = true;
  private boolean focusPaintedFlag = true;
  private boolean contentFilled = true;
  private boolean rolloverOn;
  private boolean hideActionText;
  private int iconGap = 4;
  private int vAlign = CENTER;
  private int hAlign = CENTER;
  private int vTextPos = CENTER;
  private int hTextPos = TRAILING;
  private long multiClick = 200;
  private Action action;
  public static final java.lang.String MODEL_CHANGED_PROPERTY = "model";
  public static final java.lang.String TEXT_CHANGED_PROPERTY = "text";
  public static final java.lang.String MNEMONIC_CHANGED_PROPERTY = "mnemonic";
  public static final java.lang.String MARGIN_CHANGED_PROPERTY = "margin";
  public static final java.lang.String VERTICAL_ALIGNMENT_CHANGED_PROPERTY = "verticalAlignment";
  public static final java.lang.String HORIZONTAL_ALIGNMENT_CHANGED_PROPERTY = "horizontalAlignment";
  public static final java.lang.String VERTICAL_TEXT_POSITION_CHANGED_PROPERTY = "verticalTextPosition";
  public static final java.lang.String HORIZONTAL_TEXT_POSITION_CHANGED_PROPERTY = "horizontalTextPosition";
  public static final java.lang.String BORDER_PAINTED_CHANGED_PROPERTY = "borderPainted";
  public static final java.lang.String FOCUS_PAINTED_CHANGED_PROPERTY = "focusPainted";
  public static final java.lang.String ROLLOVER_ENABLED_CHANGED_PROPERTY = "rolloverEnabled";
  public static final java.lang.String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled";
  public static final java.lang.String ICON_CHANGED_PROPERTY = "icon";
  public static final java.lang.String PRESSED_ICON_CHANGED_PROPERTY = "pressedIcon";
  public static final java.lang.String SELECTED_ICON_CHANGED_PROPERTY = "selectedIcon";
  public static final java.lang.String ROLLOVER_ICON_CHANGED_PROPERTY = "rolloverIcon";
  public static final java.lang.String ROLLOVER_SELECTED_ICON_CHANGED_PROPERTY = "rolloverSelectedIcon";
  public static final java.lang.String DISABLED_ICON_CHANGED_PROPERTY = "disabledIcon";
  public static final java.lang.String DISABLED_SELECTED_ICON_CHANGED_PROPERTY = "disabledSelectedIcon";
  protected javax.swing.ButtonModel model;
  protected javax.swing.event.ChangeListener changeListener;
  protected java.awt.event.ActionListener actionListener;
  protected java.awt.event.ItemListener itemListener;
  protected javax.swing.event.ChangeEvent changeEvent;
  protected AbstractButton() {
    setModel(new DefaultButtonModel());
  }
  protected void init(java.lang.String p0, javax.swing.Icon p1) {
    if (p0 != null) {
      setText(p0);
    }
    if (p1 != null) {
      setIcon(p1);
    }
    updateUI();
  }
  protected java.lang.String paramString() {
    return super.paramString() + ",text=" + (text == null ? "" : text);
  }
  public java.lang.String getLabel() {
    return getText();
  }
  public java.lang.String getText() {
    return text;
  }
  public javax.swing.Icon getIcon() {
    return icon;
  }
  public javax.swing.Action getAction() {
    return action;
  }
  public void setText(java.lang.String p0) {
    String old = text;
    text = p0;
    firePropertyChange("text", old, p0);
    revalidate();
  }
  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
  }
  protected void paintBorder(java.awt.Graphics p0) {
    if (isBorderPainted()) {
      super.paintBorder(p0);
    }
  }
  public void setUI(javax.swing.plaf.ButtonUI p0) {
    super.setUI(p0);
  }
  public javax.swing.plaf.ButtonUI getUI() {
    return (ButtonUI) ui;
  }
  public boolean isSelected() {
    return model.isSelected();
  }
  public boolean isBorderPainted() {
    return borderPaintedFlag;
  }
  public void setSelected(boolean p0) {
    model.setSelected(p0);
  }
  public void setBorderPainted(boolean p0) {
    borderPaintedFlag = p0;
  }
  public void setMargin(java.awt.Insets p0) {
    margin = p0;
  }
  public java.awt.Insets getMargin() {
    return margin;
  }
  public int getMnemonic() {
    return mnemonicKey;
  }
  public void doClick(int p0) {
    model.setArmed(true);
    model.setPressed(true);
    model.setPressed(false);
    model.setArmed(false);
  }
  public void doClick() {
    doClick(68);
  }
  public javax.swing.ButtonModel getModel() {
    return model;
  }
  public void setRolloverEnabled(boolean p0) {
    rolloverOn = p0;
  }
  public javax.swing.Icon getDisabledIcon() {
    return disabledIcon;
  }
  public javax.swing.Icon getSelectedIcon() {
    return selectedIcon;
  }
  public javax.swing.Icon getDisabledSelectedIcon() {
    return disabledSelectedIcon;
  }
  protected int checkVerticalKey(int p0, java.lang.String p1) {
    if (p0 == TOP || p0 == CENTER || p0 == BOTTOM) {
      return p0;
    }
    throw new IllegalArgumentException(p1);
  }
  protected int checkHorizontalKey(int p0, java.lang.String p1) {
    if (p0 == LEFT || p0 == CENTER || p0 == RIGHT || p0 == LEADING || p0 == TRAILING) {
      return p0;
    }
    throw new IllegalArgumentException(p1);
  }
  public boolean isRolloverEnabled() {
    return rolloverOn;
  }
  protected void configurePropertiesFromAction(javax.swing.Action p0) {
    if (p0 != null) {
      setText(!hideActionText ? (String) p0.getValue(Action.NAME) : null);
      setEnabled(p0.isEnabled());
      setToolTipText((String) p0.getValue(Action.SHORT_DESCRIPTION));
    }
  }
  protected java.beans.PropertyChangeListener createActionPropertyChangeListener(javax.swing.Action p0) {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        actionPropertyChanged(p0, e.getPropertyName());
      }
    };
  }
  public boolean getHideActionText() {
    return hideActionText;
  }
  public void setHideActionText(boolean p0) {
    hideActionText = p0;
  }
  public void setDisplayedMnemonicIndex(int p0) throws java.lang.IllegalArgumentException {
    mnemonicIndex = p0;
  }
  public void setMnemonic(int p0) {
    mnemonicKey = p0;
    model.setMnemonic(p0);
  }
  public void setMnemonic(char p0) {
    setMnemonic((int) Character.toUpperCase(p0));
  }
  public void setIcon(javax.swing.Icon p0) {
    Icon old = icon;
    icon = p0;
    firePropertyChange("icon", old, p0);
    revalidate();
  }
  public void removeChangeListener(javax.swing.event.ChangeListener p0) {
    listenerList.remove(ChangeListener.class, p0);
  }
  protected javax.swing.event.ChangeListener createChangeListener() {
    return new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fireStateChanged();
        repaint();
      }
    };
  }
  protected java.awt.event.ActionListener createActionListener() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireActionPerformed(e);
      }
    };
  }
  protected java.awt.event.ItemListener createItemListener() {
    return new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fireItemStateChanged(e);
      }
    };
  }
  public void addChangeListener(javax.swing.event.ChangeListener p0) {
    listenerList.add(ChangeListener.class, p0);
  }
  public void setDisabledIcon(javax.swing.Icon p0) {
    disabledIcon = p0;
  }
  public void setDisabledSelectedIcon(javax.swing.Icon p0) {
    disabledSelectedIcon = p0;
  }
  public void setAction(javax.swing.Action p0) {
    Action old = action;
    if (old != null) {
      removeActionListener(old);
    }
    action = p0;
    if (p0 != null) {
      addActionListener(p0);
      configurePropertiesFromAction(p0);
    }
    firePropertyChange("action", old, p0);
  }
  public javax.swing.Icon getPressedIcon() {
    return pressedIcon;
  }
  public javax.swing.Icon getRolloverSelectedIcon() {
    return rolloverSelectedIcon;
  }
  public javax.swing.Icon getRolloverIcon() {
    return rolloverIcon;
  }
  public void setIconTextGap(int p0) {
    iconGap = p0;
  }
  public void setContentAreaFilled(boolean p0) {
    contentFilled = p0;
  }
  public void setPressedIcon(javax.swing.Icon p0) {
    pressedIcon = p0;
  }
  public void setSelectedIcon(javax.swing.Icon p0) {
    selectedIcon = p0;
  }
  public void setRolloverIcon(javax.swing.Icon p0) {
    rolloverIcon = p0;
  }
  public void setRolloverSelectedIcon(javax.swing.Icon p0) {
    rolloverSelectedIcon = p0;
  }
  public int getVerticalAlignment() {
    return vAlign;
  }
  public void setVerticalAlignment(int p0) {
    vAlign = p0;
  }
  public int getHorizontalAlignment() {
    return hAlign;
  }
  public void setHorizontalAlignment(int p0) {
    hAlign = p0;
  }
  public int getVerticalTextPosition() {
    return vTextPos;
  }
  public void setVerticalTextPosition(int p0) {
    vTextPos = p0;
  }
  public int getHorizontalTextPosition() {
    return hTextPos;
  }
  public void setHorizontalTextPosition(int p0) {
    hTextPos = p0;
  }
  public int getIconTextGap() {
    return iconGap;
  }
  protected void actionPropertyChanged(javax.swing.Action p0, java.lang.String p1) {
    if (p1 == null) {
      return;
    }
    if (p1.equals(Action.NAME)) {
      setText((String) p0.getValue(Action.NAME));
    } else if (p1.equals("enabled")) {
      setEnabled(p0.isEnabled());
    }
  }
  public boolean isFocusPainted() {
    return focusPaintedFlag;
  }
  public void setFocusPainted(boolean p0) {
    focusPaintedFlag = p0;
  }
  public boolean isContentAreaFilled() {
    return contentFilled;
  }
  public int getDisplayedMnemonicIndex() {
    return mnemonicIndex;
  }
  public void setMultiClickThreshhold(long p0) {
    multiClick = p0;
  }
  public long getMultiClickThreshhold() {
    return multiClick;
  }
  public void setModel(javax.swing.ButtonModel p0) {
    ButtonModel old = model;
    if (old != null) {
      old.removeChangeListener(changeListener);
      old.removeActionListener(actionListener);
      old.removeItemListener(itemListener);
      changeListener = null;
      actionListener = null;
      itemListener = null;
    }
    model = p0;
    if (p0 != null) {
      changeListener = createChangeListener();
      actionListener = createActionListener();
      itemListener = createItemListener();
      p0.addChangeListener(changeListener);
      p0.addActionListener(actionListener);
      p0.addItemListener(itemListener);
    }
    firePropertyChange("model", old, p0);
  }
  public javax.swing.event.ChangeListener[] getChangeListeners() {
    return listenerList.getListeners(ChangeListener.class);
  }
  protected void fireStateChanged() {
    ChangeEvent ev = new ChangeEvent(this);
    for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
      l.stateChanged(ev);
    }
  }
  protected void fireActionPerformed(java.awt.event.ActionEvent p0) {
    String cmd = p0.getActionCommand() != null ? p0.getActionCommand() : getActionCommand();
    ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, cmd, p0.getWhen(), p0.getModifiers());
    for (ActionListener l : listenerList.getListeners(ActionListener.class)) {
      l.actionPerformed(ev);
    }
  }
  protected void fireItemStateChanged(java.awt.event.ItemEvent p0) {
    ItemEvent ev = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, p0.getStateChange());
    for (ItemListener l : listenerList.getListeners(ItemListener.class)) {
      l.itemStateChanged(ev);
    }
  }
  public void setLayout(java.awt.LayoutManager p0) {
    super.setLayout(p0);
  }
  protected void addImpl(java.awt.Component p0, java.lang.Object p1, int p2) {
    super.addImpl(p0, p1, p2);
  }
  public void removeNotify() {
    
  }
  public void setEnabled(boolean p0) {
    if (!p0 && model.isRollover()) {
      model.setRollover(false);
    }
    super.setEnabled(p0);
    model.setEnabled(p0);
  }
  public boolean imageUpdate(java.awt.Image p0, int p1, int p2, int p3, int p4, int p5) {
    return false;
  }
  public void setLabel(java.lang.String p0) { setText(p0); }
  public java.lang.String getActionCommand() {
    String ac = model.getActionCommand();
    if (ac == null) {
      ac = getText();
    }
    return ac;
  }
  public void addActionListener(java.awt.event.ActionListener p0) {
    listenerList.add(ActionListener.class, p0);
  }
  public void setActionCommand(java.lang.String p0) {
    model.setActionCommand(p0);
  }
  public void removeActionListener(java.awt.event.ActionListener p0) {
    listenerList.remove(ActionListener.class, p0);
  }
  public java.awt.event.ActionListener[] getActionListeners() {
    return listenerList.getListeners(ActionListener.class);
  }
  public void addItemListener(java.awt.event.ItemListener p0) {
    listenerList.add(ItemListener.class, p0);
  }
  public java.lang.Object[] getSelectedObjects() {
    return isSelected() ? new Object[] {getText()} : null;
  }
  public void removeItemListener(java.awt.event.ItemListener p0) {
    listenerList.remove(ItemListener.class, p0);
  }
  public java.awt.event.ItemListener[] getItemListeners() {
    return listenerList.getListeners(ItemListener.class);
  }

  protected void setUIProperty(String propertyName, Object value) {
    if (propertyName == "borderPainted") {
      setBorderPainted(((Boolean) value).booleanValue());
    } else if (propertyName == "rolloverEnabled") {
      setRolloverEnabled(((Boolean) value).booleanValue());
    } else if (propertyName == "iconTextGap") {
      setIconTextGap(((Number) value).intValue());
    } else if (propertyName == "contentAreaFilled") {
      setContentAreaFilled(((Boolean) value).booleanValue());
    } else if (propertyName == "focusPainted") {
      setFocusPainted(((Boolean) value).booleanValue());
    } else {
      super.setUIProperty(propertyName, value);
    }
  }

  boolean shouldUpdateSelectedStateFromAction() { return false; }

  public abstract class AccessibleAbstractButton extends AccessibleJComponent
      implements javax.accessibility.AccessibleAction, javax.accessibility.AccessibleValue {
    protected AccessibleAbstractButton() { }

    public String getAccessibleName() {
      String name = accessibleName;
      if (name == null) {
        name = (String) getClientProperty("AccessibleName");
      }
      if (name == null) {
        name = AbstractButton.this.getText();
      }
      if (name == null) {
        name = super.getAccessibleName();
      }
      return name;
    }

    public javax.accessibility.AccessibleStateSet getAccessibleStateSet() {
      javax.accessibility.AccessibleStateSet states = super.getAccessibleStateSet();
      if (getModel().isArmed()) {
        states.add(javax.accessibility.AccessibleState.ARMED);
      }
      if (isFocusOwner()) {
        states.add(javax.accessibility.AccessibleState.FOCUSED);
      }
      if (getModel().isPressed()) {
        states.add(javax.accessibility.AccessibleState.PRESSED);
      }
      if (isSelected()) {
        states.add(javax.accessibility.AccessibleState.CHECKED);
      }
      return states;
    }

    public javax.accessibility.AccessibleAction getAccessibleAction() { return this; }

    public javax.accessibility.AccessibleValue getAccessibleValue() { return this; }

    public int getAccessibleActionCount() { return 1; }

    public String getAccessibleActionDescription(int i) { return i == 0 ? UIManager.getString("AbstractButton.clickText") : null; }

    public boolean doAccessibleAction(int i) {
      if (i == 0) {
        doClick(0);
        return true;
      }
      return false;
    }

    public Number getCurrentAccessibleValue() { return isSelected() ? Integer.valueOf(1) : Integer.valueOf(0); }

    public boolean setCurrentAccessibleValue(Number n) {
      if (n == null) {
        return false;
      }
      setSelected(n.intValue() != 0);
      return true;
    }

    public Number getMinimumAccessibleValue() { return Integer.valueOf(0); }

    public Number getMaximumAccessibleValue() { return Integer.valueOf(1); }
  }

  public java.awt.Dimension getPreferredSize() { return isPreferredSizeSet() || ui != null ? super.getPreferredSize() : contentSize(); }

  private java.awt.Dimension contentSize() {
    java.awt.Insets in = getInsets();
    int w = 0;
    int h = 0;
    java.awt.Font f = getFont();
    String t = getText();
    if (t != null && !t.isEmpty() && f != null) {
      java.awt.FontMetrics fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(f);
      w = fm.stringWidth(t);
      h = fm.getHeight();
    }
    Icon ic = getIcon();
    int iw = 0;
    int ih = 0;
    if (ic != null) {
      iw = ic.getIconWidth();
      ih = ic.getIconHeight();
    } else if (getUIClassID().equals("CheckBoxUI") || getUIClassID().equals("RadioButtonUI")) {
      iw = 13;
      ih = 13;
    }
    int gap = (iw > 0 && w > 0) ? iconTextGapOf() : 0;
    int rw;
    int rh;
    if (getHorizontalTextPosition() == CENTER && iw > 0 && w > 0) {
      rw = Math.max(w, iw);
      rh = h + ih + gap;
    } else {
      rw = w + iw + gap;
      rh = Math.max(h, ih);
    }
    return new java.awt.Dimension(rw + in.left + in.right, rh + in.top + in.bottom);
  }

  private int iconTextGapOf() { return getIconTextGap(); }

  public java.awt.Dimension getMinimumSize() { return isMinimumSizeSet() || ui != null ? super.getMinimumSize() : contentSize(); }

  public java.awt.Dimension getMaximumSize() { return isMaximumSizeSet() || ui != null ? super.getMaximumSize() : contentSize(); }
}
