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
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

public class UIDefaults extends Hashtable<Object, Object> {
  public interface ActiveValue {
    Object createValue(UIDefaults table);
  }

  public interface LazyValue {
    Object createValue(UIDefaults table);
  }

  public static class LazyInputMap implements LazyValue {
    private final Object[] bindings;

    public LazyInputMap(Object[] bindings) {
      this.bindings = bindings;
    }

    public Object createValue(UIDefaults table) {
      return bindings == null ? null : LookAndFeel.makeInputMap(bindings);
    }
  }

  public static class ProxyLazyValue implements LazyValue {
    private final String className;
    private final String methodName;
    private final Object[] args;

    public ProxyLazyValue(String c) {
      this(c, (String) null);
    }

    public ProxyLazyValue(String c, String m) {
      this(c, m, null);
    }

    public ProxyLazyValue(String c, Object[] o) {
      this(c, null, o);
    }

    public ProxyLazyValue(String c, String m, Object[] o) {
      className = c;
      methodName = m;
      args = o;
    }

    public Object createValue(UIDefaults table) {
      try {
        Class<?> cls = Class.forName(className, true, null == null ? ClassLoader.getSystemClassLoader() : null);
        if (methodName == null) {
          if (args == null) {
            return cls.getDeclaredConstructor().newInstance();
          }
          for (java.lang.reflect.Constructor<?> k : cls.getConstructors()) {
            if (k.getParameterCount() == args.length) {
              return k.newInstance(args);
            }
          }
          return null;
        }
        for (java.lang.reflect.Method m : cls.getMethods()) {
          if (m.getName().equals(methodName) && m.getParameterCount() == (args == null ? 0 : args.length)) {
            return m.invoke(null, args);
          }
        }
      } catch (Exception e) {
        // fall through
      }
      return null;
    }
  }

  private static final Object kPendingValue = new Object();

  private Vector<String> resourceBundles;
  private Locale defaultLocale = Locale.getDefault();
  private PropertyChangeSupport changeSupport;

  public UIDefaults() {
    super(700, .75f);
  }

  public UIDefaults(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public UIDefaults(Object[] keyValueList) {
    super(keyValueList.length / 2);
    for (int i = 0; i < keyValueList.length; i += 2) {
      super.put(keyValueList[i], keyValueList[i + 1]);
    }
  }

  public Object get(Object key) {
    Object value = getFromHashtable(key);
    return value != null ? value : getFromResourceBundle(key, null);
  }

  private Object getFromHashtable(Object key) {
    Object value = super.get(key);
    if (value instanceof ActiveValue) {
      return ((ActiveValue) value).createValue(this);
    }
    if (value instanceof LazyValue) {
      Object created = ((LazyValue) value).createValue(this);
      if (created != null) {
        super.put(key, created);
      }
      return created;
    }
    return value;
  }

  private Object getFromResourceBundle(Object key, Locale l) {
    if (resourceBundles == null || resourceBundles.isEmpty() || !(key instanceof String)) {
      return null;
    }
    if (l == null) {
      l = defaultLocale;
    }
    for (int i = resourceBundles.size() - 1; i >= 0; i--) {
      try {
        return ResourceBundle.getBundle(resourceBundles.get(i), l).getObject((String) key);
      } catch (Exception e) {
        // try next
      }
    }
    return null;
  }

  public Object get(Object key, Locale l) {
    Object value = getFromHashtable(key);
    return value != null ? value : getFromResourceBundle(key, l);
  }

  public Object put(Object key, Object value) {
    Object oldValue = value == null ? super.remove(key) : super.put(key, value);
    if (key instanceof String) {
      firePropertyChange((String) key, oldValue, value);
    }
    return oldValue;
  }

  public void putDefaults(Object[] keyValueList) {
    for (int i = 0, max = keyValueList.length; i < max; i += 2) {
      Object value = keyValueList[i + 1];
      if (value == null) {
        super.remove(keyValueList[i]);
      } else {
        super.put(keyValueList[i], value);
      }
    }
    firePropertyChange("UIDefaults", null, null);
  }

  public Font getFont(Object key) {
    Object value = get(key);
    return (value instanceof Font) ? (Font) value : null;
  }

  public Font getFont(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Font) ? (Font) value : null;
  }

