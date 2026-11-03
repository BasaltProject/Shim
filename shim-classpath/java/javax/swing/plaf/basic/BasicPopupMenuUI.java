/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.basic;

import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.plaf.PopupMenuUI;

public class BasicPopupMenuUI extends PopupMenuUI {
  protected JPopupMenu popupMenu = null;

  public BasicPopupMenuUI() {
  }

  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicPopupMenuUI();
  }

  public void installUI(JComponent c) {
    popupMenu = (JPopupMenu) c;
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  public void installDefaults() {
    if (popupMenu.getLayout() == null || popupMenu.getLayout() instanceof javax.swing.plaf.UIResource) {
      popupMenu.setLayout(new DefaultMenuLayout(popupMenu, BoxLayout.Y_AXIS));
    }
    LookAndFeel.installProperty(popupMenu, "opaque", Boolean.TRUE);
    LookAndFeel.installColorsAndFont(popupMenu, "PopupMenu.background", "PopupMenu.foreground", "PopupMenu.font");
    LookAndFeel.installBorder(popupMenu, "PopupMenu.border");
  }

  protected void installListeners() {
  }

  protected void installKeyboardActions() {
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    popupMenu = null;
  }

  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(popupMenu);
  }

  protected void uninstallListeners() {
  }

  protected void uninstallKeyboardActions() {
  }

  public boolean isPopupTrigger(MouseEvent e) {
    return e.isPopupTrigger();
  }
}
