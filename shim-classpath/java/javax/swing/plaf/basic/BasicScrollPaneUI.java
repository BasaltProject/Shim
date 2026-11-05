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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.UIResource;

public class BasicScrollPaneUI extends ScrollPaneUI implements ScrollPaneConstants {
  protected JScrollPane scrollpane;
  protected ChangeListener vsbChangeListener;
  protected ChangeListener hsbChangeListener;
  protected ChangeListener viewportChangeListener;
  protected PropertyChangeListener spPropertyChangeListener;
  private MouseWheelListener mouseScrollListener;
  private boolean viewportAdjusting;

  public BasicScrollPaneUI() {
  }

  public static ComponentUI createUI(JComponent x) {
    return new BasicScrollPaneUI();
  }

  public void paint(Graphics g, JComponent c) {
    Border vpBorder = scrollpane.getViewportBorder();
    if (vpBorder != null) {
      java.awt.Rectangle r = scrollpane.getViewportBorderBounds();
      vpBorder.paintBorder(scrollpane, g, r.x, r.y, r.width, r.height);
    }
  }

  public Dimension getMaximumSize(JComponent c) {
    return null;
  }

  protected void installDefaults(JScrollPane scrollpane) {
    LookAndFeel.installBorder(scrollpane, "ScrollPane.border");
    LookAndFeel.installColorsAndFont(scrollpane, "ScrollPane.background", "ScrollPane.foreground",
        "ScrollPane.font");
    Border vpBorder = scrollpane.getViewportBorder();
    if (vpBorder == null || vpBorder instanceof UIResource) {
      scrollpane.setViewportBorder(UIManager.getBorder("ScrollPane.viewportBorder"));
    }
    LookAndFeel.installProperty(scrollpane, "opaque", Boolean.TRUE);
  }

  protected void installListeners(JScrollPane c) {
    vsbChangeListener = createVSBChangeListener();
    hsbChangeListener = createHSBChangeListener();
    viewportChangeListener = createViewportChangeListener();
    spPropertyChangeListener = createPropertyChangeListener();
    JViewport viewport = scrollpane.getViewport();
    JScrollBar vsb = scrollpane.getVerticalScrollBar();
    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
    if (viewport != null) {
      viewport.addChangeListener(viewportChangeListener);
    }
    if (vsb != null) {
      vsb.getModel().addChangeListener(vsbChangeListener);
    }
    if (hsb != null) {
      hsb.getModel().addChangeListener(hsbChangeListener);
    }
    scrollpane.addPropertyChangeListener(spPropertyChangeListener);
    mouseScrollListener = createMouseWheelListener();
    scrollpane.addMouseWheelListener(mouseScrollListener);
  }

  protected void installKeyboardActions(JScrollPane c) {
    shim.swing.KeyActions.install(c, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ScrollPane.ancestorInputMap",
        shim.swing.KeyActions.scrollPaneActions());
  }

  public void installUI(JComponent x) {
    scrollpane = (JScrollPane) x;
    installDefaults(scrollpane);
    installListeners(scrollpane);
    installKeyboardActions(scrollpane);
    syncScrollPaneWithViewport();
  }

  protected void uninstallDefaults(JScrollPane c) {
    LookAndFeel.uninstallBorder(scrollpane);
    if (scrollpane.getViewportBorder() instanceof UIResource) {
      scrollpane.setViewportBorder(null);
    }
  }

  protected void uninstallListeners(JComponent c) {
    JViewport viewport = scrollpane.getViewport();
    JScrollBar vsb = scrollpane.getVerticalScrollBar();
    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
    if (viewport != null) {
      viewport.removeChangeListener(viewportChangeListener);
    }
    if (vsb != null) {
      vsb.getModel().removeChangeListener(vsbChangeListener);
    }
    if (hsb != null) {
      hsb.getModel().removeChangeListener(hsbChangeListener);
    }
    scrollpane.removePropertyChangeListener(spPropertyChangeListener);
    if (mouseScrollListener != null) {
      scrollpane.removeMouseWheelListener(mouseScrollListener);
    }
    vsbChangeListener = null;
    hsbChangeListener = null;
    viewportChangeListener = null;
    spPropertyChangeListener = null;
    mouseScrollListener = null;
  }

