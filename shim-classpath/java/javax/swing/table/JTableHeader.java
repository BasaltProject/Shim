/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableHeaderUI;

public class JTableHeader extends JComponent implements TableColumnModelListener, Accessible {
  protected JTable table;
  protected TableColumnModel columnModel;
  protected boolean reorderingAllowed;
  protected boolean resizingAllowed;
  protected boolean updateTableInRealTime;
  protected transient TableColumn resizingColumn;
  protected transient TableColumn draggedColumn;
  protected transient int draggedDistance;

  private TableCellRenderer defaultRenderer;
  private AccessibleContext accessibleContext;

  public JTableHeader() { this(null); }

  public JTableHeader(TableColumnModel cm) {
    super();
    if (cm == null) {
      cm = createDefaultColumnModel();
    }
    setColumnModel(cm);
    initializeLocalVars();
    updateUI();
  }

  public void setTable(JTable table) {
    JTable old = this.table;
    this.table = table;
    firePropertyChange("table", old, table);
  }

  public JTable getTable() { return table; }

  public void setReorderingAllowed(boolean reorderingAllowed) {
    boolean old = this.reorderingAllowed;
    this.reorderingAllowed = reorderingAllowed;
    firePropertyChange("reorderingAllowed", old, reorderingAllowed);
  }

  public boolean getReorderingAllowed() { return reorderingAllowed; }

  public void setResizingAllowed(boolean resizingAllowed) {
    boolean old = this.resizingAllowed;
    this.resizingAllowed = resizingAllowed;
    firePropertyChange("resizingAllowed", old, resizingAllowed);
  }

  public boolean getResizingAllowed() { return resizingAllowed; }

  public TableColumn getDraggedColumn() { return draggedColumn; }

  public int getDraggedDistance() { return draggedDistance; }

  public TableColumn getResizingColumn() { return resizingColumn; }

  public void setUpdateTableInRealTime(boolean flag) { updateTableInRealTime = flag; }

  public boolean getUpdateTableInRealTime() { return updateTableInRealTime; }

  public void setDefaultRenderer(TableCellRenderer defaultRenderer) { this.defaultRenderer = defaultRenderer; }

  public TableCellRenderer getDefaultRenderer() { return defaultRenderer; }

  public int columnAtPoint(Point point) {
    int x = point.x;
    if (!getComponentOrientation().isLeftToRight()) {
      x = getWidthForOrientation() - x - 1;
    }
    return getColumnModel().getColumnIndexAtX(x);
  }

  private int getWidthForOrientation() { return getWidth(); }

  public Rectangle getHeaderRect(int column) {
    Rectangle r = new Rectangle();
    TableColumnModel cm = getColumnModel();
    r.height = getHeight();
    if (column < 0) {
      if (!getComponentOrientation().isLeftToRight()) {
        r.x = getWidth();
      }
    } else if (column >= cm.getColumnCount()) {
      if (getComponentOrientation().isLeftToRight()) {
        r.x = getWidth();
      }
    } else {
      for (int i = 0; i < column; i++) {
        r.x += cm.getColumn(i).getWidth();
      }
      if (!getComponentOrientation().isLeftToRight()) {
        r.x = getWidth() - r.x - cm.getColumn(column).getWidth();
      }
      r.width = cm.getColumn(column).getWidth();
    }
    return r;
  }

  public String getToolTipText(MouseEvent event) {
    String tip = null;
    Point p = event.getPoint();
    int column;
    if ((column = columnAtPoint(p)) != -1) {
      TableColumn aColumn = columnModel.getColumn(column);
      TableCellRenderer renderer = aColumn.getHeaderRenderer();
      if (renderer == null) {
        renderer = defaultRenderer;
      }
      Component component = renderer.getTableCellRendererComponent(getTable(), aColumn.getHeaderValue(), false, false,
          -1, column);
      if (component instanceof JComponent) {
        Rectangle cellRect = getHeaderRect(column);
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

  public TableHeaderUI getUI() { return (TableHeaderUI) ui; }

  public void setUI(TableHeaderUI ui) {
    if (this.ui != ui) {
      super.setUI(ui);
      repaint();
    }
  }

  public void updateUI() { setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "TableHeaderUI"; }

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
      firePropertyChange("columnModel", old, columnModel);
      resizeAndRepaint();
    }
  }

  public TableColumnModel getColumnModel() { return columnModel; }

  public void columnAdded(TableColumnModelEvent e) { resizeAndRepaint(); }

  public void columnRemoved(TableColumnModelEvent e) { resizeAndRepaint(); }

  public void columnMoved(TableColumnModelEvent e) { repaint(); }

  public void columnMarginChanged(ChangeEvent e) {
    resizeAndRepaint();
  }

  public void columnSelectionChanged(ListSelectionEvent e) { }

  protected TableColumnModel createDefaultColumnModel() { return new DefaultTableColumnModel(); }

  protected TableCellRenderer createDefaultRenderer() {
    return new sun.swing.table.DefaultTableCellHeaderRenderer();
  }

  protected void initializeLocalVars() {
    setOpaque(true);
    table = null;
    reorderingAllowed = true;
    resizingAllowed = true;
    draggedColumn = null;
    draggedDistance = 0;
    resizingColumn = null;
    updateTableInRealTime = true;
    setDefaultRenderer(createDefaultRenderer());
  }

  public void resizeAndRepaint() {
    revalidate();
    repaint();
  }

  public void setDraggedColumn(TableColumn aColumn) { draggedColumn = aColumn; }

  public void setDraggedDistance(int distance) { draggedDistance = distance; }

  public void setResizingColumn(TableColumn aColumn) { resizingColumn = aColumn; }

  public Dimension getPreferredSize() {
    if (getUI() != null) {
      Dimension d = getUI().getPreferredSize(this);
      if (d != null) {
        return d;
      }
    }
    long width = 0;
    int height = 0;
    TableColumnModel cm = getColumnModel();
    for (int column = 0; column < cm.getColumnCount(); column++) {
      TableColumn aColumn = cm.getColumn(column);
      width += aColumn.getPreferredWidth();
      TableCellRenderer renderer = aColumn.getHeaderRenderer();
      if (renderer == null) {
        renderer = getDefaultRenderer();
      }
      Component comp = renderer.getTableCellRendererComponent(getTable(), aColumn.getHeaderValue(), false, false, -1,
          column);
      height = Math.max(height, comp.getPreferredSize().height);
    }
    return new Dimension((int) Math.min(width, Integer.MAX_VALUE), height);
  }

  protected String paramString() {
    String reorderingAllowedString = (reorderingAllowed ? "true" : "false");
    String resizingAllowedString = (resizingAllowed ? "true" : "false");
    String updateTableInRealTimeString = (updateTableInRealTime ? "true" : "false");
    return super.paramString() + ",draggedDistance=" + draggedDistance + ",reorderingAllowed="
        + reorderingAllowedString + ",resizingAllowed=" + resizingAllowedString + ",updateTableInRealTime="
        + updateTableInRealTimeString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTableHeader();
    }
    return accessibleContext;
  }

  protected class AccessibleJTableHeader extends AccessibleJComponent {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.PANEL; }

    public int getAccessibleChildrenCount() { return columnModel.getColumnCount(); }

    public Accessible getAccessibleChild(int i) { return null; }
  }
}
