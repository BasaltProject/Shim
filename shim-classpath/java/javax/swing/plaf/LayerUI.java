/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JLayer;

public class LayerUI<V extends Component> extends ComponentUI implements Serializable {
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public LayerUI() { }

  public void paint(Graphics g, JComponent c) { c.paint(g); }

  public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (e instanceof FocusEvent) {
      processFocusEvent((FocusEvent) e, l);
    } else if (e instanceof MouseEvent) {
      switch (e.getID()) {
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_CLICKED:
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
          processMouseEvent((MouseEvent) e, l);
          break;
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
          processMouseMotionEvent((MouseEvent) e, l);
          break;
        case MouseEvent.MOUSE_WHEEL:
          processMouseWheelEvent((MouseWheelEvent) e, l);
          break;
        default:
          break;
      }
    } else if (e instanceof KeyEvent) {
      processKeyEvent((KeyEvent) e, l);
    } else if (e instanceof ComponentEvent) {
      processComponentEvent((ComponentEvent) e, l);
    } else if (e instanceof InputMethodEvent) {
      processInputMethodEvent((InputMethodEvent) e, l);
    } else if (e instanceof HierarchyEvent) {
      switch (e.getID()) {
        case HierarchyEvent.HIERARCHY_CHANGED:
          processHierarchyEvent((HierarchyEvent) e, l);
          break;
        case HierarchyEvent.ANCESTOR_MOVED:
        case HierarchyEvent.ANCESTOR_RESIZED:
          processHierarchyBoundsEvent((HierarchyEvent) e, l);
          break;
        default:
          break;
      }
    }
  }

  protected void processComponentEvent(ComponentEvent e, JLayer<? extends V> l) { }

  protected void processFocusEvent(FocusEvent e, JLayer<? extends V> l) { }

  protected void processKeyEvent(KeyEvent e, JLayer<? extends V> l) { }

  protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) { }

  protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) { }

  protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends V> l) { }

  protected void processInputMethodEvent(InputMethodEvent e, JLayer<? extends V> l) { }

  protected void processHierarchyEvent(HierarchyEvent e, JLayer<? extends V> l) { }

  protected void processHierarchyBoundsEvent(HierarchyEvent e, JLayer<? extends V> l) { }

  public void updateUI(JLayer<? extends V> l) { }

  public void installUI(JComponent c) {
    addPropertyChangeListener((JLayer<?>) c);
  }

  public void uninstallUI(JComponent c) {
    removePropertyChangeListener((JLayer<?>) c);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public PropertyChangeListener[] getPropertyChangeListeners() {
    return propertyChangeSupport.getPropertyChangeListeners();
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    return propertyChangeSupport.getPropertyChangeListeners(propertyName);
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public void applyPropertyChange(PropertyChangeEvent evt, JLayer<? extends V> l) { }

  public int getBaseline(JComponent c, int width, int height) {
    JLayer<?> l = (JLayer<?>) c;
    if (l.getView() != null) {
      return l.getView().getBaseline(width, height);
    }
    return super.getBaseline(c, width, height);
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    JLayer<?> l = (JLayer<?>) c;
    if (l.getView() != null) {
      return l.getView().getBaselineResizeBehavior();
    }
    return super.getBaselineResizeBehavior(c);
  }

  public void doLayout(JLayer<? extends V> l) {
    Component view = l.getView();
    if (view != null) {
      view.setBounds(0, 0, l.getWidth(), l.getHeight());
    }
    Component glassPane = l.getGlassPane();
    if (glassPane != null) {
      glassPane.setBounds(0, 0, l.getWidth(), l.getHeight());
    }
  }

  public Dimension getPreferredSize(JComponent c) {
    JLayer<?> l = (JLayer<?>) c;
    Component view = l.getView();
    if (view != null) {
      return view.getPreferredSize();
    }
    return super.getPreferredSize(c);
  }

  public Dimension getMinimumSize(JComponent c) {
    JLayer<?> l = (JLayer<?>) c;
    Component view = l.getView();
    if (view != null) {
      return view.getMinimumSize();
    }
    return super.getMinimumSize(c);
  }

  public Dimension getMaximumSize(JComponent c) {
    JLayer<?> l = (JLayer<?>) c;
    Component view = l.getView();
    if (view != null) {
      return view.getMaximumSize();
    }
    return super.getMaximumSize(c);
  }

  public void paintImmediately(int x, int y, int width, int height, JLayer<? extends V> l) {
    l.paintImmediately(x, y, width, height);
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h, JLayer<? extends V> l) {
    l.repaint(x, y, w, h);
    return (infoflags & (ALLBITS_)) == 0;
  }

  private static final int ALLBITS_ = java.awt.image.ImageObserver.ALLBITS | java.awt.image.ImageObserver.ABORT;
}
