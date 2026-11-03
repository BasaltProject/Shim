/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.LookAndFeel;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.MenuBarUI;

public class BasicMenuBarUI extends MenuBarUI {
  protected JMenuBar menuBar = null;
  protected ContainerListener containerListener;
  protected ChangeListener changeListener;

  public BasicMenuBarUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicMenuBarUI();
  }

  public void installUI(JComponent c) {
    menuBar = (JMenuBar) c;
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  protected void installDefaults() {
    if (menuBar.getLayout() == null || menuBar.getLayout() instanceof javax.swing.plaf.UIResource) {
      menuBar.setLayout(new DefaultMenuLayout(menuBar, BoxLayout.LINE_AXIS));
    }
    LookAndFeel.installProperty(menuBar, "opaque", Boolean.TRUE);
    LookAndFeel.installColorsAndFont(menuBar, "MenuBar.background", "MenuBar.foreground", "MenuBar.font");
    LookAndFeel.installBorder(menuBar, "MenuBar.border");
  }

  protected void installListeners() {
    containerListener = createContainerListener();
    changeListener = createChangeListener();
    menuBar.addContainerListener(containerListener);
  }

  protected void installKeyboardActions() {
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    menuBar = null;
  }

  protected void uninstallDefaults() {
    if (menuBar != null) {
      LookAndFeel.uninstallBorder(menuBar);
    }
  }

  protected void uninstallListeners() {
    menuBar.removeContainerListener(containerListener);
    containerListener = null;
    changeListener = null;
  }

  protected void uninstallKeyboardActions() {
  }

  protected ContainerListener createContainerListener() {
    return new ContainerListener() {
      public void componentAdded(ContainerEvent e) {
      }

      public void componentRemoved(ContainerEvent e) {
      }
    };
  }

  protected ChangeListener createChangeListener() {
    return new ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent e) {
      }
    };
  }

  public Dimension getMinimumSize(JComponent c) {
    return null;
  }

  public Dimension getMaximumSize(JComponent c) {
    return null;
  }
}
