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

public class JCheckBox extends JToggleButton implements javax.accessibility.Accessible {
  public static final String BORDER_PAINTED_FLAT_CHANGED_PROPERTY = "borderPaintedFlat";

  private boolean flat = false;
  private AccessibleContext accessibleContext;

  public JCheckBox() { this(null, null, false); }

  public JCheckBox(Icon icon) { this(null, icon, false); }

  public JCheckBox(Icon icon, boolean selected) { this(null, icon, selected); }

  public JCheckBox(String text) { this(text, null, false); }

  public JCheckBox(Action a) {
    this();
    setAction(a);
  }

  public JCheckBox(String text, boolean selected) { this(text, null, selected); }

  public JCheckBox(String text, Icon icon) { this(text, icon, false); }

  public JCheckBox(String text, Icon icon, boolean selected) {
    super(text, icon, selected);
    setUIProperty("borderPainted", Boolean.FALSE);
    setHorizontalAlignment(LEADING);
  }

  public void setBorderPaintedFlat(boolean b) {
    boolean oldValue = flat;
    flat = b;
    firePropertyChange(BORDER_PAINTED_FLAT_CHANGED_PROPERTY, oldValue, flat);
    if (b != oldValue) {
      revalidate();
      repaint();
    }
  }

  public boolean isBorderPaintedFlat() { return flat; }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "CheckBoxUI"; }

  boolean shouldUpdateSelectedStateFromAction() { return true; }

  protected String paramString() { return super.paramString(); }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJCheckBox();
    }
    return accessibleContext;
  }

  protected class AccessibleJCheckBox extends AccessibleJToggleButton {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.CHECK_BOX; }
  }
}
