/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.plaf.UIResource;

public class BasicDesktopPaneUI extends DesktopPaneUI {
  private static final BasicDesktopPaneUI shared = new BasicDesktopPaneUI();
  protected JDesktopPane desktop;
  protected DesktopManager desktopManager;
  @Deprecated
  protected KeyStroke minimizeKey;
  @Deprecated
  protected KeyStroke maximizeKey;
  @Deprecated
  protected KeyStroke closeKey;
  @Deprecated
  protected KeyStroke navigateKey;
  @Deprecated
  protected KeyStroke navigateKey2;
  private PropertyChangeListener pcl;

  public BasicDesktopPaneUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return shared;
  }

  public void installUI(JComponent c) {
    desktop = (JDesktopPane) c;
    installDefaults();
    installDesktopManager();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDesktopManager();
    uninstallDefaults();
    desktop = null;
  }

  protected void installDefaults() {
    if (desktop.getBackground() == null || desktop.getBackground() instanceof UIResource) {
      desktop.setBackground(UIManager.getColor("Desktop.background"));
    }
    LookAndFeel.installProperty(desktop, "opaque", Boolean.TRUE);
  }

  protected void uninstallDefaults() {
  }

  protected void installListeners() {
    pcl = createPropertyChangeListener();
    desktop.addPropertyChangeListener(pcl);
  }

  protected void uninstallListeners() {
    desktop.removePropertyChangeListener(pcl);
    pcl = null;
  }

  protected void installDesktopManager() {
    desktopManager = desktop.getDesktopManager();
    if (desktopManager == null) {
      desktopManager = new DefaultDesktopManager();
      desktop.setDesktopManager(desktopManager);
    }
  }

  protected void uninstallDesktopManager() {
    if (desktopManager instanceof UIResource) {
      desktop.setDesktopManager(null);
    }
    desktopManager = null;
  }

  protected void installKeyboardActions() {
    registerKeyboardActions();
  }

  protected void uninstallKeyboardActions() {
    unregisterKeyboardActions();
  }

  protected void registerKeyboardActions() {
  }

  protected void unregisterKeyboardActions() {
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
      }
    };
  }

  public void paint(Graphics g, JComponent c) {
  }

  public Dimension getPreferredSize(JComponent c) {
    return null;
  }

  public Dimension getMinimumSize(JComponent c) {
    return new Dimension(0, 0);
  }

  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
}