  public Color getColor(Object key) {
    Object value = get(key);
    return (value instanceof Color) ? (Color) value : null;
  }

  public Color getColor(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Color) ? (Color) value : null;
  }

  public Icon getIcon(Object key) {
    Object value = get(key);
    return (value instanceof Icon) ? (Icon) value : null;
  }

  public Icon getIcon(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Icon) ? (Icon) value : null;
  }

  public Border getBorder(Object key) {
    Object value = get(key);
    return (value instanceof Border) ? (Border) value : null;
  }

  public Border getBorder(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Border) ? (Border) value : null;
  }

  public String getString(Object key) {
    Object value = get(key);
    return (value instanceof String) ? (String) value : null;
  }

  public String getString(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof String) ? (String) value : null;
  }

  public int getInt(Object key) {
    Object value = get(key);
    return (value instanceof Integer) ? ((Integer) value).intValue() : 0;
  }

  public int getInt(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Integer) ? ((Integer) value).intValue() : 0;
  }

  public boolean getBoolean(Object key) {
    Object value = get(key);
    return (value instanceof Boolean) ? ((Boolean) value).booleanValue() : false;
  }

  public boolean getBoolean(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Boolean) ? ((Boolean) value).booleanValue() : false;
  }

  public Insets getInsets(Object key) {
    Object value = get(key);
    return (value instanceof Insets) ? (Insets) value : null;
  }

  public Insets getInsets(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Insets) ? (Insets) value : null;
  }

  public Dimension getDimension(Object key) {
    Object value = get(key);
    return (value instanceof Dimension) ? (Dimension) value : null;
  }

  public Dimension getDimension(Object key, Locale l) {
    Object value = get(key, l);
    return (value instanceof Dimension) ? (Dimension) value : null;
  }

  public Class<? extends ComponentUI> getUIClass(String uiClassID, ClassLoader uiClassLoader) {
    try {
      String className = (String) get(uiClassID);
      if (className != null) {
        if (uiClassLoader == null) {
          uiClassLoader = ClassLoader.getSystemClassLoader();
        }
        @SuppressWarnings("unchecked")
        Class<? extends ComponentUI> found = (Class<? extends ComponentUI>) Class.forName(className, true,
            uiClassLoader);
        return found;
      }
    } catch (ClassNotFoundException e) {
      return null;
    } catch (ClassCastException e) {
      return null;
    }
    return null;
  }

  public Class<? extends ComponentUI> getUIClass(String uiClassID) {
    return getUIClass(uiClassID, null);
  }

  protected void getUIError(String msg) {
    System.err.println("UIDefaults.getUI() failed: " + msg);
  }

  public ComponentUI getUI(JComponent target) {
    Object cl = get("ClassLoader");
    ClassLoader uiClassLoader = (cl != null) ? (ClassLoader) cl : target.getClass().getClassLoader();
    Class<? extends ComponentUI> uiClass = getUIClass(target.getUIClassID(), uiClassLoader);
    if (uiClass == null) {
      return null;
    }
    try {
      java.lang.reflect.Method m = uiClass.getMethod("createUI", JComponent.class);
      return (ComponentUI) m.invoke(null, target);
    } catch (Exception e) {
      getUIError("failed to create UI for " + target.getUIClassID());
      return null;
    }
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      changeSupport = new PropertyChangeSupport(this);
    }
    changeSupport.addPropertyChangeListener(listener);
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport != null) {
      changeSupport.removePropertyChangeListener(listener);
    }
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    if (changeSupport == null) {
      return new PropertyChangeListener[0];
    }
    return changeSupport.getPropertyChangeListeners();
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (changeSupport != null) {
      changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public synchronized void addResourceBundle(String bundleName) {
    if (bundleName == null) {
      return;
    }
    if (resourceBundles == null) {
      resourceBundles = new Vector<>(5);
    }
    if (!resourceBundles.contains(bundleName)) {
      resourceBundles.add(bundleName);
    }
  }

  public synchronized void removeResourceBundle(String bundleName) {
    if (resourceBundles != null) {
      resourceBundles.remove(bundleName);
    }
  }

  public void setDefaultLocale(Locale l) { defaultLocale = l; }

  public Locale getDefaultLocale() { return defaultLocale; }
}
