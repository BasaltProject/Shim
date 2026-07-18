/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

public abstract class PopupMenuUI extends javax.swing.plaf.ComponentUI {
  protected PopupMenuUI() {
  }
  public javax.swing.Popup getPopup(javax.swing.JPopupMenu popup, int x, int y) {
    javax.swing.PopupFactory popupFactory = javax.swing.PopupFactory.getSharedInstance();
    return popupFactory.getPopup(popup.getInvoker(), popup, x, y);
  }
  public boolean isPopupTrigger(java.awt.event.MouseEvent e) { return e.isPopupTrigger(); }
}
