/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.io.Serializable;
import java.util.Hashtable;

public class GridBagLayout implements LayoutManager2, Serializable {
  protected static final int MAXGRIDSIZE = 512;
  protected static final int MINSIZE = 1;
  protected static final int PREFERREDSIZE = 2;

  protected Hashtable<Component, GridBagConstraints> comptable = new Hashtable<>();
  protected GridBagConstraints defaultConstraints = new GridBagConstraints();
  protected GridBagLayoutInfo layoutInfo;
  public int[] columnWidths;
  public int[] rowHeights;
  public double[] columnWeights;
  public double[] rowWeights;

  private boolean rtl;

  public GridBagLayout() { }

  public void setConstraints(Component comp, GridBagConstraints constraints) {
    comptable.put(comp, (GridBagConstraints) constraints.clone());
  }

  public GridBagConstraints getConstraints(Component comp) {
    GridBagConstraints constraints = comptable.get(comp);
    if (constraints == null) {
      setConstraints(comp, defaultConstraints);
      constraints = comptable.get(comp);
    }
    return (GridBagConstraints) constraints.clone();
  }

  protected GridBagConstraints lookupConstraints(Component comp) {
    GridBagConstraints constraints = comptable.get(comp);
    if (constraints == null) {
      setConstraints(comp, defaultConstraints);
      constraints = comptable.get(comp);
    }
    return constraints;
  }

  private GridBagConstraints lookupConstraintsCopy(Component comp) { return lookupConstraints(comp); }

  public Point getLayoutOrigin() {
    Point origin = new Point(0, 0);
    if (layoutInfo != null) {
      origin.x = layoutInfo.startx;
      origin.y = layoutInfo.starty;
    }
    return origin;
  }

  public int[][] getLayoutDimensions() {
    if (layoutInfo == null) {
      return new int[2][0];
    }
    int[][] dim = new int[2][];
    dim[0] = new int[layoutInfo.width];
    dim[1] = new int[layoutInfo.height];
    System.arraycopy(layoutInfo.minWidth, 0, dim[0], 0, layoutInfo.width);
    System.arraycopy(layoutInfo.minHeight, 0, dim[1], 0, layoutInfo.height);
    return dim;
  }

  public double[][] getLayoutWeights() {
    if (layoutInfo == null) {
      return new double[2][0];
    }
    double[][] weights = new double[2][];
    weights[0] = new double[layoutInfo.width];
    weights[1] = new double[layoutInfo.height];
    System.arraycopy(layoutInfo.weightX, 0, weights[0], 0, layoutInfo.width);
    System.arraycopy(layoutInfo.weightY, 0, weights[1], 0, layoutInfo.height);
    return weights;
  }

  public Point location(int x, int y) {
    Point loc = new Point(0, 0);
    if (layoutInfo == null) {
      return loc;
    }
    int i;
    int d = layoutInfo.startx;
    if (!rtl) {
      for (i = 0; i < layoutInfo.width; i++) {
        d += layoutInfo.minWidth[i];
        if (d > x) {
          break;
        }
      }
    } else {
      for (i = layoutInfo.width - 1; i >= 0; i--) {
        if (d > x) {
          break;
        }
        d += layoutInfo.minWidth[i];
      }
      i++;
    }
    loc.x = i;
    d = layoutInfo.starty;
    for (i = 0; i < layoutInfo.height; i++) {
      d += layoutInfo.minHeight[i];
      if (d > y) {
        break;
      }
    }
    loc.y = i;
    return loc;
  }

  public void addLayoutComponent(String name, Component comp) { }

  public void addLayoutComponent(Component comp, Object constraints) {
    if (constraints instanceof GridBagConstraints) {
      setConstraints(comp, (GridBagConstraints) constraints);
    } else if (constraints != null) {
      throw new IllegalArgumentException("cannot add to layout: constraints must be a GridBagConstraint");
    }
  }

  public void removeLayoutComponent(Component comp) { removeComponent(comp); }

  protected void removeConstraints(Component comp) { comptable.remove(comp); }

  private void removeComponent(Component comp) { comptable.remove(comp); }

