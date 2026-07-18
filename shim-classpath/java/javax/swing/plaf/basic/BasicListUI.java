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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.UIResource;

public class BasicListUI extends ListUI {
  protected JList<Object> list = null;
  protected CellRendererPane rendererPane;
  protected FocusListener focusListener;
  protected MouseInputListener mouseInputListener;
  protected ListSelectionListener listSelectionListener;
  protected ListDataListener listDataListener;
  protected PropertyChangeListener propertyChangeListener;
  protected int[] cellHeights = null;
  protected int cellHeight = -1;
  protected int cellWidth = -1;
  protected int updateLayoutStateNeeded = modelChanged;
  protected static final int modelChanged = 1 << 0;
  protected static final int selectionModelChanged = 1 << 1;
  protected static final int fontChanged = 1 << 2;
  protected static final int fixedCellWidthChanged = 1 << 3;
  protected static final int fixedCellHeightChanged = 1 << 4;
  protected static final int prototypeCellValueChanged = 1 << 5;
  protected static final int cellRendererChanged = 1 << 6;
  private int columnCount = 1;
  private int rowsPerColumn = 1;
  private int preferredHeight;
  private int layoutOrientation;
  private int rowAtPress = -1;

  public BasicListUI() {
  }

  public static ComponentUI createUI(JComponent list) {
    return new BasicListUI();
  }

  @SuppressWarnings("unchecked")
  public void installUI(JComponent c) {
    list = (JList<Object>) c;
    layoutOrientation = list.getLayoutOrientation();
    rendererPane = new CellRendererPane();
    list.add(rendererPane);
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    uninstallListeners();
    uninstallDefaults();
    uninstallKeyboardActions();
    cellHeight = cellWidth = -1;
    cellHeights = null;
    list.remove(rendererPane);
    rendererPane = null;
    list = null;
  }

