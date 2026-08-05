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
import javax.accessibility.*;

public class SwingUtilities implements javax.swing.SwingConstants {
  SwingUtilities() {
  }
  public static java.awt.Component getRoot(java.awt.Component p0) {
    Component applet = null;
    for (Component c = p0; c != null; c = c.getParent()) {
      if (!c.isVisible()) {
        break;
      }
      if (c instanceof Window || c instanceof java.applet.Applet) {
        return c;
      }
    }
    return null;
  }
  public static java.awt.Component getUnwrappedView(javax.swing.JViewport p0) {
    Component view = p0.getView();
    while (view instanceof JLayer) {
      view = ((JLayer<?>) view).getView();
    }
    return view;
  }
  public static boolean isDescendingFrom(java.awt.Component p0, java.awt.Component p1) {
    if (p0 == p1) {
      return true;
    }
    for (Component p = p0; p != null; p = p.getParent()) {
      if (p == p1) {
        return true;
      }
    }
    return false;
  }
  public static java.awt.Component findFocusOwner(java.awt.Component p0) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    for (Component c = focusOwner; c != null; c = (c instanceof Window) ? null : c.getParent()) {
      if (c == p0) {
        return focusOwner;
      }
    }
    return null;
  }
  public static void updateComponentTreeUI(java.awt.Component p0) {
    updateComponentTreeUI0(p0);
    p0.invalidate();
    p0.validate();
    p0.repaint();
  }
  public static java.awt.Container getUnwrappedParent(java.awt.Component p0) {
    Container parent = p0.getParent();
    while (parent instanceof JLayer) {
      parent = parent.getParent();
    }
    return parent;
  }
  public static java.awt.Rectangle computeUnion(int p0, int p1, int p2, int p3, java.awt.Rectangle p4) {
    int x2 = p4.x;
    int y2 = p4.y;
    int w2 = p4.width;
    int h2 = p4.height;
    int x1 = p0;
    int y1 = p1;
    int w1 = p2;
    int h1 = p3;
    int lx = Math.min(x1, x2);
    int ly = Math.min(y1, y2);
    int rx = Math.max(x1 + w1, x2 + w2);
    int ry = Math.max(y1 + h1, y2 + h2);
    p4.x = lx;
    p4.y = ly;
    p4.width = rx - lx;
    p4.height = ry - ly;
    return p4;
  }
  public static java.awt.Component getDeepestComponentAt(java.awt.Component p0, int p1, int p2) {
    if (!p0.contains(p1, p2)) {
      return null;
    }
    if (p0 instanceof Container) {
      Component[] components = ((Container) p0).getComponents();
      for (Component comp : components) {
        if (comp != null && comp.isVisible()) {
          Point loc = comp.getLocation();
          if (comp instanceof Container) {
            comp = getDeepestComponentAt(comp, p1 - loc.x, p2 - loc.y);
          } else {
            comp = comp.getComponentAt(p1 - loc.x, p2 - loc.y);
          }
          if (comp != null && comp.isVisible()) {
            return comp;
          }
        }
      }
    }
    return p0;
  }
  public static final boolean isRectangleContainingRectangle(java.awt.Rectangle p0, java.awt.Rectangle p1) {
    return p1.x >= p0.x && (p1.x + p1.width) <= (p0.x + p0.width) && p1.y >= p0.y
        && (p1.y + p1.height) <= (p0.y + p0.height);
  }
  public static java.awt.Rectangle getLocalBounds(java.awt.Component p0) {
    Rectangle b = new Rectangle(p0.getBounds());
    b.x = b.y = 0;
    return b;
  }
  public static java.awt.Container getAncestorNamed(java.lang.String p0, java.awt.Component p1) {
    for (Component c = p1; c != null; c = c.getParent()) {
      if (p0.equals(c.getName())) {
        return (Container) c;
      }
    }
    return null;
  }
  public static java.awt.event.MouseEvent convertMouseEvent(java.awt.Component p0, java.awt.event.MouseEvent p1, java.awt.Component p2) {
    Point p = convertPoint(p0, new Point(p1.getX(), p1.getY()), p2);
    Component source = p2 == null ? p0 : p2;
    if (p2 != null) {
      source = p2;
    } else {
      source = p0;
    }
    MouseEvent newEvent;
    if (p1 instanceof java.awt.event.MouseWheelEvent) {
      java.awt.event.MouseWheelEvent mwe = (java.awt.event.MouseWheelEvent) p1;
      newEvent = new java.awt.event.MouseWheelEvent(source, mwe.getID(), mwe.getWhen(), mwe.getModifiers()
          | mwe.getModifiersEx(), p.x, p.y, mwe.getXOnScreen(), mwe.getYOnScreen(), mwe.getClickCount(),
          mwe.isPopupTrigger(), mwe.getScrollType(), mwe.getScrollAmount(), mwe.getWheelRotation(),
          mwe.getPreciseWheelRotation());
    } else {
      newEvent = new MouseEvent(source, p1.getID(), p1.getWhen(), p1.getModifiers() | p1.getModifiersEx(), p.x, p.y,
          p1.getXOnScreen(), p1.getYOnScreen(), p1.getClickCount(), p1.isPopupTrigger(), p1.getButton());
    }
    return newEvent;
  }
  public static java.awt.Window windowForComponent(java.awt.Component p0) {
    return getWindowAncestor(p0);
  }
  public static boolean isMiddleMouseButton(java.awt.event.MouseEvent p0) {
    return ((p0.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
  }
  public static int computeStringWidth(java.awt.FontMetrics p0, java.lang.String p1) {
    return p0.stringWidth(p1);
  }
  public static void replaceUIInputMap(javax.swing.JComponent p0, int p1, javax.swing.InputMap p2) {
    InputMap map = p0.getInputMap(p1, p2 != null);
    while (map != null) {
      InputMap parent = map.getParent();
      if (parent == null || (parent instanceof javax.swing.plaf.UIResource)) {
        map.setParent(p2);
        return;
      }
      map = parent;
    }
  }
  public static void replaceUIActionMap(javax.swing.JComponent p0, javax.swing.ActionMap p1) {
    ActionMap map = p0.getActionMap(p1 != null);
    while (map != null) {
      ActionMap parent = map.getParent();
      if (parent == null || (parent instanceof javax.swing.plaf.UIResource)) {
        map.setParent(p1);
        return;
      }
      map = parent;
    }
  }
  public static javax.swing.InputMap getUIInputMap(javax.swing.JComponent p0, int p1) {
    InputMap map = p0.getInputMap(p1, false);
    while (map != null) {
      InputMap parent = map.getParent();
      if (parent instanceof javax.swing.plaf.UIResource) {
        return parent;
      }
      map = parent;
    }
    return null;
  }
  public static javax.swing.ActionMap getUIActionMap(javax.swing.JComponent p0) {
    ActionMap map = p0.getActionMap(false);
    while (map != null) {
      ActionMap parent = map.getParent();
      if (parent instanceof javax.swing.plaf.UIResource) {
        return parent;
      }
      map = parent;
    }
    return null;
  }
  public static java.awt.Rectangle calculateInnerArea(javax.swing.JComponent p0, java.awt.Rectangle p1) {
    if (p0 == null) {
      return null;
    }
    Rectangle rect = p1;
    Insets insets = p0.getInsets();
    if (rect == null) {
      rect = new Rectangle();
    }
    rect.x = insets.left;
    rect.y = insets.top;
    rect.width = p0.getWidth() - insets.left - insets.right;
    rect.height = p0.getHeight() - insets.top - insets.bottom;
    return rect;
  }
  public static java.awt.Container getAncestorOfClass(java.lang.Class<?> p0, java.awt.Component p1) {
    if (p1 == null || p0 == null) {
      return null;
    }
    Container parent = p1.getParent();
    while (parent != null && !(p0.isInstance(parent))) {
      parent = parent.getParent();
    }
    return parent;
  }
  public static void convertPointToScreen(java.awt.Point p0, java.awt.Component p1) {
    int x = 0;
    int y = 0;
    do {
      if (p1 instanceof JComponent) {
        x += p1.getX();
        y += p1.getY();
      } else if (p1 instanceof java.applet.Applet || p1 instanceof Window) {
        try {
          Point pp = p1.getLocationOnScreen();
          x += pp.x;
          y += pp.y;
        } catch (IllegalComponentStateException icse) {
          x += p1.getX();
          y += p1.getY();
        }
        break;
      } else {
        x += p1.getX();
        y += p1.getY();
      }
      p1 = p1.getParent();
    } while (p1 != null);
    p0.x += x;
    p0.y += y;
  }
  public static void convertPointFromScreen(java.awt.Point p0, java.awt.Component p1) {
    int x = 0;
    int y = 0;
    do {
      if (p1 instanceof JComponent) {
        x += p1.getX();
        y += p1.getY();
      } else if (p1 instanceof java.applet.Applet || p1 instanceof Window) {
        try {
          Point pp = p1.getLocationOnScreen();
          x += pp.x;
          y += pp.y;
        } catch (IllegalComponentStateException icse) {
          x += p1.getX();
          y += p1.getY();
        }
        break;
      } else {
        x += p1.getX();
        y += p1.getY();
      }
      p1 = p1.getParent();
    } while (p1 != null);
    p0.x -= x;
    p0.y -= y;
  }
  public static boolean isLeftMouseButton(java.awt.event.MouseEvent p0) {
    return ((p0.getModifiers() & InputEvent.BUTTON1_MASK) != 0);
  }
  public static boolean isRightMouseButton(java.awt.event.MouseEvent p0) {
    return ((p0.getModifiers() & InputEvent.BUTTON3_MASK) != 0);
  }
  public static java.awt.Window getWindowAncestor(java.awt.Component p0) {
    for (Container p = p0.getParent(); p != null; p = p.getParent()) {
      if (p instanceof Window) {
        return (Window) p;
      }
    }
    return null;
  }
  public static java.awt.Rectangle computeIntersection(int p0, int p1, int p2, int p3, java.awt.Rectangle p4) {
    int x1 = p0;
    int x2 = p4.x;
    int y1 = p1;
    int y2 = p4.y;
    int w1 = p2;
    int w2 = p4.width;
    int h1 = p3;
    int h2 = p4.height;
    int lx = Math.max(x1, x2);
    int rx = Math.min(x1 + w1, x2 + w2);
    int ty = Math.max(y1, y2);
    int by = Math.min(y1 + h1, y2 + h2);
    p4.x = lx;
    p4.y = ty;
    p4.width = rx - lx;
    p4.height = by - ty;
    if (p4.width < 0 || p4.height < 0) {
      p4.x = p4.y = p4.width = p4.height = 0;
    }
    return p4;
  }
  public static void paintComponent(java.awt.Graphics p0, java.awt.Component p1, java.awt.Container p2, int p3, int p4, int p5, int p6) {
    getCellRendererPane(p1, p2).paintComponent(p0, p1, p2, p3, p4, p5, p6, false);
  }
  public static void paintComponent(java.awt.Graphics p0, java.awt.Component p1, java.awt.Container p2, java.awt.Rectangle p3) {
    paintComponent(p0, p1, p2, p3.x, p3.y, p3.width, p3.height);
  }
  public static boolean processKeyBindings(java.awt.event.KeyEvent p0) {
    if (!p0.isConsumed()) {
      Component c = p0.getComponent();
      KeyStroke ks = KeyStroke.getKeyStrokeForEvent(p0);
      boolean pressed = p0.getID() == KeyEvent.KEY_PRESSED;
      for (Component comp = c; comp != null; comp = comp.getParent()) {
        if (comp instanceof JComponent) {
          JComponent jc = (JComponent) comp;
          if (jc.processKeyBinding(ks, p0, JComponent.WHEN_FOCUSED, pressed)) {
            return true;
          }
        }
      }
      for (Component comp = c; comp != null; comp = comp.getParent()) {
        if (comp instanceof JComponent) {
          JComponent jc = (JComponent) comp;
          if (jc.processKeyBinding(ks, p0, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, pressed)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  public static boolean notifyAction(javax.swing.Action p0, javax.swing.KeyStroke p1, java.awt.event.KeyEvent p2, java.lang.Object p3, int p4) {
    return shim.swing.Actions.notifyAction(p0, p1, p2, p3, p4);
  }
  public static java.awt.Rectangle convertRectangle(java.awt.Component p0, java.awt.Rectangle p1, java.awt.Component p2) {
    Point point = new Point(p1.x, p1.y);
    point = convertPoint(p0, point, p2);
    return new Rectangle(point.x, point.y, p1.width, p1.height);
  }
  public static boolean isEventDispatchThread() {
    return java.awt.EventQueue.isDispatchThread();
  }
  public static java.lang.String layoutCompoundLabel(java.awt.FontMetrics p0, java.lang.String p1, javax.swing.Icon p2, int p3, int p4, int p5, int p6, java.awt.Rectangle p7, java.awt.Rectangle p8, java.awt.Rectangle p9, int p10) {
    return layoutCompoundLabelImpl(null, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
  }
  public static java.lang.String layoutCompoundLabel(javax.swing.JComponent p0, java.awt.FontMetrics p1, java.lang.String p2, javax.swing.Icon p3, int p4, int p5, int p6, int p7, java.awt.Rectangle p8, java.awt.Rectangle p9, java.awt.Rectangle p10, int p11) {
    return layoutCompoundLabelImpl(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11);
  }
  public static java.awt.Point convertPoint(java.awt.Component p0, java.awt.Point p1, java.awt.Component p2) {
    if (p0 == null && p2 == null) {
      return p1;
    }
    return convertPoint(p0, p1.x, p1.y, p2);
  }
  public static java.awt.Point convertPoint(java.awt.Component p0, int p1, int p2, java.awt.Component p3) {
    if (p0 == null && p3 == null) {
      return new Point(p1, p2);
    }
    Point point = new Point(p1, p2);
    Component source = p0;
    Component destination = p3;
    if (source == null) {
      source = getWindowAncestor(destination);
      if (source == null) {
        throw new Error("Source component not connected to component tree hierarchy");
      }
    }
    Point p = new Point(p1, p2);
    Component c = source;
    while (c != null && !(c instanceof Window) && !(c instanceof java.applet.Applet)) {
      p.x += c.getX();
      p.y += c.getY();
      c = c.getParent();
    }
    if (destination == null) {
      if (c instanceof Window) {
        return p;
      }
      return p;
    }
    Component d = destination;
    if (c != null && !(c instanceof Window) && !(c instanceof java.applet.Applet)) {
      throw new Error("Source component not connected to component tree hierarchy");
    }
    int dx = 0;
    int dy = 0;
    Component dc = d;
    while (dc != null && !(dc instanceof Window) && !(dc instanceof java.applet.Applet)) {
      dx += dc.getX();
      dy += dc.getY();
      dc = dc.getParent();
    }
    if (dc != c && c != null) {
      Point sw = c.getLocationOnScreen();
      Point dw = dc.getLocationOnScreen();
      p.x += sw.x - dw.x;
      p.y += sw.y - dw.y;
    }
    p.x -= dx;
    p.y -= dy;
    return p;
  }
  public static java.awt.Rectangle[] computeDifference(java.awt.Rectangle p0, java.awt.Rectangle p1) {
    if (p1 == null || !p0.intersects(p1) || isRectangleContainingRectangle(p1, p0)) {
      return new Rectangle[0];
    }
    Rectangle rectOut = new Rectangle();
    Rectangle[] rects = new Rectangle[4];
    int count = 0;
    if (p0.x < p1.x) {
      rectOut = new Rectangle(p0.x, p0.y, p1.x - p0.x, p0.height);
      rects[count++] = rectOut;
    }
    if (p0.x + p0.width > p1.x + p1.width) {
      rectOut = new Rectangle(p1.x + p1.width, p0.y, (p0.x + p0.width) - (p1.x + p1.width), p0.height);
      rects[count++] = rectOut;
    }
    int x0 = Math.max(p0.x, p1.x);
    int x1 = Math.min(p0.x + p0.width, p1.x + p1.width);
    if (p0.y < p1.y) {
      rects[count++] = new Rectangle(x0, p0.y, x1 - x0, p1.y - p0.y);
    }
    if (p0.y + p0.height > p1.y + p1.height) {
      rects[count++] = new Rectangle(x0, p1.y + p1.height, x1 - x0, (p0.y + p0.height) - (p1.y + p1.height));
    }
    return java.util.Arrays.copyOf(rects, count);
  }
  public static void invokeLater(java.lang.Runnable p0) {
    java.awt.EventQueue.invokeLater(p0);
  }
  public static int getAccessibleChildrenCount(java.awt.Component p0) {
    if (p0 instanceof Accessible) {
      return ((Accessible) p0).getAccessibleContext().getAccessibleChildrenCount();
    }
    return p0 instanceof Container ? ((Container) p0).getComponentCount() : 0;
  }
  public static javax.accessibility.Accessible getAccessibleChild(java.awt.Component p0, int p1) {
    if (p0 instanceof Accessible) {
      return ((Accessible) p0).getAccessibleContext().getAccessibleChild(p1);
    }
    return null;
  }
  public static javax.accessibility.Accessible getAccessibleAt(java.awt.Component p0, java.awt.Point p1) {
    if (p0 instanceof Container) {
      Component c = ((Container) p0).getComponentAt(p1);
      return c instanceof Accessible && c != p0 ? (Accessible) c : null;
    }
    return null;
  }
  public static int getAccessibleIndexInParent(java.awt.Component p0) {
    Container p = p0.getParent();
    if (p == null) {
      return -1;
    }
    for (int i = 0; i < p.getComponentCount(); i++) {
      if (p.getComponent(i) == p0) {
        return i;
      }
    }
    return -1;
  }
  public static javax.accessibility.AccessibleStateSet getAccessibleStateSet(java.awt.Component p0) {
    AccessibleStateSet states = new AccessibleStateSet();
    if (p0.isEnabled()) {
      states.add(AccessibleState.ENABLED);
    }
    if (p0.isFocusable()) {
      states.add(AccessibleState.FOCUSABLE);
    }
    if (p0.isVisible()) {
      states.add(AccessibleState.VISIBLE);
    }
    if (p0.isShowing()) {
      states.add(AccessibleState.SHOWING);
    }
    return states;
  }
  public static void invokeAndWait(java.lang.Runnable p0) throws java.lang.InterruptedException, java.lang.reflect.InvocationTargetException {
    java.awt.EventQueue.invokeAndWait(p0);
  }
  public static javax.swing.JRootPane getRootPane(java.awt.Component p0) {
    if (p0 instanceof RootPaneContainer) {
      return ((RootPaneContainer) p0).getRootPane();
    }
    for (; p0 != null; p0 = p0.getParent()) {
      if (p0 instanceof JRootPane) {
        return (JRootPane) p0;
      }
    }
    return null;
  }

  private static void updateComponentTreeUI0(Component c) {
    if (c instanceof JComponent) {
      JComponent jc = (JComponent) c;
      jc.updateUI();
      JPopupMenu jpm = jc.getComponentPopupMenu();
      if (jpm != null) {
        updateComponentTreeUI(jpm);
      }
    }
    Component[] children = null;
    if (c instanceof JMenu) {
      children = ((JMenu) c).getMenuComponents();
    } else if (c instanceof Container) {
      children = ((Container) c).getComponents();
    }
    if (children != null) {
      for (Component child : children) {
        updateComponentTreeUI0(child);
      }
    }
  }

  private static CellRendererPane getCellRendererPane(Component c, Container p) {
    if (c.getParent() instanceof CellRendererPane) {
      return (CellRendererPane) c.getParent();
    }
    CellRendererPane crp = new CellRendererPane();
    if (p != null) {
      p.add(crp);
    }
    return crp;
  }

  private static String layoutCompoundLabelImpl(Component c, FontMetrics fm, String text, Icon icon,
      int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition,
      Rectangle viewR, Rectangle iconR, Rectangle textR, int textIconGap) {
    if (c != null) {
      boolean ltr = c.getComponentOrientation().isLeftToRight();
      if (horizontalAlignment == LEADING) {
        horizontalAlignment = ltr ? LEFT : RIGHT;
      } else if (horizontalAlignment == TRAILING) {
        horizontalAlignment = ltr ? RIGHT : LEFT;
      }
      if (horizontalTextPosition == LEADING) {
        horizontalTextPosition = ltr ? LEFT : RIGHT;
      } else if (horizontalTextPosition == TRAILING) {
        horizontalTextPosition = ltr ? RIGHT : LEFT;
      }
    } else {
      if (horizontalAlignment == LEADING) {
        horizontalAlignment = LEFT;
      } else if (horizontalAlignment == TRAILING) {
        horizontalAlignment = RIGHT;
      }
      if (horizontalTextPosition == LEADING) {
        horizontalTextPosition = LEFT;
      } else if (horizontalTextPosition == TRAILING) {
        horizontalTextPosition = RIGHT;
      }
    }
    boolean textIsEmpty = (text == null) || text.equals("");
    if (icon != null) {
      iconR.width = icon.getIconWidth();
      iconR.height = icon.getIconHeight();
    } else {
      iconR.width = iconR.height = 0;
    }
    if (textIsEmpty) {
      textR.width = textR.height = 0;
      text = "";
    } else {
      int availTextWidth;
      int gap = (icon == null) ? 0 : textIconGap;
      if (horizontalTextPosition == CENTER) {
        availTextWidth = viewR.width;
      } else {
        availTextWidth = viewR.width - (iconR.width + gap);
      }
      javax.swing.text.View htmlView = c instanceof JComponent
          ? javax.swing.plaf.basic.BasicHTML.rendererFor((JComponent) c, text) : null;
      if (htmlView != null) {
        textR.width = Math.min(availTextWidth, (int) htmlView.getPreferredSpan(javax.swing.text.View.X_AXIS));
        textR.height = (int) htmlView.getPreferredSpan(javax.swing.text.View.Y_AXIS);
      } else {
        textR.width = fm.stringWidth(text);
        textR.height = fm.getHeight();
        if (textR.width > availTextWidth) {
          text = clipText(fm, text, availTextWidth);
          textR.width = fm.stringWidth(text);
        }
      }
    }
    int gap = (textIsEmpty || (icon == null)) ? 0 : textIconGap;
    if (verticalTextPosition == TOP) {
      textR.y = horizontalTextPosition != CENTER ? 0 : -(textR.height + gap);
    } else if (verticalTextPosition == CENTER) {
      textR.y = (iconR.height / 2) - (textR.height / 2);
    } else {
      textR.y = horizontalTextPosition != CENTER ? iconR.height - textR.height : (iconR.height + gap);
    }
    if (horizontalTextPosition == LEFT) {
      textR.x = -(textR.width + gap);
    } else if (horizontalTextPosition == CENTER) {
      textR.x = (iconR.width / 2) - (textR.width / 2);
    } else {
      textR.x = (iconR.width + gap);
    }
    int labelR_x = Math.min(iconR.x, textR.x);
    int labelR_width = Math.max(iconR.x + iconR.width, textR.x + textR.width) - labelR_x;
    int labelR_y = Math.min(iconR.y, textR.y);
    int labelR_height = Math.max(iconR.y + iconR.height, textR.y + textR.height) - labelR_y;
    int dx;
    int dy;
    if (verticalAlignment == TOP) {
      dy = viewR.y - labelR_y;
    } else if (verticalAlignment == CENTER) {
      dy = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
    } else {
      dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
    }
    if (horizontalAlignment == LEFT) {
      dx = viewR.x - labelR_x;
    } else if (horizontalAlignment == RIGHT) {
      dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
    } else {
      dx = (viewR.x + (viewR.width / 2)) - (labelR_x + (labelR_width / 2));
    }
    textR.x += dx;
    textR.y += dy;
    iconR.x += dx;
    iconR.y += dy;
    return text;
  }

  private static String clipText(FontMetrics fm, String string, int availTextWidth) {
    String clipString = "...";
    int width = fm.stringWidth(clipString);
    if (availTextWidth <= width) {
      return clipString;
    }
    availTextWidth -= width;
    int i = 0;
    int run = 0;
    for (; i < string.length(); i++) {
      run += fm.charWidth(string.charAt(i));
      if (run > availTextWidth) {
        break;
      }
    }
    return string.substring(0, i) + clipString;
  }


  static int findDisplayedMnemonicIndex(String text, int mnemonic) {
    if (text == null || mnemonic == '\0') {
      return -1;
    }
    char uc = Character.toUpperCase((char) mnemonic);
    char lc = Character.toLowerCase((char) mnemonic);
    int uci = text.indexOf(uc);
    int lci = text.indexOf(lc);
    if (uci == -1) {
      return lci;
    } else if (lci == -1) {
      return uci;
    }
    return (lci < uci) ? lci : uci;
  }
}
