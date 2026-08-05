/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class JTable extends JComponent implements TableModelListener, Scrollable, TableColumnModelListener,
    ListSelectionListener, CellEditorListener, Accessible, RowSorterListener {
  public static final int AUTO_RESIZE_OFF = 0;
  public static final int AUTO_RESIZE_NEXT_COLUMN = 1;
  public static final int AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2;
  public static final int AUTO_RESIZE_LAST_COLUMN = 3;
  public static final int AUTO_RESIZE_ALL_COLUMNS = 4;

  protected TableModel dataModel;
  protected TableColumnModel columnModel;
  protected ListSelectionModel selectionModel;
  protected JTableHeader tableHeader;
  protected int rowHeight;
  protected int rowMargin;
  protected Color gridColor;
  protected boolean showHorizontalLines;
  protected boolean showVerticalLines;
  protected int autoResizeMode;
  protected boolean autoCreateColumnsFromModel;
  protected Dimension preferredViewportSize;
  protected boolean rowSelectionAllowed;
  protected boolean cellSelectionEnabled;
  protected transient Component editorComp;
  protected transient TableCellEditor cellEditor;
  protected transient int editingColumn;
  protected transient int editingRow;
  protected transient Hashtable<Object, Object> defaultRenderersByColumnClass;
  protected transient Hashtable<Object, Object> defaultEditorsByColumnClass;
  protected Color selectionForeground;
  protected Color selectionBackground;

  private SizeSequence rowModel;
  private java.util.BitSet modelSelected = new java.util.BitSet();
  private boolean restoringSelection;
  private boolean dragEnabled;
  private boolean surrendersFocusOnKeystroke;
  private boolean fillsViewportHeight;
  private boolean autoCreateRowSorter;
  private boolean updateSelectionOnSort;
  private boolean ignoreSortChange;
  private RowSorter<? extends TableModel> sorter;
  private DropMode dropMode = DropMode.USE_SELECTION;
  private transient DropLocation dropLocation;
  private AccessibleContext accessibleContext;

  public JTable() { this(null, null, null); }

  public JTable(TableModel dm) { this(dm, null, null); }

  public JTable(TableModel dm, TableColumnModel cm) { this(dm, cm, null); }

  public JTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
    super();
    setLayout(null);
    if (cm == null) {
      cm = createDefaultColumnModel();
      autoCreateColumnsFromModel = true;
    }
    setColumnModel(cm);
    if (sm == null) {
      sm = createDefaultSelectionModel();
    }
    setSelectionModel(sm);
    if (dm == null) {
      dm = createDefaultDataModel();
    }
    setModel(dm);
    initializeLocalVars();
    updateUI();
  }

  public JTable(int numRows, int numColumns) { this(new DefaultTableModel(numRows, numColumns)); }

  public JTable(Vector<? extends Vector> rowData, Vector<?> columnNames) {
    this(new DefaultTableModel(rowData, columnNames));
  }

  public JTable(final Object[][] rowData, final Object[] columnNames) {
    this(new AbstractTableModel() {
      public String getColumnName(int column) { return columnNames[column].toString(); }

      public int getRowCount() { return rowData.length; }

      public int getColumnCount() { return columnNames.length; }

      public Object getValueAt(int row, int col) { return rowData[row][col]; }

      public boolean isCellEditable(int row, int column) { return true; }

      public void setValueAt(Object value, int row, int col) {
        rowData[row][col] = value;
        fireTableCellUpdated(row, col);
      }
    });
  }

  public void addNotify() {
    super.addNotify();
    configureEnclosingScrollPane();
  }

  protected void configureEnclosingScrollPane() {
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      JViewport port = (JViewport) parent;
      Container gp = port.getParent();
      if (gp instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) gp;
        JViewport viewport = scrollPane.getViewport();
        if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
          return;
        }
        scrollPane.setColumnHeaderView(getTableHeader());
        Border border = scrollPane.getBorder();
        if (border == null || border instanceof UIResource) {
          Border scrollPaneBorder = UIManager.getBorder("Table.scrollPaneBorder");
          if (scrollPaneBorder != null) {
            scrollPane.setBorder(scrollPaneBorder);
          }
        }
      }
    }
  }

  public void removeNotify() {
    unconfigureEnclosingScrollPane();
    super.removeNotify();
  }

  protected void unconfigureEnclosingScrollPane() {
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      JViewport port = (JViewport) parent;
      Container gp = port.getParent();
      if (gp instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) gp;
        JViewport viewport = scrollPane.getViewport();
        if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
          return;
        }
        scrollPane.setColumnHeaderView(null);
        if (scrollPane.getBorder() instanceof UIResource) {
          scrollPane.setBorder(null);
        }
      }
    }
  }

  public static JScrollPane createScrollPaneForTable(JTable aTable) { return new JScrollPane(aTable); }

  public void setTableHeader(JTableHeader tableHeader) {
    if (this.tableHeader != tableHeader) {
      JTableHeader old = this.tableHeader;
      if (old != null) {
        old.setTable(null);
      }
      this.tableHeader = tableHeader;
      if (tableHeader != null) {
        tableHeader.setTable(this);
      }
      firePropertyChange("tableHeader", old, tableHeader);
    }
  }

  public JTableHeader getTableHeader() { return tableHeader; }

  public void setRowHeight(int rowHeight) {
    if (rowHeight <= 0) {
      throw new IllegalArgumentException("New row height less than 1");
    }
    int old = this.rowHeight;
    this.rowHeight = rowHeight;
    rowModel = null;
    resizeAndRepaint();
    firePropertyChange("rowHeight", old, rowHeight);
  }

  public int getRowHeight() { return rowHeight; }

  private SizeSequence getRowModel() {
    if (rowModel == null) {
      rowModel = new SizeSequence(getRowCount(), getRowHeight());
    }
    return rowModel;
  }

  public void setRowHeight(int row, int rowHeight) {
    if (rowHeight <= 0) {
      throw new IllegalArgumentException("New row height less than 1");
    }
    getRowModel().setSize(row, rowHeight);
    resizeAndRepaint();
  }

  public int getRowHeight(int row) { return (rowModel == null) ? getRowHeight() : rowModel.getSize(row); }

  public void setRowMargin(int rowMargin) {
    int old = this.rowMargin;
    this.rowMargin = rowMargin;
    resizeAndRepaint();
    firePropertyChange("rowMargin", old, rowMargin);
  }

  public int getRowMargin() { return rowMargin; }

  public void setIntercellSpacing(Dimension intercellSpacing) {
    setRowMargin(intercellSpacing.height);
    getColumnModel().setColumnMargin(intercellSpacing.width);
    resizeAndRepaint();
  }

  public Dimension getIntercellSpacing() { return new Dimension(getColumnModel().getColumnMargin(), rowMargin); }

  public void setGridColor(Color gridColor) {
    if (gridColor == null) {
      throw new IllegalArgumentException("New color is null");
    }
    Color old = this.gridColor;
    this.gridColor = gridColor;
    firePropertyChange("gridColor", old, gridColor);
    repaint();
  }

  public Color getGridColor() { return gridColor; }

  public void setShowGrid(boolean showGrid) {
    setShowHorizontalLines(showGrid);
    setShowVerticalLines(showGrid);
    repaint();
  }

  public void setShowHorizontalLines(boolean showHorizontalLines) {
    boolean old = this.showHorizontalLines;
    this.showHorizontalLines = showHorizontalLines;
    firePropertyChange("showHorizontalLines", old, showHorizontalLines);
    repaint();
  }

  public void setShowVerticalLines(boolean showVerticalLines) {
    boolean old = this.showVerticalLines;
    this.showVerticalLines = showVerticalLines;
    firePropertyChange("showVerticalLines", old, showVerticalLines);
    repaint();
  }

  public boolean getShowHorizontalLines() { return showHorizontalLines; }

  public boolean getShowVerticalLines() { return showVerticalLines; }

  public void setAutoResizeMode(int mode) {
    if ((mode == AUTO_RESIZE_OFF) || (mode == AUTO_RESIZE_NEXT_COLUMN) || (mode == AUTO_RESIZE_SUBSEQUENT_COLUMNS)
        || (mode == AUTO_RESIZE_LAST_COLUMN) || (mode == AUTO_RESIZE_ALL_COLUMNS)) {
      int old = autoResizeMode;
      autoResizeMode = mode;
      resizeAndRepaint();
      firePropertyChange("autoResizeMode", old, autoResizeMode);
    }
  }

  public int getAutoResizeMode() { return autoResizeMode; }

  public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
    if (this.autoCreateColumnsFromModel != autoCreateColumnsFromModel) {
      boolean old = this.autoCreateColumnsFromModel;
      this.autoCreateColumnsFromModel = autoCreateColumnsFromModel;
      if (autoCreateColumnsFromModel) {
        createDefaultColumnsFromModel();
      }
      firePropertyChange("autoCreateColumnsFromModel", old, autoCreateColumnsFromModel);
    }
  }

  public boolean getAutoCreateColumnsFromModel() { return autoCreateColumnsFromModel; }

  public void createDefaultColumnsFromModel() {
    TableModel m = getModel();
    if (m != null) {
      TableColumnModel cm = getColumnModel();
      while (cm.getColumnCount() > 0) {
        cm.removeColumn(cm.getColumn(0));
      }
      for (int i = 0; i < m.getColumnCount(); i++) {
        TableColumn newColumn = new TableColumn(i);
        addColumn(newColumn);
      }
    }
  }

  public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {
    if (renderer != null) {
      defaultRenderersByColumnClass.put(columnClass, renderer);
    } else {
      defaultRenderersByColumnClass.remove(columnClass);
    }
  }

  public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
    if (columnClass == null) {
      return null;
    }
    Object renderer = defaultRenderersByColumnClass.get(columnClass);
    if (renderer != null) {
      return (TableCellRenderer) renderer;
    }
    Class<?> c = columnClass.getSuperclass();
    if (c == null && columnClass != Object.class) {
      c = Object.class;
    }
    return getDefaultRenderer(c);
  }

  public void setDefaultEditor(Class<?> columnClass, TableCellEditor editor) {
    if (editor != null) {
      defaultEditorsByColumnClass.put(columnClass, editor);
    } else {
      defaultEditorsByColumnClass.remove(columnClass);
    }
  }

  public TableCellEditor getDefaultEditor(Class<?> columnClass) {
    if (columnClass == null) {
      return null;
    }
    Object editor = defaultEditorsByColumnClass.get(columnClass);
    if (editor != null) {
      return (TableCellEditor) editor;
    }
    return getDefaultEditor(columnClass.getSuperclass());
  }

  public void setDragEnabled(boolean b) {
    if (b && GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    dragEnabled = b;
  }

  public boolean getDragEnabled() { return dragEnabled; }

  public final void setDropMode(DropMode dropMode) {
    this.dropMode = dropMode;
  }

  public final DropMode getDropMode() { return dropMode; }

  public final DropLocation getDropLocation() { return dropLocation; }

  public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
    boolean oldValue = this.autoCreateRowSorter;
    this.autoCreateRowSorter = autoCreateRowSorter;
    if (autoCreateRowSorter) {
      setRowSorter(new TableRowSorter<TableModel>(getModel()));
    }
    firePropertyChange("autoCreateRowSorter", oldValue, autoCreateRowSorter);
  }

  public boolean getAutoCreateRowSorter() { return autoCreateRowSorter; }

  public void setUpdateSelectionOnSort(boolean update) {
    if (updateSelectionOnSort != update) {
      updateSelectionOnSort = update;
      firePropertyChange("updateSelectionOnSort", !update, update);
    }
  }

  public boolean getUpdateSelectionOnSort() { return updateSelectionOnSort; }

  public void setRowSorter(RowSorter<? extends TableModel> sorter) {
    RowSorter<? extends TableModel> oldRowSorter = this.sorter;
    if (oldRowSorter != null) {
      oldRowSorter.removeRowSorterListener(this);
    }
    this.sorter = sorter;
    rowModel = null;
    clearSelectionAndLeadAnchor();
    if (sorter != null) {
      sorter.addRowSorterListener(this);
    }
    resizeAndRepaint();
    firePropertyChange("sorter", oldRowSorter, sorter);
  }

  public RowSorter<? extends TableModel> getRowSorter() { return sorter; }

  public void setSelectionMode(int selectionMode) {
    clearSelection();
    getSelectionModel().setSelectionMode(selectionMode);
    getColumnModel().getSelectionModel().setSelectionMode(selectionMode);
  }

  public void setRowSelectionAllowed(boolean rowSelectionAllowed) {
    boolean old = this.rowSelectionAllowed;
    this.rowSelectionAllowed = rowSelectionAllowed;
    if (old != rowSelectionAllowed) {
      repaint();
    }
    firePropertyChange("rowSelectionAllowed", old, rowSelectionAllowed);
  }

  public boolean getRowSelectionAllowed() { return rowSelectionAllowed; }

  public void setColumnSelectionAllowed(boolean columnSelectionAllowed) {
    boolean old = columnModel.getColumnSelectionAllowed();
    columnModel.setColumnSelectionAllowed(columnSelectionAllowed);
    if (old != columnSelectionAllowed) {
      repaint();
    }
    firePropertyChange("columnSelectionAllowed", old, columnSelectionAllowed);
  }

  public boolean getColumnSelectionAllowed() { return columnModel.getColumnSelectionAllowed(); }

  public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
    setRowSelectionAllowed(cellSelectionEnabled);
    setColumnSelectionAllowed(cellSelectionEnabled);
    boolean old = this.cellSelectionEnabled;
    this.cellSelectionEnabled = cellSelectionEnabled;
    firePropertyChange("cellSelectionEnabled", old, cellSelectionEnabled);
  }

  public boolean getCellSelectionEnabled() { return getRowSelectionAllowed() && getColumnSelectionAllowed(); }

  public void selectAll() {
    if (isEditing()) {
      removeEditor();
    }
    if (getRowCount() > 0 && getColumnCount() > 0) {
      ListSelectionModel selModel = selectionModel;
      selModel.setValueIsAdjusting(true);
      int oldLead = adjusted(selModel.getLeadSelectionIndex(), true);
      int oldAnchor = adjusted(selModel.getAnchorSelectionIndex(), true);
      setRowSelectionInterval(0, getRowCount() - 1);
      restoreLeadAnchor(selModel, oldLead, oldAnchor);
      selModel.setValueIsAdjusting(false);
      selModel = columnModel.getSelectionModel();
      selModel.setValueIsAdjusting(true);
      oldLead = adjusted(selModel.getLeadSelectionIndex(), false);
      oldAnchor = adjusted(selModel.getAnchorSelectionIndex(), false);
      setColumnSelectionInterval(0, getColumnCount() - 1);
      restoreLeadAnchor(selModel, oldLead, oldAnchor);
      selModel.setValueIsAdjusting(false);
    }
  }

  private static void restoreLeadAnchor(ListSelectionModel m, int lead, int anchor) {
    if (m instanceof DefaultListSelectionModel) {
      DefaultListSelectionModel d = (DefaultListSelectionModel) m;
      boolean old = d.isLeadAnchorNotificationEnabled();
      d.setLeadAnchorNotificationEnabled(false);
      d.setAnchorSelectionIndex(anchor);
      d.moveLeadSelectionIndex(lead);
      d.setLeadAnchorNotificationEnabled(old);
    }
  }

  public void clearSelection() {
    selectionModel.clearSelection();
    columnModel.getSelectionModel().clearSelection();
  }

  private void clearSelectionAndLeadAnchor() {
    selectionModel.setValueIsAdjusting(true);
    columnModel.getSelectionModel().setValueIsAdjusting(true);
    clearSelection();
    selectionModel.setAnchorSelectionIndex(-1);
    selectionModel.setLeadSelectionIndex(-1);
    columnModel.getSelectionModel().setAnchorSelectionIndex(-1);
    columnModel.getSelectionModel().setLeadSelectionIndex(-1);
    selectionModel.setValueIsAdjusting(false);
    columnModel.getSelectionModel().setValueIsAdjusting(false);
  }

  private int adjusted(int index, boolean row) {
    return index >= (row ? getRowCount() : getColumnCount()) ? -1 : index;
  }

  public int getSelectedRow() { return selectionModel.getMinSelectionIndex(); }

  public int getSelectedColumn() { return columnModel.getSelectionModel().getMinSelectionIndex(); }

  public int[] getSelectedRows() {
    int iMin = selectionModel.getMinSelectionIndex();
    int iMax = selectionModel.getMaxSelectionIndex();
    if ((iMin == -1) || (iMax == -1)) {
      return new int[0];
    }
    int[] rvTmp = new int[1 + (iMax - iMin)];
    int n = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (selectionModel.isSelectedIndex(i)) {
        rvTmp[n++] = i;
      }
    }
    int[] rv = new int[n];
    System.arraycopy(rvTmp, 0, rv, 0, n);
    return rv;
  }

  public int[] getSelectedColumns() { return columnModel.getSelectedColumns(); }

  public int getSelectedRowCount() {
    int iMin = selectionModel.getMinSelectionIndex();
    int iMax = selectionModel.getMaxSelectionIndex();
    int count = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (selectionModel.isSelectedIndex(i)) {
        count++;
      }
    }
    return count;
  }

  public int getSelectedColumnCount() { return columnModel.getSelectedColumnCount(); }

  public boolean isRowSelected(int row) { return selectionModel.isSelectedIndex(row); }

  public boolean isColumnSelected(int column) { return columnModel.getSelectionModel().isSelectedIndex(column); }

  public boolean isCellSelected(int row, int column) {
    if (!getRowSelectionAllowed() && !getColumnSelectionAllowed()) {
      return false;
    }
    return (!getRowSelectionAllowed() || isRowSelected(row)) && (!getColumnSelectionAllowed() || isColumnSelected(column));
  }

  private void changeSelectionModel(ListSelectionModel sm, int index, boolean toggle, boolean extend,
      boolean selected, int anchor, boolean anchorSelected) {
    if (extend) {
      if (toggle) {
        if (anchorSelected) {
          sm.addSelectionInterval(anchor, index);
        } else {
          sm.removeSelectionInterval(anchor, index);
          if (Boolean.TRUE.equals(getClientProperty("Table.isFileList"))) {
            sm.addSelectionInterval(index, index);
            sm.setAnchorSelectionIndex(anchor);
          }
        }
      } else {
        sm.setSelectionInterval(anchor, index);
      }
    } else {
      if (toggle) {
        if (selected) {
          sm.removeSelectionInterval(index, index);
        } else {
          sm.addSelectionInterval(index, index);
        }
      } else {
        sm.setSelectionInterval(index, index);
      }
    }
  }

  public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    ListSelectionModel rsm = getSelectionModel();
    ListSelectionModel csm = getColumnModel().getSelectionModel();
    int anchorRow = adjusted(rsm.getAnchorSelectionIndex(), true);
    int anchorCol = adjusted(csm.getAnchorSelectionIndex(), false);
    boolean anchorSelected = true;
    if (anchorRow == -1) {
      if (getRowCount() > 0) {
        anchorRow = 0;
      }
      anchorSelected = false;
    }
    if (anchorCol == -1) {
      if (getColumnCount() > 0) {
        anchorCol = 0;
      }
      anchorSelected = false;
    }
    boolean selected = isCellSelected(rowIndex, columnIndex);
    anchorSelected = anchorSelected && isCellSelected(anchorRow, anchorCol);
    changeSelectionModel(csm, columnIndex, toggle, extend, selected, anchorCol, anchorSelected);
    changeSelectionModel(rsm, rowIndex, toggle, extend, selected, anchorRow, anchorSelected);
  }

  public void setRowSelectionInterval(int index0, int index1) {
    checkRow(index0);
    checkRow(index1);
    selectionModel.setSelectionInterval(index0, index1);
  }

  public void setColumnSelectionInterval(int index0, int index1) {
    checkColumn(index0);
    checkColumn(index1);
    columnModel.getSelectionModel().setSelectionInterval(index0, index1);
  }

  public void addRowSelectionInterval(int index0, int index1) {
    checkRow(index0);
    checkRow(index1);
    selectionModel.addSelectionInterval(index0, index1);
  }

  public void addColumnSelectionInterval(int index0, int index1) {
    checkColumn(index0);
    checkColumn(index1);
    columnModel.getSelectionModel().addSelectionInterval(index0, index1);
  }

  public void removeRowSelectionInterval(int index0, int index1) {
    checkRow(index0);
    checkRow(index1);
    selectionModel.removeSelectionInterval(index0, index1);
  }

  public void removeColumnSelectionInterval(int index0, int index1) {
    checkColumn(index0);
    checkColumn(index1);
    columnModel.getSelectionModel().removeSelectionInterval(index0, index1);
  }

  private void checkRow(int index) {
    if (index < 0 || index >= getRowCount()) {
      throw new IllegalArgumentException("Row index out of range");
    }
  }

  private void checkColumn(int index) {
    if (index < 0 || index >= getColumnCount()) {
      throw new IllegalArgumentException("Column index out of range");
    }
  }

  public Color getSelectionForeground() { return selectionForeground; }

  public void setSelectionForeground(Color selectionForeground) {
    Color old = this.selectionForeground;
    this.selectionForeground = selectionForeground;
    firePropertyChange("selectionForeground", old, selectionForeground);
    repaint();
  }

  public Color getSelectionBackground() { return selectionBackground; }

  public void setSelectionBackground(Color selectionBackground) {
    Color old = this.selectionBackground;
    this.selectionBackground = selectionBackground;
    firePropertyChange("selectionBackground", old, selectionBackground);
    repaint();
  }

  public TableColumn getColumn(Object identifier) {
    TableColumnModel cm = getColumnModel();
    int columnIndex = cm.getColumnIndex(identifier);
    return cm.getColumn(columnIndex);
  }

  public int convertColumnIndexToModel(int viewColumnIndex) {
    return viewColumnIndex < 0 ? viewColumnIndex : getColumnModel().getColumn(viewColumnIndex).getModelIndex();
  }

  public int convertColumnIndexToView(int modelColumnIndex) {
    if (modelColumnIndex < 0) {
      return modelColumnIndex;
    }
    TableColumnModel cm = getColumnModel();
    for (int column = 0; column < getColumnCount(); column++) {
      if (cm.getColumn(column).getModelIndex() == modelColumnIndex) {
        return column;
      }
    }
    return -1;
  }

  public int convertRowIndexToView(int modelRowIndex) {
    RowSorter<?> s = getRowSorter();
    if (s != null) {
      return s.convertRowIndexToView(modelRowIndex);
    }
    return modelRowIndex;
  }

  public int convertRowIndexToModel(int viewRowIndex) {
    RowSorter<?> s = getRowSorter();
    if (s != null) {
      return s.convertRowIndexToModel(viewRowIndex);
    }
    return viewRowIndex;
  }

  public int getRowCount() {
    RowSorter<?> s = getRowSorter();
    if (s != null) {
      return s.getViewRowCount();
    }
    return getModel().getRowCount();
  }

  public int getColumnCount() { return getColumnModel().getColumnCount(); }

  public String getColumnName(int column) { return getModel().getColumnName(convertColumnIndexToModel(column)); }

  public Class<?> getColumnClass(int column) { return getModel().getColumnClass(convertColumnIndexToModel(column)); }

  public Object getValueAt(int row, int column) {
    return getModel().getValueAt(convertRowIndexToModel(row), convertColumnIndexToModel(column));
  }

  public void setValueAt(Object aValue, int row, int column) {
    getModel().setValueAt(aValue, convertRowIndexToModel(row), convertColumnIndexToModel(column));
  }

  public boolean isCellEditable(int row, int column) {
    return getModel().isCellEditable(convertRowIndexToModel(row), convertColumnIndexToModel(column));
  }

  public void addColumn(TableColumn aColumn) {
    if (aColumn.getHeaderValue() == null) {
      int modelColumn = aColumn.getModelIndex();
      String columnName = getModel().getColumnName(modelColumn);
      aColumn.setHeaderValue(columnName);
    }
    getColumnModel().addColumn(aColumn);
  }

  public void removeColumn(TableColumn aColumn) { getColumnModel().removeColumn(aColumn); }

  public void moveColumn(int column, int targetColumn) { getColumnModel().moveColumn(column, targetColumn); }

  public int rowAtPoint(Point point) {
    int y = point.y;
    int result = (rowModel == null) ? y / getRowHeight() : rowModel.getIndex(y);
    if (y < 0) {
      return -1;
    }
    if (result < 0) {
      return -1;
    } else if (result >= getRowCount()) {
      return -1;
    } else {
      return result;
    }
  }

  public int columnAtPoint(Point point) {
    int x = point.x;
    if (!getComponentOrientation().isLeftToRight()) {
      x = getWidth() - x - 1;
    }
    return getColumnModel().getColumnIndexAtX(x);
  }

  public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
    Rectangle r = new Rectangle();
    boolean valid = true;
    if (row < 0) {
      valid = false;
    } else if (row >= getRowCount()) {
      r.y = getHeight();
      valid = false;
    } else {
      r.height = getRowHeight(row);
      r.y = (rowModel == null) ? row * r.height : rowModel.getPosition(row);
    }
    if (column < 0) {
      if (!getComponentOrientation().isLeftToRight()) {
        r.x = getWidth();
      }
      valid = false;
    } else if (column >= getColumnCount()) {
      if (getComponentOrientation().isLeftToRight()) {
        r.x = getWidth();
      }
      valid = false;
    } else {
      TableColumnModel cm = getColumnModel();
      if (getComponentOrientation().isLeftToRight()) {
        for (int i = 0; i < column; i++) {
          r.x += cm.getColumn(i).getWidth();
        }
      } else {
        for (int i = cm.getColumnCount() - 1; i > column; i--) {
          r.x += cm.getColumn(i).getWidth();
        }
      }
      r.width = cm.getColumn(column).getWidth();
    }
    if (valid && !includeSpacing) {
      int rm = Math.min(getRowMargin(), r.height);
      int cm = Math.min(getColumnModel().getColumnMargin(), r.width);
      r.setBounds(r.x + cm / 2, r.y + rm / 2, r.width - cm, r.height - rm);
    }
    return r;
  }

  private interface Spans {
    int count();

    int low(int i);

    int mid(int i);

    int high(int i);

    void put(int size, int i);
  }

  private interface Limited {
    int count();

    int low(int i);

    int high(int i);

    void put(int size, int i);
  }

  public void sizeColumnsToFit(boolean lastColumnOnly) {
    int oldAutoResizeMode = autoResizeMode;
    setAutoResizeMode(lastColumnOnly ? AUTO_RESIZE_LAST_COLUMN : AUTO_RESIZE_ALL_COLUMNS);
    sizeColumnsToFit(-1);
    setAutoResizeMode(oldAutoResizeMode);
  }

  public void sizeColumnsToFit(int resizingColumn) {
    if (resizingColumn == -1) {
      setWidthsFromPreferredWidths(false);
    }
  }

  private void setWidthsFromPreferredWidths(final boolean inverse) {
    int totalWidth = getWidth();
    int totalPreferred = getPreferredSize().width;
    int target = !inverse ? totalWidth : totalPreferred;
    final TableColumnModel cm = columnModel;
    Spans r = new Spans() {
      public int count() { return cm.getColumnCount(); }

      public int low(int i) { return cm.getColumn(i).getMinWidth(); }

      public int high(int i) { return cm.getColumn(i).getMaxWidth(); }

      public int mid(int i) {
        TableColumn column = cm.getColumn(i);
        return inverse ? column.getWidth() : column.getPreferredWidth();
      }

      public void put(int size, int i) { cm.getColumn(i).setWidth(size); }
    };
    fitSpans(target, r, inverse);
  }

  private void fitSpans(long target, final Spans r, boolean inverse) {
    final int n = r.count();
    long totalPreferred = 0;
    for (int i = 0; i < n; i++) {
      totalPreferred += r.mid(i);
    }
    Limited s;
    if ((target < totalPreferred) == !inverse) {
      s = new Limited() {
        public int count() { return n; }

        public int low(int i) { return r.low(i); }

        public int high(int i) { return r.mid(i); }

        public void put(int size, int i) { r.put(size, i); }
      };
    } else {
      s = new Limited() {
        public int count() { return n; }

        public int low(int i) { return r.mid(i); }

        public int high(int i) { return r.high(i); }

        public void put(int size, int i) { r.put(size, i); }
      };
    }
    spread(target, s, !inverse);
  }

  private void spread(long target, Limited r, boolean limit) {
    long totalLow = 0;
    long totalHigh = 0;
    for (int i = 0; i < r.count(); i++) {
      totalLow += r.low(i);
      totalHigh += r.high(i);
    }
    if (limit) {
      target = Math.min(Math.max(totalLow, target), totalHigh);
    }
    for (int i = 0; i < r.count(); i++) {
      int lo = r.low(i);
      int hi = r.high(i);
      double ratio = (totalLow == totalHigh) ? 0.0 : (double) (target - totalLow) / (totalHigh - totalLow);
      int newSize = (int) Math.round(lo + ratio * (hi - lo));
      r.put(newSize, i);
      target -= newSize;
      totalLow -= lo;
      totalHigh -= hi;
    }
  }

  public void doLayout() {
    setWidthsFromPreferredWidths(false);
    if (tableHeader != null) {
      tableHeader.resizeAndRepaint();
    }
  }

  public Dimension getPreferredSize() {
    if (getUI() != null) {
      Dimension d = getUI().getPreferredSize(this);
      if (d != null) {
        return d;
      }
    }
    long width = 0;
    Enumeration<TableColumn> enumeration = getColumnModel().getColumns();
    while (enumeration.hasMoreElements()) {
      TableColumn aColumn = enumeration.nextElement();
      width = width + aColumn.getPreferredWidth();
    }
    int height = (rowModel == null) ? getRowCount() * getRowHeight() : rowModel.getPosition(getRowCount());
    return new Dimension((int) Math.min(width, Integer.MAX_VALUE), height);
  }

  protected void resizeAndRepaint() {
    revalidate();
    repaint();
  }

  public TableCellEditor getCellEditor() { return cellEditor; }

  public void setCellEditor(TableCellEditor anEditor) {
    TableCellEditor old = cellEditor;
    cellEditor = anEditor;
    firePropertyChange("tableCellEditor", old, anEditor);
  }

  public void setEditingColumn(int aColumn) { editingColumn = aColumn; }

  public void setEditingRow(int aRow) { editingRow = aRow; }

  public TableCellRenderer getCellRenderer(int row, int column) {
    TableColumn tableColumn = getColumnModel().getColumn(column);
    TableCellRenderer renderer = tableColumn.getCellRenderer();
    if (renderer == null) {
      renderer = getDefaultRenderer(getColumnClass(column));
    }
    return renderer;
  }

  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Object value = getValueAt(row, column);
    boolean isSelected = false;
    boolean hasFocus = false;
    if (!isPaintingForPrint()) {
      isSelected = isCellSelected(row, column);
      boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
      boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
      hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
    }
    return renderer.getTableCellRendererComponent(this, value, isSelected, hasFocus, row, column);
  }

  public TableCellEditor getCellEditor(int row, int column) {
    TableColumn tableColumn = getColumnModel().getColumn(column);
    TableCellEditor editor = tableColumn.getCellEditor();
    if (editor == null) {
      editor = getDefaultEditor(getColumnClass(column));
    }
    return editor;
  }

  public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Object value = getValueAt(row, column);
    boolean isSelected = isCellSelected(row, column);
    Component comp = editor.getTableCellEditorComponent(this, value, isSelected, row, column);
    if (comp instanceof JComponent) {
      JComponent jComp = (JComponent) comp;
      if (jComp.getNextFocusableComponent() == null) {
        jComp.setNextFocusableComponent(this);
      }
    }
    return comp;
  }

  public void removeEditor() {
    TableCellEditor editor = getCellEditor();
    if (editor != null) {
      editor.removeCellEditorListener(this);
      if (editorComp != null) {
        remove(editorComp);
      }
      Rectangle cellRect = getCellRect(editingRow, editingColumn, false);
      setCellEditor(null);
      setEditingColumn(-1);
      setEditingRow(-1);
      editorComp = null;
      repaint(cellRect);
    }
  }

  public boolean editCellAt(int row, int column) { return editCellAt(row, column, null); }

  public boolean editCellAt(int row, int column, EventObject e) {
    if (cellEditor != null && !cellEditor.stopCellEditing()) {
      return false;
    }
    if (row < 0 || row >= getRowCount() || column < 0 || column >= getColumnCount()) {
      return false;
    }
    if (!isCellEditable(row, column)) {
      return false;
    }
    TableCellEditor editor = getCellEditor(row, column);
    if (editor != null && editor.isCellEditable(e)) {
      editorComp = prepareEditor(editor, row, column);
      if (editorComp == null) {
        removeEditor();
        return false;
      }
      editorComp.setBounds(getCellRect(row, column, false));
      add(editorComp);
      editorComp.validate();
      editorComp.repaint();
      setCellEditor(editor);
      setEditingRow(row);
      setEditingColumn(column);
      editor.addCellEditorListener(this);
      return true;
    }
    return false;
  }

  public boolean isEditing() { return cellEditor != null; }

  public Component getEditorComponent() { return editorComp; }

  public int getEditingColumn() { return editingColumn; }

  public int getEditingRow() { return editingRow; }

  public void editingStopped(ChangeEvent e) {
    TableCellEditor editor = getCellEditor();
    if (editor != null) {
      Object value = editor.getCellEditorValue();
      setValueAt(value, editingRow, editingColumn);
      removeEditor();
    }
  }

  public void editingCanceled(ChangeEvent e) { removeEditor(); }

  public TableUI getUI() { return (TableUI) ui; }

  public void setUI(TableUI ui) {
    if (this.ui != ui) {
      super.setUI(ui);
      repaint();
    }
  }

  public void updateUI() {
    if (ui == null) {
      TableCellRenderer headerRenderer = null;
    }
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
    if (gridColor == null || gridColor instanceof UIResource) {
      Color c = UIManager.getColor("Table.gridColor");
      if (c != null) {
        gridColor = c;
      }
    }
    if (selectionBackground == null || selectionBackground instanceof UIResource) {
      selectionBackground = UIManager.getColor("Table.selectionBackground");
    }
    if (selectionForeground == null || selectionForeground instanceof UIResource) {
      selectionForeground = UIManager.getColor("Table.selectionForeground");
    }
    if (tableHeader != null && tableHeader.getParent() == null) {
      tableHeader.updateUI();
    }
    setUpDefaultRenderers();
    setUpDefaultEditors();
  }

  private void setUpDefaultRenderers() {
    if (defaultRenderersByColumnClass != null) {
      for (Object r : defaultRenderersByColumnClass.values()) {
        if (r instanceof Component) {
          SwingUtilities.updateComponentTreeUI((Component) r);
        }
      }
    }
  }

  private void setUpDefaultEditors() { }

  public String getUIClassID() { return "TableUI"; }

  public void setModel(TableModel dataModel) {
    if (dataModel == null) {
      throw new IllegalArgumentException("Cannot set a null TableModel");
    }
    if (this.dataModel != dataModel) {
      TableModel old = this.dataModel;
      if (old != null) {
        old.removeTableModelListener(this);
      }
      this.dataModel = dataModel;
      dataModel.addTableModelListener(this);
      tableChanged(new TableModelEvent(dataModel, TableModelEvent.HEADER_ROW));
      firePropertyChange("model", old, dataModel);
      if (getAutoCreateRowSorter()) {
        setRowSorter(new TableRowSorter<TableModel>(dataModel));
      }
    }
  }

  public TableModel getModel() { return dataModel; }

  public void setColumnModel(TableColumnModel columnModel) {
    if (columnModel == null) {
      throw new IllegalArgumentException("Cannot set a null ColumnModel");
    }
    TableColumnModel old = this.columnModel;
    if (columnModel != old) {
      if (old != null) {
        old.removeColumnModelListener(this);
      }
      this.columnModel = columnModel;
      columnModel.addColumnModelListener(this);
      if (tableHeader != null) {
        tableHeader.setColumnModel(columnModel);
      }
      firePropertyChange("columnModel", old, columnModel);
      resizeAndRepaint();
    }
  }

  public TableColumnModel getColumnModel() { return columnModel; }

  public void setSelectionModel(ListSelectionModel newModel) {
    if (newModel == null) {
      throw new IllegalArgumentException("Cannot set a null SelectionModel");
    }
    ListSelectionModel oldModel = selectionModel;
    if (newModel != oldModel) {
      if (oldModel != null) {
        oldModel.removeListSelectionListener(this);
      }
      selectionModel = newModel;
      newModel.addListSelectionListener(this);
      firePropertyChange("selectionModel", oldModel, newModel);
      repaint();
    }
  }

  public ListSelectionModel getSelectionModel() { return selectionModel; }

  public void sorterChanged(RowSorterEvent e) {
    if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
      JTableHeader h = getTableHeader();
      if (h != null) {
        h.repaint();
      }
    } else if (e.getType() == RowSorterEvent.Type.SORTED) {
      rowModel = null;
      if (!ignoreSortChange) {
        sortedChanged(e);
      }
    }
  }

  private void sortedChanged(RowSorterEvent e) {
    if (getUpdateSelectionOnSort()) {
      restoreFromModelSelection();
    } else {
      restoringSelection = true;
      try {
        clearSelectionAndLeadAnchor();
      } finally {
        restoringSelection = false;
      }
      modelSelected.clear();
    }
    resizeAndRepaint();
  }

  public void tableChanged(TableModelEvent e) {
    if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
      clearSelectionAndLeadAnchor();
      rowModel = null;
      if (sorter != null) {
        try {
          ignoreSortChange = true;
          sorter.modelStructureChanged();
        } finally {
          ignoreSortChange = false;
        }
      }
      if (getAutoCreateColumnsFromModel()) {
        createDefaultColumnsFromModel();
        return;
      }
      resizeAndRepaint();
      return;
    }
    if (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE) {
      clearSelectionAndLeadAnchor();
      rowModel = null;
      if (sorter != null) {
        try {
          ignoreSortChange = true;
          sorter.allRowsChanged();
        } finally {
          ignoreSortChange = false;
        }
      }
      repaint();
      return;
    }
    if (sorter != null) {
      sortedTableChanged(e);
      return;
    }
    if (e.getType() == TableModelEvent.INSERT) {
      tableRowsInserted(e);
      return;
    }
    if (e.getType() == TableModelEvent.DELETE) {
      tableRowsDeleted(e);
      return;
    }
    repaint();
  }

  private void sortedTableChanged(TableModelEvent e) {
    int modelColumn = e.getColumn();
    int start = e.getFirstRow();
    int end = e.getLastRow();
    java.util.BitSet before = (java.util.BitSet) modelSelected.clone();
    try {
      ignoreSortChange = true;
      if (e.getType() == TableModelEvent.INSERT) {
        int length = end - start + 1;
        java.util.BitSet shifted = new java.util.BitSet();
        for (int m = before.nextSetBit(0); m >= 0; m = before.nextSetBit(m + 1)) {
          shifted.set(m >= start ? m + length : m);
        }
        modelSelected = shifted;
        sorter.rowsInserted(start, end);
      } else if (e.getType() == TableModelEvent.DELETE) {
        int length = end - start + 1;
        java.util.BitSet kept = new java.util.BitSet();
        for (int m = before.nextSetBit(0); m >= 0; m = before.nextSetBit(m + 1)) {
          if (m < start) {
            kept.set(m);
          } else if (m > end) {
            kept.set(m - length);
          }
        }
        modelSelected = kept;
        sorter.rowsDeleted(start, end);
      } else {
        if (modelColumn != TableModelEvent.ALL_COLUMNS) {
          sorter.rowsUpdated(start, end, modelColumn);
        } else {
          sorter.rowsUpdated(start, end);
        }
      }
    } finally {
      ignoreSortChange = false;
    }
    rowModel = null;
    if (e.getType() != TableModelEvent.UPDATE) {
      restoreFromModelSelection();
    }
    resizeAndRepaint();
  }

  private void tableRowsInserted(TableModelEvent e) {
    int start = e.getFirstRow();
    int end = e.getLastRow();
    if (start < 0) {
      start = 0;
    }
    int length = end - start + 1;
    selectionModel.insertIndexInterval(start, length, true);
    if (rowModel != null) {
      rowModel.insertEntries(start, length, getRowHeight());
    }
    resizeAndRepaint();
  }

  private void tableRowsDeleted(TableModelEvent e) {
    int start = e.getFirstRow();
    int end = e.getLastRow();
    if (start < 0) {
      start = 0;
    }
    int deletedCount = end - start + 1;
    selectionModel.removeIndexInterval(start, end);
    if (rowModel != null) {
      rowModel.removeEntries(start, deletedCount);
    }
    resizeAndRepaint();
  }

  public void columnAdded(TableColumnModelEvent e) {
    if (isEditing()) {
      removeEditor();
    }
    resizeAndRepaint();
  }

  public void columnRemoved(TableColumnModelEvent e) {
    if (isEditing()) {
      removeEditor();
    }
    resizeAndRepaint();
  }

  public void columnMoved(TableColumnModelEvent e) {
    if (isEditing() && !getCellEditor().stopCellEditing()) {
      getCellEditor().cancelCellEditing();
    }
    repaint();
  }

  public void columnMarginChanged(ChangeEvent e) {
    if (isEditing() && !getCellEditor().stopCellEditing()) {
      getCellEditor().cancelCellEditing();
    }
    TableColumn resizingColumn = tableHeader == null ? null : tableHeader.getResizingColumn();
    if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF) {
      resizingColumn.setPreferredWidth(resizingColumn.getWidth());
    }
    resizeAndRepaint();
  }

  public void columnSelectionChanged(ListSelectionEvent e) { repaint(); }

  public void valueChanged(ListSelectionEvent e) {
    if (sorter != null && !restoringSelection) {
      syncModelSelection();
    }
    repaint();
  }

  private void syncModelSelection() {
    modelSelected.clear();
    int rows = sorter.getViewRowCount();
    for (int v : getSelectedRows()) {
      if (v >= 0 && v < rows) {
        modelSelected.set(sorter.convertRowIndexToModel(v));
      }
    }
  }

  private void restoreFromModelSelection() {
    java.util.BitSet target = new java.util.BitSet();
    for (int m = modelSelected.nextSetBit(0); m >= 0; m = modelSelected.nextSetBit(m + 1)) {
      if (m < sorter.getModelRowCount()) {
        int v = sorter.convertRowIndexToView(m);
        if (v >= 0) {
          target.set(v);
        }
      }
    }
    java.util.BitSet current = new java.util.BitSet();
    for (int v : getSelectedRows()) {
      current.set(v);
    }
    if (current.equals(target)) {
      return;
    }
    restoringSelection = true;
    try {
      selectionModel.setValueIsAdjusting(true);
      selectionModel.clearSelection();
      for (int m = modelSelected.nextSetBit(0); m >= 0; m = modelSelected.nextSetBit(m + 1)) {
        if (m < sorter.getModelRowCount()) {
          int v = sorter.convertRowIndexToView(m);
          if (v >= 0) {
            selectionModel.addSelectionInterval(v, v);
          }
        }
      }
      selectionModel.setValueIsAdjusting(false);
    } finally {
      restoringSelection = false;
    }
  }

  protected TableModel createDefaultDataModel() { return new DefaultTableModel(); }

  protected TableColumnModel createDefaultColumnModel() { return new DefaultTableColumnModel(); }

  protected ListSelectionModel createDefaultSelectionModel() { return new DefaultListSelectionModel(); }

  protected JTableHeader createDefaultTableHeader() { return new JTableHeader(columnModel); }

  protected void initializeLocalVars() {
    updateSelectionOnSort = true;
    setOpaque(true);
    createDefaultRenderers();
    createDefaultEditors();
    setTableHeader(createDefaultTableHeader());
    setShowGrid(true);
    setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    rowHeight = 16;
    setRowMargin(1);
    setRowSelectionAllowed(true);
    setCellEditor(null);
    setEditingColumn(-1);
    setEditingRow(-1);
    setSurrendersFocusOnKeystroke(false);
    setPreferredScrollableViewportSize(new Dimension(450, 400));
    setAutoscrolls(true);
  }

  protected void createDefaultRenderers() {
    defaultRenderersByColumnClass = new Hashtable<Object, Object>();
    defaultRenderersByColumnClass.put(Object.class, new DefaultTableCellRenderer.UIResource());
    defaultRenderersByColumnClass.put(Number.class, new NumberRenderer());
    defaultRenderersByColumnClass.put(Float.class, new DoubleRenderer());
    defaultRenderersByColumnClass.put(Double.class, new DoubleRenderer());
    defaultRenderersByColumnClass.put(Date.class, new DateRenderer());
    defaultRenderersByColumnClass.put(Icon.class, new IconRenderer());
    defaultRenderersByColumnClass.put(ImageIcon.class, new IconRenderer());
    defaultRenderersByColumnClass.put(Boolean.class, new BooleanRenderer());
  }

  protected void createDefaultEditors() {
    defaultEditorsByColumnClass = new Hashtable<Object, Object>();
    defaultEditorsByColumnClass.put(Object.class, new GenericEditor());
    defaultEditorsByColumnClass.put(Number.class, new NumberEditor());
    defaultEditorsByColumnClass.put(Boolean.class, new BooleanEditor());
  }

  public void setPreferredScrollableViewportSize(Dimension size) { preferredViewportSize = size; }

  public Dimension getPreferredScrollableViewportSize() { return preferredViewportSize; }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    int leadingRow = getLeadingRow(visibleRect);
    int leadingCol = getLeadingCol(visibleRect);
    if (orientation == SwingConstants.VERTICAL && leadingRow < 0) {
      return getRowHeight();
    } else if (orientation == SwingConstants.HORIZONTAL && leadingCol < 0) {
      return 100;
    }
    Rectangle leadingCellRect = getCellRect(leadingRow, leadingCol, true);
    int leadingVisibleEdge;
    int leadingCellEdge;
    if (orientation == SwingConstants.VERTICAL) {
      leadingVisibleEdge = visibleRect.y;
      leadingCellEdge = leadingCellRect.y;
    } else {
      leadingVisibleEdge = visibleRect.x;
      leadingCellEdge = leadingCellRect.x;
    }
    if (leadingCellEdge == leadingVisibleEdge) {
      int rc = leadingRow;
      if (direction >= 0) {
        return orientation == SwingConstants.VERTICAL ? getRowHeight(rc) : getColumnModel().getColumn(leadingCol).getWidth();
      }
      if (orientation == SwingConstants.VERTICAL) {
        return rc > 0 ? getRowHeight(rc - 1) : 0;
      }
      return leadingCol > 0 ? getColumnModel().getColumn(leadingCol - 1).getWidth() : 0;
    }
    int cellSize = orientation == SwingConstants.VERTICAL ? leadingCellRect.height : leadingCellRect.width;
    int hidden = leadingVisibleEdge - leadingCellEdge;
    return direction > 0 ? cellSize - hidden : hidden;
  }

  private int getLeadingRow(Rectangle visibleRect) {
    Point leadingPoint = new Point(visibleRect.x, visibleRect.y);
    return rowAtPoint(leadingPoint);
  }

  private int getLeadingCol(Rectangle visibleRect) {
    Point leadingPoint;
    if (getComponentOrientation().isLeftToRight()) {
      leadingPoint = new Point(visibleRect.x, visibleRect.y);
    } else {
      leadingPoint = new Point(visibleRect.x + visibleRect.width - 1, visibleRect.y);
    }
    return columnAtPoint(leadingPoint);
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      return visibleRect.height;
    }
    return visibleRect.width;
  }

  public boolean getScrollableTracksViewportWidth() { return !(autoResizeMode == AUTO_RESIZE_OFF); }

  public boolean getScrollableTracksViewportHeight() {
    Container parent = SwingUtilities.getUnwrappedParent(this);
    return getFillsViewportHeight() && parent instanceof JViewport && parent.getHeight() > getPreferredSize().height;
  }

  public void setFillsViewportHeight(boolean fillsViewportHeight) {
    boolean old = this.fillsViewportHeight;
    this.fillsViewportHeight = fillsViewportHeight;
    resizeAndRepaint();
    firePropertyChange("fillsViewportHeight", old, fillsViewportHeight);
  }

  public boolean getFillsViewportHeight() { return fillsViewportHeight; }

  public void setSurrendersFocusOnKeystroke(boolean surrendersFocusOnKeystroke) {
    boolean old = this.surrendersFocusOnKeystroke;
    this.surrendersFocusOnKeystroke = surrendersFocusOnKeystroke;
    firePropertyChange("surrendersFocusOnKeystroke", old, surrendersFocusOnKeystroke);
  }

  public boolean getSurrendersFocusOnKeystroke() { return surrendersFocusOnKeystroke; }

  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    return super.processKeyBinding(ks, e, condition, pressed);
  }

  public String getToolTipText(MouseEvent event) {
    String tip = null;
    Point p = event.getPoint();
    int hitColumnIndex = columnAtPoint(p);
    int hitRowIndex = rowAtPoint(p);
    if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
      TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
      Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);
      if (component instanceof JComponent) {
        Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
        p.translate(-cellRect.x, -cellRect.y);
        MouseEvent newEvent = new MouseEvent(component, event.getID(), event.getWhen(), event.getModifiers(), p.x,
            p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(),
            MouseEvent.NOBUTTON);
        tip = ((JComponent) component).getToolTipText(newEvent);
      }
    }
    if (tip == null) {
      tip = getToolTipText();
    }
    return tip;
  }

  public boolean print() throws PrinterException { return print(PrintMode.FIT_WIDTH); }

  public boolean print(PrintMode printMode) throws PrinterException { return print(printMode, null, null); }

  public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat)
      throws PrinterException {
    boolean showDialogs = !GraphicsEnvironment.isHeadless();
    return print(printMode, headerFormat, footerFormat, showDialogs, null, showDialogs);
  }

  public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat,
      boolean showPrintDialog, PrintRequestAttributeSet attr, boolean interactive) throws PrinterException {
    return print(printMode, headerFormat, footerFormat, showPrintDialog, attr, interactive, null);
  }

  public boolean print(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat,
      boolean showPrintDialog, PrintRequestAttributeSet attr, boolean interactive, PrintService service)
      throws PrinterException {
    if (GraphicsEnvironment.isHeadless() && (showPrintDialog || interactive)) {
      throw new HeadlessException();
    }
    return false;
  }

  public Printable getPrintable(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat) {
    return new Printable() {
      public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
        return pageIndex == 0 ? PAGE_EXISTS : NO_SUCH_PAGE;
      }
    };
  }

  protected String paramString() {
    String gridColorString = (gridColor != null ? gridColor.toString() : "");
    String showHorizontalLinesString = (showHorizontalLines ? "true" : "false");
    String showVerticalLinesString = (showVerticalLines ? "true" : "false");
    String autoResizeModeString;
    if (autoResizeMode == AUTO_RESIZE_OFF) {
      autoResizeModeString = "AUTO_RESIZE_OFF";
    } else if (autoResizeMode == AUTO_RESIZE_NEXT_COLUMN) {
      autoResizeModeString = "AUTO_RESIZE_NEXT_COLUMN";
    } else if (autoResizeMode == AUTO_RESIZE_SUBSEQUENT_COLUMNS) {
      autoResizeModeString = "AUTO_RESIZE_SUBSEQUENT_COLUMNS";
    } else if (autoResizeMode == AUTO_RESIZE_LAST_COLUMN) {
      autoResizeModeString = "AUTO_RESIZE_LAST_COLUMN";
    } else if (autoResizeMode == AUTO_RESIZE_ALL_COLUMNS) {
      autoResizeModeString = "AUTO_RESIZE_ALL_COLUMNS";
    } else {
      autoResizeModeString = "";
    }
    String autoCreateColumnsFromModelString = (autoCreateColumnsFromModel ? "true" : "false");
    String preferredViewportSizeString = (preferredViewportSize != null ? preferredViewportSize.toString() : "");
    String rowSelectionAllowedString = (rowSelectionAllowed ? "true" : "false");
    String cellSelectionEnabledString = (cellSelectionEnabled ? "true" : "false");
    String selectionForegroundString = (selectionForeground != null ? selectionForeground.toString() : "");
    String selectionBackgroundString = (selectionBackground != null ? selectionBackground.toString() : "");
    return super.paramString() + ",autoCreateColumnsFromModel=" + autoCreateColumnsFromModelString
        + ",autoResizeMode=" + autoResizeModeString + ",cellSelectionEnabled=" + cellSelectionEnabledString
        + ",editingColumn=" + editingColumn + ",editingRow=" + editingRow + ",gridColor=" + gridColorString
        + ",preferredViewportSize=" + preferredViewportSizeString + ",rowHeight=" + rowHeight + ",rowMargin="
        + rowMargin + ",rowSelectionAllowed=" + rowSelectionAllowedString + ",selectionBackground="
        + selectionBackgroundString + ",selectionForeground=" + selectionForegroundString
        + ",showHorizontalLines=" + showHorizontalLinesString + ",showVerticalLines=" + showVerticalLinesString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTable();
    }
    return accessibleContext;
  }

  protected class AccessibleJTable extends AccessibleJComponent {
    protected AccessibleJTable() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.TABLE; }

    public int getAccessibleChildrenCount() { return JTable.this.getRowCount() * JTable.this.getColumnCount(); }

    public Accessible getAccessibleChild(int i) { return null; }
  }

  public static final class DropLocation extends TransferHandler.DropLocation {
    private final int row;
    private final int col;
    private final boolean isInsertRow;
    private final boolean isInsertCol;

    DropLocation(Point p, int row, int col, boolean isInsertRow, boolean isInsertCol) {
      super(p);
      this.row = row;
      this.col = col;
      this.isInsertRow = isInsertRow;
      this.isInsertCol = isInsertCol;
    }

    public int getRow() { return row; }

    public int getColumn() { return col; }

    public boolean isInsertRow() { return isInsertRow; }

    public boolean isInsertColumn() { return isInsertCol; }

    public String toString() {
      return getClass().getName() + "[dropPoint=" + getDropPoint() + ",row=" + row + ",column=" + col
          + ",insertRow=" + isInsertRow + ",insertColumn=" + isInsertCol + "]";
    }
  }

  public enum PrintMode {
    NORMAL, FIT_WIDTH
  }

  static class NumberRenderer extends DefaultTableCellRenderer.UIResource {
    public NumberRenderer() {
      super();
      setHorizontalAlignment(JLabel.RIGHT);
    }
  }

  static class DoubleRenderer extends NumberRenderer {
    NumberFormat formatter;

    public DoubleRenderer() { super(); }

    public void setValue(Object value) {
      if (formatter == null) {
        formatter = NumberFormat.getInstance();
      }
      setText((value == null) ? "" : formatter.format(value));
    }
  }

  static class DateRenderer extends DefaultTableCellRenderer.UIResource {
    DateFormat formatter;

    public DateRenderer() { super(); }

    public void setValue(Object value) {
      if (formatter == null) {
        formatter = DateFormat.getDateInstance();
      }
      setText((value == null) ? "" : formatter.format(value));
    }
  }

  static class IconRenderer extends DefaultTableCellRenderer.UIResource {
    public IconRenderer() {
      super();
      setHorizontalAlignment(JLabel.CENTER);
    }

    public void setValue(Object value) { setIcon((value instanceof Icon) ? (Icon) value : null); }
  }

  static class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {
    private static final Border noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);

    public BooleanRenderer() {
      super();
      setHorizontalAlignment(JLabel.CENTER);
      setBorderPainted(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        super.setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }
      setSelected((value != null && ((Boolean) value).booleanValue()));
      if (hasFocus) {
        setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      } else {
        setBorder(noFocusBorder);
      }
      return this;
    }
  }

  class GenericEditor extends DefaultCellEditor {
    Class<?>[] argTypes = new Class<?>[] {String.class};
    java.lang.reflect.Constructor<?> constructor;
    Object value;

    public GenericEditor() {
      super(new JTextField());
      getComponent().setName("Table.editor");
    }

    public boolean stopCellEditing() {
      String s = (String) super.getCellEditorValue();
      if ("".equals(s)) {
        if (constructor.getDeclaringClass() == String.class) {
          value = s;
        }
        return super.stopCellEditing();
      }
      try {
        value = constructor.newInstance(new Object[] {s});
      } catch (Exception e) {
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
        return false;
      }
      return super.stopCellEditing();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      this.value = null;
      ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
      try {
        Class<?> type = table.getColumnClass(column);
        if (type == Object.class) {
          type = String.class;
        }
        constructor = type.getConstructor(argTypes);
      } catch (Exception e) {
        return null;
      }
      return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public Object getCellEditorValue() { return value; }
  }

  class NumberEditor extends GenericEditor {
    public NumberEditor() { ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT); }
  }

  class BooleanEditor extends DefaultCellEditor {
    public BooleanEditor() {
      super(new JCheckBox());
      JCheckBox checkBox = (JCheckBox) getComponent();
      checkBox.setHorizontalAlignment(JCheckBox.CENTER);
    }
  }
}
