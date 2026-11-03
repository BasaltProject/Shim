/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;
import javax.accessibility.*;
import javax.swing.event.*;
import javax.swing.plaf.ListUI;
import javax.swing.text.Position;

public class JList<E> extends javax.swing.JComponent implements javax.swing.Scrollable, javax.accessibility.Accessible {
  private AccessibleContext accessibleContext;
  private javax.swing.ListModel<E> bp_Model;
  private E bp_PrototypeCellValue;
  private int bp_LayoutOrientation;
  private int[] bp_SelectedIndices;
  private int bp_SelectionMode;
  private E bp_SelectedValue;
  private int bp_FixedCellWidth;
  private int bp_FixedCellHeight;
  private int bp_FirstVisibleIndex;
  private int bp_LastVisibleIndex;
  private javax.swing.event.ListSelectionListener[] bp_ListSelectionListeners;
  private final java.util.concurrent.CopyOnWriteArrayList<javax.swing.event.ListSelectionListener> bl_ListSelectionListener = new java.util.concurrent.CopyOnWriteArrayList<>();
  private java.lang.Object[] bp_SelectedValues;
  private java.util.List<E> bp_SelectedValuesList;
  private java.awt.Dimension bp_PreferredScrollableViewportSize;
  private boolean bp_ScrollableTracksViewportWidth;
  private boolean bp_ScrollableTracksViewportHeight;
  private boolean bp_SelectionEmpty;
  private int bp_VisibleRowCount;
  private javax.swing.ListCellRenderer<? super E> bp_CellRenderer;
  private int bp_AnchorSelectionIndex;
  private int bp_LeadSelectionIndex;
  private int bp_MinSelectionIndex;
  private int bp_MaxSelectionIndex;
  private boolean bp_DragEnabled;
  private javax.swing.DropMode bp_DropMode;
  private javax.swing.JList.DropLocation bp_DropLocation;
  private java.awt.Color bp_SelectionForeground;
  private java.awt.Color bp_SelectionBackground;
  private boolean bp_ValueIsAdjusting;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  private int bp_SelectedIndex;
  private javax.swing.ListSelectionModel bp_SelectionModel;
  public static final int VERTICAL = 0;
  public static final int VERTICAL_WRAP = 1;
  public static final int HORIZONTAL_WRAP = 2;
  public JList() {
    this(new AbstractListModel<E>() {
      public int getSize() { return 0; }
      public E getElementAt(int i) { throw new IndexOutOfBoundsException("No Data Model"); }
    });
  }
  public JList(java.util.Vector<? extends E> p0) {
    this(new AbstractListModel<E>() {
      public int getSize() { return p0.size(); }
      public E getElementAt(int i) { return p0.elementAt(i); }
    });
  }
  public JList(E[] p0) {
    this(new AbstractListModel<E>() {
      public int getSize() { return p0.length; }
      public E getElementAt(int i) { return p0[i]; }
    });
  }
  public JList(javax.swing.ListModel<E> p0) {
    if (p0 == null) {
      throw new IllegalArgumentException("dataModel must be non null");
    }
    bp_Model = p0;
    bp_SelectionModel = createSelectionModel();
    bp_VisibleRowCount = 8;
    bp_AnchorSelectionIndex = -1;
    bp_LeadSelectionIndex = -1;
    bp_FixedCellWidth = -1;
    bp_FixedCellHeight = -1;
    bp_SelectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
    bp_SelectionModel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        fireSelectionValueChanged(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
      }
    });
    setAutoscrolls(true);
    setOpaque(true);
    updateUI();
  }
  protected java.lang.String paramString() {
    String selectionForegroundString = (bp_SelectionForeground != null ? bp_SelectionForeground.toString() : "");
    String selectionBackgroundString = (bp_SelectionBackground != null ? bp_SelectionBackground.toString() : "");
    return super.paramString() + ",fixedCellHeight=" + bp_FixedCellHeight + ",fixedCellWidth=" + bp_FixedCellWidth
        + ",horizontalScrollIncrement=" + -1 + ",selectionBackground=" + selectionBackgroundString
        + ",selectionForeground=" + selectionForegroundString + ",visibleRowCount=" + bp_VisibleRowCount
        + ",layoutOrientation=" + bp_LayoutOrientation;
  }
  public javax.swing.ListModel<E> getModel() { return bp_Model; }
  public void clearSelection() {
    getSelectionModel().clearSelection();
  }
  public void setModel(javax.swing.ListModel<E> p0) {
    if (p0 == null) {
      throw new IllegalArgumentException("model must be non null");
    }
    ListModel<E> oldValue = bp_Model;
    bp_Model = p0;
    firePropertyChange("model", oldValue, bp_Model);
    clearSelection();
  }
  protected javax.swing.ListSelectionModel createSelectionModel() {
    return new DefaultListSelectionModel();
  }
  public E getPrototypeCellValue() { return bp_PrototypeCellValue; }
  public int locationToIndex(java.awt.Point p0) {
    ListUI ui = getUI();
    return (ui != null) ? ui.locationToIndex(this, p0) : -1;
  }
  public java.awt.Rectangle getCellBounds(int p0, int p1) {
    ListUI ui = getUI();
    return (ui != null) ? ui.getCellBounds(this, p0, p1) : null;
  }
  public int getLayoutOrientation() { return bp_LayoutOrientation; }
  public void setSelectedIndices(int[] p0) {
    ListSelectionModel sm = getSelectionModel();
    sm.clearSelection();
    int size = getModel().getSize();
    for (int i : p0) {
      if (i < size) {
        sm.addSelectionInterval(i, i);
      }
    }
  }
  public java.awt.Point indexToLocation(int p0) {
    ListUI ui = getUI();
    return (ui != null) ? ui.indexToLocation(this, p0) : null;
  }
  public int getSelectionMode() {
    return getSelectionModel().getSelectionMode();
  }
  public E getSelectedValue() {
    int i = getMinSelectionIndex();
    return (i == -1) ? null : getModel().getElementAt(i);
  }
  public void ensureIndexIsVisible(int p0) {
    Rectangle cellBounds = getCellBounds(p0, p0);
    if (cellBounds != null) {
      scrollRectToVisible(cellBounds);
    }
  }
  public int getFixedCellWidth() { return bp_FixedCellWidth; }
  public int getFixedCellHeight() { return bp_FixedCellHeight; }
  public int getFirstVisibleIndex() {
    Rectangle r = getVisibleRect();
    int first;
    if (this.getComponentOrientation().isLeftToRight()) {
      first = locationToIndex(r.getLocation());
    } else {
      first = locationToIndex(new Point((r.x + r.width) - 1, r.y));
    }
    if (first != -1) {
      Rectangle bounds = getCellBounds(first, first);
      if (bounds != null) {
        SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height, bounds);
        if (bounds.width == 0 || bounds.height == 0) {
          first = -1;
        }
      }
    }
    return first;
  }
  public void setPrototypeCellValue(E p0) {
    E oldValue = bp_PrototypeCellValue;
    bp_PrototypeCellValue = p0;
    firePropertyChange("prototypeCellValue", oldValue, p0);
  }
  public void setFixedCellWidth(int p0) {
    int oldValue = bp_FixedCellWidth;
    bp_FixedCellWidth = p0;
    firePropertyChange("fixedCellWidth", oldValue, bp_FixedCellWidth);
  }
  public void setFixedCellHeight(int p0) {
    int oldValue = bp_FixedCellHeight;
    bp_FixedCellHeight = p0;
    firePropertyChange("fixedCellHeight", oldValue, bp_FixedCellHeight);
  }
  public void setLayoutOrientation(int p0) {
    int oldValue = bp_LayoutOrientation;
    switch (p0) {
      case VERTICAL:
      case VERTICAL_WRAP:
      case HORIZONTAL_WRAP:
        bp_LayoutOrientation = p0;
        break;
      default:
        throw new IllegalArgumentException("layoutOrientation must be one of: VERTICAL, HORIZONTAL_WRAP or VERTICAL_WRAP");
    }
    firePropertyChange("layoutOrientation", oldValue, p0);
  }
  public int getLastVisibleIndex() {
    boolean leftToRight = this.getComponentOrientation().isLeftToRight();
    Rectangle r = getVisibleRect();
    Point lastPoint;
    if (leftToRight) {
      lastPoint = new Point((r.x + r.width) - 1, (r.y + r.height) - 1);
    } else {
      lastPoint = new Point(r.x, (r.y + r.height) - 1);
    }
    int location = locationToIndex(lastPoint);
    if (location != -1) {
      Rectangle bounds = getCellBounds(location, location);
      if (bounds != null) {
        SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height, bounds);
        if (bounds.width == 0 || bounds.height == 0) {
          boolean isHorizontalWrap = (getLayoutOrientation() == HORIZONTAL_WRAP);
          Point visibleLocation = isHorizontalWrap ? new Point(lastPoint.x, r.y) : new Point(r.x, lastPoint.y);
          int last;
          int visIndex = -1;
          int lIndex = location;
          location = -1;
          do {
            last = visIndex;
            visIndex = locationToIndex(visibleLocation);
            if (visIndex != -1) {
              bounds = getCellBounds(visIndex, visIndex);
              if (visIndex != lIndex && bounds != null && bounds.contains(visibleLocation)) {
                location = visIndex;
                if (isHorizontalWrap) {
                  visibleLocation.y = bounds.y + bounds.height;
                  if (visibleLocation.y >= lastPoint.y) {
                    last = visIndex;
                  }
                } else {
                  visibleLocation.x = bounds.x + bounds.width;
                  if (visibleLocation.x >= lastPoint.x) {
                    last = visIndex;
                  }
                }
              } else {
                last = visIndex;
              }
            }
          } while (visIndex != -1 && last != visIndex);
        }
      }
    }
    return location;
  }
  public void setListData(E[] p0) {
    setModel(new AbstractListModel<E>() {
      public int getSize() { return p0.length; }
      public E getElementAt(int i) { return p0[i]; }
    });
  }
  public void setListData(java.util.Vector<? extends E> p0) {
    setModel(new AbstractListModel<E>() {
      public int getSize() { return p0.size(); }
      public E getElementAt(int i) { return p0.elementAt(i); }
    });
  }
  protected void fireSelectionValueChanged(int p0, int p1, boolean p2) {
    ListSelectionEvent e = null;
    for (int i = bl_ListSelectionListener.size() - 1; i >= 0; i--) {
      if (e == null) {
        e = new ListSelectionEvent(this, p0, p1, p2);
      }
      bl_ListSelectionListener.get(i).valueChanged(e);
    }
  }
  public javax.swing.event.ListSelectionListener[] getListSelectionListeners() { return bl_ListSelectionListener.toArray(new javax.swing.event.ListSelectionListener[0]); }
  public java.lang.Object[] getSelectedValues() {
    ListSelectionModel sm = getSelectionModel();
    ListModel<E> dm = getModel();
    int iMin = sm.getMinSelectionIndex();
    int iMax = sm.getMaxSelectionIndex();
    if ((iMin < 0) || (iMax < 0)) {
      return new Object[0];
    }
    Object[] rvTmp = new Object[1 + (iMax - iMin)];
    int n = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (sm.isSelectedIndex(i)) {
        rvTmp[n++] = dm.getElementAt(i);
      }
    }
    Object[] rv = new Object[n];
    System.arraycopy(rvTmp, 0, rv, 0, n);
    return rv;
  }
  public java.util.List<E> getSelectedValuesList() {
    ListSelectionModel sm = getSelectionModel();
    ListModel<E> dm = getModel();
    int iMin = sm.getMinSelectionIndex();
    int iMax = sm.getMaxSelectionIndex();
    if ((iMin < 0) || (iMax < 0)) {
      return java.util.Collections.emptyList();
    }
    List<E> selectedItems = new java.util.ArrayList<E>();
    for (int i = iMin; i <= iMax; i++) {
      if (sm.isSelectedIndex(i)) {
        selectedItems.add(dm.getElementAt(i));
      }
    }
    return selectedItems;
  }
  public void setSelectedValue(java.lang.Object p0, boolean p1) {
    if (p0 == null) {
      setSelectedIndex(-1);
    } else if (!p0.equals(getSelectedValue())) {
      int i;
      int c;
      ListModel<E> dm = getModel();
      for (i = 0, c = dm.getSize(); i < c; i++) {
        if (p0.equals(dm.getElementAt(i))) {
          setSelectedIndex(i);
          if (p1) {
            ensureIndexIsVisible(i);
          }
          repaint();
          return;
        }
      }
      setSelectedIndex(-1);
    }
    repaint();
  }
  public java.awt.Dimension getPreferredScrollableViewportSize() {
    if (getLayoutOrientation() != VERTICAL) {
      return getPreferredSize();
    }
    Insets insets = getInsets();
    int dx = insets.left + insets.right;
    int dy = insets.top + insets.bottom;
    int visibleRowCount = getVisibleRowCount();
    int fixedCellWidth = getFixedCellWidth();
    int fixedCellHeight = getFixedCellHeight();
    if ((fixedCellWidth > 0) && (fixedCellHeight > 0)) {
      int width = fixedCellWidth + dx;
      int height = (visibleRowCount * fixedCellHeight) + dy;
      return new Dimension(width, height);
    } else if (getModel().getSize() > 0) {
      int width = getPreferredSize().width;
      int height;
      Rectangle r = getCellBounds(0, 0);
      if (r != null) {
        height = (visibleRowCount * r.height) + dy;
      } else {
        height = getPreferredSize().height;
      }
      return new Dimension(width, height);
    } else {
      fixedCellWidth = (fixedCellWidth > 0) ? fixedCellWidth : 256;
      fixedCellHeight = (fixedCellHeight > 0) ? fixedCellHeight : 16;
      return new Dimension(fixedCellWidth, fixedCellHeight * visibleRowCount);
    }
  }
  public int getScrollableUnitIncrement(java.awt.Rectangle p0, int p1, int p2) {
    if (p1 == SwingConstants.VERTICAL) {
      int row = getFirstVisibleIndex();
      if (row == -1) {
        return 0;
      } else if (p2 < 0) {
        Rectangle r = getCellBounds(row, row);
        if ((r.y == p0.y) && (row == 0)) {
          return 0;
        } else if (r.y == p0.y) {
          Point loc = r.getLocation();
          loc.y--;
          int prevIndex = locationToIndex(loc);
          Rectangle prevR = getCellBounds(prevIndex, prevIndex);
          if (prevR == null || prevR.y >= r.y) {
            return 0;
          }
          return prevR.height;
        } else {
          return p0.y - r.y;
        }
      } else {
        Rectangle r = getCellBounds(row, row);
        return r.height + r.y - p0.y;
      }
    } else if (p1 == SwingConstants.HORIZONTAL) {
      Font f = getFont();
      return (f != null) ? f.getSize() : 1;
    }
    throw new IllegalArgumentException("Invalid orientation: " + p1);
  }
  public int getScrollableBlockIncrement(java.awt.Rectangle p0, int p1, int p2) {
    if (p1 == SwingConstants.VERTICAL) {
      return p0.height;
    } else if (p1 == SwingConstants.HORIZONTAL) {
      return p0.width;
    }
    throw new IllegalArgumentException("Invalid orientation: " + p1);
  }
  public boolean getScrollableTracksViewportWidth() {
    if (getLayoutOrientation() == HORIZONTAL_WRAP && getVisibleRowCount() <= 0) {
      return true;
    }
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      return parent.getWidth() > getPreferredSize().width;
    }
    return false;
  }
  public boolean getScrollableTracksViewportHeight() {
    if (getLayoutOrientation() == VERTICAL_WRAP && getVisibleRowCount() <= 0) {
      return true;
    }
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      return parent.getHeight() > getPreferredSize().height;
    }
    return false;
  }
  public boolean isSelectionEmpty() {
    return getSelectionModel().isSelectionEmpty();
  }
  public int getVisibleRowCount() { return bp_VisibleRowCount; }
  public void setCellRenderer(javax.swing.ListCellRenderer<? super E> p0) {
    ListCellRenderer<? super E> oldValue = bp_CellRenderer;
    bp_CellRenderer = p0;
    firePropertyChange("cellRenderer", oldValue, p0);
  }
  public void setSelectionInterval(int p0, int p1) {
    getSelectionModel().setSelectionInterval(p0, p1);
  }
  public void addSelectionInterval(int p0, int p1) {
    getSelectionModel().addSelectionInterval(p0, p1);
  }
  public void removeSelectionInterval(int p0, int p1) {
    getSelectionModel().removeSelectionInterval(p0, p1);
  }
  public void setVisibleRowCount(int p0) {
    int oldValue = bp_VisibleRowCount;
    bp_VisibleRowCount = Math.max(0, p0);
    firePropertyChange("visibleRowCount", oldValue, p0);
  }
  public int getNextMatch(java.lang.String p0, int p1, javax.swing.text.Position.Bias p2) {
    ListModel<E> model = getModel();
    int max = model.getSize();
    if (p0 == null) {
      throw new IllegalArgumentException();
    }
    if (p1 < 0 || p1 >= max) {
      throw new IllegalArgumentException();
    }
    p0 = p0.toUpperCase();
    int increment = (p2 == Position.Bias.Forward) ? 1 : -1;
    int index = p1;
    do {
      E element = model.getElementAt(index);
      if (element != null) {
        String string;
        if (element instanceof String) {
          string = ((String) element).toUpperCase();
        } else {
          string = element.toString();
          if (string != null) {
            string = string.toUpperCase();
          }
        }
        if (string != null && string.startsWith(p0)) {
          return index;
        }
      }
      index = (index + increment + max) % max;
    } while (index != p1);
    return -1;
  }
  public int getAnchorSelectionIndex() {
    return getSelectionModel().getAnchorSelectionIndex();
  }
  public int getLeadSelectionIndex() {
    return getSelectionModel().getLeadSelectionIndex();
  }
  public void setSelectionMode(int p0) {
    getSelectionModel().setSelectionMode(p0);
  }
  public int getMinSelectionIndex() {
    return getSelectionModel().getMinSelectionIndex();
  }
  public int[] getSelectedIndices() {
    ListSelectionModel sm = getSelectionModel();
    int iMin = sm.getMinSelectionIndex();
    int iMax = sm.getMaxSelectionIndex();
    if ((iMin < 0) || (iMax < 0)) {
      return new int[0];
    }
    int[] rvTmp = new int[1 + (iMax - iMin)];
    int n = 0;
    for (int i = iMin; i <= iMax; i++) {
      if (sm.isSelectedIndex(i)) {
        rvTmp[n++] = i;
      }
    }
    int[] rv = new int[n];
    System.arraycopy(rvTmp, 0, rv, 0, n);
    return rv;
  }
  public boolean isSelectedIndex(int p0) {
    return getSelectionModel().isSelectedIndex(p0);
  }
  public void removeListSelectionListener(javax.swing.event.ListSelectionListener p0) { bl_ListSelectionListener.remove(p0); }
  public void addListSelectionListener(javax.swing.event.ListSelectionListener p0) { if (p0 != null) { bl_ListSelectionListener.add(p0); } }
  public int getMaxSelectionIndex() {
    return getSelectionModel().getMaxSelectionIndex();
  }
  public javax.swing.ListCellRenderer<? super E> getCellRenderer() { return bp_CellRenderer; }
  public void setDragEnabled(boolean p0) {
    if (p0 && java.awt.GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    boolean old = bp_DragEnabled;
    bp_DragEnabled = p0;
    firePropertyChange("dragEnabled", old, p0);
  }
  public boolean getDragEnabled() { return bp_DragEnabled; }
  public final void setDropMode(javax.swing.DropMode p0) { javax.swing.DropMode old = bp_DropMode; bp_DropMode = p0; firePropertyChange("dropMode", old, p0); }
  public final javax.swing.DropMode getDropMode() { return bp_DropMode; }
  public final javax.swing.JList.DropLocation getDropLocation() { return bp_DropLocation; }
  public java.awt.Color getSelectionForeground() { return bp_SelectionForeground; }
  public void setSelectionForeground(java.awt.Color p0) {
    Color oldValue = bp_SelectionForeground;
    bp_SelectionForeground = p0;
    firePropertyChange("selectionForeground", oldValue, p0);
  }
  public java.awt.Color getSelectionBackground() { return bp_SelectionBackground; }
  public void setSelectionBackground(java.awt.Color p0) {
    Color oldValue = bp_SelectionBackground;
    bp_SelectionBackground = p0;
    firePropertyChange("selectionBackground", oldValue, p0);
  }
  public void setValueIsAdjusting(boolean p0) {
    getSelectionModel().setValueIsAdjusting(p0);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJList();
    }
    return accessibleContext;
  }
  public boolean getValueIsAdjusting() {
    return getSelectionModel().getValueIsAdjusting();
  }
  public int getSelectedIndex() {
    return getMinSelectionIndex();
  }
  public void updateUI() {
    setUI((ListUI) UIManager.getUI(this));
    ListCellRenderer<? super E> renderer = getCellRenderer();
    if (renderer instanceof Component) {
      SwingUtilities.updateComponentTreeUI((Component) renderer);
    } else if (renderer == null) {
      bp_CellRenderer = (ListCellRenderer<? super E>) new DefaultListCellRenderer.UIResource();
    }
    if (bp_SelectionBackground == null || bp_SelectionBackground instanceof javax.swing.plaf.UIResource) {
      bp_SelectionBackground = UIManager.getColor("List.selectionBackground");
    }
    if (bp_SelectionForeground == null || bp_SelectionForeground instanceof javax.swing.plaf.UIResource) {
      bp_SelectionForeground = UIManager.getColor("List.selectionForeground");
    }
  }
  public void setSelectionModel(javax.swing.ListSelectionModel p0) {
    if (p0 == null) {
      throw new IllegalArgumentException("selectionModel must be non null");
    }
    ListSelectionModel oldValue = bp_SelectionModel;
    bp_SelectionModel = p0;
    firePropertyChange("selectionModel", oldValue, p0);
  }
  public void setUI(javax.swing.plaf.ListUI p0) { super.setUI(p0); }
  public javax.swing.plaf.ListUI getUI() { return (javax.swing.plaf.ListUI) ui; }
  public javax.swing.ListSelectionModel getSelectionModel() { return bp_SelectionModel; }
  public void setSelectedIndex(int p0) {
    if (p0 >= getModel().getSize()) {
      return;
    }
    getSelectionModel().setSelectionInterval(p0, p0);
  }
  public java.lang.String getUIClassID() { return "ListUI"; }
  public java.lang.String getToolTipText(java.awt.event.MouseEvent p0) {
    return super.getToolTipText(p0);
  }

  public static final class DropLocation extends javax.swing.TransferHandler.DropLocation {
    private final int index;
    private final boolean isInsert;

    DropLocation(java.awt.Point p, int index, boolean isInsert) {
      super(p);
      this.index = index;
      this.isInsert = isInsert;
    }

    public int getIndex() { return index; }

    public boolean isInsert() { return isInsert; }

    public java.lang.String toString() {
      return getClass().getName() + "[dropPoint=" + getDropPoint() + ",index=" + index + ",insert=" + isInsert + "]";
    }
  }

  protected class AccessibleJList extends AccessibleJComponent {
    public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.LIST; }
    public int getAccessibleChildrenCount() { return getModel().getSize(); }

    public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
  }
}
