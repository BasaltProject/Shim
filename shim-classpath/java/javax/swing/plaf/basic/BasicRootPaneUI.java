/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.RootPaneUI;

public class BasicRootPaneUI extends RootPaneUI implements PropertyChangeListener {
  private static final BasicRootPaneUI rootPaneUI = new BasicRootPaneUI();

  public BasicRootPaneUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return rootPaneUI;
  }

  public void installUI(JComponent c) {
    installDefaults((JRootPane) c);
    installComponents((JRootPane) c);
    installListeners((JRootPane) c);
    installKeyboardActions((JRootPane) c);
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults((JRootPane) c);
    uninstallComponents((JRootPane) c);
    uninstallListeners((JRootPane) c);
    uninstallKeyboardActions((JRootPane) c);
  }

  protected void installDefaults(JRootPane c) {
    LookAndFeel.installProperty(c, "opaque", Boolean.FALSE);
  }

  protected void installComponents(JRootPane root) {
  }

  protected void installListeners(JRootPane root) {
    root.addPropertyChangeListener(this);
  }

  protected void installKeyboardActions(JRootPane root) {
  }

  protected void uninstallDefaults(JRootPane root) {
  }

  protected void uninstallComponents(JRootPane root) {
  }

  protected void uninstallListeners(JRootPane root) {
    root.removePropertyChangeListener(this);
  }

  protected void uninstallKeyboardActions(JRootPane root) {
  }

  public void propertyChange(PropertyChangeEvent e) {
  }
}
