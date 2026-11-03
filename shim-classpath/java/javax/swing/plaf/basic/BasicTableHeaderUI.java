/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class BasicTableHeaderUI extends TableHeaderUI {
  protected JTableHeader header;
  protected CellRendererPane rendererPane;
  protected MouseInputListener mouseInputListener;
  private int rolloverColumn = -1;

  public BasicTableHeaderUI() {
  }

  public static ComponentUI createUI(JComponent h) {
    return new BasicTableHeaderUI();
  }

  protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler();
  }

  public void installUI(JComponent c) {
    header = (JTableHeader) c;
    rendererPane = new CellRendererPane();
    header.add(rendererPane);
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    header.remove(rendererPane);
    rendererPane = null;
    header = null;
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(header, "TableHeader.background", "TableHeader.foreground",
        "TableHeader.font");
  }

  protected void installListeners() {
    mouseInputListener = createMouseInputListener();
    header.addMouseListener(mouseInputListener);
    header.addMouseMotionListener(mouseInputListener);
  }

  protected void installKeyboardActions() {
  }

  protected void uninstallDefaults() {
  }

  protected void uninstallListeners() {
    header.removeMouseListener(mouseInputListener);
    header.removeMouseMotionListener(mouseInputListener);
    mouseInputListener = null;
  }

  protected void uninstallKeyboardActions() {
  }

  protected int getRolloverColumn() {
    return rolloverColumn;
  }

  protected void rolloverColumnUpdated(int oldColumn, int newColumn) {
  }

  private TableCellRenderer defaultRenderer(TableColumn column) {
    TableCellRenderer r = column.getHeaderRenderer();
    return r == null ? header.getDefaultRenderer() : r;
  }

  private Component getHeaderRenderer(int columnIndex) {
    TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
    TableCellRenderer renderer = defaultRenderer(aColumn);
    return renderer.getTableCellRendererComponent(header.getTable(), aColumn.getHeaderValue(), false, false, -1,
        columnIndex);
  }

  private int getHeaderHeight() {
    int height = 0;
    boolean accomodatedDefault = false;
    TableColumnModel columnModel = header.getColumnModel();
    for (int column = 0; column < columnModel.getColumnCount(); column++) {
      TableColumn aColumn = columnModel.getColumn(column);
      boolean isDefault = aColumn.getHeaderRenderer() == null;
      if (!isDefault || !accomodatedDefault) {
        Component comp = getHeaderRenderer(column);
        int rendererHeight = comp.getPreferredSize().height;
        height = Math.max(height, rendererHeight);
        if (rendererHeight > 0 && isDefault) {
          accomodatedDefault = true;
        }
      }
    }
    return height;
  }

  private Dimension createHeaderSize(long width) {
    if (width > Integer.MAX_VALUE) {
      width = Integer.MAX_VALUE;
    }
    return new Dimension((int) width, getHeaderHeight());
  }

  public Dimension getMinimumSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = header.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getMinWidth();
    }
    return createHeaderSize(width);
  }

  public Dimension getPreferredSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = header.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getPreferredWidth();
    }
    return createHeaderSize(width);
  }

  public Dimension getMaximumSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = header.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getMaxWidth();
    }
    return createHeaderSize(width);
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    int baseline = -1;
    TableColumnModel columnModel = header.getColumnModel();
    for (int column = 0; column < columnModel.getColumnCount(); column++) {
      Component comp = getHeaderRenderer(column);
      Dimension pref = comp.getPreferredSize();
      int b = comp.getBaseline(pref.width, height);
      if (b == -1) {
        return -1;
      }
      if (baseline == -1) {
        baseline = b;
      } else if (baseline != b) {
        return -1;
      }
    }
    return baseline;
  }

  public void paint(Graphics g, JComponent c) {
    if (header.getColumnModel().getColumnCount() <= 0) {
      return;
    }
    boolean ltr = header.getComponentOrientation().isLeftToRight();
    Rectangle clip = g.getClipBounds();
    Point left = clip.getLocation();
    Point right = new Point(clip.x + clip.width - 1, clip.y);
    TableColumnModel cm = header.getColumnModel();
    int cMin = header.columnAtPoint(ltr ? left : right);
    int cMax = header.columnAtPoint(ltr ? right : left);
    if (cMin == -1) {
      cMin = 0;
    }
    if (cMax == -1) {
      cMax = cm.getColumnCount() - 1;
    }
    Rectangle cellRect = header.getHeaderRect(ltr ? cMin : cMax);
    int columnWidth;
    if (ltr) {
      for (int column = cMin; column <= cMax; column++) {
        TableColumn aColumn = cm.getColumn(column);
        columnWidth = aColumn.getWidth();
        cellRect.width = columnWidth;
        if (aColumn != header.getDraggedColumn()) {
          paintCell(g, cellRect, column);
        }
        cellRect.x += columnWidth;
      }
    } else {
      for (int column = cMax; column >= cMin; column--) {
        TableColumn aColumn = cm.getColumn(column);
        columnWidth = aColumn.getWidth();
        cellRect.width = columnWidth;
        if (aColumn != header.getDraggedColumn()) {
          paintCell(g, cellRect, column);
        }
        cellRect.x += columnWidth;
      }
    }
    rendererPane.removeAll();
  }

  private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
    Component component = getHeaderRenderer(columnIndex);
    rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height,
        true);
  }

  public class MouseInputHandler implements MouseInputListener {
    private int mouseXOffset;

    public MouseInputHandler() {
    }

    public void mouseClicked(MouseEvent e) {
      if (!header.isEnabled() || e.getClickCount() % 2 != 1 || !SwingUtilities.isLeftMouseButton(e)) {
        return;
      }
      javax.swing.JTable table = header.getTable();
      if (table != null && table.getRowSorter() != null) {
        int column = header.columnAtPoint(e.getPoint());
        if (column != -1) {
          column = table.convertColumnIndexToModel(column);
          table.getRowSorter().toggleSortOrder(column);
        }
      }
    }

    public void mousePressed(MouseEvent e) {
      if (!header.isEnabled()) {
        return;
      }
      header.setDraggedColumn(null);
      header.setResizingColumn(null);
      header.setDraggedDistance(0);
      Point p = e.getPoint();
      TableColumnModel columnModel = header.getColumnModel();
      int index = header.columnAtPoint(p);
      if (index != -1) {
        TableColumn resizingColumn = getResizingColumn(p, index);
        if (header.getResizingAllowed() && resizingColumn != null) {
          header.setResizingColumn(resizingColumn);
          mouseXOffset = header.getComponentOrientation().isLeftToRight()
              ? p.x - resizingColumn.getWidth() : p.x + resizingColumn.getWidth();
        } else if (header.getReorderingAllowed()) {
          TableColumn hitColumn = columnModel.getColumn(index);
          header.setDraggedColumn(hitColumn);
          mouseXOffset = p.x;
        }
      }
    }

    private TableColumn getResizingColumn(Point p, int column) {
      if (column == -1) {
        return null;
      }
      Rectangle r = header.getHeaderRect(column);
      r.grow(-3, 0);
      if (r.contains(p)) {
        return null;
      }
      int midPoint = r.x + r.width / 2;
      int columnIndex;
      if (header.getComponentOrientation().isLeftToRight()) {
        columnIndex = p.x < midPoint ? column - 1 : column;
      } else {
        columnIndex = p.x < midPoint ? column : column - 1;
      }
      if (columnIndex == -1) {
        return null;
      }
      return header.getColumnModel().getColumn(columnIndex);
    }

    public void mouseDragged(MouseEvent e) {
      if (!header.isEnabled()) {
        return;
      }
      int mouseX = e.getX();
      TableColumn resizingColumn = header.getResizingColumn();
      TableColumn draggedColumn = header.getDraggedColumn();
      boolean ltr = header.getComponentOrientation().isLeftToRight();
      if (resizingColumn != null) {
        int oldWidth = resizingColumn.getWidth();
        int newWidth = ltr ? mouseX - mouseXOffset : mouseXOffset - mouseX;
        header.setResizingColumn(resizingColumn);
        resizingColumn.setWidth(newWidth);
        javax.swing.JTable table = header.getTable();
        if (table != null) {
          table.revalidate();
          table.repaint();
        }
        if (oldWidth == newWidth) {
          return;
        }
      } else if (draggedColumn != null) {
        TableColumnModel cm = header.getColumnModel();
        int draggedDistance = mouseX - mouseXOffset;
        int direction = draggedDistance < 0 ? -1 : 1;
        int columnIndex = viewIndexForColumn(draggedColumn);
        int newColumnIndex = columnIndex + (ltr ? direction : -direction);
        if (0 <= newColumnIndex && newColumnIndex < cm.getColumnCount()) {
          int width = cm.getColumn(newColumnIndex).getWidth();
          if (Math.abs(draggedDistance) > (width / 2)) {
            mouseXOffset = mouseXOffset + direction * width;
            header.setDraggedDistance(draggedDistance - direction * width);
            cm.moveColumn(columnIndex, newColumnIndex);
            return;
          }
        }
        header.setDraggedDistance(draggedDistance);
      }
      header.repaint();
    }

    private int viewIndexForColumn(TableColumn aColumn) {
      TableColumnModel cm = header.getColumnModel();
      for (int column = 0; column < cm.getColumnCount(); column++) {
        if (cm.getColumn(column) == aColumn) {
          return column;
        }
      }
      return -1;
    }

    public void mouseReleased(MouseEvent e) {
      header.setResizingColumn(null);
      header.setDraggedColumn(null);
      header.setDraggedDistance(0);
      header.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
  }
}
