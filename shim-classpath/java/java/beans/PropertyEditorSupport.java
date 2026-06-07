/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.*;
import java.lang.reflect.*;

public class PropertyEditorSupport implements java.beans.PropertyEditor {
  private Object value;
  private Object source;
  private java.util.Vector<PropertyChangeListener> listeners;
  public PropertyEditorSupport() {
    setSource(this);
  }
  public PropertyEditorSupport(java.lang.Object p0) {
    if (p0 == null) {
      throw new NullPointerException();
    }
    setSource(p0);
  }
  public java.lang.Object getValue() {
    return value;
  }
  public void setValue(java.lang.Object p0) {
    this.value = p0;
    firePropertyChange();
  }
  public boolean isPaintable() {
    return false;
  }
  public void paintValue(java.awt.Graphics p0, java.awt.Rectangle p1) {
    
  }
  public java.lang.String getJavaInitializationString() {
    return "???";
  }
  public java.lang.String getAsText() {
    return (this.value != null) ? this.value.toString() : null;
  }
  public void setAsText(java.lang.String p0) throws java.lang.IllegalArgumentException {
    if (value instanceof String) {
      setValue(p0);
      return;
    }
    throw new java.lang.IllegalArgumentException(p0);
  }
  public java.lang.String[] getTags() {
    return null;
  }
  public java.awt.Component getCustomEditor() {
    return null;
  }
  public boolean supportsCustomEditor() {
    return false;
  }
  public java.lang.Object getSource() {
    return source;
  }
  public void firePropertyChange() {
    java.util.Vector<PropertyChangeListener> targets;
    synchronized (this) {
      if (listeners == null) {
        return;
      }
      targets = new java.util.Vector<>(listeners);
    }
    PropertyChangeEvent evt = new PropertyChangeEvent(source, null, null, null);
    for (int i = 0; i < targets.size(); i++) {
      targets.elementAt(i).propertyChange(evt);
    }
  }
  public void addPropertyChangeListener(java.beans.PropertyChangeListener p0) {
    synchronized (this) {
      if (listeners == null) {
        listeners = new java.util.Vector<>();
      }
      listeners.addElement(p0);
    }
  }
  public void removePropertyChangeListener(java.beans.PropertyChangeListener p0) {
    synchronized (this) {
      if (listeners == null) {
        return;
      }
      listeners.removeElement(p0);
    }
  }
  public void setSource(java.lang.Object p0) {
    source = p0;
  }
}
