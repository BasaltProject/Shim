/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.AWTEventListener;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Toolkit {
  private static Toolkit toolkit;

  protected Map<String, Object> desktopProperties = new HashMap<>();
  protected PropertyChangeSupport desktopPropsSupport = new PropertyChangeSupport(this);

  private final java.util.List<AWTEventListener> awtListeners = new java.util.ArrayList<>();

  protected Toolkit() { }

  protected abstract EventQueue getSystemEventQueueImpl();

  public static synchronized Toolkit getDefaultToolkit() {
    if (toolkit == null) {
      toolkit = new shim.awt.HeadlessToolkit();
    }
    return toolkit;
  }

  public static String getProperty(String key, String defaultValue) {
    if (key == null || defaultValue == null) {
      throw new NullPointerException("key or defaultValue is null");
    }
    return defaultValue;
  }

  protected static Container getNativeContainer(Component c) {
    return c.getParent();
  }

  protected void initializeDesktopProperties() { }

  public final synchronized Object getDesktopProperty(String propertyName) {
    if (propertyName == null) {
      return null;
    }
    return desktopProperties.get(propertyName);
  }

  protected final void setDesktopProperty(String name, Object newValue) {
    Object oldValue = desktopProperties.put(name, newValue);
    desktopPropsSupport.firePropertyChange(name, oldValue, newValue);
  }

  protected Object lazilyLoadDesktopProperty(String name) {
    return null;
  }

  public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
    desktopPropsSupport.addPropertyChangeListener(name, pcl);
  }

  public void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
    desktopPropsSupport.removePropertyChangeListener(name, pcl);
  }

  public PropertyChangeListener[] getPropertyChangeListeners() {
    return desktopPropsSupport.getPropertyChangeListeners();
  }

  public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    return desktopPropsSupport.getPropertyChangeListeners(propertyName);
  }

  public abstract Dimension getScreenSize() throws HeadlessException;

  public abstract int getScreenResolution() throws HeadlessException;

  public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
    throw new HeadlessException();
  }

  public abstract ColorModel getColorModel() throws HeadlessException;

  @Deprecated
  public abstract String[] getFontList();

  @Deprecated
  public abstract FontMetrics getFontMetrics(Font font);

  public abstract void sync();

  public abstract Image getImage(String filename);

  public abstract Image getImage(URL url);

  public abstract Image createImage(String filename);

  public abstract Image createImage(URL url);

  public abstract boolean prepareImage(Image image, int width, int height, ImageObserver observer);

  public abstract int checkImage(Image image, int width, int height, ImageObserver observer);

  public abstract Image createImage(ImageProducer producer);

  public Image createImage(byte[] imagedata) {
    return createImage(imagedata, 0, imagedata.length);
  }

  public abstract Image createImage(byte[] imagedata, int imageoffset, int imagelength);

  public abstract PrintJob getPrintJob(Frame frame, String jobtitle, Properties props);

  public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes,
      PageAttributes pageAttributes) {
    throw new HeadlessException();
  }

  public abstract void beep();

  public abstract Clipboard getSystemClipboard() throws HeadlessException;

  public Clipboard getSystemSelection() throws HeadlessException {
    throw new HeadlessException();
  }

  @Deprecated
  public int getMenuShortcutKeyMask() throws HeadlessException {
    return getMenuShortcutKeyMaskEx();
  }

  public int getMenuShortcutKeyMaskEx() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
    if (!(keyCode == java.awt.event.KeyEvent.VK_CAPS_LOCK || keyCode == java.awt.event.KeyEvent.VK_NUM_LOCK
        || keyCode == java.awt.event.KeyEvent.VK_SCROLL_LOCK || keyCode == java.awt.event.KeyEvent.VK_KANA_LOCK)) {
      throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
    }
    if (GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    throw new UnsupportedOperationException("Toolkit.getLockingKeyState");
  }

  public void setLockingKeyState(int keyCode, boolean on) throws UnsupportedOperationException {
    if (!(keyCode == java.awt.event.KeyEvent.VK_CAPS_LOCK || keyCode == java.awt.event.KeyEvent.VK_NUM_LOCK
        || keyCode == java.awt.event.KeyEvent.VK_SCROLL_LOCK || keyCode == java.awt.event.KeyEvent.VK_KANA_LOCK)) {
      throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
    }
    if (GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    throw new UnsupportedOperationException("Toolkit.setLockingKeyState");
  }

  public Cursor createCustomCursor(Image cursor, Point hotSpot, String name)
      throws IndexOutOfBoundsException, HeadlessException {
    throw new HeadlessException();
  }

  public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
    throw new HeadlessException();
  }

  public int getMaximumCursorColors() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean isFrameStateSupported(int state) throws HeadlessException {
    throw new HeadlessException();
  }

  public final EventQueue getSystemEventQueue() {
    return getSystemEventQueueImpl();
  }

  public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass,
      DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
    return null;
  }

  public final void setDynamicLayout(final boolean dynamic) throws HeadlessException {
    throw new HeadlessException();
  }

  protected boolean isDynamicLayoutSet() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean isDynamicLayoutActive() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean isAlwaysOnTopSupported() {
    return true;
  }

  public abstract boolean isModalityTypeSupported(Dialog.ModalityType modalityType);

  public abstract boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType modalExclusionType);

  public void addAWTEventListener(AWTEventListener listener, long eventMask) {
    if (listener != null) {
      synchronized (awtListeners) {
        awtListeners.add(listener);
      }
    }
  }

  public void removeAWTEventListener(AWTEventListener listener) {
    if (listener != null) {
      synchronized (awtListeners) {
        awtListeners.remove(listener);
      }
    }
  }

  public AWTEventListener[] getAWTEventListeners() {
    synchronized (awtListeners) {
      return awtListeners.toArray(new AWTEventListener[0]);
    }
  }

  public AWTEventListener[] getAWTEventListeners(long eventMask) {
    return getAWTEventListeners();
  }

  public abstract Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight)
      throws HeadlessException;

  public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
    throw new HeadlessException();
  }

  protected void loadSystemColors(int[] systemColors) throws HeadlessException {
    throw new HeadlessException();
  }
}
