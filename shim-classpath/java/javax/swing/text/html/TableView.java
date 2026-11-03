/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

class TableView extends BlockView implements ViewFactory {
  private final List<RowView> rows = new ArrayList<RowView>();
  private boolean gridValid;
  private int[] columnSpans = new int[0];
  private int[] columnOffsets = new int[0];
  private int[] columnMins = new int[0];
  private int[] columnPrefs = new int[0];
  private float[] columnPercent = new float[0];
  private boolean[] columnFixed = new boolean[0];
  private boolean tableSized;
  private int cellSpacing = 2;
  private int borderInset;
  private int captionIndex = -1;
  private int rowGap;
  private boolean skipComments;

  public TableView(Element elem) {
    super(elem, View.Y_AXIS);
  }

  public View create(Element elem) {
    Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
    if (o == HTML.Tag.TR) {
      return new RowView(elem);
    }
    if (o == HTML.Tag.TD || o == HTML.Tag.TH) {
      return new CellView(elem);
    }
    if (o == HTML.Tag.CAPTION) {
      return new ParagraphView(elem);
    }
    View p = getParent();
    if (p != null) {
      ViewFactory f = p.getViewFactory();
      if (f != null) {
        return f.create(elem);
      }
    }
    return null;
  }

  public ViewFactory getViewFactory() { return this; }

  protected void setPropertiesFromAttributes() {
    super.setPropertiesFromAttributes();
    Object cs = getElement().getAttributes().getAttribute(HTML.Attribute.CELLSPACING);
    cellSpacing = 2;
    if (cs != null) {
      try {
        cellSpacing = Integer.parseInt(cs.toString().trim());
      } catch (NumberFormatException e) {
        cellSpacing = 2;
      }
    }
    borderInset = 0;
    Object bo = getElement().getAttributes().getAttribute(HTML.Attribute.BORDER);
    if (bo != null) {
      try {
        borderInset = Math.max(0, Integer.parseInt(bo.toString().trim()));
      } catch (NumberFormatException e) {
        borderInset = 1;
      }
    }
    rowGap = cellSpacing + borderInset;
    updateInsets();
  }

