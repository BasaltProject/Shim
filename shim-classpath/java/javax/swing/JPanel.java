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

public class JPanel extends javax.swing.JComponent implements javax.accessibility.Accessible {
  public JPanel() {
    this(new FlowLayout(), true);
  }
  public JPanel(boolean p0) {
    this(new FlowLayout(), p0);
  }
  public JPanel(java.awt.LayoutManager p0) {
    this(p0, true);
  }
  public JPanel(java.awt.LayoutManager p0, boolean p1) {
    setLayout(p0);
    setDoubleBuffered(p1);
    setUIProperty("opaque", Boolean.TRUE);
    updateUI();
  }
  protected java.lang.String paramString() {
    return super.paramString();
  }
  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
  }
  public void setUI(javax.swing.plaf.PanelUI p0) {
    super.setUI(p0);
  }
  public javax.swing.plaf.PanelUI getUI() {
    return (PanelUI) super.getUI();
  }
  public java.lang.String getUIClassID() {
    return "PanelUI";
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    return null;
  }
}
