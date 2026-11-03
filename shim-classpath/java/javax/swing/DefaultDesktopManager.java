/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.Serializable;

public class DefaultDesktopManager implements DesktopManager, Serializable {
  static final String HAS_BEEN_ICONIFIED_PROPERTY = "wasIconOnce";
  static final String PREVIOUS_BOUNDS_PROPERTY = "previousBounds";

  public DefaultDesktopManager() { }

  public void openFrame(JInternalFrame f) {
    if (f.getDesktopIcon().getParent() != null) {
      f.getDesktopIcon().getParent().add(f);
      removeIconFor(f);
    }
  }

  public void closeFrame(JInternalFrame f) {
    JDesktopPane d = f.getDesktopPane();
    if (d == null) {
      return;
    }
    boolean findNext = f.isSelected();
    Container c = f.getParent();
    JInternalFrame nextFrame = null;
    if (findNext) {
      nextFrame = d.getNextFrame(f);
      try {
        f.setSelected(false);
      } catch (PropertyVetoException e2) {
        findNext = false;
      }
    }
    if (c != null) {
      c.remove(f);
      c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
    }
    removeIconFor(f);
    if (f.getNormalBounds() != null) {
      f.setNormalBounds(null);
    }
    if (wasIcon(f)) {
      setWasIcon(f, null);
    }
    if (nextFrame != null) {
      try {
        nextFrame.setSelected(true);
      } catch (PropertyVetoException e2) {
        return;
      }
    }
  }

  public void maximizeFrame(JInternalFrame f) {
    if (f.isIcon()) {
      try {
        f.setIcon(false);
      } catch (PropertyVetoException e2) {
        return;
      }
    } else {
      f.setNormalBounds(f.getBounds());
      Rectangle desktopBounds = f.getParent().getBounds();
      setBoundsForFrame(f, 0, 0, desktopBounds.width, desktopBounds.height);
    }
    try {
      f.setSelected(true);
    } catch (PropertyVetoException e2) {
      return;
    }
  }

  public void minimizeFrame(JInternalFrame f) {
    if (f.isIcon()) {
      iconifyFrame(f);
      return;
    }
    if ((f.getNormalBounds()) != null) {
      Rectangle r = f.getNormalBounds();
      f.setNormalBounds(null);
      try {
        f.setSelected(true);
      } catch (PropertyVetoException e2) {
        return;
      }
      setBoundsForFrame(f, r.x, r.y, r.width, r.height);
    }
  }

  public void iconifyFrame(JInternalFrame f) {
    JInternalFrame.JDesktopIcon desktopIcon;
    Container c = f.getParent();
    JDesktopPane d = f.getDesktopPane();
    boolean findNext = f.isSelected();
    if (c == null || d == null) {
      return;
    }
    desktopIcon = f.getDesktopIcon();
    if (!wasIcon(f)) {
      Rectangle r = getBoundsForIconOf(f);
      desktopIcon.setBounds(r.x, r.y, r.width, r.height);
      setWasIcon(f, Boolean.TRUE);
    }
    if (c instanceof JLayeredPane) {
      int layer = JLayeredPane.getLayer(f);
      JLayeredPane.putLayer(desktopIcon, layer);
    }
    if (!f.isMaximum()) {
      f.setNormalBounds(f.getBounds());
    }
    c.remove(f);
    c.add(desktopIcon);
    c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
    try {
      f.setSelected(false);
    } catch (PropertyVetoException e2) {
      return;
    }
    if (findNext) {
      d.selectFrame(true);
    }
  }

  public void deiconifyFrame(JInternalFrame f) {
    JInternalFrame.JDesktopIcon desktopIcon = f.getDesktopIcon();
    Container c = desktopIcon.getParent();
    JDesktopPane d = f.getDesktopPane();
    if (c != null && d != null) {
      c.add(f);
      if (f.isMaximum()) {
        Rectangle r = c.getBounds();
        if (f.getWidth() != r.width || f.getHeight() != r.height) {
          setBoundsForFrame(f, 0, 0, r.width, r.height);
        }
      }
      removeIconFor(f);
      if (f.isSelected()) {
        f.moveToFront();
      } else {
        try {
          f.setSelected(true);
        } catch (PropertyVetoException e2) {
          return;
        }
      }
    }
  }

