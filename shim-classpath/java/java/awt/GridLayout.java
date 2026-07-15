/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.util.*;

public class GridLayout implements java.awt.LayoutManager, java.io.Serializable {
  int hgap;
  int vgap;
  int rows;
  int cols;
  public GridLayout(int p0, int p1, int p2, int p3) {
    if ((p0 == 0) && (p1 == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.rows = p0;
    this.cols = p1;
    this.hgap = p2;
    this.vgap = p3;
  }
  public GridLayout(int p0, int p1) {
    if ((p0 == 0) && (p1 == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.rows = p0;
    this.cols = p1;
  }
  public GridLayout() {
    this.rows = 1;
    this.cols = 0;
  }
  public java.lang.String toString() {
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + ",rows=" + rows + ",cols=" + cols + "]";
  }
  public int getRows() {
    return rows;
  }
  public void setRows(int p0) {
    if ((p0 == 0) && (this.cols == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.rows = p0;
  }
  public int getColumns() {
    return cols;
  }
  public void setColumns(int p0) {
    if ((p0 == 0) && (this.rows == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.cols = p0;
  }
  public void removeLayoutComponent(java.awt.Component p0) {
    
  }
  public void addLayoutComponent(java.lang.String p0, java.awt.Component p1) {
    
  }
  public void layoutContainer(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Insets insets = p0.getInsets();
      int ncomponents = p0.getComponentCount();
      int nrows = rows;
      int ncols = cols;
      if (ncomponents == 0) {
        return;
      }
      if (nrows > 0) {
        ncols = (ncomponents + nrows - 1) / nrows;
      } else {
        nrows = (ncomponents + ncols - 1) / ncols;
      }
      int w = p0.getWidth() - (insets.left + insets.right);
      int h = p0.getHeight() - (insets.top + insets.bottom);
      w = (w - (ncols - 1) * hgap) / ncols;
      h = (h - (nrows - 1) * vgap) / nrows;
      boolean ltr = p0.getComponentOrientation().isLeftToRight();
      for (int c = 0, x = insets.left; c < ncols; c++, x += w + hgap) {
        for (int r = 0, y = insets.top; r < nrows; r++, y += h + vgap) {
          int i = r * ncols + c;
          if (i < ncomponents) {
            p0.getComponent(i).setBounds(ltr ? x : p0.getWidth() - x - w, y, w, h);
          }
        }
      }
    }
  }
  public java.awt.Dimension preferredLayoutSize(java.awt.Container p0) {
    return layoutSize(p0, false);
  }
  public java.awt.Dimension minimumLayoutSize(java.awt.Container p0) {
    return layoutSize(p0, true);
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

  private Dimension layoutSize(Container parent, boolean min) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = rows;
      int ncols = cols;
      if (nrows > 0) {
        ncols = (ncomponents + nrows - 1) / nrows;
      } else if (ncols > 0) {
        nrows = (ncomponents + ncols - 1) / ncols;
      }
      int w = 0;
      int h = 0;
      for (int i = 0; i < ncomponents; i++) {
        Dimension d = min ? parent.getComponent(i).getMinimumSize() : parent.getComponent(i).getPreferredSize();
        w = Math.max(w, d.width);
        h = Math.max(h, d.height);
      }
      return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * hgap,
          insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
    }
  }
}
