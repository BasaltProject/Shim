/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class EventListenerList implements Serializable {
  private static final Object[] kEmpty = new Object[0];

  protected transient Object[] listenerList = kEmpty;

  public EventListenerList() { }

  public Object[] getListenerList() { return listenerList; }

  public int getListenerCount() { return listenerList.length / 2; }

  public int getListenerCount(Class<?> t) {
    int n = 0;
    Object[] snapshot = listenerList;
    for (int i = 0; i < snapshot.length; i += 2) {
      if (snapshot[i] == t) {
        n++;
      }
    }
    return n;
  }

  @SuppressWarnings("unchecked")
  public <T extends EventListener> T[] getListeners(Class<T> t) {
    Object[] snapshot = listenerList;
    List<T> found = new ArrayList<>();
    for (int i = snapshot.length - 2; i >= 0; i -= 2) {
      if (snapshot[i] == t) {
        found.add((T) snapshot[i + 1]);
      }
    }
    return found.toArray((T[]) Array.newInstance(t, found.size()));
  }

  public synchronized <T extends EventListener> void add(Class<T> t, T l) {
    if (l == null) {
      return;
    }
    if (!t.isInstance(l)) {
      throw new IllegalArgumentException("Listener " + l + " is not of type " + t);
    }
    Object[] grown = java.util.Arrays.copyOf(listenerList, listenerList.length + 2);
    grown[grown.length - 2] = t;
    grown[grown.length - 1] = l;
    listenerList = grown;
  }

  public synchronized <T extends EventListener> void remove(Class<T> t, T l) {
    if (l == null) {
      return;
    }
    if (!t.isInstance(l)) {
      throw new IllegalArgumentException("Listener " + l + " is not of type " + t);
    }
    for (int i = listenerList.length - 2; i >= 0; i -= 2) {
      if (listenerList[i] == t && l.equals(listenerList[i + 1])) {
        Object[] shrunk = new Object[listenerList.length - 2];
        System.arraycopy(listenerList, 0, shrunk, 0, i);
        System.arraycopy(listenerList, i + 2, shrunk, i, shrunk.length - i);
        listenerList = shrunk.length == 0 ? kEmpty : shrunk;
        return;
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("EventListenerList: ").append(getListenerCount()).append(" listeners: ");
    for (int i = 0; i + 1 < listenerList.length; i += 2) {
      sb.append(" type ").append(((Class<?>) listenerList[i]).getName()).append(" listener ").append(listenerList[i + 1]);
    }
    return sb.toString();
  }
}
