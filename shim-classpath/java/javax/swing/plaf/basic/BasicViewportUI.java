/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;

public class BasicViewportUI extends ViewportUI {
  private static final BasicViewportUI viewportUI = new BasicViewportUI();

  public BasicViewportUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return viewportUI;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    installDefaults(c);
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults(c);
    super.uninstallUI(c);
  }

  protected void installDefaults(JComponent c) {
    LookAndFeel.installColorsAndFont(c, "Viewport.background", "Viewport.foreground", "Viewport.font");
    LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
  }

  protected void uninstallDefaults(JComponent c) {
  }
}
