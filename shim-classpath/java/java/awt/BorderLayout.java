/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.util.*;

public class BorderLayout implements java.awt.LayoutManager2, java.io.Serializable {
  int hgap;
  int vgap;
  Component north;
  Component west;
  Component east;
  Component south;
  Component center;
  Component firstLine;
  Component lastLine;
  Component firstItem;
  Component lastItem;
  public static final java.lang.String NORTH = "North";
  public static final java.lang.String SOUTH = "South";
  public static final java.lang.String EAST = "East";
  public static final java.lang.String WEST = "West";
  public static final java.lang.String CENTER = "Center";
  public static final java.lang.String BEFORE_FIRST_LINE = "First";
  public static final java.lang.String AFTER_LAST_LINE = "Last";
  public static final java.lang.String BEFORE_LINE_BEGINS = "Before";
  public static final java.lang.String AFTER_LINE_ENDS = "After";
  public static final java.lang.String PAGE_START = "First";
  public static final java.lang.String PAGE_END = "Last";
  public static final java.lang.String LINE_START = "Before";
  public static final java.lang.String LINE_END = "After";
  public BorderLayout() {
    
  }
  public BorderLayout(int p0, int p1) {
    this.hgap = p0;
    this.vgap = p1;
  }
  public java.lang.String toString() {
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
  }
  public void removeLayoutComponent(java.awt.Component p0) {
    synchronized (p0.getTreeLock()) {
      if (p0 == center) {
        center = null;
      } else if (p0 == north) {
        north = null;
      } else if (p0 == south) {
        south = null;
      } else if (p0 == east) {
        east = null;
      } else if (p0 == west) {
        west = null;
      }
      if (p0 == firstLine) {
        firstLine = null;
      } else if (p0 == lastLine) {
        lastLine = null;
      } else if (p0 == firstItem) {
        firstItem = null;
      } else if (p0 == lastItem) {
        lastItem = null;
      }
    }
  }
  public void addLayoutComponent(java.lang.String p0, java.awt.Component p1) {
    synchronized (p1.getTreeLock()) {
      if (p0 == null) {
        p0 = "Center";
      }
      if ("Center".equals(p0)) {
        center = p1;
      } else if ("North".equals(p0)) {
        north = p1;
      } else if ("South".equals(p0)) {
        south = p1;
      } else if ("East".equals(p0)) {
        east = p1;
      } else if ("West".equals(p0)) {
        west = p1;
      } else if ("First".equals(p0)) {
        firstLine = p1;
      } else if ("Last".equals(p0)) {
        lastLine = p1;
      } else if ("Before".equals(p0)) {
        firstItem = p1;
      } else if ("After".equals(p0)) {
        lastItem = p1;
      } else {
        throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + p0);
      }
    }
  }
  public void addLayoutComponent(java.awt.Component p0, java.lang.Object p1) {
    synchronized (p0.getTreeLock()) {
      if ((p1 == null) || (p1 instanceof String)) {
        addLayoutComponent((String) p1, p0);
      } else {
        throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");
      }
    }
  }
  public void layoutContainer(java.awt.Container p0) {
    synchronized (p0.getTreeLock()) {
      Insets insets = p0.getInsets();
      int top = insets.top;
      int bottom = p0.getHeight() - insets.bottom;
      int left = insets.left;
      int right = p0.getWidth() - insets.right;
      Component c;
      if ((c = north()) != null) {
        c.setSize(right - left, c.getHeight());
        Dimension d = c.getPreferredSize();
        c.setBounds(left, top, right - left, d.height);
        top += d.height + vgap;
      }
      if ((c = south()) != null) {
        Dimension d = c.getPreferredSize();
        c.setBounds(left, bottom - d.height, right - left, d.height);
        bottom -= d.height + vgap;
      }
      if ((c = east()) != null) {
        Dimension d = c.getPreferredSize();
        c.setBounds(right - d.width, top, d.width, bottom - top);
        right -= d.width + hgap;
      }
      if ((c = west()) != null) {
        Dimension d = c.getPreferredSize();
        c.setBounds(left, top, d.width, bottom - top);
        left += d.width + hgap;
      }
      if ((c = center) != null) {
        c.setBounds(left, top, right - left, bottom - top);
      }
    }
  }
  public void invalidateLayout(java.awt.Container p0) {
    
  }
  public java.awt.Dimension preferredLayoutSize(java.awt.Container p0) {
    return size(p0, false);
  }
  public java.awt.Dimension minimumLayoutSize(java.awt.Container p0) {
    return size(p0, true);
  }
  public java.awt.Dimension maximumLayoutSize(java.awt.Container p0) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }
  public float getLayoutAlignmentX(java.awt.Container p0) {
    return 0.5f;
  }
  public float getLayoutAlignmentY(java.awt.Container p0) {
    return 0.5f;
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
  public java.awt.Component getLayoutComponent(java.lang.Object p0) {
    if ("Center".equals(p0)) {
      return center;
    } else if ("North".equals(p0)) {
      return firstLine != null ? firstLine : north;
    } else if ("South".equals(p0)) {
      return lastLine != null ? lastLine : south;
    } else if ("West".equals(p0)) {
      return firstItem != null ? firstItem : west;
    } else if ("East".equals(p0)) {
      return lastItem != null ? lastItem : east;
    }
    throw new IllegalArgumentException("cannot get component: invalid constraint: " + p0);
  }
  public java.awt.Component getLayoutComponent(java.awt.Container p0, java.lang.Object p1) {
    return getLayoutComponent(p1);
  }
  public java.lang.Object getConstraints(java.awt.Component p0) {
    if (p0 == null) {
      return null;
    }
    if (p0 == center) {
      return "Center";
    } else if (p0 == north) {
      return "North";
    } else if (p0 == south) {
      return "South";
    } else if (p0 == west) {
      return "West";
    } else if (p0 == east) {
      return "East";
    }
    return null;
  }

  private Component north() { return firstLine != null ? firstLine : north; }

  private Component south() { return lastLine != null ? lastLine : south; }

  private Component east() { return lastItem != null ? lastItem : east; }

  private Component west() { return firstItem != null ? firstItem : west; }

  private Dimension size(Container target, boolean min) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      Component c;
      if ((c = east()) != null) {
        Dimension d = min ? c.getMinimumSize() : c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = west()) != null) {
        Dimension d = min ? c.getMinimumSize() : c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = center) != null) {
        Dimension d = min ? c.getMinimumSize() : c.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = north()) != null) {
        Dimension d = min ? c.getMinimumSize() : c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }
      if ((c = south()) != null) {
        Dimension d = min ? c.getMinimumSize() : c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }
      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
      return dim;
    }
  }
}
