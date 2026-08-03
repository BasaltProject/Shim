/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.BitSet;
import java.util.Vector;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;

public abstract class TableView extends BoxView {
  int[] columnSpans;
  int[] columnOffsets;
  SizeRequirements totalColumnRequirements;
  SizeRequirements[] columnRequirements;
  Vector<TableRow> rows;
  boolean gridValid;
  private static final BitSet EMPTY = new BitSet();

  public TableView(Element elem) {
    super(elem, View.Y_AXIS);
    rows = new Vector<TableRow>();
    gridValid = false;
    totalColumnRequirements = new SizeRequirements();
    columnSpans = new int[0];
    columnOffsets = new int[0];
    columnRequirements = new SizeRequirements[0];
  }

  protected TableRow createTableRow(Element elem) { return new TableRow(elem); }

  @Deprecated
  protected TableCell createTableCell(Element elem) { return new TableCell(elem); }

  int getColumnCount() { return columnSpans.length; }

  int getColumnSpan(int col) { return col < columnSpans.length ? columnSpans[col] : 0; }

  int getRowCount() { return rows.size(); }

  int getMultiRowSpan(int row0, int row1) {
    TableRow rv0 = getRow(row0);
    TableRow rv1 = getRow(row1);
    if (rv0 != null && rv1 != null) {
      int index0 = rv0.viewIndex;
      int index1 = rv1.viewIndex;
      return getOffset(Y_AXIS, index1) - getOffset(Y_AXIS, index0) + getSpan(Y_AXIS, index1);
    }
    return 0;
  }

  int getRowSpan(int row) {
    TableRow rv = getRow(row);
    return rv != null ? getSpan(Y_AXIS, rv.viewIndex) : 0;
  }

  TableRow getRow(int row) { return row >= 0 && row < rows.size() ? rows.elementAt(row) : null; }

  protected View getViewAtPoint(int x, int y, Rectangle alloc) {
    int n = getViewCount();
    View v = null;
    Rectangle allocation = new Rectangle();
    for (int i = 0; i < n; i++) {
      allocation.setBounds(alloc);
      childAllocation(i, allocation);
      v = getView(i);
      if (v instanceof TableRow) {
        v = ((TableRow) v).findViewAtPoint(x, y, allocation);
        if (v != null) {
          alloc.setBounds(allocation);
          return v;
        }
      }
    }
    return super.getViewAtPoint(x, y, alloc);
  }

  protected void layoutColumns(int targetSpan, int[] offsets, int[] spans, SizeRequirements[] reqs) {
    SizeRequirements.calculateTiledPositions(targetSpan, null, reqs, offsets, spans);
  }

