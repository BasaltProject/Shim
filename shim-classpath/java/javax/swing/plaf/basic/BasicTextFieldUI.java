/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Element;
import javax.swing.text.FieldView;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;

public class BasicTextFieldUI extends BasicTextUI {
  public BasicTextFieldUI() { }

  public static ComponentUI createUI(JComponent c) { return new BasicTextFieldUI(); }

  protected String getPropertyPrefix() { return "TextField"; }

  public View create(Element elem) { return new FieldView(elem); }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    View rootView = getRootView((JTextComponent) c);
    if (rootView.getViewCount() > 0) {
      Insets insets = c.getInsets();
      height = height - insets.top - insets.bottom;
      if (height > 0) {
        int baseline = insets.top;
        View fieldView = rootView.getView(0);
        int vspan = (int) fieldView.getPreferredSpan(View.Y_AXIS);
        if (height != vspan) {
          int slop = height - vspan;
          baseline += slop / 2;
        }
        FontMetrics fm = c.getFontMetrics(c.getFont());
        baseline += fm.getAscent();
        return baseline;
      }
    }
    return -1;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.CENTER_OFFSET;
  }
}
