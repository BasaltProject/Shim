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

public class JButton extends javax.swing.AbstractButton implements javax.accessibility.Accessible {
  private boolean defaultCapable = true;
  public JButton(java.lang.String p0, javax.swing.Icon p1) {
    setModel(new DefaultButtonModel());
    init(p0, p1);
  }
  public JButton(javax.swing.Action p0) {
    this(null, null);
    setAction(p0);
  }
  @java.beans.ConstructorProperties({"text"})
  public JButton(java.lang.String p0) {
    this(p0, null);
  }
  public JButton(javax.swing.Icon p0) {
    this(null, p0);
  }
  public JButton() {
    this(null, null);
  }
  protected java.lang.String paramString() {
    return super.paramString();
  }
  public boolean isDefaultButton() {
    JRootPane root = SwingUtilities.getRootPane(this);
    return root != null && root.getDefaultButton() == this;
  }
  public boolean isDefaultCapable() {
    return defaultCapable;
  }
  public void setDefaultCapable(boolean p0) {
    defaultCapable = p0;
  }
  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
  }
  public java.lang.String getUIClassID() {
    return "ButtonUI";
  }
  private javax.accessibility.AccessibleContext accessibleContext;
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJButton();
    }
    return accessibleContext;
  }
  public void removeNotify() {
    
  }

  protected class AccessibleJButton extends AccessibleAbstractButton {
    public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.PUSH_BUTTON; }
  }
}
