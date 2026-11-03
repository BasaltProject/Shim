/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TreeModelEvent;

final class TreeRows {
  private final AbstractLayoutCache owner;
  private final boolean fixedHeights;
  private final Set<TreePath> expanded = new HashSet<TreePath>();
  private List<TreePath> rows;
  private Map<TreePath, Integer> rowIndex;
  private List<Rectangle> rects;

  TreeRows(AbstractLayoutCache owner, boolean fixedHeights) {
    this.owner = owner;
    this.fixedHeights = fixedHeights;
  }

  void reset() {
    expanded.clear();
    rows = null;
    TreeModel m = owner.getModel();
    if (m != null && m.getRoot() != null) {
      expanded.add(new TreePath(m.getRoot()));
    }
  }

  void invalidate() {
    rows = null;
    rects = null;
  }

  private List<TreePath> rows() {
    if (rows == null) {
      List<TreePath> list = new ArrayList<TreePath>();
      TreeModel m = owner.getModel();
      if (m != null && m.getRoot() != null) {
        TreePath rootPath = new TreePath(m.getRoot());
        if (owner.isRootVisible()) {
          list.add(rootPath);
          if (expanded.contains(rootPath)) {
            addChildren(m, rootPath, list);
          }
        } else {
          addChildren(m, rootPath, list);
        }
      }
      rows = list;
      rowIndex = new HashMap<TreePath, Integer>();
      for (int i = 0; i < list.size(); i++) {
        rowIndex.put(list.get(i), i);
      }
    }
    return rows;
  }

  private void addChildren(TreeModel m, TreePath parent, List<TreePath> out) {
    Object node = parent.getLastPathComponent();
    int n = m.getChildCount(node);
    for (int i = 0; i < n; i++) {
      Object child = m.getChild(node, i);
      TreePath cp = parent.pathByAddingChild(child);
      out.add(cp);
      if (expanded.contains(cp) && !m.isLeaf(child)) {
        addChildren(m, cp, out);
      }
    }
  }

  int rowCount() { return rows().size(); }

  TreePath pathForRow(int row) {
    List<TreePath> r = rows();
    return row >= 0 && row < r.size() ? r.get(row) : null;
  }

  int rowForPath(TreePath path) {
    if (path == null) {
      return -1;
    }
    rows();
    Integer i = rowIndex.get(path);
    return i == null ? -1 : i;
  }

  boolean isExpanded(TreePath path) {
    if (path == null) {
      return false;
    }
    if (!expanded.contains(path)) {
      return false;
    }
    TreePath p = path.getParentPath();
    while (p != null) {
      if (!expanded.contains(p)) {
        return false;
      }
      p = p.getParentPath();
    }
    return true;
  }

  void setExpanded(TreePath path, boolean value) {
    if (path == null) {
      return;
    }
    if (value) {
      TreePath p = path;
      while (p != null) {
        expanded.add(p);
        p = p.getParentPath();
      }
    } else {
      expanded.remove(path);
    }
    invalidate();
  }

  Enumeration<TreePath> visiblePathsFrom(TreePath path) {
    int row = rowForPath(path);
    if (row < 0) {
      return null;
    }
    List<TreePath> r = rows();
    return Collections.enumeration(new ArrayList<TreePath>(r.subList(row, r.size())));
  }

  int visibleChildCount(TreePath path) {
    int row = rowForPath(path);
    if (row < 0 || !isExpanded(path)) {
      return 0;
    }
    List<TreePath> r = rows();
    int depth = path.getPathCount();
    int count = 0;
    for (int i = row + 1; i < r.size() && r.get(i).getPathCount() > depth; i++) {
      count++;
    }
    return count;
  }

  private Rectangle bounds(TreePath path, int row, Rectangle placeIn) {
    Object value = path.getLastPathComponent();
    int depth = path.getPathCount() - 1;
    boolean exp = isExpanded(path);
    Rectangle r = owner.getNodeDimensions(value, row, depth, exp, placeIn);
    if (r == null) {
      return null;
    }
    if (fixedHeights && owner.isFixedRowHeight()) {
      r.y = row * owner.getRowHeight();
      if (r.height == 0) {
        r.height = owner.getRowHeight();
      }
    } else {
      int y = 0;
      if (rects == null) {
        rects = new ArrayList<Rectangle>();
      }
      for (int i = 0; i < row; i++) {
        Rectangle rr = i < rects.size() ? rects.get(i) : null;
        if (rr == null) {
          TreePath p = pathForRow(i);
          rr = owner.getNodeDimensions(p.getLastPathComponent(), i, p.getPathCount() - 1,
              isExpanded(p), null);
          while (rects.size() <= i) {
            rects.add(null);
          }
          rects.set(i, rr);
        }
        y += rr == null ? 0 : rr.height;
      }
      r.y = y;
    }
    return r;
  }

  Rectangle getBounds(TreePath path, Rectangle placeIn) {
    int row = rowForPath(path);
    if (row < 0) {
      return null;
    }
    return bounds(path, row, placeIn);
  }

  TreePath closestTo(int x, int y) {
    int n = rowCount();
    if (n == 0) {
      return null;
    }
    if (owner.isFixedRowHeight()) {
      int row = Math.max(0, Math.min(n - 1, y / owner.getRowHeight()));
      return pathForRow(row);
    }
    int acc = 0;
    for (int i = 0; i < n; i++) {
      Rectangle r = getBounds(pathForRow(i), null);
      int h = r == null ? 0 : r.height;
      if (y < acc + h || i == n - 1) {
        return pathForRow(i);
      }
      acc += h;
    }
    return null;
  }

  void nodesRemoved(TreeModelEvent e) {
    TreePath parent = e.getTreePath();
    Object[] children = e.getChildren();
    if (parent != null && children != null) {
      for (Object child : children) {
        dropSubtree(parent.pathByAddingChild(child));
      }
    }
    invalidate();
  }

  void structureChanged(TreeModelEvent e) {
    TreePath p = e.getTreePath();
    if (p == null || p.getPathCount() <= 1) {
      TreeModel m = owner.getModel();
      expanded.clear();
      if (m != null && m.getRoot() != null) {
        expanded.add(new TreePath(m.getRoot()));
      }
    } else {
      List<TreePath> drop = new ArrayList<TreePath>();
      for (TreePath t : expanded) {
        if (!t.equals(p) && p.isDescendant(t)) {
          drop.add(t);
        }
      }
      expanded.removeAll(drop);
    }
    invalidate();
  }

  private void dropSubtree(TreePath p) {
    List<TreePath> drop = new ArrayList<TreePath>();
    for (TreePath t : expanded) {
      if (p.isDescendant(t)) {
        drop.add(t);
      }
    }
    expanded.removeAll(drop);
  }
}
