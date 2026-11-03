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

public class JRadioButton extends JToggleButton implements javax.accessibility.Accessible {
  private AccessibleContext accessibleContext;

  public JRadioButton() { this(null, null, false); }

  public JRadioButton(Icon icon) { this(null, icon, false); }

  public JRadioButton(Action a) {
    this();
    setAction(a);
  }

  public JRadioButton(Icon icon, boolean selected) { this(null, icon, selected); }

  public JRadioButton(String text) { this(text, null, false); }

  public JRadioButton(String text, boolean selected) { this(text, null, selected); }

  public JRadioButton(String text, Icon icon) { this(text, icon, false); }

  public JRadioButton(String text, Icon icon, boolean selected) {
    super(text, icon, selected);
    setBorderPainted(false);
    setHorizontalAlignment(LEADING);
  }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "RadioButtonUI"; }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJRadioButton();
    }
    return accessibleContext;
  }

  protected class AccessibleJRadioButton extends AccessibleJToggleButton {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.RADIO_BUTTON; }
  }
}
