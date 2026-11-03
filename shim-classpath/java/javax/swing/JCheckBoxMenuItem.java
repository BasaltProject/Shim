/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class JCheckBoxMenuItem extends JMenuItem implements SwingConstants, javax.accessibility.Accessible {
  public JCheckBoxMenuItem() { this(null, null, false); }

  public JCheckBoxMenuItem(Icon icon) { this(null, icon, false); }

  public JCheckBoxMenuItem(String text) { this(text, null, false); }

  public JCheckBoxMenuItem(Action a) {
    this();
    setAction(a);
  }

  public JCheckBoxMenuItem(String text, Icon icon) { this(text, icon, false); }

  public JCheckBoxMenuItem(String text, boolean b) { this(text, null, b); }

  public JCheckBoxMenuItem(String text, Icon icon, boolean b) {
    super(text, icon);
    setModel(new JToggleButton.ToggleButtonModel());
    setSelected(b);
    setFocusable(false);
  }

  public String getUIClassID() { return "CheckBoxMenuItemUI"; }

  public boolean getState() { return isSelected(); }

  public synchronized void setState(boolean b) { setSelected(b); }

  public Object[] getSelectedObjects() {
    if (!isSelected()) {
      return null;
    }
    Object[] selectedObjects = new Object[1];
    selectedObjects[0] = getText();
    return selectedObjects;
  }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJCheckBoxMenuItem();
    }
    return accessibleContext;
  }

  protected class AccessibleJCheckBoxMenuItem extends AccessibleJMenuItem {
    protected AccessibleJCheckBoxMenuItem() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.CHECK_BOX; }
  }
}