  public void activateFrame(JInternalFrame f) {
    Container p = f.getParent();
    JDesktopPane d = f.getDesktopPane();
    JInternalFrame currentlyActiveFrame = (d == null) ? null : d.getSelectedFrame();
    if (p == null) {
      p = f.getDesktopIcon().getParent();
      if (p == null) {
        return;
      }
    }
    if (currentlyActiveFrame == null) {
      if (d != null) {
        d.setSelectedFrame(f);
      }
    } else if (currentlyActiveFrame != f) {
      if (currentlyActiveFrame.isSelected()) {
        try {
          currentlyActiveFrame.setSelected(false);
        } catch (PropertyVetoException e2) {
          return;
        }
      }
      if (d != null) {
        d.setSelectedFrame(f);
      }
    }
    f.moveToFront();
  }

  public void deactivateFrame(JInternalFrame f) {
    JDesktopPane d = f.getDesktopPane();
    JInternalFrame currentlyActiveFrame = (d == null) ? null : d.getSelectedFrame();
    if (currentlyActiveFrame == f) {
      d.setSelectedFrame(null);
    }
  }

  public void beginDraggingFrame(JComponent f) { }

  public void dragFrame(JComponent f, int newX, int newY) { setBoundsForFrame(f, newX, newY, f.getWidth(), f.getHeight()); }

  public void endDraggingFrame(JComponent f) { }

  public void beginResizingFrame(JComponent f, int direction) { }

  public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
    setBoundsForFrame(f, newX, newY, newWidth, newHeight);
  }

  public void endResizingFrame(JComponent f) { }

  public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
    f.setBounds(newX, newY, newWidth, newHeight);
    f.revalidate();
  }

  protected void removeIconFor(JInternalFrame f) {
    JInternalFrame.JDesktopIcon di = f.getDesktopIcon();
    Container c = di.getParent();
    if (c != null) {
      c.remove(di);
      c.repaint(di.getX(), di.getY(), di.getWidth(), di.getHeight());
    }
  }

  protected Rectangle getBoundsForIconOf(JInternalFrame f) {
    JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
    Dimension prefSize = icon.getPreferredSize();
    Container c = f.getParent();
    if (c == null) {
      c = f.getDesktopIcon().getParent();
    }
    if (c == null) {
      return new Rectangle(0, 0, prefSize.width, prefSize.height);
    }
    Rectangle parentBounds = c.getBounds();
    java.awt.Component[] components = c.getComponents();
    Rectangle availableRectangle = null;
    JInternalFrame.JDesktopIcon currentIcon = null;
    int x = 0;
    int y = parentBounds.height - prefSize.height;
    int w = prefSize.width;
    int h = prefSize.height;
    boolean found = false;
    while (!found) {
      availableRectangle = new Rectangle(x, y, w, h);
      found = true;
      for (int i = 0; i < components.length; i++) {
        if (components[i] instanceof JInternalFrame) {
          currentIcon = ((JInternalFrame) components[i]).getDesktopIcon();
        } else if (components[i] instanceof JInternalFrame.JDesktopIcon) {
          currentIcon = (JInternalFrame.JDesktopIcon) components[i];
        } else {
          continue;
        }
        if (!currentIcon.equals(icon)) {
          if (availableRectangle.intersects(currentIcon.getBounds())) {
            found = false;
            break;
          }
        }
      }
      if (currentIcon == null) {
        return new Rectangle(0, y, w, h);
      }
      x += currentIcon.getBounds().width;
      if (x + w > parentBounds.width) {
        x = 0;
        y -= h;
      }
    }
    return availableRectangle;
  }

  protected void setPreviousBounds(JInternalFrame f, Rectangle r) { f.putClientProperty(PREVIOUS_BOUNDS_PROPERTY, r); }

  protected Rectangle getPreviousBounds(JInternalFrame f) {
    return (Rectangle) f.getClientProperty(PREVIOUS_BOUNDS_PROPERTY);
  }

  protected void setWasIcon(JInternalFrame f, Boolean value) {
    if (value != null) {
      f.putClientProperty(HAS_BEEN_ICONIFIED_PROPERTY, value);
    }
  }

  protected boolean wasIcon(JInternalFrame f) {
    return (f.getClientProperty(HAS_BEEN_ICONIFIED_PROPERTY) == Boolean.TRUE);
  }
}
