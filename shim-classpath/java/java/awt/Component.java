/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.awt.im.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public abstract class Component implements java.awt.image.ImageObserver, java.awt.MenuContainer, java.io.Serializable {
  int x;
  int y;
  int width;
  int height;
  Container parent;
  String name;
  boolean nameExplicitlySet;

  String constructComponentName() {
    return null;
  }
  Color foreground;
  Color background;
  Font font;
  Locale locale;
  Cursor cursor;
  boolean visible = true;
  boolean enabled = true;
  boolean valid;
  boolean focusable = true;
  boolean focusTraversalKeysEnabled = true;
  boolean ignoreRepaint;
  long eventMask;
  Dimension prefSize;
  Dimension minSize;
  Dimension maxSize;
  DropTarget dropTarget;
  ComponentOrientation componentOrientation = ComponentOrientation.UNKNOWN;
  private PropertyChangeSupport changeSupport;
  private static final Object treeLock = new Object();
  private final java.util.concurrent.CopyOnWriteArrayList<ComponentListener> componentListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<FocusListener> focusListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<KeyListener> keyListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<MouseListener> mouseListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<MouseMotionListener> mouseMotionListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<MouseWheelListener> mouseWheelListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<InputMethodListener> inputMethodListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<HierarchyListener> hierarchyListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final java.util.concurrent.CopyOnWriteArrayList<HierarchyBoundsListener> hierarchyBoundsListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

  private synchronized PropertyChangeSupport changeSupport() {
    if (changeSupport == null) {
      changeSupport = new PropertyChangeSupport(this);
    }
    return changeSupport;
  }

  public static final float TOP_ALIGNMENT = Float.intBitsToFloat(0);
  public static final float CENTER_ALIGNMENT = Float.intBitsToFloat(1056964608);
  public static final float BOTTOM_ALIGNMENT = Float.intBitsToFloat(1065353216);
  public static final float LEFT_ALIGNMENT = Float.intBitsToFloat(0);
  public static final float RIGHT_ALIGNMENT = Float.intBitsToFloat(1065353216);
  protected javax.accessibility.AccessibleContext accessibleContext;
  protected Component() {
  }
  public java.lang.String getName() {
    if (name == null && !nameExplicitlySet) {
      synchronized (this) {
        if (name == null && !nameExplicitlySet) {
          name = constructComponentName();
        }
      }
    }
    return name;
  }
  public void remove(java.awt.MenuComponent p0) {
    
  }
  public java.awt.Dimension size() {
    return getSize();
  }
  public java.lang.String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  public void update(java.awt.Graphics p0) {
    paint(p0);
  }
  public void add(java.awt.PopupMenu p0) {
    
  }
  public boolean contains(java.awt.Point p0) {
    return contains(p0.x, p0.y);
  }
  public boolean contains(int p0, int p1) {
    return (p0 >= 0) && (p0 < width) && (p1 >= 0) && (p1 < height);
  }
  public void list(java.io.PrintStream p0, int p1) {
    p0.println(" ".repeat(p1) + this);
  }
  public void list(java.io.PrintStream p0) {
    list(p0, 0);
  }
  public void list() {
    list(System.out, 0);
  }
  public void list(java.io.PrintWriter p0) {
    list(p0, 0);
  }
  public void list(java.io.PrintWriter p0, int p1) {
    p0.println(" ".repeat(p1) + this);
  }
  public java.awt.Rectangle getBounds(java.awt.Rectangle p0) {
    if (p0 == null) {
      return new Rectangle(x, y, width, height);
    }
    p0.setBounds(x, y, width, height);
    return p0;
  }
  public java.awt.Rectangle getBounds() {
    return new Rectangle(x, y, width, height);
  }
  public void validate() {
    valid = true;
  }
  public java.awt.Rectangle bounds() {
    return getBounds();
  }
  public java.awt.Point location() {
    return getLocation();
  }
  public java.awt.Point getLocation(java.awt.Point p0) {
    if (p0 == null) {
      return new Point(x, y);
    }
    p0.setLocation(x, y);
    return p0;
  }
  public java.awt.Point getLocation() {
    return new Point(x, y);
  }
  public java.awt.Container getParent() {
    return parent;
  }
  public boolean isEnabled() {
    return enabled;
  }
  public void setName(java.lang.String p0) {
    String old = name;
    name = p0;
    nameExplicitlySet = true;
    firePropertyChange("name", old, p0);
  }
  public void print(java.awt.Graphics p0) {
    paint(p0);
  }
  public boolean action(java.awt.Event p0, java.lang.Object p1) {
    return false;
  }
  public java.awt.Dimension getSize(java.awt.Dimension p0) {
    if (p0 == null) {
      return new Dimension(width, height);
    }
    p0.setSize(width, height);
    return p0;
  }
  public java.awt.Dimension getSize() {
    return new Dimension(width, height);
  }
  protected java.lang.String paramString() {
    String str = (getName() == null ? "" : getName()) + "," + x + "," + y + "," + width + "x" + height;
    if (!isValid()) {
      str += ",invalid";
    }
    if (!visible) {
      str += ",hidden";
    }
    if (!enabled) {
      str += ",disabled";
    }
    return str;
  }
  public boolean isOpaque() {
    return false;
  }
  public void resize(int p0, int p1) {
    setSize(p0, p1);
  }
  public void resize(java.awt.Dimension p0) {
    setSize(p0.width, p0.height);
  }
  public void enable() {
    enable(true);
  }
  public void enable(boolean p0) {
    if (enabled != p0) {
      enabled = p0;
    }
  }
  public void invalidate() {
    valid = false;
    if (parent != null && parent.isValid()) {
      parent.invalidate();
    }
  }
  public boolean isValid() {
    return valid;
  }
  public void move(int p0, int p1) {
    setLocation(p0, p1);
  }
  public java.util.Locale getLocale() {
    if (locale == null) {
      if (parent != null) {
        return parent.getLocale();
      }
      return Locale.getDefault();
    }
    return locale;
  }
  public void setLocale(java.util.Locale p0) {
    Locale old = locale;
    locale = p0;
    firePropertyChange("locale", old, p0);
  }
  public void setSize(int p0, int p1) {
    reshape(x, y, p0, p1);
  }
  public void setSize(java.awt.Dimension p0) {
    setSize(p0.width, p0.height);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    return accessibleContext;
  }
  public final java.lang.Object getTreeLock() {
    return treeLock;
  }
  public void addNotify() {
    
  }
  public void layout() {
    
  }
  public java.awt.GraphicsConfiguration getGraphicsConfiguration() {
    return null;
  }
  public void removeNotify() {
    
  }
  public final void dispatchEvent(java.awt.AWTEvent p0) {
    if (p0 instanceof KeyEvent || p0 instanceof MouseEvent) {
      if (!enabled) {
        return;
      }
    }
    processEvent(p0);
  }
  public boolean isVisible() {
    return visible;
  }
  public boolean isDisplayable() {
    return false;
  }
  public boolean isFocusable() {
    return focusable;
  }
  public boolean isFocusCycleRoot(java.awt.Container p0) {
    return getFocusCycleRootAncestor() == p0;
  }
  public boolean isLightweight() {
    return true;
  }
  public boolean isFocusOwner() {
    return false;
  }
  public void transferFocus() {
    
  }
  public void doLayout() {
    layout();
  }
  public java.awt.Font getFont() {
    Font f = font;
    if (f != null) {
      return f;
    }
    Container p = parent;
    return (p != null) ? p.getFont() : null;
  }
  public void setFont(java.awt.Font p0) {
    Font old = font;
    font = p0;
    firePropertyChange("font", old, p0);
    if (valid) {
      invalidate();
    }
  }
  public java.awt.Dimension preferredSize() {
    if (prefSize != null) {
      return new Dimension(prefSize);
    }
    return getMinimumSize();
  }
  public boolean isPreferredSizeSet() {
    return prefSize != null;
  }
  public java.awt.Dimension minimumSize() {
    if (minSize != null) {
      return new Dimension(minSize);
    }
    return new Dimension(width, height);
  }
  public boolean isMinimumSizeSet() {
    return minSize != null;
  }
  public boolean isMaximumSizeSet() {
    return maxSize != null;
  }
  public java.awt.Dimension getMaximumSize() {
    if (maxSize != null) {
      return new Dimension(maxSize);
    }
    return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
  }
  public float getAlignmentX() {
    return CENTER_ALIGNMENT;
  }
  public float getAlignmentY() {
    return CENTER_ALIGNMENT;
  }
  public boolean isShowing() {
    return false;
  }
  public void paint(java.awt.Graphics p0) {
    
  }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) {
    java.util.List<EventListener> l;
    if (p0 == ComponentListener.class) {
      l = new java.util.ArrayList<EventListener>(componentListeners);
    } else if (p0 == FocusListener.class) {
      l = new java.util.ArrayList<EventListener>(focusListeners);
    } else if (p0 == KeyListener.class) {
      l = new java.util.ArrayList<EventListener>(keyListeners);
    } else if (p0 == MouseListener.class) {
      l = new java.util.ArrayList<EventListener>(mouseListeners);
    } else if (p0 == MouseMotionListener.class) {
      l = new java.util.ArrayList<EventListener>(mouseMotionListeners);
    } else if (p0 == MouseWheelListener.class) {
      l = new java.util.ArrayList<EventListener>(mouseWheelListeners);
    } else if (p0 == HierarchyListener.class) {
      l = new java.util.ArrayList<EventListener>(hierarchyListeners);
    } else if (p0 == HierarchyBoundsListener.class) {
      l = new java.util.ArrayList<EventListener>(hierarchyBoundsListeners);
    } else if (p0 == InputMethodListener.class) {
      l = new java.util.ArrayList<EventListener>(inputMethodListeners);
    } else if (p0 == PropertyChangeListener.class) {
      l = new java.util.ArrayList<EventListener>(java.util.Arrays.asList(getPropertyChangeListeners()));
    } else {
      l = new java.util.ArrayList<EventListener>();
    }
    T[] result = (T[]) java.lang.reflect.Array.newInstance(p0, l.size());
    for (int i = 0; i < result.length; i++) {
      result[i] = (T) l.get(i);
    }
    return result;
  }
  protected void processEvent(java.awt.AWTEvent p0) {
    if (p0 instanceof FocusEvent) {
      processFocusEvent((FocusEvent) p0);
    } else if (p0 instanceof MouseEvent) {
      switch (p0.getID()) {
      case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_RELEASED:
      case MouseEvent.MOUSE_CLICKED:
      case MouseEvent.MOUSE_ENTERED:
      case MouseEvent.MOUSE_EXITED:
        processMouseEvent((MouseEvent) p0);
        break;
      case MouseEvent.MOUSE_MOVED:
      case MouseEvent.MOUSE_DRAGGED:
        processMouseMotionEvent((MouseEvent) p0);
        break;
      case MouseEvent.MOUSE_WHEEL:
        processMouseWheelEvent((MouseWheelEvent) p0);
        break;
      default:
        break;
      }
    } else if (p0 instanceof KeyEvent) {
      processKeyEvent((KeyEvent) p0);
    } else if (p0 instanceof ComponentEvent) {
      processComponentEvent((ComponentEvent) p0);
    } else if (p0 instanceof InputMethodEvent) {
      processInputMethodEvent((InputMethodEvent) p0);
    } else if (p0 instanceof HierarchyEvent) {
      switch (p0.getID()) {
      case HierarchyEvent.HIERARCHY_CHANGED:
        processHierarchyEvent((HierarchyEvent) p0);
        break;
      default:
        processHierarchyBoundsEvent((HierarchyEvent) p0);
        break;
      }
    }
  }
  public boolean handleEvent(java.awt.Event p0) {
    return false;
  }
  protected final void enableEvents(long p0) {
    eventMask |= p0;
  }
  public java.awt.Component getComponentAt(int p0, int p1) {
    return contains(p0, p1) ? this : null;
  }
  public java.awt.Component getComponentAt(java.awt.Point p0) {
    return getComponentAt(p0.x, p0.y);
  }
  public void deliverEvent(java.awt.Event p0) {
    
  }
  public boolean postEvent(java.awt.Event p0) {
    return false;
  }
  public java.awt.Component locate(int p0, int p1) {
    return getComponentAt(p0, p1);
  }
  public void transferFocusBackward() {
    
  }
  public java.awt.Container getFocusCycleRootAncestor() {
    Container p = parent;
    while (p != null && !p.isFocusCycleRoot()) {
      p = p.getParent();
    }
    return p;
  }
  public void firePropertyChange(java.lang.String p0, double p1, double p2) {
    firePropertyChange(p0, Double.valueOf(p1), Double.valueOf(p2));
  }
  public void firePropertyChange(java.lang.String p0, float p1, float p2) {
    firePropertyChange(p0, Float.valueOf(p1), Float.valueOf(p2));
  }
  public void firePropertyChange(java.lang.String p0, long p1, long p2) {
    firePropertyChange(p0, Long.valueOf(p1), Long.valueOf(p2));
  }
  public void firePropertyChange(java.lang.String p0, short p1, short p2) {
    firePropertyChange(p0, Short.valueOf(p1), Short.valueOf(p2));
  }
  public void firePropertyChange(java.lang.String p0, byte p1, byte p2) {
    firePropertyChange(p0, Byte.valueOf(p1), Byte.valueOf(p2));
  }
  protected void firePropertyChange(java.lang.String p0, int p1, int p2) {
    firePropertyChange(p0, Integer.valueOf(p1), Integer.valueOf(p2));
  }
  protected void firePropertyChange(java.lang.String p0, boolean p1, boolean p2) {
    firePropertyChange(p0, Boolean.valueOf(p1), Boolean.valueOf(p2));
  }
  public void firePropertyChange(java.lang.String p0, char p1, char p2) {
    firePropertyChange(p0, Character.valueOf(p1), Character.valueOf(p2));
  }
  protected void firePropertyChange(java.lang.String p0, java.lang.Object p1, java.lang.Object p2) {
    PropertyChangeSupport s = changeSupport;
    if (s != null) {
      s.firePropertyChange(p0, p1, p2);
    }
  }
  protected boolean requestFocus(boolean p0, java.awt.event.FocusEvent.Cause p1) {
    return false;
  }
  protected boolean requestFocus(boolean p0) {
    return false;
  }
  public void requestFocus() {
    
  }
  public void requestFocus(java.awt.event.FocusEvent.Cause p0) {
    
  }
  public void applyComponentOrientation(java.awt.ComponentOrientation p0) {
    setComponentOrientation(p0);
  }
  public void addPropertyChangeListener(java.beans.PropertyChangeListener p0) {
    changeSupport().addPropertyChangeListener(p0);
  }
  public void addPropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) {
    changeSupport().addPropertyChangeListener(p0, p1);
  }
  public void setVisible(boolean p0) {
    show(p0);
  }
  public int getX() {
    return x;
  }
  public int getY() {
    return y;
  }
  public int getWidth() {
    return width;
  }
  public int getHeight() {
    return height;
  }
  public void setBounds(int p0, int p1, int p2, int p3) {
    reshape(p0, p1, p2, p3);
  }
  public void setBounds(java.awt.Rectangle p0) {
    setBounds(p0.x, p0.y, p0.width, p0.height);
  }
  public java.awt.Dimension getPreferredSize() {
    return preferredSize();
  }
  public java.awt.Dimension getMinimumSize() {
    return minimumSize();
  }
  public java.awt.Point getMousePosition() throws java.awt.HeadlessException {
    throw new HeadlessException();
  }
  java.util.Set<java.awt.AWTKeyStroke>[] focusTraversalKeys;

  @SuppressWarnings("unchecked")
  public void setFocusTraversalKeys(int id, java.util.Set<? extends java.awt.AWTKeyStroke> keystrokes) {
    if (id < 0 || id >= 3 + (this instanceof Container ? 1 : 0)) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }
    java.util.Set<java.awt.AWTKeyStroke> oldKeys;
    synchronized (this) {
      if (focusTraversalKeys == null) {
        focusTraversalKeys = new java.util.Set[4];
      }
      if (keystrokes != null) {
        for (java.awt.AWTKeyStroke keystroke : keystrokes) {
          if (keystroke == null) {
            throw new IllegalArgumentException("cannot set null focus traversal key");
          }
          if (keystroke.getKeyChar() != java.awt.event.KeyEvent.CHAR_UNDEFINED) {
            throw new IllegalArgumentException("focus traversal keys cannot map to KEY_TYPED events");
          }
          for (int i = 0; i < 4; i++) {
            if (i == id) {
              continue;
            }
            if (getFocusTraversalKeys_NoIDCheck(i).contains(keystroke)) {
              throw new IllegalArgumentException("focus traversal keys must be unique for a Component");
            }
          }
        }
      }
      oldKeys = focusTraversalKeys[id];
      focusTraversalKeys[id] = keystrokes != null
          ? java.util.Collections.unmodifiableSet(new java.util.HashSet<java.awt.AWTKeyStroke>(keystrokes)) : null;
    }
    firePropertyChange(KeyboardFocusManager.kKeyNames[id].replace("Default", ""), oldKeys, keystrokes);
  }

  public java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys(int id) {
    if (id < 0 || id >= 3 + (this instanceof Container ? 1 : 0)) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }
    return getFocusTraversalKeys_NoIDCheck(id);
  }

  final java.util.Set<java.awt.AWTKeyStroke> getFocusTraversalKeys_NoIDCheck(int id) {
    java.util.Set<java.awt.AWTKeyStroke> keystrokes = focusTraversalKeys != null ? focusTraversalKeys[id] : null;
    if (keystrokes != null) {
      return keystrokes;
    }
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(id);
  }

  public boolean areFocusTraversalKeysSet(int id) {
    if (id < 0 || id >= 3 + (this instanceof Container ? 1 : 0)) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }
    return focusTraversalKeys != null && focusTraversalKeys[id] != null;
  }
  public java.awt.Toolkit getToolkit() {
    return Toolkit.getDefaultToolkit();
  }
  public void setLocation(int p0, int p1) {
    reshape(p0, p1, width, height);
  }
  public void setLocation(java.awt.Point p0) {
    setLocation(p0.x, p0.y);
  }
  public java.awt.Point getLocationOnScreen() {
    throw new IllegalComponentStateException("component must be showing on the screen to determine its location");
  }
  public void setEnabled(boolean p0) {
    enable(p0);
  }
  public void disable() {
    enable(false);
  }
  public java.awt.im.InputContext getInputContext() {
    return parent == null ? null : parent.getInputContext();
  }
  public void show(boolean p0) {
    if (p0) {
      show();
    } else {
      hide();
    }
  }
  private void setVisibleFlag(boolean p0) {
    if (visible != p0) {
      visible = p0;
      invalidate();
      dispatchEvent(new ComponentEvent(this, p0 ? ComponentEvent.COMPONENT_SHOWN : ComponentEvent.COMPONENT_HIDDEN));
    }
  }
  public void show() {
    setVisibleFlag(true);
  }
  public void repaint() {
    
  }
  public void repaint(long p0) {
    
  }
  public void repaint(long p0, int p1, int p2, int p3, int p4) {
    
  }
  public void repaint(int p0, int p1, int p2, int p3) {
    
  }
  public void hide() {
    setVisibleFlag(false);
  }
  public java.awt.Color getForeground() {
    Color c = foreground;
    if (c != null) {
      return c;
    }
    Container p = parent;
    return (p != null) ? p.getForeground() : null;
  }
  public void setForeground(java.awt.Color p0) {
    Color old = foreground;
    foreground = p0;
    firePropertyChange("foreground", old, p0);
  }
  public java.awt.Color getBackground() {
    Color c = background;
    if (c != null) {
      return c;
    }
    Container p = parent;
    return (p != null) ? p.getBackground() : null;
  }
  public void setBackground(java.awt.Color p0) {
    Color old = background;
    background = p0;
    firePropertyChange("background", old, p0);
  }
  public java.awt.image.ColorModel getColorModel() {
    throw new HeadlessException();
  }
  public void reshape(int p0, int p1, int p2, int p3) {
    boolean moved = (x != p0) || (y != p1);
    boolean resized = (width != p2) || (height != p3);
    x = p0;
    y = p1;
    width = p2;
    height = p3;
    if (resized || moved) {
      invalidate();
    }
  }
  public java.awt.Graphics getGraphics() {
    return null;
  }
  public java.awt.Image createImage(int p0, int p1) {
    return null;
  }
  public java.awt.Image createImage(java.awt.image.ImageProducer p0) {
    return Toolkit.getDefaultToolkit().createImage(p0);
  }
  public java.awt.image.VolatileImage createVolatileImage(int p0, int p1, java.awt.ImageCapabilities p2) throws java.awt.AWTException { return null; }
  public java.awt.image.VolatileImage createVolatileImage(int p0, int p1) {
    return null;
  }
  public boolean prepareImage(java.awt.Image p0, int p1, int p2, java.awt.image.ImageObserver p3) {
    return Toolkit.getDefaultToolkit().prepareImage(p0, p1, p2, p3);
  }
  public boolean prepareImage(java.awt.Image p0, java.awt.image.ImageObserver p1) {
    return Toolkit.getDefaultToolkit().prepareImage(p0, -1, -1, p1);
  }
  public int checkImage(java.awt.Image p0, java.awt.image.ImageObserver p1) {
    return Toolkit.getDefaultToolkit().checkImage(p0, -1, -1, p1);
  }
  public int checkImage(java.awt.Image p0, int p1, int p2, java.awt.image.ImageObserver p3) {
    return Toolkit.getDefaultToolkit().checkImage(p0, p1, p2, p3);
  }
  public boolean inside(int p0, int p1) {
    return contains(p0, p1);
  }
  protected void processKeyEvent(java.awt.event.KeyEvent p0) {
    for (KeyListener l : keyListeners.toArray(new KeyListener[0])) {
      switch (p0.getID()) {
      case KeyEvent.KEY_TYPED:
        l.keyTyped(p0);
        break;
      case KeyEvent.KEY_PRESSED:
        l.keyPressed(p0);
        break;
      case KeyEvent.KEY_RELEASED:
        l.keyReleased(p0);
        break;
      default:
        break;
      }
    }
  }
  public java.beans.PropertyChangeListener[] getPropertyChangeListeners() {
    return changeSupport().getPropertyChangeListeners();
  }
  public java.beans.PropertyChangeListener[] getPropertyChangeListeners(java.lang.String p0) {
    return changeSupport().getPropertyChangeListeners(p0);
  }
  protected java.awt.AWTEvent coalesceEvents(java.awt.AWTEvent p0, java.awt.AWTEvent p1) {
    return null;
  }
  protected void processFocusEvent(java.awt.event.FocusEvent p0) {
    for (FocusListener l : focusListeners.toArray(new FocusListener[0])) {
      if (p0.getID() == FocusEvent.FOCUS_GAINED) {
        l.focusGained(p0);
      } else if (p0.getID() == FocusEvent.FOCUS_LOST) {
        l.focusLost(p0);
      }
    }
  }
  protected void processMouseEvent(java.awt.event.MouseEvent p0) {
    for (MouseListener l : mouseListeners.toArray(new MouseListener[0])) {
      switch (p0.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        l.mousePressed(p0);
        break;
      case MouseEvent.MOUSE_RELEASED:
        l.mouseReleased(p0);
        break;
      case MouseEvent.MOUSE_CLICKED:
        l.mouseClicked(p0);
        break;
      case MouseEvent.MOUSE_EXITED:
        l.mouseExited(p0);
        break;
      case MouseEvent.MOUSE_ENTERED:
        l.mouseEntered(p0);
        break;
      default:
        break;
      }
    }
  }
  protected void processMouseMotionEvent(java.awt.event.MouseEvent p0) {
    for (MouseMotionListener l : mouseMotionListeners.toArray(new MouseMotionListener[0])) {
      if (p0.getID() == MouseEvent.MOUSE_MOVED) {
        l.mouseMoved(p0);
      } else if (p0.getID() == MouseEvent.MOUSE_DRAGGED) {
        l.mouseDragged(p0);
      }
    }
  }
  protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent p0) {
    for (MouseWheelListener l : mouseWheelListeners.toArray(new MouseWheelListener[0])) {
      l.mouseWheelMoved(p0);
    }
  }
  protected void processComponentEvent(java.awt.event.ComponentEvent p0) {
    for (ComponentListener l : componentListeners.toArray(new ComponentListener[0])) {
      switch (p0.getID()) {
      case ComponentEvent.COMPONENT_RESIZED:
        l.componentResized(p0);
        break;
      case ComponentEvent.COMPONENT_MOVED:
        l.componentMoved(p0);
        break;
      case ComponentEvent.COMPONENT_SHOWN:
        l.componentShown(p0);
        break;
      case ComponentEvent.COMPONENT_HIDDEN:
        l.componentHidden(p0);
        break;
      default:
        break;
      }
    }
  }
  protected void processInputMethodEvent(java.awt.event.InputMethodEvent p0) {
    
  }
  protected void processHierarchyEvent(java.awt.event.HierarchyEvent p0) {
    for (HierarchyListener l : hierarchyListeners.toArray(new HierarchyListener[0])) {
      l.hierarchyChanged(p0);
    }
  }
  protected void processHierarchyBoundsEvent(java.awt.event.HierarchyEvent p0) {
    for (HierarchyBoundsListener l : hierarchyBoundsListeners.toArray(new HierarchyBoundsListener[0])) {
      if (p0.getID() == HierarchyEvent.ANCESTOR_MOVED) {
        l.ancestorMoved(p0);
      } else {
        l.ancestorResized(p0);
      }
    }
  }
  public boolean mouseEnter(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean mouseExit(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean mouseMove(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean mouseDown(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean mouseDrag(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean mouseUp(java.awt.Event p0, int p1, int p2) {
    return false;
  }
  public boolean keyDown(java.awt.Event p0, int p1) {
    return false;
  }
  public boolean keyUp(java.awt.Event p0, int p1) {
    return false;
  }
  public boolean gotFocus(java.awt.Event p0, java.lang.Object p1) {
    return false;
  }
  public boolean lostFocus(java.awt.Event p0, java.lang.Object p1) {
    return false;
  }
  public boolean isFocusTraversable() {
    return focusable;
  }
  public void nextFocus() {
    
  }
  public boolean requestFocusInWindow(java.awt.event.FocusEvent.Cause p0) {
    return false;
  }
  public boolean requestFocusInWindow() {
    return false;
  }
  protected boolean requestFocusInWindow(boolean p0) {
    return false;
  }
  public boolean hasFocus() {
    return false;
  }
  public void removePropertyChangeListener(java.beans.PropertyChangeListener p0) {
    changeSupport().removePropertyChangeListener(p0);
  }
  public void removePropertyChangeListener(java.lang.String p0, java.beans.PropertyChangeListener p1) {
    changeSupport().removePropertyChangeListener(p0, p1);
  }
  public void addComponentListener(java.awt.event.ComponentListener p0) {
    if (p0 == null) {
      return;
    }
    componentListeners.add(p0);
  }
  public void addFocusListener(java.awt.event.FocusListener p0) {
    if (p0 == null) {
      return;
    }
    focusListeners.add(p0);
  }
  public void addKeyListener(java.awt.event.KeyListener p0) {
    if (p0 == null) {
      return;
    }
    keyListeners.add(p0);
  }
  public void addMouseListener(java.awt.event.MouseListener p0) {
    if (p0 == null) {
      return;
    }
    mouseListeners.add(p0);
  }
  public void addMouseMotionListener(java.awt.event.MouseMotionListener p0) {
    if (p0 == null) {
      return;
    }
    mouseMotionListeners.add(p0);
  }
  public void addInputMethodListener(java.awt.event.InputMethodListener p0) {
    if (p0 == null) {
      return;
    }
    inputMethodListeners.add(p0);
  }
  public void addHierarchyListener(java.awt.event.HierarchyListener p0) {
    if (p0 == null) {
      return;
    }
    hierarchyListeners.add(p0);
  }
  public void addHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener p0) {
    if (p0 == null) {
      return;
    }
    hierarchyBoundsListeners.add(p0);
  }
  public void addMouseWheelListener(java.awt.event.MouseWheelListener p0) {
    if (p0 == null) {
      return;
    }
    mouseWheelListeners.add(p0);
  }
  public void setComponentOrientation(java.awt.ComponentOrientation p0) {
    ComponentOrientation old = componentOrientation;
    componentOrientation = p0;
    firePropertyChange("componentOrientation", old, p0);
    invalidate();
  }
  public void setDropTarget(java.awt.dnd.DropTarget p0) {
    dropTarget = p0;
  }
  public java.awt.dnd.DropTarget getDropTarget() {
    return dropTarget;
  }
  public boolean isDoubleBuffered() {
    return false;
  }
  public void enableInputMethods(boolean p0) {
    
  }
  public boolean isForegroundSet() {
    return foreground != null;
  }
  public boolean isBackgroundSet() {
    return background != null;
  }
  public boolean isFontSet() {
    return font != null;
  }
  public void setPreferredSize(java.awt.Dimension p0) {
    Dimension old = prefSize;
    prefSize = p0;
    firePropertyChange("preferredSize", old, p0);
  }
  public void setMinimumSize(java.awt.Dimension p0) {
    Dimension old = minSize;
    minSize = p0;
    firePropertyChange("minimumSize", old, p0);
  }
  public void setMaximumSize(java.awt.Dimension p0) {
    Dimension old = maxSize;
    maxSize = p0;
    firePropertyChange("maximumSize", old, p0);
  }
  public int getBaseline(int p0, int p1) {
    if (p0 < 0 || p1 < 0) {
      throw new IllegalArgumentException("Width and height must be >= 0");
    }
    return -1;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior() {
    return BaselineResizeBehavior.OTHER;
  }
  public void revalidate() {
    invalidate();
    validate();
  }
  public java.awt.FontMetrics getFontMetrics(java.awt.Font p0) {
    return Toolkit.getDefaultToolkit().getFontMetrics(p0);
  }
  public void setCursor(java.awt.Cursor p0) {
    cursor = p0;
  }
  public java.awt.Cursor getCursor() {
    if (cursor != null) {
      return cursor;
    }
    Container p = parent;
    return p != null ? p.getCursor() : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  }
  public boolean isCursorSet() {
    return cursor != null;
  }
  public void paintAll(java.awt.Graphics p0) {
    if (isShowing()) {
      paint(p0);
    }
  }
  public void printAll(java.awt.Graphics p0) {
    paintAll(p0);
  }
  public boolean imageUpdate(java.awt.Image p0, int p1, int p2, int p3, int p4, int p5) {
    return (p1 & (ALLBITS | ABORT)) == 0;
  }
  public void setIgnoreRepaint(boolean p0) {
    ignoreRepaint = p0;
  }
  public boolean getIgnoreRepaint() {
    return ignoreRepaint;
  }
  public void removeComponentListener(java.awt.event.ComponentListener p0) {
    componentListeners.remove(p0);
  }
  public java.awt.event.ComponentListener[] getComponentListeners() {
    return componentListeners.toArray(new ComponentListener[0]);
  }
  public void removeFocusListener(java.awt.event.FocusListener p0) {
    focusListeners.remove(p0);
  }
  public java.awt.event.FocusListener[] getFocusListeners() {
    return focusListeners.toArray(new FocusListener[0]);
  }
  public void removeHierarchyListener(java.awt.event.HierarchyListener p0) {
    hierarchyListeners.remove(p0);
  }
  public java.awt.event.HierarchyListener[] getHierarchyListeners() {
    return hierarchyListeners.toArray(new HierarchyListener[0]);
  }
  public void removeHierarchyBoundsListener(java.awt.event.HierarchyBoundsListener p0) {
    hierarchyBoundsListeners.remove(p0);
  }
  public java.awt.event.HierarchyBoundsListener[] getHierarchyBoundsListeners() {
    return hierarchyBoundsListeners.toArray(new HierarchyBoundsListener[0]);
  }
  public void removeKeyListener(java.awt.event.KeyListener p0) {
    keyListeners.remove(p0);
  }
  public java.awt.event.KeyListener[] getKeyListeners() {
    return keyListeners.toArray(new KeyListener[0]);
  }
  public void removeMouseListener(java.awt.event.MouseListener p0) {
    mouseListeners.remove(p0);
  }
  public java.awt.event.MouseListener[] getMouseListeners() {
    return mouseListeners.toArray(new MouseListener[0]);
  }
  public void removeMouseMotionListener(java.awt.event.MouseMotionListener p0) {
    mouseMotionListeners.remove(p0);
  }
  public java.awt.event.MouseMotionListener[] getMouseMotionListeners() {
    return mouseMotionListeners.toArray(new MouseMotionListener[0]);
  }
  public void removeMouseWheelListener(java.awt.event.MouseWheelListener p0) {
    mouseWheelListeners.remove(p0);
  }
  public java.awt.event.MouseWheelListener[] getMouseWheelListeners() {
    return mouseWheelListeners.toArray(new MouseWheelListener[0]);
  }
  public void removeInputMethodListener(java.awt.event.InputMethodListener p0) {
    inputMethodListeners.remove(p0);
  }
  public java.awt.event.InputMethodListener[] getInputMethodListeners() {
    return inputMethodListeners.toArray(new InputMethodListener[0]);
  }
  public java.awt.im.InputMethodRequests getInputMethodRequests() {
    return null;
  }
  protected final void disableEvents(long p0) {
    eventMask &= ~p0;
  }
  public void setFocusable(boolean p0) {
    boolean old = focusable;
    focusable = p0;
    firePropertyChange("focusable", old, p0);
  }
  public void setFocusTraversalKeysEnabled(boolean p0) {
    boolean old = focusTraversalKeysEnabled;
    focusTraversalKeysEnabled = p0;
    firePropertyChange("focusTraversalKeysEnabled", old, p0);
  }
  public boolean getFocusTraversalKeysEnabled() {
    return focusTraversalKeysEnabled;
  }
  public void transferFocusUpCycle() {
    
  }
  public java.awt.ComponentOrientation getComponentOrientation() {
    return componentOrientation;
  }
  public void setMixingCutoutShape(java.awt.Shape p0) {
    
  }

  protected abstract class AccessibleAWTComponent extends javax.accessibility.AccessibleContext
      implements java.io.Serializable, javax.accessibility.AccessibleComponent {
    protected java.awt.event.FocusListener accessibleAWTFocusHandler;
    protected java.awt.event.ComponentListener accessibleAWTComponentHandler;

    protected AccessibleAWTComponent() { }

    public String getAccessibleName() { return accessibleName; }

    public String getAccessibleDescription() { return accessibleDescription; }

    public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.AWT_COMPONENT; }

    public javax.accessibility.AccessibleStateSet getAccessibleStateSet() {
      javax.accessibility.AccessibleStateSet states = new javax.accessibility.AccessibleStateSet();
      if (Component.this.isEnabled()) {
        states.add(javax.accessibility.AccessibleState.ENABLED);
      }
      if (Component.this.isFocusable()) {
        states.add(javax.accessibility.AccessibleState.FOCUSABLE);
      }
      if (Component.this.isVisible()) {
        states.add(javax.accessibility.AccessibleState.VISIBLE);
      }
      if (Component.this.isShowing()) {
        states.add(javax.accessibility.AccessibleState.SHOWING);
      }
      return states;
    }

    public javax.accessibility.Accessible getAccessibleParent() {
      if (accessibleParent != null) {
        return accessibleParent;
      }
      Container parent = getParent();
      return parent instanceof javax.accessibility.Accessible ? (javax.accessibility.Accessible) parent : null;
    }

    public int getAccessibleIndexInParent() {
      Container parent = getParent();
      if (parent == null) {
        return -1;
      }
      for (int i = 0; i < parent.getComponentCount(); i++) {
        if (parent.getComponent(i) == Component.this) {
          return i;
        }
      }
      return -1;
    }

    public int getAccessibleChildrenCount() { return 0; }

    public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }

    public java.util.Locale getLocale() { return Component.this.getLocale(); }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
      super.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
      super.removePropertyChangeListener(listener);
    }

    public javax.accessibility.AccessibleComponent getAccessibleComponent() { return this; }

    public Color getBackground() { return Component.this.getBackground(); }

    public void setBackground(Color c) { Component.this.setBackground(c); }

    public Color getForeground() { return Component.this.getForeground(); }

    public void setForeground(Color c) { Component.this.setForeground(c); }

    public Cursor getCursor() { return Component.this.getCursor(); }

    public void setCursor(Cursor cursor) { Component.this.setCursor(cursor); }

    public Font getFont() { return Component.this.getFont(); }

    public void setFont(Font f) { Component.this.setFont(f); }

    public FontMetrics getFontMetrics(Font f) { return f == null ? null : Component.this.getFontMetrics(f); }

    public boolean isEnabled() { return Component.this.isEnabled(); }

    public void setEnabled(boolean b) { Component.this.setEnabled(b); }

    public boolean isVisible() { return Component.this.isVisible(); }

    public void setVisible(boolean b) { Component.this.setVisible(b); }

    public boolean isShowing() { return Component.this.isShowing(); }

    public boolean contains(Point p) { return Component.this.contains(p); }

    public Point getLocationOnScreen() { return Component.this.isShowing() ? Component.this.getLocationOnScreen() : null; }

    public Point getLocation() { return Component.this.getLocation(); }

    public void setLocation(Point p) { Component.this.setLocation(p); }

    public Rectangle getBounds() { return Component.this.getBounds(); }

    public void setBounds(Rectangle r) { Component.this.setBounds(r); }

    public Dimension getSize() { return Component.this.getSize(); }

    public void setSize(Dimension d) { Component.this.setSize(d); }

    public javax.accessibility.Accessible getAccessibleAt(Point p) { return null; }

    public boolean isFocusTraversable() { return Component.this.isFocusTraversable(); }

    public void requestFocus() { Component.this.requestFocus(); }

    public void addFocusListener(java.awt.event.FocusListener l) { Component.this.addFocusListener(l); }

    public void removeFocusListener(java.awt.event.FocusListener l) { Component.this.removeFocusListener(l); }
  }

  public enum BaselineResizeBehavior {
    CONSTANT_ASCENT, CONSTANT_DESCENT, CENTER_OFFSET, OTHER;
  }
}
