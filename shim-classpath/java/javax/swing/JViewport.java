/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serializable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;

public class JViewport extends JComponent implements Accessible {
  public static final int BLIT_SCROLL_MODE = 1;
  public static final int BACKINGSTORE_SCROLL_MODE = 2;
  public static final int SIMPLE_SCROLL_MODE = 0;

  protected boolean isViewSizeSet = false;
  protected Point lastPaintPosition = null;
  protected boolean backingStore = false;
  protected transient Image backingStoreImage = null;
  protected boolean scrollUnderway = false;

  private transient ChangeEvent changeEvent;
  private int scrollMode = BLIT_SCROLL_MODE;
  private boolean viewChanged;


  private ViewListener viewListenerImpl;

  public JViewport() {
    super();
    setLayout(createLayoutManager());
    setOpaque(true);
    updateUI();
    setInheritsPopupMenu(true);
  }

  public ViewportUI getUI() { return (ViewportUI) ui; }

  public void setUI(ViewportUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((ViewportUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "ViewportUI"; }

  protected void addImpl(Component child, Object constraints, int index) {
    setView(child);
  }

  public void remove(Component child) {
    child.removeComponentListener(viewListenerImpl);
    super.remove(child);
  }

  public void scrollRectToVisible(Rectangle contentRect) {
    Component view = getView();
    if (view == null) {
      return;
    }
    if (!view.isValid()) {
      validateView();
    }
    int dx = positiveDelta(contentRect.x, contentRect.width, getWidth(), view.getX());
    int dy = positiveDelta(contentRect.y, contentRect.height, getHeight(), view.getY());
    if (dx != 0 || dy != 0) {
      Point p = getViewPosition();
      Dimension viewSize = view.getSize();
      int startX = p.x;
      int startY = p.y;
      p.x += dx;
      p.y += dy;
      if (p.x < 0) {
        p.x = 0;
      }
      if (p.y < 0) {
        p.y = 0;
      }
      Dimension extent = getExtentSize();
      if (p.x + extent.width > viewSize.width) {
        p.x = Math.max(0, viewSize.width - extent.width);
      }
      if (p.y + extent.height > viewSize.height) {
        p.y = Math.max(0, viewSize.height - extent.height);
      }
      if (p.x != startX || p.y != startY) {
        setViewPosition(p);
        scrollUnderway = false;
      }
    }
  }

  private int positiveDelta(int start, int size, int visible, int viewOrigin) {
    int low = start + viewOrigin;
    int high = low + size;
    if (low >= 0 && high <= visible) {
      return 0;
    }
    if (size > visible) {
      return low < 0 && high > visible ? 0 : (high < visible ? high - visible : low);
    }
    return low < 0 ? low : high - visible;
  }

  private void validateView() {
    Component validateRoot = null;
    for (Component c = this; c != null; c = c.getParent()) {
      if (c instanceof JComponent && ((JComponent) c).isValidateRoot()) {
        validateRoot = c;
        break;
      }
    }
    if (validateRoot != null) {
      validateRoot.validate();
    }
  }

  protected boolean isPaintingOrigin() { return scrollMode == BACKINGSTORE_SCROLL_MODE; }

  public final void setBorder(Border border) {
    if (border != null) {
      throw new IllegalArgumentException("JViewport.setBorder() not supported");
    }
  }

  public final Insets getInsets() { return new Insets(0, 0, 0, 0); }

  public final Insets getInsets(Insets insets) {
    insets.left = insets.top = insets.right = insets.bottom = 0;
    return insets;
  }

  public boolean isOptimizedDrawingEnabled() { return false; }

  public void paint(Graphics g) {
    int width = getWidth();
    int height = getHeight();
    if (width <= 0 || height <= 0) {
      return;
    }
    super.paint(g);
    lastPaintPosition = getViewLocation();
  }

  public void reshape(int x, int y, int w, int h) {
    boolean sizeChanged = getWidth() != w || getHeight() != h;
    if (sizeChanged) {
      backingStoreImage = null;
    }
    super.reshape(x, y, w, h);
    if (sizeChanged || viewChanged) {
      viewChanged = false;
      fireStateChanged();
    }
  }

  public void setScrollMode(int mode) {
    scrollMode = mode;
    backingStore = mode == BACKINGSTORE_SCROLL_MODE;
  }

  public int getScrollMode() { return scrollMode; }

  public boolean isBackingStoreEnabled() { return scrollMode == BACKINGSTORE_SCROLL_MODE; }

  public void setBackingStoreEnabled(boolean enabled) {
    if (enabled) {
      setScrollMode(BACKINGSTORE_SCROLL_MODE);
    } else {
      setScrollMode(BLIT_SCROLL_MODE);
    }
  }

  private Component getView0() { return getComponentCount() > 0 ? getComponent(0) : null; }

  public Component getView() { return getView0(); }

  public void setView(Component view) {
    Component old = getView();
    if (old != view) {
      if (old != null) {
        old.removeComponentListener(viewListenerImpl);
        super.remove(old);
      }
      if (view != null) {
        super.addImpl(view, null, -1);
        if (viewListenerImpl == null) {
          viewListenerImpl = createViewListener();
        }
        view.addComponentListener(viewListenerImpl);
      }
      if (hasHadValidView) {
        fireStateChanged();
      } else if (view != null) {
        hasHadValidView = true;
      }
      viewChanged = true;
      revalidate();
      repaint();
    }
  }

  private boolean hasHadValidView;

  public Dimension getViewSize() {
    Component view = getView();
    return view == null ? new Dimension(0, 0) : view.getSize();
  }

  public void setViewSize(Dimension newSize) {
    Component view = getView();
    if (view != null) {
      Dimension oldSize = view.getSize();
      if (!newSize.equals(oldSize)) {
        scrollUnderway = false;
        view.setSize(newSize);
        isViewSizeSet = true;
        fireStateChanged();
      }
    }
  }

  private Point getViewLocation() {
    Component view = getView();
    return view == null ? new Point(0, 0) : view.getLocation();
  }

  public Point getViewPosition() {
    Component view = getView();
    if (view != null) {
      Point p = view.getLocation();
      p.x = -p.x;
      p.y = -p.y;
      return p;
    }
    return new Point(0, 0);
  }

  public void setViewPosition(Point p) {
    Component view = getView();
    if (view == null) {
      return;
    }
    int oldX = view.getX();
    int oldY = view.getY();
    int x = p.x;
    int y = p.y;
    if (oldX != -x || oldY != -y) {
      view.setLocation(-x, -y);
      fireStateChanged();
    }
  }

  public Rectangle getViewRect() { return new Rectangle(getViewPosition(), getExtentSize()); }

  protected boolean computeBlit(int dx, int dy, Point blitFrom, Point blitTo, Dimension blitSize, Rectangle blitPaint) {
    int dxAbs = Math.abs(dx);
    int dyAbs = Math.abs(dy);
    Dimension extent = getExtentSize();
    if (dx == 0 && dy != 0 && dyAbs < extent.height) {
      if (dy < 0) {
        blitFrom.y = -dy;
        blitTo.y = 0;
        blitPaint.y = extent.height + dy;
      } else {
        blitFrom.y = 0;
        blitTo.y = dy;
        blitPaint.y = 0;
      }
      blitPaint.x = blitFrom.x = blitTo.x = 0;
      blitSize.width = extent.width;
      blitSize.height = extent.height - dyAbs;
      blitPaint.width = extent.width;
      blitPaint.height = dyAbs;
      return true;
    } else if (dy == 0 && dx != 0 && dxAbs < extent.width) {
      if (dx < 0) {
        blitFrom.x = -dx;
        blitTo.x = 0;
        blitPaint.x = extent.width + dx;
      } else {
        blitFrom.x = 0;
        blitTo.x = dx;
        blitPaint.x = 0;
      }
      blitPaint.y = blitFrom.y = blitTo.y = 0;
      blitSize.width = extent.width - dxAbs;
      blitSize.height = extent.height;
      blitPaint.width = dxAbs;
      blitPaint.height = extent.height;
      return true;
    }
    return false;
  }

  public Dimension getExtentSize() { return getSize(); }

  public Dimension toViewCoordinates(Dimension size) { return new Dimension(size); }

  public Point toViewCoordinates(Point p) {
    Point pos = getViewPosition();
    return new Point(p.x + pos.x, p.y + pos.y);
  }

  public void setExtentSize(Dimension newExtent) {
    Dimension oldExtent = getExtentSize();
    if (!newExtent.equals(oldExtent)) {
      setSize(newExtent);
      fireStateChanged();
    }
  }

  protected ViewListener createViewListener() { return new ViewListener(); }

  protected LayoutManager createLayoutManager() { return ViewportLayout.SHARED_INSTANCE; }

  public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

  public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  public void repaint(long tm, int x, int y, int w, int h) {
    java.awt.Container parent = getParent();
    if (parent != null) {
      parent.repaint(tm, x + getX(), y + getY(), w, h);
    } else {
      super.repaint(tm, x, y, w, h);
    }
  }

  protected String paramString() {
    return super.paramString() + ",isViewSizeSet=" + isViewSizeSet + ",lastPaintPosition="
        + (lastPaintPosition != null ? lastPaintPosition.toString() : "") + ",scrollUnderway=" + scrollUnderway;
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJViewport();
    }
    return accessibleContext;
  }

  protected class AccessibleJViewport extends AccessibleJComponent {
    protected AccessibleJViewport() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.VIEWPORT; }
  }

  protected class ViewListener extends ComponentAdapter implements Serializable {
    public void componentResized(ComponentEvent e) {
      fireStateChanged();
      revalidate();
    }
  }
}