  @SuppressWarnings("unchecked")
  protected void installDefaults() {
    list.setLayout(null);
    LookAndFeel.installBorder(list, "List.border");
    LookAndFeel.installColorsAndFont(list, "List.background", "List.foreground", "List.font");
    LookAndFeel.installProperty(list, "opaque", Boolean.TRUE);
    if (list.getCellRenderer() == null) {
      list.setCellRenderer((ListCellRenderer<Object>) UIManager.get("List.cellRenderer"));
    }
    java.awt.Color sbg = list.getSelectionBackground();
    if (sbg == null || sbg instanceof UIResource) {
      list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
    }
    java.awt.Color sfg = list.getSelectionForeground();
    if (sfg == null || sfg instanceof UIResource) {
      list.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
    }
  }

  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(list);
    if (list.getFont() instanceof UIResource) {
      list.setFont(null);
    }
    if (list.getForeground() instanceof UIResource) {
      list.setForeground(null);
    }
    if (list.getBackground() instanceof UIResource) {
      list.setBackground(null);
    }
    if (list.getSelectionBackground() instanceof UIResource) {
      list.setSelectionBackground(null);
    }
    if (list.getSelectionForeground() instanceof UIResource) {
      list.setSelectionForeground(null);
    }
    if (list.getCellRenderer() instanceof UIResource) {
      list.setCellRenderer(null);
    }
  }

  protected void installListeners() {
    focusListener = createFocusListener();
    mouseInputListener = createMouseInputListener();
    propertyChangeListener = createPropertyChangeListener();
    listSelectionListener = createListSelectionListener();
    listDataListener = createListDataListener();
    list.addFocusListener(focusListener);
    list.addMouseListener(mouseInputListener);
    list.addMouseMotionListener(mouseInputListener);
    list.addPropertyChangeListener(propertyChangeListener);
    ListModel<Object> model = list.getModel();
    if (model != null) {
      model.addListDataListener(listDataListener);
    }
    ListSelectionModel sel = list.getSelectionModel();
    if (sel != null) {
      sel.addListSelectionListener(listSelectionListener);
    }
  }

  protected void uninstallListeners() {
    list.removeFocusListener(focusListener);
    list.removeMouseListener(mouseInputListener);
    list.removeMouseMotionListener(mouseInputListener);
    list.removePropertyChangeListener(propertyChangeListener);
    ListModel<Object> model = list.getModel();
    if (model != null) {
      model.removeListDataListener(listDataListener);
    }
    ListSelectionModel sel = list.getSelectionModel();
    if (sel != null) {
      sel.removeListSelectionListener(listSelectionListener);
    }
    focusListener = null;
    mouseInputListener = null;
    listSelectionListener = null;
    listDataListener = null;
    propertyChangeListener = null;
  }

  protected void installKeyboardActions() {
    shim.swing.KeyActions.install(list, JComponent.WHEN_FOCUSED, "List.focusInputMap",
        shim.swing.ListKeyActions.create());
  }

  protected void uninstallKeyboardActions() {
    shim.swing.KeyActions.uninstall(list, JComponent.WHEN_FOCUSED);
  }

  protected FocusListener createFocusListener() {
    return new FocusHandler();
  }

  protected ListDataListener createListDataListener() {
    return new ListDataHandler();
  }

  protected ListSelectionListener createListSelectionListener() {
    return new ListSelectionHandler();
  }

  protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler();
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected void maybeUpdateLayoutState() {
    if (updateLayoutStateNeeded != 0) {
      updateLayoutState();
      updateLayoutStateNeeded = 0;
    }
  }

  @SuppressWarnings("unchecked")
  protected void updateLayoutState() {
    int fixedHeight = list.getFixedCellHeight();
    int fixedWidth = list.getFixedCellWidth();
    cellWidth = fixedWidth != -1 ? fixedWidth : -1;
    if (fixedHeight != -1) {
      cellHeight = fixedHeight;
      cellHeights = null;
    } else {
      cellHeight = -1;
      cellHeights = new int[list.getModel().getSize()];
    }
    if (fixedWidth == -1 || fixedHeight == -1) {
      ListModel<Object> model = list.getModel();
      int size = model.getSize();
      ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) list.getCellRenderer();
      Object proto = list.getPrototypeCellValue();
      if (proto != null && renderer != null) {
        Component c = renderer.getListCellRendererComponent(list, proto, 0, false, false);
        rendererPane.add(c);
        Dimension d = c.getPreferredSize();
        if (fixedWidth == -1) {
          cellWidth = d.width;
        }
        if (fixedHeight == -1) {
          cellHeight = d.height;
          cellHeights = null;
        }
      } else if (size > 0 && renderer != null) {
        if (cellWidth == -1) {
          cellWidth = 0;
        }
        for (int i = 0; i < size; i++) {
          Component c = renderer.getListCellRendererComponent(list, model.getElementAt(i), i, false, false);
          rendererPane.add(c);
          Dimension d = c.getPreferredSize();
          if (fixedWidth == -1) {
            cellWidth = Math.max(d.width, cellWidth);
          }
          if (fixedHeight == -1) {
            cellHeights[i] = d.height;
          }
        }
      } else {
        if (cellWidth == -1) {
          cellWidth = 0;
        }
        if (cellHeights == null) {
          cellHeights = new int[size];
        }
      }
    }
    columnCount = 1;
    if (layoutOrientation != JList.VERTICAL) {
      updateWrapState();
    }
  }

  private void updateWrapState() {
    int size = list.getModel().getSize();
    int maxH = cellHeight;
    if (cellHeights != null) {
      maxH = 0;
      for (int h : cellHeights) {
        maxH = Math.max(maxH, h);
      }
      cellHeight = maxH;
      cellHeights = null;
    }
    int visible = list.getVisibleRowCount();
    Insets in = list.getInsets();
    if (layoutOrientation == JList.VERTICAL_WRAP) {
      if (visible > 0) {
        rowsPerColumn = visible;
      } else {
        int avail = list.getHeight() - in.top - in.bottom;
        rowsPerColumn = maxH <= 0 ? 1 : Math.max(1, avail / maxH);
      }
      rowsPerColumn = Math.max(1, rowsPerColumn);
      columnCount = Math.max(1, (size + rowsPerColumn - 1) / rowsPerColumn);
      if (size > 0 && rowsPerColumn > size) {
        rowsPerColumn = size;
      }
      preferredHeight = rowsPerColumn * maxH;
    } else {
      if (visible > 0) {
        columnCount = Math.max(1, (size + visible - 1) / visible);
      } else {
        int avail = list.getWidth() - in.left - in.right;
        columnCount = cellWidth <= 0 ? 1 : Math.max(1, avail / cellWidth);
      }
      if (size > 0 && columnCount > size) {
        columnCount = size;
      }
      rowsPerColumn = Math.max(1, (size + columnCount - 1) / columnCount);
      preferredHeight = rowsPerColumn * maxH;
    }
  }

  protected int getRowHeight(int row) {
    return getHeight(0, row);
  }

  private int getHeight(int column, int row) {
    if (column < 0 || column > columnCount || row < 0) {
      return -1;
    }
    if (layoutOrientation != JList.VERTICAL) {
      return cellHeight;
    }
    if (row >= list.getModel().getSize()) {
      return -1;
    }
    return cellHeights == null ? cellHeight : (row < cellHeights.length ? cellHeights[row] : -1);
  }

  protected int convertYToRow(int y0) {
    return convertLocationToRow(0, y0, false);
  }

  protected int convertRowToY(int row) {
    if (row >= list.getModel().getSize() || row < 0) {
      return -1;
    }
    Rectangle bounds = getCellBounds(list, row, row);
    return bounds == null ? -1 : bounds.y;
  }

  private int convertLocationToRow(int x, int y0, boolean closest) {
    int size = list.getModel().getSize();
    if (size <= 0) {
      return -1;
    }
    Insets insets = list.getInsets();
    if (cellHeights == null) {
      int row = cellHeight == 0 ? 0 : (y0 - insets.top) / cellHeight;
      if (y0 - insets.top < 0) {
        row = closest ? 0 : -1;
      }
      int rows = layoutOrientation == JList.VERTICAL ? size : rowsPerColumn;
      if (closest) {
        row = Math.max(0, Math.min(rows - 1, row));
      } else if (row < 0 || row >= rows) {
        return -1;
      }
      if (layoutOrientation == JList.VERTICAL) {
        return row;
      }
      return convertCellToIndex(x, row, closest);
    }
    if (size > cellHeights.length) {
      return -1;
    }
    int y = insets.top;
    int row = 0;
    if (closest && y0 < y) {
      return 0;
    }
    for (int i = 0; i < size; i++) {
      if (y0 >= y && y0 < y + cellHeights[i]) {
        return row;
      }
      y += cellHeights[i];
      row++;
    }
    return closest ? size - 1 : -1;
  }

  private int convertCellToIndex(int x, int row, boolean closest) {
    Insets in = list.getInsets();
    int col = cellWidth <= 0 ? 0 : (x - in.left) / cellWidth;
    if (x - in.left < 0) {
      col = 0;
    }
    int size = list.getModel().getSize();
    if (col >= columnCount) {
      if (!closest) {
        return -1;
      }
      col = columnCount - 1;
    }
    int index = layoutOrientation == JList.VERTICAL_WRAP ? col * rowsPerColumn + row : row * columnCount + col;
    if (index >= size) {
      if (!closest) {
        return -1;
      }
      index = size - 1;
    }
    return index;
  }

  private int modelToRow(int index) {
    return layoutOrientation == JList.VERTICAL_WRAP ? index % rowsPerColumn : index / columnCount;
  }

  private int modelToColumn(int index) {
    return layoutOrientation == JList.VERTICAL_WRAP ? index / rowsPerColumn : index % columnCount;
  }

  public int locationToIndex(JList<?> list, Point location) {
    maybeUpdateLayoutState();
    return convertLocationToRow(location.x, location.y, true);
  }

  public Point indexToLocation(JList<?> list, int index) {
    maybeUpdateLayoutState();
    Rectangle r = getCellBounds(list, index, index);
    return r == null ? null : new Point(r.x, r.y);
  }

  private Rectangle cellBounds(int index) {
    int size = list.getModel().getSize();
    if (index < 0 || index >= size) {
      return null;
    }
    Insets in = list.getInsets();
    if (layoutOrientation == JList.VERTICAL) {
      int y = in.top;
      int h;
      if (cellHeights == null) {
        y += cellHeight * index;
        h = cellHeight;
      } else {
        for (int i = 0; i < index && i < cellHeights.length; i++) {
          y += cellHeights[i];
        }
        h = index < cellHeights.length ? cellHeights[index] : 0;
      }
      return new Rectangle(in.left, y, list.getWidth() - in.left - in.right, h);
    }
    int row = modelToRow(index);
    int col = modelToColumn(index);
    return new Rectangle(in.left + col * cellWidth, in.top + row * cellHeight, cellWidth, cellHeight);
  }

  public Rectangle getCellBounds(JList<?> list, int index1, int index2) {
    maybeUpdateLayoutState();
    int min = Math.min(index1, index2);
    int max = Math.max(index1, index2);
    if (min >= list.getModel().getSize()) {
      return null;
    }
    Rectangle minBounds = cellBounds(min);
    if (minBounds == null) {
      return null;
    }
    if (min == max) {
      return minBounds;
    }
    Rectangle maxBounds = cellBounds(max);
    if (maxBounds != null) {
      Insets in = list.getInsets();
      if (layoutOrientation == JList.HORIZONTAL_WRAP) {
        if (modelToRow(min) != modelToRow(max)) {
          minBounds.x = in.left;
          minBounds.width = list.getWidth() - in.left - in.right;
        }
      } else if (layoutOrientation == JList.VERTICAL_WRAP) {
        if (modelToColumn(min) != modelToColumn(max)) {
          minBounds.y = in.top;
          minBounds.height = list.getHeight() - in.top - in.bottom;
        }
      }
      minBounds.add(maxBounds);
    }
    return minBounds;
  }

  @SuppressWarnings("unchecked")
  public void paint(Graphics g, JComponent c) {
    maybeUpdateLayoutState();
    ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) list.getCellRenderer();
    ListModel<Object> dataModel = list.getModel();
    ListSelectionModel selModel = list.getSelectionModel();
    int size = dataModel.getSize();
    if (renderer == null || size == 0) {
      return;
    }
    Rectangle paintBounds = g.getClipBounds();
    if (paintBounds == null) {
      paintBounds = new Rectangle(0, 0, list.getWidth(), list.getHeight());
    }
    int startColumn;
    int endColumn;
    if (c.getComponentOrientation().isLeftToRight()) {
      startColumn = convertLocationToColumn(paintBounds.x, paintBounds.y);
      endColumn = convertLocationToColumn(paintBounds.x + paintBounds.width, paintBounds.y);
    } else {
      startColumn = convertLocationToColumn(paintBounds.x + paintBounds.width, paintBounds.y);
      endColumn = convertLocationToColumn(paintBounds.x, paintBounds.y);
    }
    int maxY = paintBounds.y + paintBounds.height;
    int leadIndex = list.getLeadSelectionIndex();
    int rowIncrement = layoutOrientation == JList.HORIZONTAL_WRAP ? columnCount : 1;
    for (int colCounter = startColumn; colCounter <= endColumn; colCounter++) {
      int row = convertLocationToRowInColumn(paintBounds.y, colCounter);
      int rowCount = rowsInColumn(colCounter);
      int index = getModelIndex(colCounter, row);
      Rectangle rowBounds = getCellBounds(list, index, index);
      if (rowBounds == null) {
        return;
      }
      while (row < rowCount && rowBounds.y < maxY && index < size) {
        rowBounds.height = getHeight(colCounter, row);
        g.setClip(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
        g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);
        paintCell(g, index, rowBounds, renderer, dataModel, selModel, leadIndex);
        rowBounds.y += rowBounds.height;
        index += rowIncrement;
        row++;
      }
    }
    rendererPane.removeAll();
  }

  private int convertLocationToColumn(int x, int y) {
    if (cellWidth <= 0 || layoutOrientation == JList.VERTICAL) {
      return 0;
    }
    Insets in = list.getInsets();
    int col;
    if (list.getComponentOrientation().isLeftToRight()) {
      col = (x - in.left) / cellWidth;
    } else {
      col = (list.getWidth() - x - in.right - 1) / cellWidth;
    }
    return Math.max(0, Math.min(col, columnCount - 1));
  }

  private int convertLocationToRowInColumn(int y, int column) {
    int x = 0;
    if (layoutOrientation != JList.VERTICAL) {
      x = list.getInsets().left + column * cellWidth;
    }
    int r = convertLocationToRow(x, y, true);
    if (r < 0) {
      return 0;
    }
    return layoutOrientation == JList.VERTICAL ? r : modelToRow(r);
  }

  private int rowsInColumn(int column) {
    int size = list.getModel().getSize();
    if (layoutOrientation == JList.VERTICAL) {
      return size;
    }
    if (layoutOrientation == JList.VERTICAL_WRAP) {
      if (column < columnCount - 1) {
        return rowsPerColumn;
      }
      return size - (columnCount - 1) * rowsPerColumn;
    }
    return rowsPerColumn;
  }

  private int getModelIndex(int column, int row) {
    if (layoutOrientation == JList.VERTICAL_WRAP) {
      return column * rowsPerColumn + row;
    }
    if (layoutOrientation == JList.HORIZONTAL_WRAP) {
      return row * columnCount + column;
    }
    return row;
  }

  protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer<Object> cellRenderer,
      ListModel<Object> dataModel, ListSelectionModel selModel, int leadIndex) {
    Object value = dataModel.getElementAt(row);
    boolean cellHasFocus = list.hasFocus() && row == leadIndex;
    boolean isSelected = selModel.isSelectedIndex(row);
    Component rendererComponent = cellRenderer.getListCellRendererComponent(list, value, row, isSelected,
        cellHasFocus);
    int cw = layoutOrientation != JList.VERTICAL ? cellWidth : rowBounds.width;
    rendererPane.paintComponent(g, rendererComponent, list, rowBounds.x, rowBounds.y, cw, rowBounds.height, true);
  }

  public Dimension getPreferredSize(JComponent c) {
    maybeUpdateLayoutState();
    int lastRow = list.getModel().getSize() - 1;
    if (lastRow < 0) {
      return new Dimension(0, 0);
    }
    Insets insets = list.getInsets();
    int width = cellWidth * columnCount + insets.left + insets.right;
    int height;
    if (layoutOrientation != JList.VERTICAL) {
      height = preferredHeight + insets.top + insets.bottom;
    } else {
      Rectangle bounds = getCellBounds(list, lastRow, lastRow);
      height = bounds != null ? bounds.y + bounds.height + insets.bottom : 0;
    }
    return new Dimension(width, height);
  }

  @SuppressWarnings("unchecked")
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    maybeUpdateLayoutState();
    int rowHeight = list.getFixedCellHeight();
    if (rowHeight == -1 && list.getModel().getSize() > 0) {
      rowHeight = getRowHeight(0);
    }
    ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) list.getCellRenderer();
    if (rowHeight <= 0 || renderer == null || list.getModel().getSize() == 0) {
      return -1;
    }
    Component rc = renderer.getListCellRendererComponent(list, list.getModel().getElementAt(0), 0, false, false);
    int base = rc.getBaseline(Integer.MAX_VALUE / 2, rowHeight);
    return base < 0 ? -1 : list.getInsets().top + base;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
  }

  protected void selectPreviousIndex() {
    int s = list.getSelectedIndex();
    if (s > 0) {
      list.setSelectedIndex(s - 1);
      list.ensureIndexIsVisible(s - 1);
    }
  }

  protected void selectNextIndex() {
    int s = list.getSelectedIndex();
    if (s + 1 < list.getModel().getSize()) {
      list.setSelectedIndex(s + 1);
      list.ensureIndexIsVisible(s + 1);
    }
  }

  private void redrawList() {
    list.revalidate();
    list.repaint();
  }

  private void bump(int flag) {
    updateLayoutStateNeeded |= flag;
    redrawList();
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public PropertyChangeHandler() {
    }

    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent e) {
      String name = e.getPropertyName();
      if ("model".equals(name)) {
        ListModel<Object> oldModel = (ListModel<Object>) e.getOldValue();
        ListModel<Object> newModel = (ListModel<Object>) e.getNewValue();
        if (oldModel != null) {
          oldModel.removeListDataListener(listDataListener);
        }
        if (newModel != null) {
          newModel.addListDataListener(listDataListener);
        }
        bump(modelChanged);
      } else if ("selectionModel".equals(name)) {
        ListSelectionModel oldModel = (ListSelectionModel) e.getOldValue();
        ListSelectionModel newModel = (ListSelectionModel) e.getNewValue();
        if (oldModel != null) {
          oldModel.removeListSelectionListener(listSelectionListener);
        }
        if (newModel != null) {
          newModel.addListSelectionListener(listSelectionListener);
        }
        bump(selectionModelChanged);
      } else if ("cellRenderer".equals(name)) {
        bump(cellRendererChanged);
      } else if ("font".equals(name)) {
        bump(fontChanged);
      } else if ("prototypeCellValue".equals(name)) {
        bump(prototypeCellValueChanged);
      } else if ("fixedCellHeight".equals(name)) {
        bump(fixedCellHeightChanged);
      } else if ("fixedCellWidth".equals(name)) {
        bump(fixedCellWidthChanged);
      } else if ("selectionForeground".equals(name) || "selectionBackground".equals(name)) {
        list.repaint();
      } else if ("layoutOrientation".equals(name)) {
        layoutOrientation = list.getLayoutOrientation();
        bump(modelChanged);
      } else if ("visibleRowCount".equals(name)) {
        if (layoutOrientation != JList.VERTICAL) {
          bump(modelChanged);
        }
      } else if ("componentOrientation".equals(name)) {
        redrawList();
      }
    }
  }

  public class ListDataHandler implements ListDataListener {
    public ListDataHandler() {
    }

    public void contentsChanged(ListDataEvent e) {
      bump(modelChanged);
    }

    public void intervalAdded(ListDataEvent e) {
      updateLayoutStateNeeded = modelChanged;
      int minIndex = Math.min(e.getIndex0(), e.getIndex1());
      int maxIndex = Math.max(e.getIndex0(), e.getIndex1());
      ListSelectionModel sm = list.getSelectionModel();
      if (sm != null) {
        sm.insertIndexInterval(minIndex, maxIndex - minIndex + 1, true);
      }
      redrawList();
    }

    public void intervalRemoved(ListDataEvent e) {
      updateLayoutStateNeeded = modelChanged;
      ListSelectionModel sm = list.getSelectionModel();
      if (sm != null) {
        sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
      }
      redrawList();
    }
  }

  public class ListSelectionHandler implements ListSelectionListener {
    public ListSelectionHandler() {
    }

    public void valueChanged(ListSelectionEvent e) {
      maybeUpdateLayoutState();
      Rectangle bounds = getCellBounds(list, e.getFirstIndex(), e.getLastIndex());
      if (bounds != null) {
        list.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
      }
    }
  }

  public class FocusHandler implements FocusListener {
    public FocusHandler() {
    }

    protected void repaintCellFocus() {
      int leadIndex = list.getLeadSelectionIndex();
      if (leadIndex != -1) {
        Rectangle r = getCellBounds(list, leadIndex, leadIndex);
        if (r != null) {
          list.repaint(r.x, r.y, r.width, r.height);
        }
      }
    }

    public void focusGained(FocusEvent e) {
      repaintCellFocus();
    }

    public void focusLost(FocusEvent e) {
      repaintCellFocus();
    }
  }

  public class MouseInputHandler implements MouseInputListener {
    public MouseInputHandler() {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      if (e.isConsumed() || !SwingUtilities.isLeftMouseButton(e) || !list.isEnabled()) {
        return;
      }
      if (list.isRequestFocusEnabled()) {
        list.requestFocus();
      }
      int row = convertLocationToRow(e.getX(), e.getY(), false);
      if (row == -1) {
        return;
      }
      Rectangle bounds = getCellBounds(list, row, row);
      if (bounds == null || !bounds.contains(e.getPoint())) {
        return;
      }
      rowAtPress = row;
      adjustSelection(e, row);
    }

    public void mouseDragged(MouseEvent e) {
      if (e.isConsumed() || !SwingUtilities.isLeftMouseButton(e) || !list.isEnabled()) {
        return;
      }
      int row = convertLocationToRow(e.getX(), e.getY(), true);
      if (row != -1 && rowAtPress != -1) {
        ListSelectionModel sm = list.getSelectionModel();
        int anchor = sm.getAnchorSelectionIndex();
        sm.setValueIsAdjusting(true);
        if (anchor >= 0 && !e.isControlDown() && sm.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
          sm.setSelectionInterval(anchor, row);
        } else {
          sm.setSelectionInterval(row, row);
        }
        list.ensureIndexIsVisible(row);
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || !list.isEnabled()) {
        return;
      }
      if (list.getSelectionModel().getValueIsAdjusting()) {
        list.getSelectionModel().setValueIsAdjusting(false);
      }
      rowAtPress = -1;
    }

    public void mouseMoved(MouseEvent e) {
    }

    private void adjustSelection(MouseEvent e, int row) {
      ListSelectionModel sm = list.getSelectionModel();
      boolean ctrl = e.isControlDown();
      boolean shift = e.isShiftDown();
      boolean selected = sm.isSelectedIndex(row);
      int mode = sm.getSelectionMode();
      if (mode == ListSelectionModel.SINGLE_SELECTION) {
        if (ctrl && selected) {
          sm.removeSelectionInterval(row, row);
        } else {
          sm.setSelectionInterval(row, row);
        }
      } else if (shift && sm.getAnchorSelectionIndex() >= 0) {
        int anchor = sm.getAnchorSelectionIndex();
        sm.setValueIsAdjusting(true);
        sm.setSelectionInterval(anchor, row);
        sm.setAnchorSelectionIndex(anchor);
        sm.setLeadSelectionIndex(row);
      } else if (ctrl && mode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
        if (selected) {
          sm.removeSelectionInterval(row, row);
        } else {
          sm.addSelectionInterval(row, row);
        }
      } else {
        sm.setValueIsAdjusting(true);
        sm.setSelectionInterval(row, row);
      }
    }
  }
}