  private void updateInsets() {
    int top = borderInset + rowGap;
    int bottom = borderInset + rowGap;
    captionIndex = -1;
    for (int i = 0; i < getViewCount(); i++) {
      View v = getView(i);
      if (!(v instanceof RowView) && v.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute)
          == HTML.Tag.CAPTION) {
        captionIndex = i;
        int h = (int) v.getPreferredSpan(Y_AXIS);
        Object side = v.getAttributes().getAttribute(CSS.Attribute.CAPTION_SIDE);
        if (side != null && side.toString().equals("bottom")) {
          bottom += h;
        } else {
          top += h;
        }
        break;
      }
    }
    setInsets((short) top, (short) borderInset, (short) bottom, (short) borderInset);
  }

  public void setParent(View parent) {
    super.setParent(parent);
    if (parent != null) {
      setPropertiesFromAttributes();
    }
  }

  public void replace(int offset, int length, View[] views) {
    super.replace(offset, length, views);
    gridValid = false;
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.changedUpdate(e, a, f);
    gridValid = false;
  }

  public void preferenceChanged(View child, boolean width, boolean height) {
    super.preferenceChanged(child, width, height);
  }

  public int getColumnCount() { return columnSpans.length; }

  public int getColumnSpan(int col) { return col < columnSpans.length ? columnSpans[col] : 0; }

  public int getRowCount() { return rows.size(); }

  public int getMultiRowSpan(int row0, int row1) {
    RowView rv0 = getRow(row0);
    RowView rv1 = getRow(row1);
    if (rv0 != null && rv1 != null) {
      return getOffset(Y_AXIS, rv1.viewIndex) - getOffset(Y_AXIS, rv0.viewIndex) + getSpan(Y_AXIS, rv1.viewIndex);
    }
    return 0;
  }

  public int getRowSpan(int row) {
    RowView rv = getRow(row);
    return rv != null ? getSpan(Y_AXIS, rv.viewIndex) : 0;
  }

  RowView getRow(int row) { return row >= 0 && row < rows.size() ? rows.get(row) : null; }

  static int intAttr(View v, HTML.Attribute key) {
    Object o = v.getElement().getAttributes().getAttribute(key);
    if (o != null) {
      try {
        return Math.max(1, Integer.parseInt(o.toString().trim()));
      } catch (NumberFormatException e) {
        return 1;
      }
    }
    return 1;
  }

  static int getColumnsOccupied(View v) { return intAttr(v, HTML.Attribute.COLSPAN); }

  static int getRowsOccupied(View v) { return intAttr(v, HTML.Attribute.ROWSPAN); }

  private void updateGrid() {
    if (gridValid) {
      return;
    }
    rows.clear();
    int n = getViewCount();
    for (int i = 0; i < n; i++) {
      View v = getView(i);
      if (v instanceof RowView) {
        RowView rv = (RowView) v;
        rows.add(rv);
        rv.fill.clear();
        rv.rowIndex = rows.size() - 1;
        rv.viewIndex = i;
      }
    }
    int maxColumns = 0;
    int nrows = rows.size();
    for (int row = 0; row < nrows; row++) {
      RowView rv = rows.get(row);
      int col = 0;
      for (int cell = 0; cell < rv.getViewCount(); cell++, col++) {
        View cv = rv.getView(cell);
        while (rv.fill.get(col)) {
          col++;
        }
        int rowSpan = getRowsOccupied(cv);
        int colSpan = getColumnsOccupied(cv);
        if (colSpan > 1 || rowSpan > 1) {
          for (int i = row; i < row + rowSpan; i++) {
            RowView target = getRow(i);
            for (int j = col; j < col + colSpan; j++) {
              if ((i != row || j != col) && target != null) {
                target.fill.set(j);
              }
            }
          }
          col += colSpan - 1;
        }
      }
      maxColumns = Math.max(maxColumns, col);
    }
    columnSpans = new int[maxColumns];
    columnOffsets = new int[maxColumns];
    columnMins = new int[maxColumns];
    columnPrefs = new int[maxColumns];
    columnPercent = new float[maxColumns];
    columnFixed = new boolean[maxColumns];
    gridValid = true;
  }

  private boolean computeSkipComments() {
    java.awt.Container host = getContainer();
    if (host instanceof javax.swing.text.JTextComponent) {
      return !((javax.swing.text.JTextComponent) host).isEditable();
    }
    return true;
  }

  private void calculateColumnRequirements() {
    skipComments = computeSkipComments();
    int ncols = columnSpans.length;
    for (int i = 0; i < ncols; i++) {
      columnMins[i] = 0;
      columnPrefs[i] = 0;
      columnPercent[i] = 0;
      columnFixed[i] = false;
    }
    boolean hasMulti = false;
    int nrows = rows.size();
    for (int i = 0; i < nrows; i++) {
      RowView row = rows.get(i);
      int col = 0;
      for (int cell = 0; cell < row.getViewCount(); cell++, col++) {
        View cv = row.getView(cell);
        if (skipComments && !(cv instanceof CellView)) {
          col--;
          continue;
        }
        while (row.fill.get(col)) {
          col++;
        }
        int colSpan = getColumnsOccupied(cv);
        if (colSpan == 1) {
          columnMins[col] = Math.max((int) cv.getMinimumSpan(X_AXIS), columnMins[col]);
          columnPrefs[col] = Math.max((int) cv.getPreferredSpan(X_AXIS), columnPrefs[col]);
          Object w = cv.getElement().getAttributes().getAttribute(HTML.Attribute.WIDTH);
          CSS.Len len = w == null ? null : CSS.Len.parse(w.toString());
          if (len != null) {
            columnFixed[col] = true;
            if (len.percent) {
              columnPercent[col] = Math.max(columnPercent[col], len.value);
            }
          }
        } else {
          hasMulti = true;
          col += colSpan - 1;
        }
      }
    }
    if (!hasMulti) {
      return;
    }
    for (int i = 0; i < nrows; i++) {
      RowView row = rows.get(i);
      int col = 0;
      for (int cell = 0; cell < row.getViewCount(); cell++, col++) {
        View cv = row.getView(cell);
        if (skipComments && !(cv instanceof CellView)) {
          col--;
          continue;
        }
        while (row.fill.get(col)) {
          col++;
        }
        int colSpan = getColumnsOccupied(cv);
        if (colSpan > 1) {
          int last = Math.min(ncols, col + colSpan);
          int count = last - col;
          int gaps = cellSpacing * (count - 1);
          spread(columnMins, col, count, (int) cv.getMinimumSpan(X_AXIS) - gaps);
          spread(columnPrefs, col, count, (int) cv.getPreferredSpan(X_AXIS) - gaps);
          col += colSpan - 1;
        }
      }
    }
    for (int i = 0; i < ncols; i++) {
      if (columnPrefs[i] < columnMins[i]) {
        columnPrefs[i] = columnMins[i];
      }
    }
  }

  private static void spread(int[] reqs, int from, int count, int needed) {
    long current = 0;
    for (int i = from; i < from + count; i++) {
      current += reqs[i];
    }
    if (needed > current && count > 0) {
      int extra = (int) (needed - current);
      int each = (extra + count - 1) / count;
      for (int i = from; i < from + count; i++) {
        reqs[i] += each;
      }
    }
  }

  private int minSum() {
    long s = 0;
    for (int v : columnMins) {
      s += v;
    }
    return (int) s;
  }

  private int prefSum() {
    long s = 0;
    for (int v : columnPrefs) {
      s += v;
    }
    return (int) s;
  }

  private int gapsTotal() { return 2 * rowGap + cellSpacing * Math.max(0, columnSpans.length - 1); }

  protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    updateGrid();
    updateInsets();
    calculateColumnRequirements();
    if (r == null) {
      r = new SizeRequirements();
    }
    int gaps = gapsTotal();
    r.minimum = minSum() + gaps;
    r.preferred = prefSum() + gaps;
    r.maximum = r.preferred;
    Object w = getAttributes().getAttribute(CSS.Attribute.WIDTH);
    CSS.Len len = w == null ? null : CSS.Len.parse(w.toString());
    tableSized = len != null;
    if (len != null && !len.percent) {
      int fixed = (int) len.get(false);
      r.preferred = Math.max(fixed, r.minimum);
      r.minimum = r.preferred;
      r.maximum = r.preferred;
    }
    r.alignment = tableAlignment();
    return r;
  }

  private float tableAlignment() {
    Object al = getElement().getAttributes().getAttribute(HTML.Attribute.ALIGN);
    Object ta = getAttributes().getAttribute(CSS.Attribute.TEXT_ALIGN);
    String v = al != null ? al.toString() : ta != null ? ta.toString() : "";
    if (v.equals("center")) {
      return 0.5f;
    }
    return v.equals("right") ? 1f : 0f;
  }

  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    updateGrid();
    int ncols = columnSpans.length;
    int total = targetSpan - gapsTotal();
    allocateColumns(total, targetSpan);
    int x = rowGap;
    for (int i = 0; i < ncols; i++) {
      columnOffsets[i] = x;
      x += columnSpans[i] + cellSpacing;
    }
    for (RowView row : rows) {
      row.layoutChanged(X_AXIS);
      row.layoutChanged(Y_AXIS);
    }
    for (int i = 0; i < getViewCount(); i++) {
      offsets[i] = 0;
      View v = getView(i);
      spans[i] = i == captionIndex ? (int) v.getPreferredSpan(X_AXIS) : targetSpan;
    }
  }

  private void allocateColumns(int total, int rowSpan) {
    int n = columnSpans.length;
    if (n == 0) {
      return;
    }
    int[] result = new int[n];
    boolean[] exact = new boolean[n];
    boolean[] px = new boolean[n];
    long exactSum = 0;
    long flexPref = 0;
    long flexMin = 0;
    int autoCount = 0;
    for (int i = 0; i < n; i++) {
      if (tableSized && columnPercent[i] > 0) {
        result[i] = Math.max(columnMins[i], (int) (columnPercent[i] * rowSpan / 100f));
        exact[i] = true;
        exactSum += result[i];
      } else {
        flexPref += columnPrefs[i];
        flexMin += columnMins[i];
        px[i] = columnFixed[i];
        if (!px[i]) {
          autoCount++;
        }
      }
    }
    long remaining = total - exactSum;
    if (remaining >= flexPref) {
      for (int i = 0; i < n; i++) {
        if (!exact[i]) {
          result[i] = columnPrefs[i];
        }
      }
      int extra = (int) (remaining - flexPref);
      int share = autoCount > 0 ? autoCount : n;
      if (autoCount == 0 && flexPref == 0) {
        share = 0;
      }
      if (share > 0 && extra > 0) {
        int each = extra / share;
        int rem = extra - each * share;
        for (int i = 0; i < n; i++) {
          boolean takes = autoCount > 0 ? (!exact[i] && !px[i]) : !exact[i];
          if (takes) {
            result[i] += each + (rem > 0 ? 1 : 0);
            if (rem > 0) {
              rem--;
            }
          }
        }
      }
    } else if (remaining > flexMin) {
      long room = remaining - flexMin;
      long flex = flexPref - flexMin;
      long given = 0;
      int lastFlex = -1;
      for (int i = 0; i < n; i++) {
        if (!exact[i]) {
          long part = flex == 0 ? 0 : room * (columnPrefs[i] - columnMins[i]) / flex;
          result[i] = columnMins[i] + (int) part;
          given += result[i];
          if (columnPrefs[i] > columnMins[i]) {
            lastFlex = i;
          }
        }
      }
      if (lastFlex >= 0) {
        result[lastFlex] += (int) (remaining - given);
      }
    } else {
      for (int i = 0; i < n; i++) {
        if (!exact[i]) {
          result[i] = columnMins[i];
        }
      }
    }
    System.arraycopy(result, 0, columnSpans, 0, n);
  }

  protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
    updateGrid();
    updateInsets();
    if (r == null) {
      r = new SizeRequirements();
    }
    int[] h = rowHeights();
    int sum = 0;
    for (int v : h) {
      sum += v;
    }
    int n = rows.size();
    sum += cellSpacing * Math.max(0, n - 1);
    r.minimum = sum;
    r.preferred = sum;
    r.maximum = sum;
    r.alignment = 0f;
    return r;
  }

  private int[] rowHeights() {
    int n = rows.size();
    int[] h = new int[n];
    for (int i = 0; i < n; i++) {
      h[i] = (int) rows.get(i).singleRowHeight();
    }
    for (int i = 0; i < n; i++) {
      RowView row = rows.get(i);
      for (int c = 0; c < row.getViewCount(); c++) {
        View cv = row.getView(c);
        int rs = getRowsOccupied(cv);
        if (rs > 1) {
          int last = Math.min(n, i + rs) - 1;
          int count = last - i + 1;
          int span = 0;
          for (int k = i; k <= last; k++) {
            span += h[k];
          }
          int need = (int) cv.getPreferredSpan(Y_AXIS);
          if (need > span) {
            int extra = need - span;
            int each = extra / count;
            int rem = extra - each * count;
            for (int k = i; k <= last; k++) {
              h[k] += each + (rem > 0 ? 1 : 0);
              if (rem > 0) {
                rem--;
              }
            }
          }
        }
      }
    }
    return h;
  }

  protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    int[] h = rowHeights();
    int y = 0;
    int ri = 0;
    for (int i = 0; i < getViewCount(); i++) {
      View v = getView(i);
      if (v instanceof RowView && ri < h.length) {
        offsets[i] = y;
        spans[i] = h[ri];
        y += h[ri] + cellSpacing;
        ri++;
      } else if (i == captionIndex) {
        Object side = v.getAttributes().getAttribute(CSS.Attribute.CAPTION_SIDE);
        int ch = (int) v.getPreferredSpan(Y_AXIS);
        if (side != null && side.toString().equals("bottom")) {
          offsets[i] = Math.max(0, targetSpan) + rowGap + borderInset;
        } else {
          offsets[i] = -ch - rowGap - borderInset;
        }
        spans[i] = ch;
      } else {
        offsets[i] = 0;
        spans[i] = 0;
      }
    }
  }

  protected View getViewAtPoint(int x, int y, Rectangle alloc) {
    int n = getViewCount();
    Rectangle allocation = new Rectangle();
    for (int i = 0; i < n; i++) {
      allocation.setBounds(alloc);
      childAllocation(i, allocation);
      View v = getView(i);
      if (v instanceof RowView) {
        View hit = ((RowView) v).findViewAtPoint(x, y, allocation);
        if (hit != null) {
          alloc.setBounds(allocation);
          return hit;
        }
      }
    }
    return super.getViewAtPoint(x, y, alloc);
  }

  public int getResizeWeight(int axis) { return 0; }

  public float getAlignment(int axis) { return axis == X_AXIS ? tableAlignment() : super.getAlignment(axis); }

  class RowView extends BlockView {
    final BitSet fill = new BitSet();
    int rowIndex;
    int viewIndex;

    RowView(Element elem) {
      super(elem, View.X_AXIS);
    }

    public int getResizeWeight(int axis) { return 1; }

    float singleRowHeight() {
      float h = 0;
      for (int c = 0; c < getViewCount(); c++) {
        View cv = getView(c);
        if (getRowsOccupied(cv) == 1) {
          h = Math.max(h, cv.getPreferredSpan(Y_AXIS));
        }
      }
      return h;
    }

    View findViewAtPoint(int x, int y, Rectangle alloc) {
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        Shape s = getChildAllocation(i, alloc);
        if (s != null && s.getBounds().contains(x, y)) {
          alloc.setBounds(s.getBounds());
          return getView(i);
        }
      }
      return null;
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
      if (r == null) {
        r = new SizeRequirements();
      }
      int insets = TableView.this.getLeftInset() + TableView.this.getRightInset();
      r.minimum = (int) TableView.this.getMinimumSpan(X_AXIS) - insets;
      r.preferred = (int) TableView.this.getPreferredSpan(X_AXIS) - insets;
      r.maximum = (int) TableView.this.getMaximumSpan(X_AXIS) - insets;
      r.alignment = 0f;
      return r;
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
      long min = 0;
      long pref = 0;
      long max = 0;
      for (int i = 0; i < getViewCount(); i++) {
        View v = getView(i);
        if (getRowsOccupied(v) > 1) {
          continue;
        }
        min = Math.max((int) v.getMinimumSpan(axis), min);
        pref = Math.max((int) v.getPreferredSpan(axis), pref);
        max = Math.max((int) v.getMaximumSpan(axis), max);
      }
      if (r == null) {
        r = new SizeRequirements();
        r.alignment = 0.5f;
      }
      r.preferred = (int) pref;
      r.minimum = (int) min;
      r.maximum = (int) max;
      return r;
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      int col = 0;
      int ncells = getViewCount();
      for (int cell = 0; cell < ncells; cell++) {
        View cv = getView(cell);
        if (skipComments && !(cv instanceof CellView)) {
          offsets[cell] = 0;
          spans[cell] = 0;
          continue;
        }
        while (fill.get(col)) {
          col++;
        }
        int colSpan = getColumnsOccupied(cv);
        if (col >= columnSpans.length) {
          offsets[cell] = 0;
          spans[cell] = 0;
          col++;
          continue;
        }
        spans[cell] = columnSpans[col];
        offsets[cell] = columnOffsets[col];
        if (colSpan > 1) {
          int n = columnSpans.length;
          for (int j = 1; j < colSpan; j++) {
            if (col + j < n) {
              spans[cell] += columnSpans[col + j];
              spans[cell] += cellSpacing;
            }
          }
          col += colSpan - 1;
        }
        col++;
      }
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      super.layoutMinorAxis(targetSpan, axis, offsets, spans);
      int ncells = getViewCount();
      for (int cell = 0; cell < ncells; cell++) {
        View cv = getView(cell);
        offsets[cell] = 0;
        spans[cell] = targetSpan;
        int rowSpan = getRowsOccupied(cv);
        if (rowSpan > 1) {
          int row1 = Math.min(rows.size() - 1, rowIndex + rowSpan - 1);
          spans[cell] = getMultiRowSpan(rowIndex, row1);
        }
      }
    }
  }

  class CellView extends BlockView {
    CellView(Element elem) {
      super(elem, View.Y_AXIS);
    }

    public int getResizeWeight(int axis) { return 1; }

    boolean spanSetFromAttributes(int axis, SizeRequirements r) {
      if (axis == Y_AXIS) {
        Object h = getAttributes().getAttribute(CSS.Attribute.HEIGHT);
        CSS.Len len = h == null ? null : CSS.Len.parse(h.toString());
        if (len != null && !len.percent) {
          r.minimum = r.preferred = r.maximum = (int) len.get(false);
          return true;
        }
        return false;
      }
      return false;
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
      SizeRequirements req = super.calculateMajorAxisRequirements(axis, r);
      req.maximum = Integer.MAX_VALUE;
      return req;
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
      SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
      Object w = getElement().getAttributes().getAttribute(HTML.Attribute.WIDTH);
      CSS.Len len = w == null ? null : CSS.Len.parse(w.toString());
      if (len != null && !len.percent) {
        req.preferred = Math.max(req.minimum, (int) len.get(false) - getLeftInset() - getRightInset());
        req.maximum = Math.max(req.maximum, req.preferred);
      }
      if (getElement().getAttributes().getAttribute(HTML.Attribute.NOWRAP) != null) {
        req.minimum = req.preferred;
      }
      return req;
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      super.layoutMajorAxis(targetSpan, axis, offsets, spans);
      int used = 0;
      int n = spans.length;
      for (int i = 0; i < n; i++) {
        used += spans[i];
      }
      int adjust = 0;
      if (used < targetSpan) {
        Object va = getAttributes().getAttribute(CSS.Attribute.VERTICAL_ALIGN);
        String align = va == null ? "middle" : va.toString();
        if (align.equals("top")) {
          adjust = 0;
        } else if (align.equals("bottom")) {
          adjust = targetSpan - used;
        } else {
          adjust = (targetSpan - used) / 2;
        }
      }
      for (int i = 0; i < n; i++) {
        offsets[i] += adjust;
      }
    }
  }
}