  public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      GridBagLayoutInfo info = getLayoutInfo(parent, PREFERREDSIZE);
      return getMinSize(parent, info);
    }
  }

  public Dimension minimumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      GridBagLayoutInfo info = getLayoutInfo(parent, MINSIZE);
      return getMinSize(parent, info);
    }
  }

  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public float getLayoutAlignmentX(Container parent) { return 0.5f; }

  public float getLayoutAlignmentY(Container parent) { return 0.5f; }

  public void invalidateLayout(Container target) { }

  public void layoutContainer(Container parent) { arrangeGrid(parent); }

  public String toString() { return getClass().getName(); }

  protected GridBagLayoutInfo getLayoutInfo(Container parent, int sizeflag) {
    return computeInfo(parent, sizeflag);
  }

  protected GridBagLayoutInfo GetLayoutInfo(Container parent, int sizeflag) {
    return getLayoutInfo(parent, sizeflag);
  }

  protected Dimension getMinSize(Container parent, GridBagLayoutInfo info) {
    Dimension d = new Dimension();
    Insets insets = parent.getInsets();
    long w = 0;
    long h = 0;
    for (int i = 0; i < info.width; i++) {
      w += info.minWidth[i];
    }
    for (int i = 0; i < info.height; i++) {
      h += info.minHeight[i];
    }
    d.width = (int) (w + insets.left + insets.right);
    d.height = (int) (h + insets.top + insets.bottom);
    return d;
  }

  protected Dimension GetMinSize(Container parent, GridBagLayoutInfo info) { return getMinSize(parent, info); }

  protected void arrangeGrid(Container parent) {
    Insets insets = parent.getInsets();
    Component[] components = parent.getComponents();
    if (components.length == 0 && (columnWidths == null || columnWidths.length == 0)
        && (rowHeights == null || rowHeights.length == 0)) {
      return;
    }
    Rectangle r = new Rectangle();
    GridBagLayoutInfo info = getLayoutInfo(parent, PREFERREDSIZE);
    Dimension d = getMinSize(parent, info);
    if (parent.width < d.width || parent.height < d.height) {
      info = getLayoutInfo(parent, MINSIZE);
      d = getMinSize(parent, info);
    }
    layoutInfo = info;
    rtl = !parent.getComponentOrientation().isLeftToRight();
    r.width = d.width;
    r.height = d.height;
    int diffw = parent.width - r.width;
    if (diffw != 0) {
      double weight = 0.0;
      for (int i = 0; i < info.width; i++) {
        weight += info.weightX[i];
      }
      if (weight > 0.0) {
        for (int i = 0; i < info.width; i++) {
          int dx = (int) (((double) diffw * info.weightX[i]) / weight);
          info.minWidth[i] += dx;
          r.width += dx;
          if (info.minWidth[i] < 0) {
            r.width -= info.minWidth[i];
            info.minWidth[i] = 0;
          }
        }
      }
      diffw = parent.width - r.width;
    } else {
      diffw = 0;
    }
    int diffh = parent.height - r.height;
    if (diffh != 0) {
      double weight = 0.0;
      for (int i = 0; i < info.height; i++) {
        weight += info.weightY[i];
      }
      if (weight > 0.0) {
        for (int i = 0; i < info.height; i++) {
          int dy = (int) (((double) diffh * info.weightY[i]) / weight);
          info.minHeight[i] += dy;
          r.height += dy;
          if (info.minHeight[i] < 0) {
            r.height -= info.minHeight[i];
            info.minHeight[i] = 0;
          }
        }
      }
      diffh = parent.height - r.height;
    } else {
      diffh = 0;
    }
    info.startx = diffw / 2 + insets.left;
    info.starty = diffh / 2 + insets.top;
    for (Component comp : components) {
      if (!comp.isVisible()) {
        continue;
      }
      GridBagConstraints constraints = lookupConstraints(comp);
      if (!rtl) {
        r.x = info.startx;
        for (int i = 0; i < constraints.tempX; i++) {
          r.x += info.minWidth[i];
        }
      } else {
        r.x = parent.width - (diffw / 2 + insets.right);
        for (int i = 0; i < constraints.tempX; i++) {
          r.x -= info.minWidth[i];
        }
      }
      r.y = info.starty;
      for (int i = 0; i < constraints.tempY; i++) {
        r.y += info.minHeight[i];
      }
      r.width = 0;
      for (int i = constraints.tempX; i < (constraints.tempX + constraints.tempWidth); i++) {
        r.width += info.minWidth[i];
      }
      r.height = 0;
      for (int i = constraints.tempY; i < (constraints.tempY + constraints.tempHeight); i++) {
        r.height += info.minHeight[i];
      }
      adjustForGravity(constraints, r);
      if (r.x < 0) {
        r.width += r.x;
        r.x = 0;
      }
      if (r.y < 0) {
        r.height += r.y;
        r.y = 0;
      }
      if ((r.width <= 0) || (r.height <= 0)) {
        comp.setBounds(0, 0, 0, 0);
      } else if (comp.x != r.x || comp.y != r.y || comp.width != r.width || comp.height != r.height) {
        comp.setBounds(r.x, r.y, r.width, r.height);
      }
    }
  }

  protected void ArrangeGrid(Container parent) { arrangeGrid(parent); }

  protected void adjustForGravity(GridBagConstraints constraints, Rectangle r) {
    if (!rtl) {
      r.x += constraints.insets.left;
    } else {
      r.x -= r.width - constraints.insets.right;
    }
    r.width -= (constraints.insets.left + constraints.insets.right);
    r.y += constraints.insets.top;
    r.height -= (constraints.insets.top + constraints.insets.bottom);
    int diffx = 0;
    if ((constraints.fill != GridBagConstraints.HORIZONTAL && constraints.fill != GridBagConstraints.BOTH)
        && (r.width > (constraints.minWidth + constraints.ipadx))) {
      diffx = r.width - (constraints.minWidth + constraints.ipadx);
      r.width = constraints.minWidth + constraints.ipadx;
    }
    int diffy = 0;
    if ((constraints.fill != GridBagConstraints.VERTICAL && constraints.fill != GridBagConstraints.BOTH)
        && (r.height > (constraints.minHeight + constraints.ipady))) {
      diffy = r.height - (constraints.minHeight + constraints.ipady);
      r.height = constraints.minHeight + constraints.ipady;
    }
    switch (constraints.anchor) {
      case GridBagConstraints.PAGE_START:
      case GridBagConstraints.NORTH:
        r.x += diffx / 2;
        break;
      case GridBagConstraints.NORTHEAST:
        r.x += diffx;
        break;
      case GridBagConstraints.EAST:
        r.x += diffx;
        r.y += diffy / 2;
        break;
      case GridBagConstraints.SOUTHEAST:
        r.x += diffx;
        r.y += diffy;
        break;
      case GridBagConstraints.PAGE_END:
      case GridBagConstraints.SOUTH:
        r.x += diffx / 2;
        r.y += diffy;
        break;
      case GridBagConstraints.SOUTHWEST:
        r.y += diffy;
        break;
      case GridBagConstraints.WEST:
        r.y += diffy / 2;
        break;
      case GridBagConstraints.NORTHWEST:
        break;
      case GridBagConstraints.LINE_START:
        if (rtl) {
          r.x += diffx;
        }
        r.y += diffy / 2;
        break;
      case GridBagConstraints.LINE_END:
        if (!rtl) {
          r.x += diffx;
        }
        r.y += diffy / 2;
        break;
      case GridBagConstraints.FIRST_LINE_START:
        if (rtl) {
          r.x += diffx;
        }
        break;
      case GridBagConstraints.FIRST_LINE_END:
        if (!rtl) {
          r.x += diffx;
        }
        break;
      case GridBagConstraints.LAST_LINE_START:
        if (rtl) {
          r.x += diffx;
        }
        r.y += diffy;
        break;
      case GridBagConstraints.LAST_LINE_END:
        if (!rtl) {
          r.x += diffx;
        }
        r.y += diffy;
        break;
      case GridBagConstraints.CENTER:
      default:
        r.x += diffx / 2;
        r.y += diffy / 2;
        break;
    }
  }

  protected void AdjustForGravity(GridBagConstraints constraints, Rectangle r) { adjustForGravity(constraints, r); }

  private GridBagLayoutInfo computeInfo(Container parent, int sizeflag) {
    Component[] components = parent.getComponents();
    int[] xMax = new int[16];
    int[] yMax = new int[16];
    int maxW = 0;
    int maxH = 0;
    int curRow = -1;
    int curCol = -1;
    for (Component comp : components) {
      if (!comp.isVisible()) {
        continue;
      }
      GridBagConstraints c = lookupConstraints(comp);
      int curX = c.gridx;
      int curY = c.gridy;
      int curW = c.gridwidth;
      int curH = c.gridheight;
      if (curW <= 0) {
        curW = curX >= 0 ? Math.max(1, curW + maxW - curX) : 1;
      }
      if (curH <= 0) {
        curH = curY >= 0 ? Math.max(1, curH + maxH - curY) : 1;
      }
      if (curX < 0 && curY < 0) {
        if (curRow >= 0) {
          curY = curRow;
        } else if (curCol >= 0) {
          curX = curCol;
        } else {
          curY = 0;
        }
      }
      if (curX < 0) {
        int px = 0;
        for (int i = curY; i < curY + curH; i++) {
          px = Math.max(px, at(xMax, i));
        }
        curX = px - curX - 1;
        if (curX < 0) {
          curX = 0;
        }
      } else if (curY < 0) {
        int py = 0;
        for (int i = curX; i < curX + curW; i++) {
          py = Math.max(py, at(yMax, i));
        }
        curY = py - curY - 1;
        if (curY < 0) {
          curY = 0;
        }
      }
      maxW = Math.max(maxW, curX + curW);
      maxH = Math.max(maxH, curY + curH);
      xMax = grow(xMax, curY + curH);
      yMax = grow(yMax, curX + curW);
      for (int i = curX; i < curX + curW; i++) {
        yMax[i] = curY + curH;
      }
      for (int i = curY; i < curY + curH; i++) {
        xMax[i] = curX + curW;
      }
      Dimension dim = sizeflag == PREFERREDSIZE ? comp.getPreferredSize() : comp.getMinimumSize();
      c.minWidth = dim.width;
      c.minHeight = dim.height;
      if (c.gridheight == 0 && c.gridwidth == 0) {
        curRow = curCol = -1;
      }
      if (c.gridheight == 0 && curRow < 0) {
        curCol = curX + curW;
      } else if (c.gridwidth == 0 && curCol < 0) {
        curRow = curY + curH;
      }
      c.tempX = curX;
      c.tempY = curY;
      c.tempWidth = curW;
      c.tempHeight = curH;
    }
    if (columnWidths != null && maxW < columnWidths.length) {
      maxW = columnWidths.length;
    }
    if (rowHeights != null && maxH < rowHeights.length) {
      maxH = rowHeights.length;
    }
    for (Component comp : components) {
      if (!comp.isVisible()) {
        continue;
      }
      GridBagConstraints c = lookupConstraints(comp);
      if (c.gridwidth == 0) {
        c.tempWidth = Math.max(1, maxW - c.tempX);
      } else if (c.gridwidth < 0) {
        c.tempWidth = Math.max(1, maxW - c.tempX + c.gridwidth);
      }
      if (c.gridheight == 0) {
        c.tempHeight = Math.max(1, maxH - c.tempY);
      } else if (c.gridheight < 0) {
        c.tempHeight = Math.max(1, maxH - c.tempY + c.gridheight);
      }
    }
    GridBagLayoutInfo r = new GridBagLayoutInfo(maxW, maxH);
    r.minWidth = new int[maxW];
    r.minHeight = new int[maxH];
    r.weightX = new double[maxW];
    r.weightY = new double[maxH];
    if (columnWidths != null) {
      System.arraycopy(columnWidths, 0, r.minWidth, 0, Math.min(columnWidths.length, maxW));
    }
    if (rowHeights != null) {
      System.arraycopy(rowHeights, 0, r.minHeight, 0, Math.min(rowHeights.length, maxH));
    }
    if (columnWeights != null) {
      System.arraycopy(columnWeights, 0, r.weightX, 0, Math.min(maxW, columnWeights.length));
    }
    if (rowWeights != null) {
      System.arraycopy(rowWeights, 0, r.weightY, 0, Math.min(maxH, rowWeights.length));
    }
    int nextSize = Integer.MAX_VALUE;
    for (int i = 1; i != Integer.MAX_VALUE; i = nextSize, nextSize = Integer.MAX_VALUE) {
      for (Component comp : components) {
        if (!comp.isVisible()) {
          continue;
        }
        GridBagConstraints c = lookupConstraints(comp);
        if (c.tempWidth == i) {
          int px = c.tempX + c.tempWidth;
          double weightDiff = c.weightx;
          int pixelsDiff = c.minWidth + c.ipadx + c.insets.left + c.insets.right;
          for (int k = c.tempX; k < px; k++) {
            pixelsDiff -= r.minWidth[k];
            weightDiff -= r.weightX[k];
          }
          if (pixelsDiff > 0) {
            double weight = 0.0;
            for (int k = c.tempX; k < px; k++) {
              weight += r.weightX[k];
            }
            for (int k = c.tempX; weight > 0.0 && k < px; k++) {
              double wt = r.weightX[k];
              int dx = (int) ((wt * ((double) pixelsDiff)) / weight);
              r.minWidth[k] += dx;
              pixelsDiff -= dx;
              weight -= wt;
            }
            r.minWidth[px - 1] += pixelsDiff;
          }
          if (weightDiff > 0.0) {
            double weight = 0.0;
            for (int k = c.tempX; k < px; k++) {
              weight += r.weightX[k];
            }
            for (int k = c.tempX; weight > 0.0 && k < px; k++) {
              double wt = r.weightX[k];
              double dw = (wt * weightDiff) / weight;
              r.weightX[k] += dw;
              weightDiff -= dw;
              weight -= wt;
            }
            r.weightX[px - 1] += weightDiff;
          }
        } else if (c.tempWidth > i && c.tempWidth < nextSize) {
          nextSize = c.tempWidth;
        }
      }
    }
    nextSize = Integer.MAX_VALUE;
    for (int i = 1; i != Integer.MAX_VALUE; i = nextSize, nextSize = Integer.MAX_VALUE) {
      for (Component comp : components) {
        if (!comp.isVisible()) {
          continue;
        }
        GridBagConstraints c = lookupConstraints(comp);
        if (c.tempHeight == i) {
          int py = c.tempY + c.tempHeight;
          double weightDiff = c.weighty;
          int pixelsDiff = c.minHeight + c.ipady + c.insets.top + c.insets.bottom;
          for (int k = c.tempY; k < py; k++) {
            pixelsDiff -= r.minHeight[k];
            weightDiff -= r.weightY[k];
          }
          if (pixelsDiff > 0) {
            double weight = 0.0;
            for (int k = c.tempY; k < py; k++) {
              weight += r.weightY[k];
            }
            for (int k = c.tempY; weight > 0.0 && k < py; k++) {
              double wt = r.weightY[k];
              int dy = (int) ((wt * ((double) pixelsDiff)) / weight);
              r.minHeight[k] += dy;
              pixelsDiff -= dy;
              weight -= wt;
            }
            r.minHeight[py - 1] += pixelsDiff;
          }
          if (weightDiff > 0.0) {
            double weight = 0.0;
            for (int k = c.tempY; k < py; k++) {
              weight += r.weightY[k];
            }
            for (int k = c.tempY; weight > 0.0 && k < py; k++) {
              double wt = r.weightY[k];
              double dw = (wt * weightDiff) / weight;
              r.weightY[k] += dw;
              weightDiff -= dw;
              weight -= wt;
            }
            r.weightY[py - 1] += weightDiff;
          }
        } else if (c.tempHeight > i && c.tempHeight < nextSize) {
          nextSize = c.tempHeight;
        }
      }
    }
    return r;
  }

  private static int at(int[] a, int i) { return i < a.length ? a[i] : 0; }

  private static int[] grow(int[] a, int need) {
    if (need <= a.length) {
      return a;
    }
    return java.util.Arrays.copyOf(a, Math.max(need, a.length * 2));
  }
}
