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

public class JRadioButtonMenuItem extends JMenuItem implements javax.accessibility.Accessible {
  public JRadioButtonMenuItem() { this(null, null, false); }

  public JRadioButtonMenuItem(Icon icon) { this(null, icon, false); }

  public JRadioButtonMenuItem(String text) { this(text, null, false); }

  public JRadioButtonMenuItem(Action a) {
    this();
    setAction(a);
  }

  public JRadioButtonMenuItem(String text, Icon icon) { this(text, icon, false); }

  public JRadioButtonMenuItem(String text, boolean selected) {
    this(text);
    setSelected(selected);
  }

  public JRadioButtonMenuItem(Icon icon, boolean selected) {
    this(null, icon, selected);
  }

  public JRadioButtonMenuItem(String text, Icon icon, boolean selected) {
    super(text, icon);
    setModel(new JToggleButton.ToggleButtonModel());
    setSelected(selected);
    setFocusable(false);
  }

  public String getUIClassID() { return "RadioButtonMenuItemUI"; }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJRadioButtonMenuItem();
    }
    return accessibleContext;
  }

  protected class AccessibleJRadioButtonMenuItem extends AccessibleJMenuItem {
    protected AccessibleJRadioButtonMenuItem() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.RADIO_BUTTON; }
  }
}
