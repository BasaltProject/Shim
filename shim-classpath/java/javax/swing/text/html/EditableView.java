/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

class EditableView extends ComponentView {
  private boolean isVisible;

  EditableView(Element e) {
    super(e);
  }

  public float getMinimumSpan(int axis) { return isVisible ? super.getMinimumSpan(axis) : 0; }

  public float getPreferredSpan(int axis) { return isVisible ? super.getPreferredSpan(axis) : 0; }

  public float getMaximumSpan(int axis) { return isVisible ? super.getMaximumSpan(axis) : 0; }

  public void paint(Graphics g, Shape allocation) {
    if (isVisible) {
      super.paint(g, allocation);
    }
  }

  public int getViewIndex(int x, int y, Shape allocation) { return isVisible ? 1 : -1; }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    if (pos >= p0 && pos <= p1) {
      Rectangle r = a.getBounds();
      if (pos == p1) {
        r.x += r.width;
      }
      r.width = 0;
      return r;
    }
    throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
  }

  public void setParent(View p) {
    if (p != null) {
      Container host = p.getContainer();
      if (host != null) {
        isVisible = host instanceof JTextComponent && ((JTextComponent) host).isEditable();
      }
    }
    super.setParent(p);
  }

  public boolean isVisible() { return isVisible; }

  void setVisible(boolean visible) { isVisible = visible; }
}
