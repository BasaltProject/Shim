/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;

public class BasicPanelUI extends PanelUI {
  private static final BasicPanelUI panelUI = new BasicPanelUI();

  public BasicPanelUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return panelUI;
  }

  public void installUI(JComponent c) {
    JPanel p = (JPanel) c;
    super.installUI(p);
    installDefaults(p);
  }

  public void uninstallUI(JComponent c) {
    JPanel p = (JPanel) c;
    uninstallDefaults(p);
    super.uninstallUI(c);
  }

  protected void installDefaults(JPanel p) {
    LookAndFeel.installColorsAndFont(p, "Panel.background", "Panel.foreground", "Panel.font");
    LookAndFeel.installBorder(p, "Panel.border");
  }

  protected void uninstallDefaults(JPanel p) {
    LookAndFeel.uninstallBorder(p);
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    javax.swing.border.Border border = c.getBorder();
    if (border instanceof javax.swing.border.AbstractBorder) {
      return ((javax.swing.border.AbstractBorder) border).getBaseline(c, width, height);
    }
    return -1;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    javax.swing.border.Border border = c.getBorder();
    if (border instanceof javax.swing.border.AbstractBorder) {
      return ((javax.swing.border.AbstractBorder) border).getBaselineResizeBehavior(c);
    }
    return Component.BaselineResizeBehavior.OTHER;
  }
}
