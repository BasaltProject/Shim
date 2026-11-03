/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.UIResource;

public class JScrollPane extends JComponent implements ScrollPaneConstants, Accessible {
  private Border viewportBorder;
  private boolean wheelScrollState = true;

  protected int verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED;
  protected int horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED;
  protected JViewport viewport;
  protected JScrollBar verticalScrollBar;
  protected JScrollBar horizontalScrollBar;
  protected JViewport rowHeader;
  protected JViewport columnHeader;
  protected Component lowerLeft;
  protected Component lowerRight;
  protected Component upperLeft;
  protected Component upperRight;

  public JScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
    setLayout(new ScrollPaneLayout.UIResource());
    setVerticalScrollBarPolicy(vsbPolicy);
    setHorizontalScrollBarPolicy(hsbPolicy);
    setViewport(createViewport());
    setVerticalScrollBar(createVerticalScrollBar());
    setHorizontalScrollBar(createHorizontalScrollBar());
    if (view != null) {
      setViewportView(view);
    }
    setUIProperty("opaque", true);
    updateUI();
    if (!this.getComponentOrientation().isLeftToRight()) {
      viewport.setViewPosition(new java.awt.Point(Integer.MAX_VALUE, 0));
    }
  }

  public JScrollPane(Component view) { this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED); }

  public JScrollPane(int vsbPolicy, int hsbPolicy) { this(null, vsbPolicy, hsbPolicy); }

  public JScrollPane() { this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED); }

  public ScrollPaneUI getUI() { return (ScrollPaneUI) ui; }

  public void setUI(ScrollPaneUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((ScrollPaneUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "ScrollPaneUI"; }

  public void setLayout(LayoutManager layout) {
    if (layout instanceof ScrollPaneLayout) {
      super.setLayout(layout);
      ((ScrollPaneLayout) layout).syncWithScrollPane(this);
    } else if (layout == null) {
      super.setLayout(layout);
    } else {
      String s = "layout of JScrollPane must be a ScrollPaneLayout";
      throw new ClassCastException(s);
    }
  }

  public boolean isValidateRoot() { return true; }

  public int getVerticalScrollBarPolicy() { return verticalScrollBarPolicy; }

  public void setVerticalScrollBarPolicy(int policy) {
    switch (policy) {
      case VERTICAL_SCROLLBAR_AS_NEEDED:
      case VERTICAL_SCROLLBAR_NEVER:
      case VERTICAL_SCROLLBAR_ALWAYS:
        break;
      default:
        throw new IllegalArgumentException("invalid verticalScrollBarPolicy");
    }
    int old = verticalScrollBarPolicy;
    verticalScrollBarPolicy = policy;
    firePropertyChange("verticalScrollBarPolicy", old, policy);
    revalidate();
    repaint();
  }

  public int getHorizontalScrollBarPolicy() { return horizontalScrollBarPolicy; }

  public void setHorizontalScrollBarPolicy(int policy) {
    switch (policy) {
      case HORIZONTAL_SCROLLBAR_AS_NEEDED:
      case HORIZONTAL_SCROLLBAR_NEVER:
      case HORIZONTAL_SCROLLBAR_ALWAYS:
        break;
      default:
        throw new IllegalArgumentException("invalid horizontalScrollBarPolicy");
    }
    int old = horizontalScrollBarPolicy;
    horizontalScrollBarPolicy = policy;
    firePropertyChange("horizontalScrollBarPolicy", old, policy);
    revalidate();
    repaint();
  }

  public Border getViewportBorder() { return viewportBorder; }

  public void setViewportBorder(Border viewportBorder) {
    Border oldValue = this.viewportBorder;
    this.viewportBorder = viewportBorder;
    firePropertyChange("viewportBorder", oldValue, viewportBorder);
  }

  public Rectangle getViewportBorderBounds() {
    Rectangle borderR = new Rectangle(getSize());
    java.awt.Insets insets = getInsets();
    borderR.x = insets.left;
    borderR.y = insets.top;
    borderR.width -= insets.left + insets.right;
    borderR.height -= insets.top + insets.bottom;
    LayoutManager lm = getLayout();
    if (lm instanceof ScrollPaneLayout) {
      return ((ScrollPaneLayout) lm).getViewportBorderBounds(this);
    }
    return borderR;
  }

  public JScrollBar createHorizontalScrollBar() { return new ScrollBar(JScrollBar.HORIZONTAL); }

  public JScrollBar getHorizontalScrollBar() { return horizontalScrollBar; }

  public void setHorizontalScrollBar(JScrollBar horizontalScrollBar) {
    JScrollBar old = getHorizontalScrollBar();
    this.horizontalScrollBar = horizontalScrollBar;
    if (horizontalScrollBar != null) {
      add(horizontalScrollBar, HORIZONTAL_SCROLLBAR);
    } else if (old != null) {
      remove(old);
    }
    firePropertyChange("horizontalScrollBar", old, horizontalScrollBar);
    revalidate();
    repaint();
  }

  public JScrollBar createVerticalScrollBar() { return new ScrollBar(JScrollBar.VERTICAL); }

  public JScrollBar getVerticalScrollBar() { return verticalScrollBar; }

  public void setVerticalScrollBar(JScrollBar verticalScrollBar) {
    JScrollBar old = getVerticalScrollBar();
    this.verticalScrollBar = verticalScrollBar;
    add(verticalScrollBar, VERTICAL_SCROLLBAR);
    firePropertyChange("verticalScrollBar", old, verticalScrollBar);
    revalidate();
    repaint();
  }

  protected JViewport createViewport() { return new JViewport(); }

  public JViewport getViewport() { return viewport; }

  public void setViewport(JViewport newViewport) {
    JViewport old = getViewport();
    viewport = newViewport;
    if (newViewport != null) {
      add(newViewport, VIEWPORT);
    } else if (old != null) {
      remove(old);
    }
    firePropertyChange("viewport", old, newViewport);
    if (accessibleContext != null) {
      ((AccessibleJScrollPane) accessibleContext).resetViewPort();
    }
    revalidate();
    repaint();
  }

  public void setViewportView(Component view) {
    if (getViewport() == null) {
      setViewport(createViewport());
    }
    getViewport().setView(view);
  }

  public JViewport getRowHeader() { return rowHeader; }

  public void setRowHeader(JViewport rowHeader) {
    JViewport old = getRowHeader();
    this.rowHeader = rowHeader;
    if (rowHeader != null) {
      add(rowHeader, ROW_HEADER);
    } else if (old != null) {
      remove(old);
    }
    firePropertyChange("rowHeader", old, rowHeader);
    revalidate();
    repaint();
  }

  public void setRowHeaderView(Component view) {
    if (getRowHeader() == null) {
      setRowHeader(createViewport());
    }
    getRowHeader().setView(view);
  }

  public JViewport getColumnHeader() { return columnHeader; }

  public void setColumnHeader(JViewport columnHeader) {
    JViewport old = getColumnHeader();
    this.columnHeader = columnHeader;
    if (columnHeader != null) {
      add(columnHeader, COLUMN_HEADER);
    } else if (old != null) {
      remove(old);
    }
    firePropertyChange("columnHeader", old, columnHeader);
    revalidate();
    repaint();
  }

  public void setColumnHeaderView(Component view) {
    if (getColumnHeader() == null) {
      setColumnHeader(createViewport());
    }
    getColumnHeader().setView(view);
  }

  public Component getCorner(String key) {
    boolean isLeftToRight = getComponentOrientation().isLeftToRight();
    if (key.equals(LOWER_LEADING_CORNER)) {
      key = isLeftToRight ? LOWER_LEFT_CORNER : LOWER_RIGHT_CORNER;
    } else if (key.equals(LOWER_TRAILING_CORNER)) {
      key = isLeftToRight ? LOWER_RIGHT_CORNER : LOWER_LEFT_CORNER;
    } else if (key.equals(UPPER_LEADING_CORNER)) {
      key = isLeftToRight ? UPPER_LEFT_CORNER : UPPER_RIGHT_CORNER;
    } else if (key.equals(UPPER_TRAILING_CORNER)) {
      key = isLeftToRight ? UPPER_RIGHT_CORNER : UPPER_LEFT_CORNER;
    }
    if (key.equals(LOWER_LEFT_CORNER)) {
      return lowerLeft;
    } else if (key.equals(LOWER_RIGHT_CORNER)) {
      return lowerRight;
    } else if (key.equals(UPPER_LEFT_CORNER)) {
      return upperLeft;
    } else if (key.equals(UPPER_RIGHT_CORNER)) {
      return upperRight;
    }
    return null;
  }

  public void setCorner(String key, Component corner) {
    Component old;
    boolean isLeftToRight = getComponentOrientation().isLeftToRight();
    if (key.equals(LOWER_LEADING_CORNER)) {
      key = isLeftToRight ? LOWER_LEFT_CORNER : LOWER_RIGHT_CORNER;
    } else if (key.equals(LOWER_TRAILING_CORNER)) {
      key = isLeftToRight ? LOWER_RIGHT_CORNER : LOWER_LEFT_CORNER;
    } else if (key.equals(UPPER_LEADING_CORNER)) {
      key = isLeftToRight ? UPPER_LEFT_CORNER : UPPER_RIGHT_CORNER;
    } else if (key.equals(UPPER_TRAILING_CORNER)) {
      key = isLeftToRight ? UPPER_RIGHT_CORNER : UPPER_LEFT_CORNER;
    }
    if (key.equals(LOWER_LEFT_CORNER)) {
      old = lowerLeft;
      lowerLeft = corner;
    } else if (key.equals(LOWER_RIGHT_CORNER)) {
      old = lowerRight;
      lowerRight = corner;
    } else if (key.equals(UPPER_LEFT_CORNER)) {
      old = upperLeft;
      upperLeft = corner;
    } else if (key.equals(UPPER_RIGHT_CORNER)) {
      old = upperRight;
      upperRight = corner;
    } else {
      throw new IllegalArgumentException("invalid corner key");
    }
    if (old != null) {
      remove(old);
    }
    if (corner != null) {
      add(corner, key);
    }
    firePropertyChange(key, old, corner);
    revalidate();
    repaint();
  }

  public void setComponentOrientation(ComponentOrientation co) {
    super.setComponentOrientation(co);
    if (verticalScrollBar != null) {
      verticalScrollBar.setComponentOrientation(co);
    }
    if (horizontalScrollBar != null) {
      horizontalScrollBar.setComponentOrientation(co);
    }
  }

  public boolean isWheelScrollingEnabled() { return wheelScrollState; }

  public void setWheelScrollingEnabled(boolean handleWheel) {
    boolean old = wheelScrollState;
    wheelScrollState = handleWheel;
    firePropertyChange("wheelScrollingEnabled", old, handleWheel);
  }

  protected String paramString() {
    String viewportBorderString = viewportBorder != null ? viewportBorder.toString() : "";
    String viewportString = viewport != null ? viewport.toString() : "";
    String verticalScrollBarPolicyString;
    if (verticalScrollBarPolicy == VERTICAL_SCROLLBAR_AS_NEEDED) {
      verticalScrollBarPolicyString = "VERTICAL_SCROLLBAR_AS_NEEDED";
    } else if (verticalScrollBarPolicy == VERTICAL_SCROLLBAR_NEVER) {
      verticalScrollBarPolicyString = "VERTICAL_SCROLLBAR_NEVER";
    } else if (verticalScrollBarPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
      verticalScrollBarPolicyString = "VERTICAL_SCROLLBAR_ALWAYS";
    } else {
      verticalScrollBarPolicyString = "";
    }
    String horizontalScrollBarPolicyString;
    if (horizontalScrollBarPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED) {
      horizontalScrollBarPolicyString = "HORIZONTAL_SCROLLBAR_AS_NEEDED";
    } else if (horizontalScrollBarPolicy == HORIZONTAL_SCROLLBAR_NEVER) {
      horizontalScrollBarPolicyString = "HORIZONTAL_SCROLLBAR_NEVER";
    } else if (horizontalScrollBarPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
      horizontalScrollBarPolicyString = "HORIZONTAL_SCROLLBAR_ALWAYS";
    } else {
      horizontalScrollBarPolicyString = "";
    }
    String horizontalScrollBarString = horizontalScrollBar != null ? horizontalScrollBar.toString() : "";
    String verticalScrollBarString = verticalScrollBar != null ? verticalScrollBar.toString() : "";
    String columnHeaderString = columnHeader != null ? columnHeader.toString() : "";
    String rowHeaderString = rowHeader != null ? rowHeader.toString() : "";
    String lowerLeftString = lowerLeft != null ? lowerLeft.toString() : "";
    String lowerRightString = lowerRight != null ? lowerRight.toString() : "";
    String upperLeftString = upperLeft != null ? upperLeft.toString() : "";
    String upperRightString = upperRight != null ? upperRight.toString() : "";
    return super.paramString() + ",columnHeader=" + columnHeaderString + ",horizontalScrollBar="
        + horizontalScrollBarString + ",horizontalScrollBarPolicy=" + horizontalScrollBarPolicyString
        + ",lowerLeft=" + lowerLeftString + ",lowerRight=" + lowerRightString + ",rowHeader=" + rowHeaderString
        + ",upperLeft=" + upperLeftString + ",upperRight=" + upperRightString + ",verticalScrollBar="
        + verticalScrollBarString + ",verticalScrollBarPolicy=" + verticalScrollBarPolicyString + ",viewport="
        + viewportString + ",viewportBorder=" + viewportBorderString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJScrollPane();
    }
    return accessibleContext;
  }

  protected class ScrollBar extends JScrollBar implements UIResource {
    private boolean unitIncrementSet;
    private boolean blockIncrementSet;

    public ScrollBar(int orientation) {
      super(orientation);
      putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
    }

    public void setUnitIncrement(int unitIncrement) {
      unitIncrementSet = true;
      putClientProperty("JScrollBar.fastWheelScrolling", null);
      super.setUnitIncrement(unitIncrement);
    }

    public int getUnitIncrement(int direction) {
      JViewport vp = getViewport();
      if (!unitIncrementSet && vp != null && vp.getView() instanceof Scrollable) {
        Scrollable view = (Scrollable) (vp.getView());
        Rectangle vr = vp.getViewRect();
        return view.getScrollableUnitIncrement(vr, getOrientation(), direction);
      }
      return super.getUnitIncrement(direction);
    }

    public void setBlockIncrement(int blockIncrement) {
      blockIncrementSet = true;
      putClientProperty("JScrollBar.fastWheelScrolling", null);
      super.setBlockIncrement(blockIncrement);
    }

    public int getBlockIncrement(int direction) {
      JViewport vp = getViewport();
      if (blockIncrementSet || vp == null) {
        return super.getBlockIncrement(direction);
      } else if (vp.getView() instanceof Scrollable) {
        Scrollable view = (Scrollable) (vp.getView());
        Rectangle vr = vp.getViewRect();
        return view.getScrollableBlockIncrement(vr, getOrientation(), direction);
      } else if (getOrientation() == VERTICAL) {
        return vp.getExtentSize().height;
      } else {
        return vp.getExtentSize().width;
      }
    }
  }

  protected class AccessibleJScrollPane extends AccessibleJComponent implements ChangeListener, PropertyChangeListener {
    protected JViewport viewPort = null;

    public AccessibleJScrollPane() {
      super();
      JScrollPane scrollPane = JScrollPane.this;
      scrollPane.addPropertyChangeListener(this);
      scrollPane.getViewport().addChangeListener(this);
      viewPort = scrollPane.getViewport();
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.SCROLL_PANE; }

    public void stateChanged(ChangeEvent e) {
      if (e == null) {
        throw new NullPointerException();
      }
      firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, Boolean.valueOf(false),
          Boolean.valueOf(true));
    }

    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if (propertyName == "horizontalScrollBar" || propertyName == "verticalScrollBar") {
        if (e.getNewValue() instanceof JScrollBar) {
          ((JScrollBar) e.getNewValue()).addPropertyChangeListener(this);
        }
        if (e.getOldValue() instanceof JScrollBar) {
          ((JScrollBar) e.getOldValue()).removePropertyChangeListener(this);
        }
      } else if (propertyName == "viewport") {
        resetViewPort();
      }
    }

    void resetViewPort() {
      if (viewPort != null) {
        viewPort.removeChangeListener(this);
      }
      viewPort = JScrollPane.this.getViewport();
      if (viewPort != null) {
        viewPort.addChangeListener(this);
      }
    }
  }
}
