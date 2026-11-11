/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

public class UIManager implements Serializable {
  public static class LookAndFeelInfo {
    private final String name;
    private final String className;

    public LookAndFeelInfo(String name, String className) {
      this.name = name;
      this.className = className;
    }

    public String getName() { return name; }

    public String getClassName() { return className; }

    public String toString() { return getClass().getName() + "[" + getName() + " " + getClassName() + "]"; }
  }

  private static final String kMetal = "javax.swing.plaf.metal.MetalLookAndFeel";
  private static LookAndFeelInfo[] installed = {new LookAndFeelInfo("Metal", kMetal),
      new LookAndFeelInfo("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel"),
      new LookAndFeelInfo("CDE/Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
      new LookAndFeelInfo("GTK+", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")};
  private static LookAndFeel current;
  private static final UIDefaults userDefaults = new UIDefaults();
  private static final List<LookAndFeel> auxiliary = new ArrayList<>();
  private static final PropertyChangeSupport changes = new PropertyChangeSupport(UIManager.class);

  public UIManager() { }

  public static LookAndFeelInfo[] getInstalledLookAndFeels() { return installed.clone(); }

  public static void setInstalledLookAndFeels(LookAndFeelInfo[] infos) { installed = infos.clone(); }

  public static void installLookAndFeel(LookAndFeelInfo info) {
    LookAndFeelInfo[] n = java.util.Arrays.copyOf(installed, installed.length + 1);
    n[installed.length] = info;
    installed = n;
  }

  public static void installLookAndFeel(String name, String className) {
    installLookAndFeel(new LookAndFeelInfo(name, className));
  }

  private static synchronized LookAndFeel ensure() {
    if (current == null) {
      try {
        current = (LookAndFeel) Class.forName(kMetal).getDeclaredConstructor().newInstance();
        current.initialize();
      } catch (Exception e) {
        throw new Error("Cannot create the default look and feel", e);
      }
    }
    return current;
  }

  public static LookAndFeel getLookAndFeel() { return ensure(); }

  public static void setLookAndFeel(LookAndFeel newLookAndFeel) throws UnsupportedLookAndFeelException {
    if (newLookAndFeel != null && !newLookAndFeel.isSupportedLookAndFeel()) {
      throw new UnsupportedLookAndFeelException(newLookAndFeel.toString() + " not supported on this platform");
    }
    LookAndFeel old = current;
    if (old != null) {
      old.uninitialize();
    }
    current = newLookAndFeel;
    if (newLookAndFeel != null) {
      newLookAndFeel.initialize();
    }
    changes.firePropertyChange("lookAndFeel", old, newLookAndFeel);
  }

  public static void setLookAndFeel(String className) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, UnsupportedLookAndFeelException {
    if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(className) || className.indexOf('.') >= 0) {
      try {
        setLookAndFeel((LookAndFeel) Class.forName(className, true, ClassLoader.getSystemClassLoader())
            .getDeclaredConstructor().newInstance());
      } catch (NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
        throw new InstantiationException(e.toString());
      }
    }
  }

  public static String getSystemLookAndFeelClassName() { return kMetal; }

  public static String getCrossPlatformLookAndFeelClassName() { return kMetal; }

  public static UIDefaults getDefaults() { return getLookAndFeelDefaults(); }

  public static UIDefaults getLookAndFeelDefaults() {
    UIDefaults d = ensure().getDefaults();
    return d == null ? userDefaults : d;
  }

  public static Object get(Object key) {
    Object v = userDefaults.get(key);
    return v != null ? v : getLookAndFeelDefaults().get(key);
  }

  public static Object get(Object key, Locale l) {
    Object v = userDefaults.get(key, l);
    return v != null ? v : getLookAndFeelDefaults().get(key, l);
  }

  public static Object put(Object key, Object value) { return userDefaults.put(key, value); }

  public static Font getFont(Object key) { Object v = get(key); return v instanceof Font ? (Font) v : null; }

  public static Font getFont(Object key, Locale l) { Object v = get(key, l); return v instanceof Font ? (Font) v : null; }

  public static Color getColor(Object key) { Object v = get(key); return v instanceof Color ? (Color) v : null; }

  public static Color getColor(Object key, Locale l) { Object v = get(key, l); return v instanceof Color ? (Color) v : null; }

  public static Icon getIcon(Object key) { Object v = get(key); return v instanceof Icon ? (Icon) v : null; }

  public static Icon getIcon(Object key, Locale l) { Object v = get(key, l); return v instanceof Icon ? (Icon) v : null; }

  public static Border getBorder(Object key) { Object v = get(key); return v instanceof Border ? (Border) v : null; }

  public static Border getBorder(Object key, Locale l) { Object v = get(key, l); return v instanceof Border ? (Border) v : null; }

  public static String getString(Object key) { Object v = get(key); return v instanceof String ? (String) v : null; }

  public static String getString(Object key, Locale l) { Object v = get(key, l); return v instanceof String ? (String) v : null; }

  public static int getInt(Object key) { Object v = get(key); return v instanceof Integer ? (Integer) v : 0; }

  public static int getInt(Object key, Locale l) { Object v = get(key, l); return v instanceof Integer ? (Integer) v : 0; }

  public static boolean getBoolean(Object key) { Object v = get(key); return v instanceof Boolean ? (Boolean) v : false; }

  public static boolean getBoolean(Object key, Locale l) { Object v = get(key, l); return v instanceof Boolean ? (Boolean) v : false; }

  public static Insets getInsets(Object key) { Object v = get(key); return v instanceof Insets ? (Insets) v : null; }

  public static Insets getInsets(Object key, Locale l) { Object v = get(key, l); return v instanceof Insets ? (Insets) v : null; }

  public static Dimension getDimension(Object key) { Object v = get(key); return v instanceof Dimension ? (Dimension) v : null; }

  public static Dimension getDimension(Object key, Locale l) { Object v = get(key, l); return v instanceof Dimension ? (Dimension) v : null; }

  public static ComponentUI getUI(JComponent target) {
    ComponentUI ui = getDefaults().getUI(target);
    return ui;
  }

  public static LookAndFeel createLookAndFeel(String name) throws UnsupportedLookAndFeelException {
    try {
      return (LookAndFeel) Class.forName(name).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new UnsupportedLookAndFeelException("Could not load " + name);
    }
  }

  public static void addAuxiliaryLookAndFeel(LookAndFeel laf) {
    if (!auxiliary.contains(laf)) {
      auxiliary.add(laf);
    }
  }

  public static boolean removeAuxiliaryLookAndFeel(LookAndFeel laf) { return auxiliary.remove(laf); }

  public static LookAndFeel[] getAuxiliaryLookAndFeels() {
    return auxiliary.isEmpty() ? null : auxiliary.toArray(new LookAndFeel[0]);
  }

  public static void addPropertyChangeListener(PropertyChangeListener listener) { changes.addPropertyChangeListener(listener); }

  public static void removePropertyChangeListener(PropertyChangeListener listener) { changes.removePropertyChangeListener(listener); }

  public static PropertyChangeListener[] getPropertyChangeListeners() { return changes.getPropertyChangeListeners(); }
}
