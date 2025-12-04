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

public class VetoableChangeSupport implements Serializable {
  private static final long serialVersionUID = -5090210921595982017L;

  private final Object source;
  private final List<VetoableChangeListener> common = new ArrayList<>();
  private final Map<String, List<VetoableChangeListener>> named = new LinkedHashMap<>();

  public VetoableChangeSupport(Object sourceBean) {
    this.source = Objects.requireNonNull(sourceBean);
  }

  public synchronized void addVetoableChangeListener(VetoableChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listener instanceof VetoableChangeListenerProxy) {
      VetoableChangeListenerProxy proxy = (VetoableChangeListenerProxy) listener;
      addVetoableChangeListener(proxy.getPropertyName(), proxy.getListener());
    } else {
      common.add(listener);
    }
  }

  public synchronized void removeVetoableChangeListener(VetoableChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listener instanceof VetoableChangeListenerProxy) {
      VetoableChangeListenerProxy proxy = (VetoableChangeListenerProxy) listener;
      removeVetoableChangeListener(proxy.getPropertyName(), proxy.getListener());
    } else {
      common.remove(listener);
    }
  }

  public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
    List<VetoableChangeListener> all = new ArrayList<>(common);
    for (Map.Entry<String, List<VetoableChangeListener>> e : named.entrySet()) {
      for (VetoableChangeListener l : e.getValue()) {
        all.add(new VetoableChangeListenerProxy(e.getKey(), l));
      }
    }
    return all.toArray(new VetoableChangeListener[0]);
  }

  public synchronized void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    if (listener == null || propertyName == null) {
      return;
    }
    if (listener instanceof VetoableChangeListenerProxy) {
      listener = ((VetoableChangeListenerProxy) listener).getListener();
    }
    named.computeIfAbsent(propertyName, k -> new ArrayList<>()).add(listener);
  }

  public synchronized void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    if (listener == null || propertyName == null) {
      return;
    }
    if (listener instanceof VetoableChangeListenerProxy) {
      listener = ((VetoableChangeListenerProxy) listener).getListener();
    }
    List<VetoableChangeListener> list = named.get(propertyName);
    if (list != null) {
      list.remove(listener);
      if (list.isEmpty()) {
        named.remove(propertyName);
      }
    }
  }

  public synchronized VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
    List<VetoableChangeListener> list = propertyName == null ? null : named.get(propertyName);
    return list == null ? new VetoableChangeListener[0] : list.toArray(new VetoableChangeListener[0]);
  }

  public void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
      throws PropertyVetoException {
    if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
      fireVetoableChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
    }
  }

  public void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException {
    if (oldValue != newValue) {
      fireVetoableChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }
  }

  public void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue)
      throws PropertyVetoException {
    if (oldValue != newValue) {
      fireVetoableChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }
  }

  public void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
    if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
      String name = event.getPropertyName();
      VetoableChangeListener[] commonCopy;
      VetoableChangeListener[] namedCopy;
      synchronized (this) {
        commonCopy = common.toArray(new VetoableChangeListener[0]);
        List<VetoableChangeListener> list = name == null ? null : named.get(name);
        namedCopy = list == null ? new VetoableChangeListener[0] : list.toArray(new VetoableChangeListener[0]);
      }
      fire(commonCopy, event);
      fire(namedCopy, event);
    }
  }

  private void fire(VetoableChangeListener[] listeners, PropertyChangeEvent event) throws PropertyVetoException {
    for (int i = 0; i < listeners.length; i++) {
      try {
        listeners[i].vetoableChange(event);
      } catch (PropertyVetoException veto) {
        PropertyChangeEvent revert = new PropertyChangeEvent(source, event.getPropertyName(), event.getNewValue(),
            event.getOldValue());
        for (int j = 0; j < i; j++) {
          try {
            listeners[j].vetoableChange(revert);
          } catch (PropertyVetoException ignored) {
          }
        }
        throw veto;
      }
    }
  }

  public synchronized boolean hasListeners(String propertyName) {
    if (!common.isEmpty()) {
      return true;
    }
    List<VetoableChangeListener> list = propertyName == null ? null : named.get(propertyName);
    return list != null && !list.isEmpty();
  }
}