  protected void uninstallKeyboardActions(JScrollPane c) {
    shim.swing.KeyActions.uninstall(c, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  public void uninstallUI(JComponent c) {
    uninstallKeyboardActions(scrollpane);
    uninstallListeners(scrollpane);
    uninstallDefaults(scrollpane);
    scrollpane = null;
  }

  protected void syncScrollPaneWithViewport() {
    JViewport viewport = scrollpane.getViewport();
    JScrollBar vsb = scrollpane.getVerticalScrollBar();
    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
    JViewport rowHead = scrollpane.getRowHeader();
    JViewport colHead = scrollpane.getColumnHeader();
    boolean ltr = scrollpane.getComponentOrientation().isLeftToRight();
    if (viewport != null) {
      Dimension extentSize = viewport.getExtentSize();
      Dimension viewSize = viewport.getViewSize();
      Point viewPosition = viewport.getViewPosition();
      if (vsb != null) {
        int extent = extentSize.height;
        int max = viewSize.height;
        int value = Math.max(0, Math.min(viewPosition.y, max - extent));
        vsb.setValues(value, extent, 0, max);
      }
      if (hsb != null) {
        int extent = extentSize.width;
        int max = viewSize.width;
        int value;
        if (ltr) {
          value = Math.max(0, Math.min(viewPosition.x, max - extent));
        } else {
          int currentValue = hsb.getValue();
          int oldExtent = hsb.getVisibleAmount();
          int oldMax = hsb.getMaximum();
          int oldMin = hsb.getMinimum();
          int newMin = 0;
          if (oldMax == oldMin) {
            value = Math.max(0, max - extent);
          } else {
            value = Math.max(0, Math.min(max - extent, max - extent - viewPosition.x));
          }
        }
        hsb.setValues(value, extent, 0, max);
      }
      if (rowHead != null) {
        Point p = rowHead.getViewPosition();
        p.y = viewport.getViewPosition().y;
        p.x = 0;
        rowHead.setViewPosition(p);
      }
      if (colHead != null) {
        Point p = colHead.getViewPosition();
        p.x = viewport.getViewPosition().x;
        p.y = 0;
        colHead.setViewPosition(p);
      }
    }
  }

  protected void updateScrollBarDisplayPolicy(PropertyChangeEvent e) {
    scrollpane.revalidate();
    scrollpane.repaint();
  }

  protected void updateViewport(PropertyChangeEvent e) {
    JViewport oldViewport = (JViewport) e.getOldValue();
    JViewport newViewport = (JViewport) e.getNewValue();
    if (oldViewport != null) {
      oldViewport.removeChangeListener(viewportChangeListener);
    }
    if (newViewport != null) {
      Point p = newViewport.getViewPosition();
      if (scrollpane.getComponentOrientation().isLeftToRight()) {
        p.x = Math.max(p.x, 0);
      } else {
        int max = newViewport.getViewSize().width;
        int extent = newViewport.getExtentSize().width;
        p.x = max > extent ? Math.max(0, max - extent) : 0;
      }
      p.y = Math.max(p.y, 0);
      newViewport.setViewPosition(p);
      newViewport.addChangeListener(viewportChangeListener);
    }
  }

  protected void updateRowHeader(PropertyChangeEvent e) {
    JViewport newRowHead = (JViewport) (e.getNewValue());
    if (newRowHead != null) {
      JViewport viewport = scrollpane.getViewport();
      Point p = newRowHead.getViewPosition();
      p.y = (viewport != null) ? viewport.getViewPosition().y : 0;
      newRowHead.setViewPosition(p);
    }
  }

  protected void updateColumnHeader(PropertyChangeEvent e) {
    JViewport newColHead = (JViewport) (e.getNewValue());
    if (newColHead != null) {
      JViewport viewport = scrollpane.getViewport();
      Point p = newColHead.getViewPosition();
      p.x = (viewport != null) ? viewport.getViewPosition().x : 0;
      newColHead.setViewPosition(p);
    }
  }

  private void updateScrollBar(PropertyChangeEvent e, ChangeListener l) {
    JScrollBar oldSb = (JScrollBar) e.getOldValue();
    JScrollBar newSb = (JScrollBar) e.getNewValue();
    if (oldSb != null) {
      oldSb.getModel().removeChangeListener(l);
    }
    if (newSb != null) {
      newSb.getModel().addChangeListener(l);
    }
  }

  protected ChangeListener createViewportChangeListener() {
    return new ViewportChangeHandler();
  }

  protected ChangeListener createHSBChangeListener() {
    return new HSBChangeListener();
  }

  protected ChangeListener createVSBChangeListener() {
    return new VSBChangeListener();
  }

  protected MouseWheelListener createMouseWheelListener() {
    return new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (scrollpane.isWheelScrollingEnabled() && e.getWheelRotation() != 0) {
          JScrollBar toScroll = scrollpane.getVerticalScrollBar();
          if (toScroll == null || !toScroll.isVisible() || e.isShiftDown()) {
            toScroll = scrollpane.getHorizontalScrollBar();
            if (toScroll == null || !toScroll.isVisible()) {
              return;
            }
          }
          int direction = e.getWheelRotation() < 0 ? -1 : 1;
          int delta = e.getUnitsToScroll() * toScroll.getUnitIncrement(direction);
          toScroll.setValue(toScroll.getValue() + delta);
          e.consume();
        }
      }
    };
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    JViewport viewport = scrollpane.getViewport();
    Insets spInsets = scrollpane.getInsets();
    int y = spInsets.top;
    height = height - spInsets.top - spInsets.bottom;
    width = width - spInsets.left - spInsets.right;
    JViewport columnHeader = scrollpane.getColumnHeader();
    if (columnHeader != null && columnHeader.isVisible()) {
      Component header = columnHeader.getView();
      if (header != null && header.isVisible()) {
        Dimension headerPref = header.getPreferredSize();
        int baseline = header.getBaseline(headerPref.width, headerPref.height);
        if (baseline >= 0) {
          return y + baseline;
        }
      }
      Dimension columnPref = columnHeader.getPreferredSize();
      height -= columnPref.height;
      y += columnPref.height;
    }
    Component view = (viewport == null) ? null : viewport.getView();
    if (view != null && view.isVisible() && view.getBaselineResizeBehavior() == Component.BaselineResizeBehavior
        .CONSTANT_ASCENT) {
      Border viewportBorder = scrollpane.getViewportBorder();
      if (viewportBorder != null) {
        Insets vpbInsets = viewportBorder.getBorderInsets(scrollpane);
        y += vpbInsets.top;
        height = height - vpbInsets.top - vpbInsets.bottom;
        width = width - vpbInsets.left - vpbInsets.right;
      }
      if (view.getWidth() > 0 && view.getHeight() > 0) {
        Dimension min = view.getMinimumSize();
        width = Math.max(min.width, view.getWidth());
        height = Math.max(min.height, view.getHeight());
      }
      if (width > 0 && height > 0) {
        int baseline = view.getBaseline(width, height);
        if (baseline > 0) {
          return y + baseline;
        }
      }
    }
    return -1;
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    JViewport viewport = scrollpane.getViewport();
    Component view = (viewport == null) ? null : viewport.getView();
    if (view != null && view.getBaselineResizeBehavior() == Component.BaselineResizeBehavior.CONSTANT_ASCENT) {
      return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }
    return Component.BaselineResizeBehavior.OTHER;
  }