  void updateGrid() {
    if (gridValid) {
      return;
    }
    rows.removeAllElements();
    int n = getViewCount();
    for (int i = 0; i < n; i++) {
      View v = getView(i);
      if (v instanceof TableRow) {
        TableRow rv = (TableRow) v;
        rv.clearFilledColumns();
        rows.addElement(rv);
        rv.rowIndex = rows.size() - 1;
        rv.viewIndex = i;
      }
    }
    int maxColumns = 0;
    int nrows = rows.size();
    for (int row = 0; row < nrows; row++) {
      TableRow rv = getRow(row);
      int col = 0;
      for (int cell = 0; cell < rv.getViewCount(); cell++, col++) {
        View cv = rv.getView(cell);
        while (rv.isFilled(col)) {
          col++;
        }
        int rowSpan = 1;
        int colSpan = 1;
        if (cv instanceof GridCell) {
          GridCell gc = (GridCell) cv;
          gc.setGridLocation(row, col);
          rowSpan = gc.getRowCount();
          colSpan = gc.getColumnCount();
        }
        if (colSpan > 1 || rowSpan > 1) {
          int rowLimit = row + rowSpan;
          int colLimit = col + colSpan;
          for (int r = row; r < rowLimit; r++) {
            for (int c = col; c < colLimit; c++) {
              if (r != row || c != col) {
                TableRow target = getRow(r);
                if (target != null) {
                  target.fillColumn(c);
                }
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
    columnRequirements = new SizeRequirements[maxColumns];
    for (int i = 0; i < maxColumns; i++) {
      columnRequirements[i] = new SizeRequirements();
      columnRequirements[i].maximum = Integer.MAX_VALUE;
    }
    gridValid = true;
  }

  void calculateColumnRequirements(int axis) {
    for (SizeRequirements req : columnRequirements) {
      req.minimum = 0;
      req.preferred = 0;
      req.maximum = Integer.MAX_VALUE;
    }
    int nrows = getRowCount();
    for (int i = 0; i < nrows; i++) {
      TableRow row = getRow(i);
      int col = 0;
      for (int cell = 0; cell < row.getViewCount(); cell++, col++) {
        View cv = row.getView(cell);
        while (row.isFilled(col)) {
          col++;
        }
        int colSpan = cv instanceof GridCell ? ((GridCell) cv).getColumnCount() : 1;
        if (colSpan == 1 && col < columnRequirements.length) {
          SizeRequirements req = columnRequirements[col];
          req.minimum = Math.max((int) cv.getMinimumSpan(axis), req.minimum);
          req.preferred = Math.max((int) cv.getPreferredSpan(axis), req.preferred);
        } else {
          col += colSpan - 1;
        }
      }
    }
    long min = 0;
    long pref = 0;
    long max = 0;
    for (SizeRequirements req : columnRequirements) {
      min += req.minimum;
      pref += req.preferred;
      max += req.maximum;
    }
    totalColumnRequirements.minimum = (int) min;
    totalColumnRequirements.preferred = (int) pref;
    totalColumnRequirements.maximum = (int) Math.min(max, Integer.MAX_VALUE);
  }

  protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    updateGrid();
    calculateColumnRequirements(axis);
    if (r == null) {
      r = new SizeRequirements();
    }
    r.minimum = totalColumnRequirements.minimum;
    r.preferred = totalColumnRequirements.preferred;
    r.maximum = totalColumnRequirements.maximum;
    r.alignment = 0.0f;
    return r;
  }

  protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
    updateGrid();
    int n = getRowCount();
    for (int i = 0; i < n; i++) {
      getRow(i).layoutChanged(axis);
    }
    layoutColumns(targetSpan, columnOffsets, columnSpans, columnRequirements);
    super.layoutMinorAxis(targetSpan, axis, offsets, spans);
  }

  protected View getViewAtPosition(int pos, Rectangle a) {
    int n = getViewCount();
    for (int i = 0; i < n; i++) {
      View v = getView(i);
      int p0 = v.getStartOffset();
      int p1 = v.getEndOffset();
      if (pos >= p0 && pos < p1) {
        if (a != null) {
          childAllocation(i, a);
        }
        return v;
      }
    }
    if (pos == getEndOffset()) {
      View v = getView(n - 1);
      if (a != null) {
        childAllocation(n - 1, a);
      }
      return v;
    }
    return null;
  }

  public void replace(int offset, int length, View[] views) {
    super.replace(offset, length, views);
    gridValid = false;
  }

  protected void forwardUpdate(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a, ViewFactory f) {
    super.forwardUpdate(ec, e, a, f);
    if (a != null) {
      java.awt.Container c = getContainer();
      if (c != null) {
        Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
        c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
      }
    }
  }

  public class TableRow extends BoxView {
    BitSet fillColumns = new BitSet();
    int rowIndex;
    int viewIndex;

    public TableRow(Element elem) {
      super(elem, View.X_AXIS);
    }

    void clearFilledColumns() { fillColumns.and(EMPTY); }

    void fillColumn(int col) { fillColumns.set(col); }

    boolean isFilled(int col) { return fillColumns.get(col); }

    int getColumnCount() {
      int nfill = 0;
      int n = fillColumns.size();
      for (int i = 0; i < n; i++) {
        if (fillColumns.get(i)) {
          nfill++;
        }
      }
      return getViewCount() + nfill;
    }

    public void replace(int offset, int length, View[] views) {
      super.replace(offset, length, views);
      invalidateGrid();
    }

    void invalidateGrid() {
      TableView.this.gridValid = false;
    }

    public float getPreferredSpan(int axis) { return super.getPreferredSpan(axis); }

    public float getMinimumSpan(int axis) { return super.getMinimumSpan(axis); }

    public float getMaximumSpan(int axis) { return super.getMaximumSpan(axis); }

    public int getResizeWeight(int axis) { return 1; }

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
      SizeRequirements req = new SizeRequirements();
      req.minimum = totalColumnRequirements.minimum;
      req.maximum = totalColumnRequirements.maximum;
      req.preferred = totalColumnRequirements.preferred;
      req.alignment = 0f;
      return req;
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      int col = 0;
      int ncells = getViewCount();
      for (int cell = 0; cell < ncells; cell++) {
        View cv = getView(cell);
        while (isFilled(col)) {
          col++;
        }
        int colSpan = cv instanceof GridCell ? ((GridCell) cv).getColumnCount() : 1;
        if (col >= columnSpans.length) {
          offsets[cell] = 0;
          spans[cell] = 0;
          continue;
        }
        offsets[cell] = columnOffsets[col];
        spans[cell] = columnSpans[col];
        for (int j = 1; j < colSpan && col + j < columnSpans.length; j++) {
          spans[cell] += columnSpans[col + j];
        }
        col += colSpan;
      }
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      super.layoutMinorAxis(targetSpan, axis, offsets, spans);
      int ncells = getViewCount();
      for (int cell = 0; cell < ncells; cell++) {
        View cv = getView(cell);
        if (cv instanceof GridCell) {
          GridCell gc = (GridCell) cv;
          if (gc.getRowCount() > 1) {
            int row0 = gc.getGridRow();
            int row1 = Math.min(row0 + gc.getRowCount() - 1, getRowCount() - 1);
            spans[cell] = getMultiRowSpan(row0, row1);
          }
        }
      }
    }

    protected View getViewAtPosition(int pos, Rectangle a) {
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        View v = getView(i);
        if (pos >= v.getStartOffset() && pos < v.getEndOffset()) {
          if (a != null) {
            childAllocation(i, a);
          }
          return v;
        }
      }
      return null;
    }
  }

  interface GridCell {
    void setGridLocation(int row, int col);

    int getGridRow();

    int getGridColumn();

    int getColumnCount();

    int getRowCount();
  }

  @Deprecated
  public class TableCell extends BoxView implements GridCell {
    int row;
    int col;

    public TableCell(Element elem) {
      super(elem, View.Y_AXIS);
    }

    public void setGridLocation(int row, int col) {
      this.row = row;
      this.col = col;
    }

    public int getGridRow() { return row; }

    public int getGridColumn() { return col; }

    public int getRowCount() { return 1; }

    public int getColumnCount() { return 1; }
  }
}
