/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

class HRuleView extends View {
  private static final int SPACE_ABOVE = 3;
  private static final int SPACE_BELOW = 3;
  private float size;
  private boolean noshade;
  private float topMargin;
  private float leftMargin;
  private float rightMargin;
  private int alignment = javax.swing.text.StyleConstants.ALIGN_CENTER;
  private CSS.Len widthValue;
  private float bottomMargin;
  private AttributeSet attr;

  HRuleView(Element elem) {
    super(elem);
    setPropertiesFromAttributes();
  }

  protected void setPropertiesFromAttributes() {
    StyleSheet sheet = ((HTMLDocument) getDocument()).getStyleSheet();
    AttributeSet eAttr = getElement().getAttributes();
    attr = sheet.getViewAttributes(this);
    noshade = eAttr.getAttribute(HTML.Attribute.NOSHADE) != null;
    Object value = eAttr.getAttribute(HTML.Attribute.SIZE);
    if (value instanceof String) {
      try {
        size = Integer.parseInt((String) value);
      } catch (NumberFormatException e) {
        size = 1;
      }
    }
    Object al = eAttr.getAttribute(HTML.Attribute.ALIGN);
    if (al != null) {
      String v = al.toString();
      alignment = v.equals("left") ? javax.swing.text.StyleConstants.ALIGN_LEFT : v.equals("right")
          ? javax.swing.text.StyleConstants.ALIGN_RIGHT : javax.swing.text.StyleConstants.ALIGN_CENTER;
    }
    Object wv = attr.getAttribute(CSS.Attribute.WIDTH);
    widthValue = wv == null ? null : CSS.Len.parse(wv.toString());
    leftMargin = margin(CSS.Attribute.MARGIN_LEFT);
    rightMargin = margin(CSS.Attribute.MARGIN_RIGHT);
    topMargin = margin(CSS.Attribute.MARGIN_TOP);
    bottomMargin = margin(CSS.Attribute.MARGIN_BOTTOM);
  }

  private float margin(CSS.Attribute key) {
    Object v = attr.getAttribute(key);
    CSS.Len l = v == null ? null : CSS.Len.parse(v.toString());
    return l == null || l.percent ? 0 : l.get(false);
  }

  public AttributeSet getAttributes() { return attr == null ? super.getAttributes() : attr; }

  public void paint(Graphics g, Shape a) {
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
    int y = alloc.y + SPACE_ABOVE + (int) topMargin;
    int width = alloc.width - (int) (leftMargin + rightMargin);
    if (widthValue != null) {
      width = (int) (widthValue.percent ? widthValue.get((float) width) : widthValue.get(false));
    }
    int height = alloc.height - (SPACE_ABOVE + SPACE_BELOW + (int) topMargin + (int) bottomMargin);
    if (size > 0) {
      height = (int) size;
    }
    int x;
    switch (alignment) {
      case javax.swing.text.StyleConstants.ALIGN_CENTER:
        x = alloc.x + (alloc.width / 2) - (width / 2);
        break;
      case javax.swing.text.StyleConstants.ALIGN_RIGHT:
        x = alloc.x + alloc.width - width - (int) rightMargin;
        break;
      default:
        x = alloc.x + (int) leftMargin;
        break;
    }
    if (noshade) {
      g.setColor(java.awt.Color.black);
      g.fillRect(x, y, width, height);
    } else {
      java.awt.Color bg = getContainer().getBackground();
      java.awt.Color bottom;
      java.awt.Color top;
      if (bg == null || bg.equals(java.awt.Color.white)) {
        top = java.awt.Color.darkGray;
        bottom = java.awt.Color.lightGray;
      } else {
        top = java.awt.Color.lightGray;
        bottom = java.awt.Color.darkGray;
      }
      g.setColor(bottom);
      g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
      g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
      g.setColor(top);
      g.drawLine(x, y, x + width - 1, y);
      g.drawLine(x, y, x, y + height - 1);
    }
  }

  public float getPreferredSpan(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return 1;
      case View.Y_AXIS:
        if (size > 0) {
          return size + SPACE_ABOVE + SPACE_BELOW + topMargin + bottomMargin;
        }
        if (noshade) {
          return 3 + SPACE_ABOVE + SPACE_BELOW + topMargin + bottomMargin;
        }
        return 2 + SPACE_ABOVE + SPACE_BELOW + topMargin + bottomMargin;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public int getResizeWeight(int axis) {
    if (axis == View.X_AXIS) {
      return 1;
    }
    return 0;
  }

  public int getBreakWeight(int axis, float pos, float len) {
    if (axis == X_AXIS) {
      return ForcedBreakWeight;
    }
    return BadBreakWeight;
  }

  public View breakView(int axis, int offset, float pos, float len) { return null; }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    if ((pos >= p0) && (pos <= p1)) {
      Rectangle r = a.getBounds();
      if (pos == p1) {
        r.x += r.width;
      }
      r.width = 0;
      return r;
    }
    throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
  }

  public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
    Rectangle alloc = (Rectangle) a;
    if (x < alloc.x + (alloc.width / 2)) {
      bias[0] = Position.Bias.Forward;
      return getStartOffset();
    }
    bias[0] = Position.Bias.Backward;
    return getEndOffset();
  }
}
