/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ParagraphView extends javax.swing.text.ParagraphView {
  private AttributeSet attr;
  private StyleSheet.BoxPainter painter;
  private CSS.Len cssWidth;
  private CSS.Len cssHeight;

  public ParagraphView(Element elem) {
    super(elem);
  }

  public void setParent(View parent) {
    super.setParent(parent);
    if (parent != null) {
      setPropertiesFromAttributes();
    }
  }

  public AttributeSet getAttributes() {
    if (attr == null) {
      attr = getStyleSheet().getViewAttributes(this);
    }
    return attr;
  }

  protected void setPropertiesFromAttributes() {
    StyleSheet sheet = getStyleSheet();
    attr = sheet.getViewAttributes(this);
    painter = sheet.getBoxPainter(attr);
    if (attr != null) {
      super.setPropertiesFromAttributes();
      setInsets((short) painter.getInset(TOP, this), (short) painter.getInset(LEFT, this),
          (short) painter.getInset(BOTTOM, this), (short) painter.getInset(RIGHT, this));
      Object o = attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
      if (o != null) {
        String ta = o.toString();
        if (ta.equals("left")) {
          setJustification(StyleConstants.ALIGN_LEFT);
        } else if (ta.equals("center")) {
          setJustification(StyleConstants.ALIGN_CENTER);
        } else if (ta.equals("right")) {
          setJustification(StyleConstants.ALIGN_RIGHT);
        } else if (ta.equals("justify")) {
          setJustification(StyleConstants.ALIGN_JUSTIFIED);
        }
      }
      Object w = attr.getAttribute(CSS.Attribute.WIDTH);
      Object h = attr.getAttribute(CSS.Attribute.HEIGHT);
      cssWidth = w == null ? null : CSS.Len.parse(w.toString());
      cssHeight = h == null ? null : CSS.Len.parse(h.toString());
    }
  }

  protected StyleSheet getStyleSheet() {
    HTMLDocument doc = (HTMLDocument) getDocument();
    return doc.getStyleSheet();
  }

  protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    r = super.calculateMinorAxisRequirements(axis, r);
    if (BlockView_spanSet(axis, r)) {
      int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
      r.minimum -= margin;
      r.preferred -= margin;
      r.maximum -= margin;
    }
    return r;
  }

  private boolean BlockView_spanSet(int axis, SizeRequirements r) {
    CSS.Len len = axis == X_AXIS ? cssWidth : cssHeight;
    if (len != null && !len.percent) {
      r.minimum = r.preferred = r.maximum = (int) len.get(false) + (axis == X_AXIS ? getLeftInset() + getRightInset()
          : getTopInset() + getBottomInset());
      return true;
    }
    return false;
  }

  public int getBreakWeight(int axis, float len) {
    return BadBreakWeight;
  }

  public float getAlignment(int axis) { return super.getAlignment(axis); }

  public void paint(Graphics g, Shape a) {
    if (a == null) {
      return;
    }
    java.awt.Rectangle r = (a instanceof java.awt.Rectangle) ? (java.awt.Rectangle) a : a.getBounds();
    if (painter != null) {
      painter.paint(g, r.x, r.y, r.width, r.height, this);
    }
    super.paint(g, a);
  }

  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    setPropertiesFromAttributes();
    super.changedUpdate(changes, a, f);
  }
}
