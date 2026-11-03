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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class BasicTableUI extends TableUI {
  protected JTable table;
  protected CellRendererPane rendererPane;
  protected KeyListener keyListener;
  protected FocusListener focusListener;
  protected MouseInputListener mouseInputListener;
  private Point pressPoint;

  public BasicTableUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new BasicTableUI();
  }

  protected FocusListener createFocusListener() {
    return new FocusHandler();
  }

  protected KeyListener createKeyListener() {
    return new KeyHandler();
  }

  protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler();
  }

  public void installUI(JComponent c) {
    table = (JTable) c;
    rendererPane = new CellRendererPane();
    table.add(rendererPane);
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    table.remove(rendererPane);
    rendererPane = null;
    table = null;
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(table, "Table.background", "Table.foreground", "Table.font");
    java.awt.Color sbg = table.getSelectionBackground();
    if (sbg == null || sbg instanceof UIResource) {
      table.setSelectionBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
    }
    java.awt.Color sfg = table.getSelectionForeground();
    if (sfg == null || sfg instanceof UIResource) {
      table.setSelectionForeground(javax.swing.UIManager.getColor("Table.selectionForeground"));
    }
    java.awt.Color grid = table.getGridColor();
    if (grid == null || grid instanceof UIResource) {
      table.setGridColor(javax.swing.UIManager.getColor("Table.gridColor"));
    }
    java.awt.Container parent = SwingUtilities.getUnwrappedParent(table);
    if (parent != null) {
      parent = parent.getParent();
      if (parent != null && parent instanceof javax.swing.JScrollPane) {
        LookAndFeel.installBorder((javax.swing.JScrollPane) parent, "Table.scrollPaneBorder");
      }
    }
  }

  protected void installListeners() {
    focusListener = createFocusListener();
    keyListener = createKeyListener();
    mouseInputListener = createMouseInputListener();
    table.addFocusListener(focusListener);
    table.addKeyListener(keyListener);
    table.addMouseListener(mouseInputListener);
    table.addMouseMotionListener(mouseInputListener);
  }

  protected void installKeyboardActions() {
  }

  protected void uninstallDefaults() {
  }

  protected void uninstallListeners() {
    table.removeFocusListener(focusListener);
    table.removeKeyListener(keyListener);
    table.removeMouseListener(mouseInputListener);
    table.removeMouseMotionListener(mouseInputListener);
    focusListener = null;
    keyListener = null;
    mouseInputListener = null;
  }

  protected void uninstallKeyboardActions() {
  }

  private Dimension createTableSize(long width) {
    int height = 0;
    int rowCount = table.getRowCount();
    if (rowCount > 0 && table.getColumnCount() > 0) {
      Rectangle r = table.getCellRect(rowCount - 1, 0, true);
      height = r.y + r.height;
    }
    long w = Math.abs(width);
    long h = Math.abs((long) height);
    return new Dimension((int) Math.min(w, Integer.MAX_VALUE), (int) Math.min(h, Integer.MAX_VALUE));
  }

  public Dimension getMinimumSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = table.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getMinWidth();
    }
    return createTableSize(width);
  }

  public Dimension getPreferredSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = table.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getPreferredWidth();
    }
    return createTableSize(width);
  }

  public Dimension getMaximumSize(JComponent c) {
    long width = 0;
    Enumeration<TableColumn> e = table.getColumnModel().getColumns();
    while (e.hasMoreElements()) {
      width += e.nextElement().getMaxWidth();
    }
    return createTableSize(width);
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    if (table.getRowCount() == 0) {
      return -1;
    }
    int rowHeight = table.getRowHeight(0);
    if (table.getColumnCount() == 0) {
      return -1;
    }
    TableCellRenderer renderer = table.getCellRenderer(0, 0);
    Component comp = table.prepareRenderer(renderer, 0, 0);
    int base = comp.getBaseline(table.getColumnModel().getColumn(0).getWidth(), rowHeight - table.getRowMargin());
    return base < 0 ? -1 : base + table.getRowMargin() / 2;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
  }

  public void paint(Graphics g, JComponent c) {
    Rectangle clip = g.getClipBounds();
    Rectangle bounds = table.getBounds();
    bounds.x = bounds.y = 0;
    if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 || !bounds.intersects(clip)) {
      paintDropLines(g);
      return;
    }
    boolean ltr = table.getComponentOrientation().isLeftToRight();
    Point upperLeft = clip.getLocation();
    Point lowerRight = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
    int rMin = table.rowAtPoint(upperLeft);
    int rMax = table.rowAtPoint(lowerRight);
    if (rMin == -1) {
      rMin = 0;
    }
    if (rMax == -1) {
      rMax = table.getRowCount() - 1;
    }
    int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight);
    int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);
    if (cMin == -1) {
      cMin = 0;
    }
    if (cMax == -1) {
      cMax = table.getColumnCount() - 1;
    }
    paintGrid(g, rMin, rMax, cMin, cMax);
    paintCells(g, rMin, rMax, cMin, cMax);
    paintDropLines(g);
  }

  private void paintDropLines(Graphics g) {
  }

  private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
    g.setColor(table.getGridColor());
    Rectangle minCell = table.getCellRect(rMin, cMin, true);
    Rectangle maxCell = table.getCellRect(rMax, cMax, true);
    Rectangle damaged = minCell.union(maxCell);
    if (table.getShowHorizontalLines()) {
      int tableWidth = damaged.x + damaged.width;
      int y = damaged.y;
      for (int row = rMin; row <= rMax; row++) {
        y += table.getRowHeight(row);
        g.drawLine(damaged.x, y - 1, tableWidth - 1, y - 1);
      }
    }
    if (table.getShowVerticalLines()) {
      TableColumnModel cm = table.getColumnModel();
      int tableHeight = damaged.y + damaged.height;
      int x;
      if (table.getComponentOrientation().isLeftToRight()) {
        x = damaged.x;
        for (int column = cMin; column <= cMax; column++) {
          x += cm.getColumn(column).getWidth();
          g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
        }
      } else {
        x = damaged.x;
        for (int column = cMax; column >= cMin; column--) {
          g.drawLine(x, 0, x, tableHeight - 1);
          x += cm.getColumn(column).getWidth();
        }
      }
    }
  }

  private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax) {
    TableColumnModel cm = table.getColumnModel();
    int columnMargin = cm.getColumnMargin();
    Rectangle cellRect;
    TableColumn aColumn;
    int columnWidth;
    if (table.getComponentOrientation().isLeftToRight()) {
      for (int row = rMin; row <= rMax; row++) {
        cellRect = table.getCellRect(row, cMin, false);
        for (int column = cMin; column <= cMax; column++) {
          aColumn = cm.getColumn(column);
          columnWidth = aColumn.getWidth();
          cellRect.width = columnWidth - columnMargin;
          paintCell(g, cellRect, row, column);
          cellRect.x += columnWidth;
        }
      }
    } else {
      for (int row = rMin; row <= rMax; row++) {
        cellRect = table.getCellRect(row, cMin, false);
        aColumn = cm.getColumn(cMin);
        cellRect.width = aColumn.getWidth() - columnMargin;
        cellRect.x += columnMargin;
        for (int column = cMin; column <= cMax; column++) {
          aColumn = cm.getColumn(column);
          columnWidth = aColumn.getWidth();
          cellRect.width = columnWidth - columnMargin;
          paintCell(g, cellRect, row, column);
          if (column < cMax) {
            cellRect.x -= cm.getColumn(column + 1).getWidth();
          }
        }
      }
    }
    rendererPane.removeAll();
  }

  private void paintCell(Graphics g, Rectangle cellRect, int row, int column) {
    if (table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column) {
      Component component = table.getEditorComponent();
      component.setBounds(cellRect);
      component.validate();
    } else {
      TableCellRenderer renderer = table.getCellRenderer(row, column);
      Component component = table.prepareRenderer(renderer, row, column);
      rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height,
          true);
    }
  }

  public class KeyHandler implements KeyListener {
    public KeyHandler() {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
  }

  public class FocusHandler implements FocusListener {
    public FocusHandler() {
    }

    private void repaintAnchorCell() {
      int row = table.getSelectionModel().getLeadSelectionIndex();
      int col = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();
      if (row >= 0 && col >= 0 && row < table.getRowCount() && col < table.getColumnCount()) {
        table.repaint(table.getCellRect(row, col, false));
      }
    }

    public void focusGained(FocusEvent e) {
      repaintAnchorCell();
    }

    public void focusLost(FocusEvent e) {
      repaintAnchorCell();
    }
  }

  public class MouseInputHandler implements MouseInputListener {
    public MouseInputHandler() {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      if (e.isConsumed() || !SwingUtilities.isLeftMouseButton(e) || !table.isEnabled()) {
        return;
      }
      if (table.isRequestFocusEnabled()) {
        table.requestFocus();
      }
      Point p = e.getPoint();
      pressPoint = p;
      int row = table.rowAtPoint(p);
      int column = table.columnAtPoint(p);
      if (row < 0 || column < 0) {
        return;
      }
      if (table.editCellAt(row, column, e)) {
        return;
      }
      if (table.isEditing() && !table.getCellEditor().stopCellEditing()) {
        return;
      }
      table.changeSelection(row, column, e.isControlDown(), e.isShiftDown());
    }

    public void mouseReleased(MouseEvent e) {
      pressPoint = null;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
      if (e.isConsumed() || !SwingUtilities.isLeftMouseButton(e) || !table.isEnabled() || pressPoint == null) {
        return;
      }
      if (table.isEditing()) {
        return;
      }
      Point p = e.getPoint();
      int row = table.rowAtPoint(p);
      int column = table.columnAtPoint(p);
      if (row < 0) {
        row = p.y < 0 ? 0 : table.getRowCount() - 1;
      }
      if (column < 0) {
        column = p.x < 0 ? 0 : table.getColumnCount() - 1;
      }
      if (row >= 0 && column >= 0) {
        table.changeSelection(row, column, false, true);
      }
    }
  }
}
