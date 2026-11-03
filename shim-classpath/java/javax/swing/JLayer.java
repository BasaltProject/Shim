/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.accessibility.Accessible;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;

public final class JLayer<V extends Component> extends JComponent implements Scrollable, PropertyChangeListener,
    Accessible {
  private V view;
  private LayerUI<? super V> layerUI;
  private JPanel glassPane;
  private long eventMask;

  public JLayer() { this(null); }

  public JLayer(V view) { this(view, new LayerUI<V>()); }

  public JLayer(V view, LayerUI<V> ui) {
    setGlassPane(createGlassPane());
    setView(view);
    setUI(ui);
  }

  public V getView() { return view; }

  public void setView(V view) {
    Component oldView = getView();
    if (oldView != null) {
      super.remove(oldView);
    }
    if (view != null) {
      super.addImpl(view, null, getComponentCount());
    }
    this.view = view;
    firePropertyChange("view", oldView, view);
    revalidate();
    repaint();
  }

  public void setUI(LayerUI<? super V> ui) {
    super.setUI(ui);
    this.layerUI = ui;
  }

  @SuppressWarnings("unchecked")
  public LayerUI<? super V> getUI() { return layerUI; }

  public JPanel getGlassPane() { return glassPane; }

  public void setGlassPane(JPanel glassPane) {
    Component oldGlassPane = getGlassPane();
    boolean isGlassPaneVisible = false;
    if (oldGlassPane != null) {
      isGlassPaneVisible = oldGlassPane.isVisible();
      super.remove(oldGlassPane);
    }
    if (glassPane != null) {
      glassPane.setVisible(isGlassPaneVisible);
      super.addImpl(glassPane, null, 0);
    }
    this.glassPane = glassPane;
    firePropertyChange("glassPane", oldGlassPane, glassPane);
    revalidate();
    repaint();
  }

  public JPanel createGlassPane() { return new DefaultLayerGlassPane(); }

  public void setLayout(LayoutManager mgr) {
    if (mgr != null) {
      throw new IllegalArgumentException("JLayer.setLayout() not supported");
    }
  }

  public void setBorder(Border border) {
    if (border != null) {
      throw new IllegalArgumentException("JLayer.setBorder() not supported");
    }
  }

  public Border getBorder() { return null; }

  protected void addImpl(Component comp, Object constraints, int index) {
    throw new UnsupportedOperationException(
        "Adding components to JLayer is not supported, use setView() or setGlassPane() instead");
  }

  public void remove(Component comp) {
    if (comp == null) {
      super.remove(comp);
    } else if (comp == getView()) {
      setView(null);
    } else if (comp == getGlassPane()) {
      setGlassPane(null);
    } else {
      super.remove(comp);
    }
  }

  public void removeAll() {
    if (view != null) {
      setView(null);
    }
    if (glassPane != null) {
      setGlassPane(null);
    }
  }

  protected boolean isPaintingOrigin() { return true; }

  public void paintImmediately(int x, int y, int w, int h) {
    if (getUI() != null) {
      layerUI.paintImmediately(x, y, w, h, this);
    } else {
      super.paintImmediately(x, y, w, h);
    }
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
    if (getUI() != null) {
      return layerUI.imageUpdate(img, infoflags, x, y, w, h, this);
    }
    return super.imageUpdate(img, infoflags, x, y, w, h);
  }

  public void paint(Graphics g) {
    if (!isPainting) {
      isPainting = true;
      try {
        super.paintComponent(g);
      } finally {
        isPainting = false;
      }
    } else {
      super.paint(g);
    }
  }

  private transient boolean isPainting;

  protected void paintComponent(Graphics g) {
    if (layerUI != null) {
      layerUI.paint(g, this);
    }
  }

  public boolean isOptimizedDrawingEnabled() { return false; }

  public void propertyChange(PropertyChangeEvent evt) {
    if (getUI() != null) {
      layerUI.applyPropertyChange(evt, this);
    }
  }

  public void setLayerEventMask(long layerEventMask) {
    long oldEventMask = getLayerEventMask();
    this.eventMask = layerEventMask;
    firePropertyChange("layerEventMask", oldEventMask, layerEventMask);
  }

  public long getLayerEventMask() { return eventMask; }

  public void updateUI() {
    if (getUI() != null) {
      getUI().updateUI(this);
    }
  }

  public Dimension getPreferredScrollableViewportSize() {
    if (getView() instanceof Scrollable) {
      return ((Scrollable) getView()).getPreferredScrollableViewportSize();
    }
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (getView() instanceof Scrollable) {
      return ((Scrollable) getView()).getScrollableBlockIncrement(visibleRect, orientation, direction);
    }
    return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
  }

  public boolean getScrollableTracksViewportHeight() {
    if (getView() instanceof Scrollable) {
      return ((Scrollable) getView()).getScrollableTracksViewportHeight();
    }
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    if (getView() instanceof Scrollable) {
      return ((Scrollable) getView()).getScrollableTracksViewportWidth();
    }
    return false;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (getView() instanceof Scrollable) {
      return ((Scrollable) getView()).getScrollableUnitIncrement(visibleRect, orientation, direction);
    }
    return 1;
  }

  public void addNotify() { super.addNotify(); }

  public void removeNotify() { super.removeNotify(); }

  public void doLayout() {
    if (getUI() != null) {
      layerUI.doLayout(this);
    }
  }

  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJComponent() { };
    }
    return accessibleContext;
  }

  static class DefaultLayerGlassPane extends JPanel {
    public DefaultLayerGlassPane() { setOpaque(false); }

    public boolean contains(int x, int y) { return false; }
  }
}
