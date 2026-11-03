/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public class FlowLayout implements java.awt.LayoutManager, java.io.Serializable {
  int align;
  int hgap;
  int vgap;
  private boolean alignOnBaseline;
  public static final int LEFT = 0;
  public static final int CENTER = 1;
  public static final int RIGHT = 2;
  public static final int LEADING = 3;
  public static final int TRAILING = 4;
  public FlowLayout(int p0, int p1, int p2) {
    this.hgap = p1;
    this.vgap = p2;
    setAlignment(p0);
  }
  public FlowLayout(int p0) {
    this.hgap = 5;
    this.vgap = 5;
    setAlignment(p0);
  }
  public FlowLayout() {
    this.hgap = 5;
    this.vgap = 5;
    this.align = CENTER;
  }
  public java.lang.String toString() {
    String str = "";
    switch (align) {
    case LEFT:
      str = ",align=left";
      break;
    case CENTER:
      str = ",align=center";
      break;
    case RIGHT:
      str = ",align=right";
      break;
    case LEADING:
      str = ",align=leading";
      break;
    case TRAILING:
      str = ",align=trailing";
      break;
    }
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str + "]";
  }
  public void setAlignment(int p0) {
    this.align = p0;
  }
  public boolean getAlignOnBaseline() {
    return alignOnBaseline;
  }
  public int getAlignment() {
    return align;
  }
  public void setAlignOnBaseline(boolean p0) {
    alignOnBaseline = p0;
  }
  public void removeLayoutComponent(java.awt.Component p0) {
    
  }
  public void addLayoutComponent(java.lang.String p0, java.awt.Component p1) {
    
  }
  public void layoutContainer(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Insets insets = p0.getInsets();
      int maxwidth = p0.getWidth() - (insets.left + insets.right + hgap * 2);
      int nmembers = p0.getComponentCount();
      int x = 0;
      int y = insets.top + vgap;
      int rowh = 0;
      int start = 0;
      boolean ltr = p0.getComponentOrientation().isLeftToRight();
      for (int i = 0; i < nmembers; i++) {
        Component m = p0.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          m.setSize(d.width, d.height);
          if ((x == 0) || ((x + d.width) <= maxwidth)) {
            if (x > 0) {
              x += hgap;
            }
            x += d.width;
            rowh = Math.max(rowh, d.height);
          } else {
            moveRow(p0, insets.left + hgap, y, maxwidth - x, rowh, start, i, ltr);
            x = d.width;
            y += vgap + rowh;
            rowh = d.height;
            start = i;
          }
        }
      }
      moveRow(p0, insets.left + hgap, y, maxwidth - x, rowh, start, nmembers, ltr);
    }
  }
  public java.awt.Dimension preferredLayoutSize(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int nmembers = p0.getComponentCount();
      boolean firstVisibleComponent = true;
      for (int i = 0; i < nmembers; i++) {
        Component m = p0.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          dim.height = Math.max(dim.height, d.height);
          if (firstVisibleComponent) {
            firstVisibleComponent = false;
          } else {
            dim.width += hgap;
          }
          dim.width += d.width;
        }
      }
      Insets insets = p0.getInsets();
      dim.width += insets.left + insets.right + hgap * 2;
      dim.height += insets.top + insets.bottom + vgap * 2;
      return dim;
    }
  }
  public java.awt.Dimension minimumLayoutSize(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      int nmembers = p0.getComponentCount();
      for (int i = 0; i < nmembers; i++) {
        Component m = p0.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getMinimumSize();
          dim.height = Math.max(dim.height, d.height);
          if (i > 0) {
            dim.width += hgap;
          }
          dim.width += d.width;
        }
      }
      Insets insets = p0.getInsets();
      dim.width += insets.left + insets.right + hgap * 2;
      dim.height += insets.top + insets.bottom + vgap * 2;
      return dim;
    }
  }
  public int getHgap() {
    return hgap;
  }
  public void setHgap(int p0) {
    hgap = p0;
  }
  public int getVgap() {
    return vgap;
  }
  public void setVgap(int p0) {
    vgap = p0;
  }

  private void moveRow(Container target, int x, int y, int width, int height, int rowStart, int rowEnd, boolean ltr) {
    switch (align) {
    case LEFT:
      x += ltr ? 0 : width;
      break;
    case CENTER:
      x += width / 2;
      break;
    case RIGHT:
      x += ltr ? width : 0;
      break;
    case LEADING:
      break;
    case TRAILING:
      x += width;
      break;
    }
    for (int i = rowStart; i < rowEnd; i++) {
      Component m = target.getComponent(i);
      if (m.isVisible()) {
        int cy = y + (height - m.getHeight()) / 2;
        if (ltr) {
          m.setLocation(x, cy);
        } else {
          m.setLocation(target.getWidth() - x - m.getWidth(), cy);
        }
        x += m.getWidth() + hgap;
      }
    }
  }
}
