/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Hashtable;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class JLayeredPane extends JComponent implements Accessible {
  public static final Integer DEFAULT_LAYER = Integer.valueOf(0);
  public static final Integer PALETTE_LAYER = Integer.valueOf(100);
  public static final Integer MODAL_LAYER = Integer.valueOf(200);
  public static final Integer POPUP_LAYER = Integer.valueOf(300);
  public static final Integer DRAG_LAYER = Integer.valueOf(400);
  public static final Integer FRAME_CONTENT_LAYER = Integer.valueOf(-30000);
  public static final String LAYER_PROPERTY = "layeredContainerLayer";

  private Hashtable<Component, Integer> componentToLayer;
  private boolean optimizedDrawingPossible = true;

  public JLayeredPane() {
    setLayout(null);
  }

  protected void addImpl(Component comp, Object constraints, int index) {
    int layer;
    int pos;
    if (constraints instanceof Integer) {
      layer = ((Integer) constraints).intValue();
      setLayer(comp, layer);
    } else {
      layer = getLayer(comp);
    }
    pos = insertIndexForLayer(layer, index);
    super.addImpl(comp, constraints, pos);
    comp.validate();
    comp.repaint();
    validateOptimizedDrawing();
  }

  public void remove(int index) {
    Component c = getComponent(index);
    super.remove(index);
    if (c != null && !(c instanceof JComponent)) {
      getComponentToLayer().remove(c);
    }
    validateOptimizedDrawing();
  }

  public void removeAll() {
    Component[] children = getComponents();
    Hashtable<Component, Integer> cToL = getComponentToLayer();
    for (int counter = children.length - 1; counter >= 0; counter--) {
      Component c = children[counter];
      if (c != null && !(c instanceof JComponent)) {
        cToL.remove(c);
      }
    }
    super.removeAll();
    validateOptimizedDrawing();
  }

  public boolean isOptimizedDrawingEnabled() { return optimizedDrawingPossible; }

  public static void putLayer(JComponent c, int layer) {
    Integer layerObj = Integer.valueOf(layer);
    c.putClientProperty(LAYER_PROPERTY, layerObj);
  }

  public static int getLayer(JComponent c) {
    Integer i = (Integer) c.getClientProperty(LAYER_PROPERTY);
    if (i != null) {
      return i.intValue();
    }
    return DEFAULT_LAYER.intValue();
  }

  public static JLayeredPane getLayeredPaneAbove(Component c) {
    if (c == null) {
      return null;
    }
    Component parent = c.getParent();
    while (parent != null && !(parent instanceof JLayeredPane)) {
      parent = parent.getParent();
    }
    return (JLayeredPane) parent;
  }

  public void setLayer(Component c, int layer) { setLayer(c, layer, -1); }

  public void setLayer(Component c, int layer, int position) {
    Integer layerObj = getObjectForLayer(layer);
    if (layer == getLayer(c) && position == getPosition(c)) {
      repaint(c.getBounds());
      return;
    }
    if (c instanceof JComponent) {
      ((JComponent) c).putClientProperty(LAYER_PROPERTY, layerObj);
    } else {
      getComponentToLayer().put(c, layerObj);
    }
    if (c.getParent() == null || c.getParent() != this) {
      repaint(c.getBounds());
      return;
    }
    int index = insertIndexForLayer(c, layer, position);
    setComponentZOrder(c, index);
    repaint(c.getBounds());
  }

  public int getLayer(Component c) {
    Integer i;
    if (c instanceof JComponent) {
      i = (Integer) ((JComponent) c).getClientProperty(LAYER_PROPERTY);
    } else {
      i = getComponentToLayer().get(c);
    }
    if (i == null) {
      return DEFAULT_LAYER.intValue();
    }
    return i.intValue();
  }

  public int getIndexOf(Component c) {
    int count = getComponentCount();
    for (int i = 0; i < count; i++) {
      if (c == getComponent(i)) {
        return i;
      }
    }
    return -1;
  }

  public void moveToFront(Component c) { setPosition(c, 0); }

  public void moveToBack(Component c) { setPosition(c, -1); }

  public void setPosition(Component c, int position) { setLayer(c, getLayer(c), position); }

  public int getPosition(Component c) {
    int layer = getLayer(c);
    int start = 0;
    int count = getComponentCount();
    while (start < count && getLayer(getComponent(start)) > layer) {
      start++;
    }
    int idx = getIndexOf(c);
    if (idx < 0) {
      return -1;
    }
    return idx - start;
  }

  public int highestLayer() {
    if (getComponentCount() > 0) {
      return getLayer(getComponent(0));
    }
    return 0;
  }

  public int lowestLayer() {
    int count = getComponentCount();
    if (count > 0) {
      return getLayer(getComponent(count - 1));
    }
    return 0;
  }

  public int getComponentCountInLayer(int layer) {
    int n = 0;
    int count = getComponentCount();
    for (int i = 0; i < count; i++) {
      if (getLayer(getComponent(i)) == layer) {
        n++;
      }
    }
    return n;
  }

  public Component[] getComponentsInLayer(int layer) {
    Component[] result = new Component[getComponentCountInLayer(layer)];
    int n = 0;
    int count = getComponentCount();
    for (int i = 0; i < count; i++) {
      Component c = getComponent(i);
      if (getLayer(c) == layer) {
        result[n++] = c;
      }
    }
    return result;
  }

  public void paint(Graphics g) {
    if (isOpaque()) {
      Rectangle r = g.getClipBounds();
      java.awt.Color c = getBackground();
      if (c == null) {
        c = java.awt.Color.lightGray;
      }
      g.setColor(c);
      if (r != null) {
        g.fillRect(r.x, r.y, r.width, r.height);
      } else {
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    }
    super.paint(g);
  }

  protected Hashtable<Component, Integer> getComponentToLayer() {
    if (componentToLayer == null) {
      componentToLayer = new Hashtable<Component, Integer>(4);
    }
    return componentToLayer;
  }

  protected Integer getObjectForLayer(int layer) {
    switch (layer) {
      case 0:
        return DEFAULT_LAYER;
      case 100:
        return PALETTE_LAYER;
      case 200:
        return MODAL_LAYER;
      case 300:
        return POPUP_LAYER;
      case 400:
        return DRAG_LAYER;
      default:
        return Integer.valueOf(layer);
    }
  }

  protected int insertIndexForLayer(int layer, int position) { return insertIndexForLayer(null, layer, position); }

  private int insertIndexForLayer(Component skip, int layer, int position) {
    int start = 0;
    int count = 0;
    int total = 0;
    boolean started = false;
    int n = getComponentCount();
    for (int i = 0; i < n; i++) {
      Component c = getComponent(i);
      if (c == skip) {
        continue;
      }
      int l = getLayer(c);
      if (!started && l <= layer) {
        started = true;
        start = total;
      }
      if (l == layer) {
        count++;
      }
      total++;
    }
    if (!started) {
      start = total;
    }
    if (position == -1 || position > count) {
      return start + count;
    }
    return start + position;
  }

  private void validateOptimizedDrawing() {
    boolean layeredComponentFound = false;
    for (Component c : getComponents()) {
      if (c instanceof JComponent) {
        Integer layer = (Integer) ((JComponent) c).getClientProperty(LAYER_PROPERTY);
        if (layer != null && !layer.equals(FRAME_CONTENT_LAYER)) {
          layeredComponentFound = true;
          break;
        }
      }
    }
    optimizedDrawingPossible = !layeredComponentFound;
  }

  protected String paramString() {
    return super.paramString() + ",optimizedDrawingPossible=" + (optimizedDrawingPossible ? "true" : "false");
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJLayeredPane();
    }
    return accessibleContext;
  }

  protected class AccessibleJLayeredPane extends AccessibleJComponent {
    protected AccessibleJLayeredPane() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.LAYERED_PANE; }
  }
}
