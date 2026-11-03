/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SwingUtilities;

public class ComponentView extends View {
  private Component createdC;

  public ComponentView(Element elem) {
    super(elem);
  }

  protected Component createComponent() {
    AttributeSet attr = getElement().getAttributes();
    return StyleConstants.getComponent(attr);
  }

  public final Component getComponent() { return createdC; }

  public void paint(Graphics g, Shape a) {
    if (createdC != null) {
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      createdC.setBounds(alloc.x, alloc.y, alloc.width, alloc.height);
    }
  }

  public float getPreferredSpan(int axis) {
    if ((axis != X_AXIS) && (axis != Y_AXIS)) {
      throw new IllegalArgumentException("Invalid axis: " + axis);
    }
    if (createdC != null) {
      Dimension size = createdC.getPreferredSize();
      return axis == X_AXIS ? size.width : size.height;
    }
    return 0;
  }

  public float getMinimumSpan(int axis) {
    if ((axis != X_AXIS) && (axis != Y_AXIS)) {
      throw new IllegalArgumentException("Invalid axis: " + axis);
    }
    if (createdC != null) {
      Dimension size = createdC.getMinimumSize();
      return axis == X_AXIS ? size.width : size.height;
    }
    return 0;
  }

  public float getMaximumSpan(int axis) {
    if ((axis != X_AXIS) && (axis != Y_AXIS)) {
      throw new IllegalArgumentException("Invalid axis: " + axis);
    }
    if (createdC != null) {
      Dimension size = createdC.getMaximumSize();
      return axis == X_AXIS ? size.width : size.height;
    }
    return 0;
  }

  public float getAlignment(int axis) {
    if (createdC != null) {
      switch (axis) {
        case View.X_AXIS:
          return createdC.getAlignmentX();
        case View.Y_AXIS:
          return createdC.getAlignmentY();
        default:
          break;
      }
    }
    return super.getAlignment(axis);
  }

  public void setParent(View p) {
    super.setParent(p);
    if (p != null) {
      if (createdC == null) {
        createdC = createComponent();
      }
      Container host = getContainer();
      if (host != null && createdC != null && createdC.getParent() == null) {
        host.add(createdC, this);
      }
    } else if (createdC != null) {
      Container parent = createdC.getParent();
      if (parent != null) {
        parent.remove(createdC);
      }
    }
  }

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
