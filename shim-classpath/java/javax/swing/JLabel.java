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
import java.util.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

public class JLabel extends javax.swing.JComponent implements javax.swing.SwingConstants, javax.accessibility.Accessible {
  private String text = "";
  private Icon icon;
  private Icon disabledIcon;
  private int mnemonic;
  private int mnemonicIndex = -1;
  private int verticalAlignment = CENTER;
  private int horizontalAlignment = LEADING;
  private int verticalTextPosition = CENTER;
  private int horizontalTextPosition = TRAILING;
  private int iconTextGap = 4;
  protected java.awt.Component labelFor;
  public JLabel() {
    this("", null, LEADING);
  }
  public JLabel(javax.swing.Icon p0) {
    this(null, p0, LEADING);
  }
  public JLabel(javax.swing.Icon p0, int p1) {
    this(null, p0, p1);
  }
  public JLabel(java.lang.String p0, javax.swing.Icon p1, int p2) {
    this.text = p0;
    this.icon = p1;
    this.horizontalAlignment = p2;
    updateUI();
  }
  public JLabel(java.lang.String p0, int p1) {
    this(p0, null, p1);
  }
  public JLabel(java.lang.String p0) {
    this(p0, null, LEADING);
  }
  protected java.lang.String paramString() {
    return super.paramString() + ",text=" + (text != null ? text : "");
  }
  public java.lang.String getText() {
    return text;
  }
  public javax.swing.Icon getIcon() {
    return icon;
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
  public void setUI(javax.swing.plaf.LabelUI p0) {
    super.setUI(p0);
  }
  public javax.swing.plaf.LabelUI getUI() {
    return (LabelUI) super.getUI();
  }
  public java.lang.String getUIClassID() {
    return "LabelUI";
  }
  public javax.swing.Icon getDisabledIcon() {
    return disabledIcon;
  }
  protected int checkVerticalKey(int p0, java.lang.String p1) {
    if ((p0 == TOP) || (p0 == CENTER) || (p0 == BOTTOM)) {
      return p0;
    }
    throw new IllegalArgumentException(p1);
  }
  protected int checkHorizontalKey(int p0, java.lang.String p1) {
    if ((p0 == LEFT) || (p0 == CENTER) || (p0 == RIGHT) || (p0 == LEADING) || (p0 == TRAILING)) {
      return p0;
    }
    throw new IllegalArgumentException(p1);
  }
  public void setDisplayedMnemonicIndex(int p0) throws java.lang.IllegalArgumentException {
    mnemonicIndex = p0;
  }
  public void setIcon(javax.swing.Icon p0) {
    Icon old = icon;
    icon = p0;
    firePropertyChange("icon", old, p0);
    revalidate();
  }
  public void setDisabledIcon(javax.swing.Icon p0) {
    Icon old = disabledIcon;
    disabledIcon = p0;
    firePropertyChange("disabledIcon", old, p0);
  }
  public void setIconTextGap(int p0) {
    iconTextGap = p0;
  }
  public int getVerticalAlignment() {
    return verticalAlignment;
  }
  public void setVerticalAlignment(int p0) {
    verticalAlignment = p0;
  }
  public int getHorizontalAlignment() {
    return horizontalAlignment;
  }
  public void setHorizontalAlignment(int p0) {
    horizontalAlignment = p0;
  }
  public int getVerticalTextPosition() {
    return verticalTextPosition;
  }
  public void setVerticalTextPosition(int p0) {
    verticalTextPosition = p0;
  }
  public int getHorizontalTextPosition() {
    return horizontalTextPosition;
  }
  public void setHorizontalTextPosition(int p0) {
    horizontalTextPosition = p0;
  }
  public int getIconTextGap() {
    return iconTextGap;
  }
  public int getDisplayedMnemonicIndex() {
    return mnemonicIndex;
  }
  public int getDisplayedMnemonic() {
    return mnemonic;
  }
  public void setDisplayedMnemonic(char p0) {
    mnemonic = Character.toUpperCase(p0);
  }
  public void setDisplayedMnemonic(int p0) {
    mnemonic = p0;
  }
  public java.awt.Component getLabelFor() {
    return labelFor;
  }
  public void setLabelFor(java.awt.Component p0) {
    labelFor = p0;
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    return null;
  }
  public boolean imageUpdate(java.awt.Image p0, int p1, int p2, int p3, int p4, int p5) {
    return false;
  }

  public Dimension getPreferredSize() { return isPreferredSizeSet() || ui != null ? super.getPreferredSize() : contentSize(); }

  private Dimension contentSize() {
    Font f = getFont();
    int w = 0;
    int h = 0;
    if (text != null && !text.isEmpty() && f != null) {
      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
      w = fm.stringWidth(text);
      h = fm.getHeight();
    }
    if (icon != null) {
      w += icon.getIconWidth() + (w > 0 ? iconTextGap : 0);
      h = Math.max(h, icon.getIconHeight());
    }
    Insets in = getInsets();
    return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
  }

  public Dimension getMinimumSize() { return isMinimumSizeSet() || ui != null ? super.getMinimumSize() : contentSize(); }

  public Dimension getMaximumSize() { return isMaximumSizeSet() || ui != null ? super.getMaximumSize() : contentSize(); }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (ui != null) {
      return;
    }
    Insets in = getInsets();
    int x = in.left;
    int y = in.top;
    if (icon != null) {
      icon.paintIcon(this, g, x, y);
      x += icon.getIconWidth() + iconTextGap;
    }
    if (text != null && getFont() != null) {
      g.setColor(getForeground() == null ? Color.BLACK : getForeground());
      g.setFont(getFont());
      g.drawString(text, x, y + g.getFontMetrics().getAscent());
    }
  }
}
