/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RepaintManager {
  private static RepaintManager sharedManager = new RepaintManager();

  private final List<Component> invalidComponents = new ArrayList<Component>();
  private final Map<Component, Rectangle> dirtyComponents = new IdentityHashMap<Component, Rectangle>();
  private Dimension doubleBufferMaxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  private boolean doubleBufferingEnabled = true;

  public static RepaintManager currentManager(Component c) { return sharedManager; }

  public static RepaintManager currentManager(JComponent c) { return currentManager((Component) c); }

  public static void setCurrentManager(RepaintManager aRepaintManager) {
    sharedManager = aRepaintManager != null ? aRepaintManager : new RepaintManager();
  }

  public RepaintManager() { }

  public synchronized void addInvalidComponent(JComponent invalidComponent) {
    Component validateRoot = null;
    for (Component c = invalidComponent; c != null; c = c.getParent()) {
      if ((c instanceof CellRendererPane) && c.getParent() != null) {
        return;
      }
      if (c instanceof JComponent && ((JComponent) c).isValidateRoot()) {
        validateRoot = c;
        break;
      }
    }
    if (validateRoot == null) {
      return;
    }
    Component root = null;
    for (Component c = validateRoot; c != null; c = c.getParent()) {
      if (c instanceof Window || c instanceof Applet) {
        root = c;
        break;
      }
    }
    if (root == null) {
      return;
    }
    for (Component c : invalidComponents) {
      if (c == validateRoot) {
        return;
      }
    }
    invalidComponents.add(validateRoot);
  }

  public synchronized void removeInvalidComponent(JComponent component) { invalidComponents.remove(component); }

  public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
    if (w <= 0 || h <= 0 || c == null || c.getWidth() <= 0 || c.getHeight() <= 0) {
      return;
    }
    Component root = null;
    for (Component p = c; p != null; p = p.getParent()) {
      if (!p.isVisible()) {
        return;
      }
      if (p instanceof Window || p instanceof Applet) {
        root = p;
        break;
      }
    }
    if (root == null) {
      return;
    }
    synchronized (this) {
      Rectangle r = dirtyComponents.get(c);
      Rectangle added = new Rectangle(x, y, w, h);
      dirtyComponents.put(c, r == null ? added : r.union(added));
    }
  }

  public void addDirtyRegion(Window window, int x, int y, int w, int h) { }

  public void addDirtyRegion(Applet applet, int x, int y, int w, int h) { }

  public Rectangle getDirtyRegion(JComponent aComponent) {
    Rectangle r;
    synchronized (this) {
      r = dirtyComponents.get(aComponent);
    }
    if (r == null) {
      return new Rectangle(0, 0, 0, 0);
    }
    return new Rectangle(r);
  }

  public void markCompletelyDirty(JComponent aComponent) {
    addDirtyRegion(aComponent, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public void markCompletelyClean(JComponent aComponent) {
    synchronized (this) {
      dirtyComponents.remove(aComponent);
    }
  }

  public boolean isCompletelyDirty(JComponent aComponent) {
    Rectangle r = getDirtyRegion(aComponent);
    return r.width == Integer.MAX_VALUE && r.height == Integer.MAX_VALUE;
  }

  public void validateInvalidComponents() {
    List<Component> roots;
    synchronized (this) {
      roots = new ArrayList<Component>(invalidComponents);
      invalidComponents.clear();
    }
    for (Component c : roots) {
      c.validate();
    }
  }

  public void paintDirtyRegions() {
    synchronized (this) {
      dirtyComponents.clear();
    }
  }

  public synchronized String toString() {
    StringBuilder sb = new StringBuilder();
    if (dirtyComponents != null) {
      sb.append("" + dirtyComponents);
    }
    return sb.toString();
  }

  public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
    Dimension max = getDoubleBufferMaximumSize();
    int width = Math.max(1, Math.min(proposedWidth, max.width));
    int height = Math.max(1, Math.min(proposedHeight, max.height));
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
    return getOffscreenBuffer(c, proposedWidth, proposedHeight);
  }

  public void setDoubleBufferMaximumSize(Dimension d) { doubleBufferMaxSize = d; }

  public Dimension getDoubleBufferMaximumSize() { return doubleBufferMaxSize; }

  public void setDoubleBufferingEnabled(boolean aFlag) { doubleBufferingEnabled = aFlag; }

  public boolean isDoubleBufferingEnabled() { return doubleBufferingEnabled; }
}
