/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

public class DefaultComboBoxModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>, Serializable {
  Vector<E> objects;
  Object selectedObject;

  public DefaultComboBoxModel() {
    objects = new Vector<>();
  }

  public DefaultComboBoxModel(final E[] items) {
    objects = new Vector<>(items.length);
    int i;
    int c;
    for (i = 0, c = items.length; i < c; i++) {
      objects.addElement(items[i]);
    }
    if (getSize() > 0) {
      selectedObject = getElementAt(0);
    }
  }

  public DefaultComboBoxModel(Vector<E> v) {
    objects = v;
    if (getSize() > 0) {
      selectedObject = getElementAt(0);
    }
  }

  public void setSelectedItem(Object anObject) {
    if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null) {
      selectedObject = anObject;
      fireContentsChanged(this, -1, -1);
    }
  }

  public Object getSelectedItem() { return selectedObject; }

  public int getSize() { return objects.size(); }

  public E getElementAt(int index) {
    if (index >= 0 && index < objects.size()) {
      return objects.elementAt(index);
    }
    return null;
  }

  public int getIndexOf(Object anObject) { return objects.indexOf(anObject); }

  public void addElement(E anObject) {
    objects.addElement(anObject);
    fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
    if (objects.size() == 1 && selectedObject == null && anObject != null) {
      setSelectedItem(anObject);
    }
  }

  public void insertElementAt(E anObject, int index) {
    objects.insertElementAt(anObject, index);
    fireIntervalAdded(this, index, index);
  }

  public void removeElementAt(int index) {
    if (getElementAt(index) == selectedObject) {
      if (index == 0) {
        setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
      } else {
        setSelectedItem(getElementAt(index - 1));
      }
    }
    objects.removeElementAt(index);
    fireIntervalRemoved(this, index, index);
  }

  public void removeElement(Object anObject) {
    int index = objects.indexOf(anObject);
    if (index != -1) {
      removeElementAt(index);
    }
  }

  public void removeAllElements() {
    if (objects.size() > 0) {
      int firstIndex = 0;
      int lastIndex = objects.size() - 1;
      objects.removeAllElements();
      selectedObject = null;
      fireIntervalRemoved(this, firstIndex, lastIndex);
    } else {
      selectedObject = null;
    }
  }

  public void addAll(Collection<? extends E> c) {
    if (c.isEmpty()) {
      return;
    }
    int startIndex = getSize();
    objects.addAll(c);
    fireIntervalAdded(this, startIndex, getSize() - 1);
  }

  public void addAll(int index, Collection<? extends E> c) {
    if (index < 0 || index > getSize()) {
      throw new ArrayIndexOutOfBoundsException("index out of range: " + index);
    }
    if (c.isEmpty()) {
      return;
    }
    objects.addAll(index, c);
    fireIntervalAdded(this, index, index + c.size() - 1);
  }
}
