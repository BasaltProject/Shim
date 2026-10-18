/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

public class BeansChangeModern {
  static final String SOURCE = "bean";

  static PropertyChangeListener logger(String tag) {
    return e -> System.out.println(tag + " " + e.getPropertyName() + " " + e.getOldValue() + "->" + e.getNewValue()
        + (e instanceof IndexedPropertyChangeEvent ? " @" + ((IndexedPropertyChangeEvent) e).getIndex() : ""));
  }

  public static void main(String[] args) throws Exception {
    PropertyChangeSupport support = new PropertyChangeSupport(SOURCE);
    support.addPropertyChangeListener(logger("all"));
    support.addPropertyChangeListener("size", logger("size"));
    support.addPropertyChangeListener(new PropertyChangeListenerProxy("color", logger("color")));
    support.firePropertyChange("size", 1, 2);
    support.firePropertyChange("size", 2, 2);
    support.firePropertyChange("color", "red", "blue");
    support.firePropertyChange("flag", false, true);
    support.firePropertyChange("nothing", null, null);
    support.firePropertyChange("onull", null, "x");
    support.fireIndexedPropertyChange("items", 3, "a", "b");
    support.fireIndexedPropertyChange("items", 4, 5, 5);
    System.out.println(support.getPropertyChangeListeners().length + " "
        + support.getPropertyChangeListeners("size").length + " " + support.getPropertyChangeListeners("none").length);
    System.out.println(support.hasListeners("size") + " " + support.hasListeners(null));
    for (PropertyChangeListener l : support.getPropertyChangeListeners()) {
      boolean proxy = l instanceof PropertyChangeListenerProxy;
      System.out.println(proxy ? ((PropertyChangeListenerProxy) l).getPropertyName() : "-");
    }
    PropertyChangeEvent event = new PropertyChangeEvent(SOURCE, "p", 1, 2);
    event.setPropagationId("id");
    System.out.println(event.toString().replaceAll("@[0-9a-f]+", ""));
    System.out.println(new IndexedPropertyChangeEvent(SOURCE, "p", 1, 2, 7).toString().replaceAll("@[0-9a-f]+", ""));

    VetoableChangeSupport veto = new VetoableChangeSupport(SOURCE);
    VetoableChangeListener ok = e -> System.out.println("ok " + e.getOldValue() + "->" + e.getNewValue());
    VetoableChangeListener no = e -> {
      System.out.println("no " + e.getOldValue() + "->" + e.getNewValue());
      throw new PropertyVetoException("rejected", e);
    };
    veto.addVetoableChangeListener(ok);
    veto.addVetoableChangeListener(no);
    veto.addVetoableChangeListener(ok);
    try {
      veto.fireVetoableChange("v", 1, 2);
    } catch (PropertyVetoException e) {
      System.out.println(e.getMessage() + " " + e.getPropertyChangeEvent().getPropertyName());
    }
    veto.fireVetoableChange("v", 3, 3);
    try {
      new PropertyChangeSupport(null);
    } catch (NullPointerException e) {
      System.out.println("npe");
    }
  }
}
