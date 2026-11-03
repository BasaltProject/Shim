/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public class NavigationFilter {
  public NavigationFilter() {
  }
  public void setDot(javax.swing.text.NavigationFilter.FilterBypass p0, int p1, javax.swing.text.Position.Bias p2) {
    p0.setDot(p1, p2);
  }
  public void moveDot(javax.swing.text.NavigationFilter.FilterBypass p0, int p1, javax.swing.text.Position.Bias p2) {
    p0.moveDot(p1, p2);
  }
  public int getNextVisualPositionFrom(javax.swing.text.JTextComponent p0, int p1, javax.swing.text.Position.Bias p2, int p3, javax.swing.text.Position.Bias[] p4) throws javax.swing.text.BadLocationException {
    javax.swing.plaf.TextUI ui = p0.getUI();
    return ui == null ? p1 : ui.getNextVisualPositionFrom(p0, p1, p2, p3, p4);
  }

  public static abstract class FilterBypass {
    protected FilterBypass() {
    }
    public abstract void setDot(int p0, javax.swing.text.Position.Bias p1);
    public abstract void moveDot(int p0, javax.swing.text.Position.Bias p1);
    public abstract javax.swing.text.Caret getCaret();
  }
}
