/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JPasswordField;

public class PasswordView extends FieldView {
  public PasswordView(Element elem) {
    super(elem);
  }

  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    Container c = getContainer();
    if (c instanceof JPasswordField) {
      JPasswordField f = (JPasswordField) c;
      if (!f.echoCharIsSet()) {
        return super.drawUnselectedText(g, x, y, p0, p1);
      }
      if (f.isEnabled()) {
        g.setColor(f.getForeground());
      } else {
        g.setColor(f.getDisabledTextColor());
      }
      char echoChar = f.getEchoChar();
      int n = p1 - p0;
      for (int i = 0; i < n; i++) {
        x = drawEchoCharacter(g, x, y, echoChar);
      }
    }
    return x;
  }

  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    g.setColor(selected);
    Container c = getContainer();
    if (c instanceof JPasswordField) {
      JPasswordField f = (JPasswordField) c;
      if (!f.echoCharIsSet()) {
        return super.drawSelectedText(g, x, y, p0, p1);
      }
      char echoChar = f.getEchoChar();
      int n = p1 - p0;
      for (int i = 0; i < n; i++) {
        x = drawEchoCharacter(g, x, y, echoChar);
      }
    }
    return x;
  }

  protected int drawEchoCharacter(Graphics g, int x, int y, char c) {
    g.drawChars(new char[] {c}, 0, 1, x, y);
    return x + g.getFontMetrics().charWidth(c);
  }

  public float getPreferredSpan(int axis) {
    switch (axis) {
      case View.X_AXIS:
        Container c = getContainer();
        if (c instanceof JPasswordField) {
          JPasswordField f = (JPasswordField) c;
          if (f.echoCharIsSet()) {
            char echoChar = f.getEchoChar();
            FontMetrics m = f.getFontMetrics(f.getFont());
            return m.charWidth(echoChar) * getDocument().getLength();
          }
        }
        return super.getPreferredSpan(axis);
      default:
        return super.getPreferredSpan(axis);
    }
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    Container c = getContainer();
    if (c instanceof JPasswordField) {
      JPasswordField f = (JPasswordField) c;
      if (f.echoCharIsSet()) {
        char echoChar = f.getEchoChar();
        FontMetrics m = f.getFontMetrics(f.getFont());
        Rectangle alloc = adjustAllocation(a).getBounds();
        int dx = (pos - getStartOffset()) * m.charWidth(echoChar);
        alloc.x += dx;
        alloc.width = 1;
        return alloc;
      }
    }
    return super.modelToView(pos, a, b);
  }

  public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
    Container c = getContainer();
    if (c instanceof JPasswordField && ((JPasswordField) c).echoCharIsSet()) {
      JPasswordField f = (JPasswordField) c;
      bias[0] = Position.Bias.Forward;
      char echoChar = f.getEchoChar();
      int charWidth = f.getFontMetrics(f.getFont()).charWidth(echoChar);
      a = adjustAllocation(a);
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int n = (charWidth > 0 ? ((int) fx - alloc.x) / charWidth : Integer.MAX_VALUE);
      if (n < 0) {
        n = 0;
      } else if (n > (getStartOffset() + getDocument().getLength())) {
        n = getDocument().getLength() - getStartOffset();
      }
      return getStartOffset() + n;
    }
    return super.viewToModel(fx, fy, a, bias);
  }
}