  public class ViewportChangeHandler implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (!viewportAdjusting) {
        syncScrollPaneWithViewport();
      }
    }
  }

  public class HSBChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      JViewport viewport = scrollpane.getViewport();
      if (viewport != null) {
        javax.swing.BoundedRangeModel model = (javax.swing.BoundedRangeModel) e.getSource();
        Point p = viewport.getViewPosition();
        int value = model.getValue();
        if (scrollpane.getComponentOrientation().isLeftToRight()) {
          p.x = value;
        } else {
          int max = viewport.getViewSize().width;
          int extent = viewport.getExtentSize().width;
          p.x = max - extent - value;
        }
        viewportAdjusting = true;
        try {
          viewport.setViewPosition(p);
        } finally {
          viewportAdjusting = false;
        }
      }
    }
  }

  public class VSBChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      JViewport viewport = scrollpane.getViewport();
      if (viewport != null) {
        javax.swing.BoundedRangeModel model = (javax.swing.BoundedRangeModel) e.getSource();
        Point p = viewport.getViewPosition();
        p.y = model.getValue();
        viewportAdjusting = true;
        try {
          viewport.setViewPosition(p);
        } finally {
          viewportAdjusting = false;
        }
      }
    }
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if ("verticalScrollBarDisplayPolicy".equals(propertyName)
          || "horizontalScrollBarDisplayPolicy".equals(propertyName)) {
        updateScrollBarDisplayPolicy(e);
      } else if ("viewport".equals(propertyName)) {
        updateViewport(e);
      } else if ("rowHeader".equals(propertyName)) {
        updateRowHeader(e);
      } else if ("columnHeader".equals(propertyName)) {
        updateColumnHeader(e);
      } else if ("verticalScrollBar".equals(propertyName)) {
        updateScrollBar(e, vsbChangeListener);
      } else if ("horizontalScrollBar".equals(propertyName)) {
        updateScrollBar(e, hsbChangeListener);
      } else if ("componentOrientation".equals(propertyName)) {
        scrollpane.revalidate();
        scrollpane.repaint();
      }
    }
  }
}
