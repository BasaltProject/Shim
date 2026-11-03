/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

public class BasicEditorPaneUI extends BasicTextUI {
  public BasicEditorPaneUI() { }

  public static ComponentUI createUI(JComponent c) { return new BasicEditorPaneUI(); }

  protected String getPropertyPrefix() { return "EditorPane"; }

  public void installUI(JComponent c) {
    super.installUI(c);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  public EditorKit getEditorKit(JTextComponent tc) {
    JEditorPane pane = (JEditorPane) getComponent();
    return pane.getEditorKit();
  }

  protected void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
  }
}
