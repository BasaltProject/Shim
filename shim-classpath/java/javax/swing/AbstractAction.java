/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.SwingPropertyChangeSupport;

public abstract class AbstractAction implements Action, Cloneable, Serializable {
  protected boolean enabled = true;
  protected SwingPropertyChangeSupport changeSupport;
  private Map<String, Object> values;

  public AbstractAction() { }

  public AbstractAction(String name) { putValue(Action.NAME, name); }

  public AbstractAction(String name, Icon icon) {
    this(name);
    putValue(Action.SMALL_ICON, icon);
  }

  public Object getValue(String key) {
    if ("enabled".equals(key)) {
      return enabled;
    }
    return values == null ? null : values.get(key);
  }

  public void putValue(String key, Object newValue) {
    Object oldValue = null;
    if ("enabled".equals(key)) {
      if (newValue == null || !(newValue instanceof Boolean)) {
        newValue = false;
      }
      oldValue = enabled;
      enabled = (Boolean) newValue;
    } else {
      if (values == null) {
        values = new LinkedHashMap<>();
      }
      oldValue = values.get(key);
      if (newValue == null) {
        values.remove(key);
      } else {
        values.put(key, newValue);
      }
    }
    firePropertyChange(key, oldValue, newValue);
  }

  public boolean isEnabled() { return enabled; }

  public void setEnabled(boolean newValue) {
    boolean oldValue = this.enabled;
    if (oldValue != newValue) {
      this.enabled = newValue;
      firePropertyChange("enabled", Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }
  }

  public Object[] getKeys() {
    if (values == null) {
      return new Object[0];
    }
    return values.keySet().toArray();
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (changeSupport == null || (oldValue != null && newValue != null && oldValue.equals(newValue))) {
      return;
    }
    changeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      changeSupport = new SwingPropertyChangeSupport(this);
    }
    changeSupport.addPropertyChangeListener(listener);
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
    if (changeSupport == null) {
      return;
    }
    changeSupport.removePropertyChangeListener(listener);
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    if (changeSupport == null) {
      return new PropertyChangeListener[0];
    }
    return changeSupport.getPropertyChangeListeners();
  }

  protected Object clone() throws CloneNotSupportedException {
    AbstractAction newAction = (AbstractAction) super.clone();
    synchronized (this) {
      if (values != null) {
        newAction.values = new LinkedHashMap<>(values);
      }
    }
    newAction.changeSupport = null;
    return newAction;
  }
}
