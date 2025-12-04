/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PropertyChangeSupport implements Serializable {
  private static final long serialVersionUID = 6401253773779951803L;

  private final Object source;
  private final List<PropertyChangeListener> common = new ArrayList<>();
  private final Map<String, List<PropertyChangeListener>> named = new LinkedHashMap<>();

  public PropertyChangeSupport(Object sourceBean) {
    this.source = Objects.requireNonNull(sourceBean);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      addPropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    } else {
      common.add(listener);
    }
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listener instanceof PropertyChangeListenerProxy) {
      PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
      removePropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    } else {
      common.remove(listener);
    }
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    List<PropertyChangeListener> all = new ArrayList<>(common);
    for (Map.Entry<String, List<PropertyChangeListener>> e : named.entrySet()) {
      for (PropertyChangeListener l : e.getValue()) {
        all.add(new PropertyChangeListenerProxy(e.getKey(), l));
      }
    }
    return all.toArray(new PropertyChangeListener[0]);
  }

  public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (listener == null || propertyName == null) {
      return;
    }
    if (listener instanceof PropertyChangeListenerProxy) {
      listener = ((PropertyChangeListenerProxy) listener).getListener();
    }
    named.computeIfAbsent(propertyName, k -> new ArrayList<>()).add(listener);
  }

  public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (listener == null || propertyName == null) {
      return;
    }
    if (listener instanceof PropertyChangeListenerProxy) {
      listener = ((PropertyChangeListenerProxy) listener).getListener();
    }
    List<PropertyChangeListener> list = named.get(propertyName);
    if (list != null) {
      list.remove(listener);
      if (list.isEmpty()) {
        named.remove(propertyName);
      }
    }
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    List<PropertyChangeListener> list = propertyName == null ? null : named.get(propertyName);
    return list == null ? new PropertyChangeListener[0] : list.toArray(new PropertyChangeListener[0]);
  }

  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
      firePropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
    }
  }

  public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    if (oldValue != newValue) {
      firePropertyChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }
  }

  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    if (oldValue != newValue) {
      firePropertyChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }
  }

  public void firePropertyChange(PropertyChangeEvent event) {
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
    if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
      String name = event.getPropertyName();
      PropertyChangeListener[] commonCopy;
      PropertyChangeListener[] namedCopy;
      synchronized (this) {
        commonCopy = common.toArray(new PropertyChangeListener[0]);
        List<PropertyChangeListener> list = name == null ? null : named.get(name);
        namedCopy = list == null ? new PropertyChangeListener[0] : list.toArray(new PropertyChangeListener[0]);
      }
      for (PropertyChangeListener l : commonCopy) {
        l.propertyChange(event);
      }
      for (PropertyChangeListener l : namedCopy) {
        l.propertyChange(event);
      }
    }
  }

  public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
    if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
      firePropertyChange(new IndexedPropertyChangeEvent(source, propertyName, oldValue, newValue, index));
    }
  }

  public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
    if (oldValue != newValue) {
      fireIndexedPropertyChange(propertyName, index, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }
  }

  public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
    if (oldValue != newValue) {
      fireIndexedPropertyChange(propertyName, index, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }
  }

  public synchronized boolean hasListeners(String propertyName) {
    if (!common.isEmpty()) {
      return true;
    }
    List<PropertyChangeListener> list = propertyName == null ? null : named.get(propertyName);
    return list != null && !list.isEmpty();
  }
}
