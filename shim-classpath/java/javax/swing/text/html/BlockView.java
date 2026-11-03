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
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class BlockView extends BoxView {
  private AttributeSet attr;
  private StyleSheet.BoxPainter painter;
  private CSS.Len cssWidth;
  private CSS.Len cssHeight;

  public BlockView(Element elem, int axis) {
    super(elem, axis);
  }

  public void setParent(View parent) {
    super.setParent(parent);
    if (parent != null) {
      setPropertiesFromAttributes();
    }
  }

  protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
    if (r == null) {
      r = new SizeRequirements();
    }
    if (!spanSetFromAttributes(axis, r)) {
      r = super.calculateMajorAxisRequirements(axis, r);
    } else {
      SizeRequirements parentR = super.calculateMajorAxisRequirements(axis, null);
      int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
      r.minimum -= margin;
      r.preferred -= margin;
      r.maximum -= margin;
      constrainSize(axis, r, parentR);
    }
    return r;
  }

  protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    if (r == null) {
      r = new SizeRequirements();
    }
    if (!spanSetFromAttributes(axis, r)) {
      r = super.calculateMinorAxisRequirements(axis, r);
    } else {
      SizeRequirements parentR = super.calculateMinorAxisRequirements(axis, null);
      int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
      r.minimum -= margin;
      r.preferred -= margin;
      r.maximum -= margin;
      constrainSize(axis, r, parentR);
    }
    if (axis == X_AXIS) {
      Object o = getAttributes().getAttribute(CSS.Attribute.TEXT_ALIGN);
      if (o != null) {
        String align = o.toString();
        if (align.equals("center")) {
          r.alignment = 0.5f;
        } else if (align.equals("right")) {
          r.alignment = 1.0f;
        } else {
          r.alignment = 0.0f;
        }
      }
    }
    return r;
  }

  boolean spanSetFromAttributes(int axis, SizeRequirements r) {
    CSS.Len len = axis == X_AXIS ? cssWidth : cssHeight;
    if (len != null && !len.percent) {
      r.minimum = r.preferred = r.maximum = (int) len.get(false) + (axis == X_AXIS ? getLeftInset()
          + getRightInset() : getTopInset() + getBottomInset());
      return true;
    }
    return false;
  }

  private void constrainSize(int axis, SizeRequirements want, SizeRequirements min) {
    if (min.minimum > want.minimum) {
      want.minimum = want.preferred = min.minimum;
      want.maximum = Math.max(want.maximum, min.maximum);
    }
  }

  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    int n = getViewCount();
    Object key = (axis == X_AXIS) ? CSS.Attribute.WIDTH : CSS.Attribute.HEIGHT;
    for (int i = 0; i < n; i++) {
      View v = getView(i);
      int min = (int) v.getMinimumSpan(axis);
      int max;
      AttributeSet a = v.getAttributes();
      Object lv = a.getAttribute(key);
      CSS.Len len = lv == null ? null : CSS.Len.parse(lv.toString());
      if (len != null && len.percent) {
        min = Math.max((int) len.get((float) targetSpan), min);
        max = min;
      } else {
        max = (int) v.getMaximumSpan(axis);
      }
      if (max < targetSpan) {
        float align = v.getAlignment(axis);
        offsets[i] = (int) ((targetSpan - max) * align);
        spans[i] = max;
      } else {
        offsets[i] = 0;
        spans[i] = Math.max(min, targetSpan);
      }
    }
  }

  public void paint(Graphics g, Shape allocation) {
    Rectangle a = (Rectangle) allocation;
    if (painter != null) {
      painter.paint(g, a.x, a.y, a.width, a.height, this);
    }
    super.paint(g, a);
  }

  public AttributeSet getAttributes() {
    if (attr == null) {
      attr = getStyleSheet().getViewAttributes(this);
    }
    return attr;
  }

  public int getResizeWeight(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return 1;
      case View.Y_AXIS:
        return 0;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getAlignment(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return 0;
      case View.Y_AXIS:
        if (getViewCount() == 0) {
          return 0;
        }
        float span = getPreferredSpan(View.Y_AXIS);
        View v = getView(0);
        float above = v.getPreferredSpan(View.Y_AXIS);
        return (((int) span) != 0) ? (above * v.getAlignment(View.Y_AXIS)) / span : 0;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    super.changedUpdate(changes, a, f);
    int pos = changes.getOffset();
    if (pos <= getStartOffset() && (pos + changes.getLength()) >= getEndOffset()) {
      setPropertiesFromAttributes();
    }
  }

  public float getPreferredSpan(int axis) { return super.getPreferredSpan(axis); }

  public float getMinimumSpan(int axis) { return super.getMinimumSpan(axis); }

  public float getMaximumSpan(int axis) { return super.getMaximumSpan(axis); }

  protected void setPropertiesFromAttributes() {
    StyleSheet sheet = getStyleSheet();
    attr = sheet.getViewAttributes(this);
    painter = sheet.getBoxPainter(attr);
    if (attr != null) {
      setInsets((short) painter.getInset(TOP, this), (short) painter.getInset(LEFT, this),
          (short) painter.getInset(BOTTOM, this), (short) painter.getInset(RIGHT, this));
    }
    Object w = attr.getAttribute(CSS.Attribute.WIDTH);
    Object h = attr.getAttribute(CSS.Attribute.HEIGHT);
    cssWidth = w == null ? null : CSS.Len.parse(w.toString());
    cssHeight = h == null ? null : CSS.Len.parse(h.toString());
  }

  protected StyleSheet getStyleSheet() {
    HTMLDocument doc = (HTMLDocument) getDocument();
    return doc.getStyleSheet();
  }
}
