/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;

public class BasicTextAreaUI extends BasicTextUI {
  public BasicTextAreaUI() { }

  public static ComponentUI createUI(JComponent ta) { return new BasicTextAreaUI(); }

  protected String getPropertyPrefix() { return "TextArea"; }

  protected void installDefaults() {
    super.installDefaults();
  }

  protected void propertyChange(PropertyChangeEvent evt) {
    String name = evt.getPropertyName();
    if ("lineWrap".equals(name) || "wrapStyleWord".equals(name) || "tabSize".equals(name)) {
      modelChanged();
    }
  }

  public View create(Element elem) {
    JTextComponent c = getComponent();
    if (c instanceof JTextArea) {
      JTextArea area = (JTextArea) c;
      if (area.getLineWrap()) {
        return new WrappedPlainView(elem, area.getWrapStyleWord());
      }
      return new PlainView(elem);
    }
    return null;
  }

  public Dimension getPreferredSize(JComponent c) {
    return super.getPreferredSize(c);
  }

  public Dimension getMinimumSize(JComponent c) {
    return super.getMinimumSize(c);
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    Insets insets = ((JTextComponent) c).getInsets();
    return insets.top + c.getFontMetrics(c.getFont()).getAscent();
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
  }
}
