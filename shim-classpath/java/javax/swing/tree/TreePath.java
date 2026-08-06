/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.io.Serializable;
import java.util.Arrays;

public class TreePath implements Serializable {
  private Object[] components;

  protected TreePath() { }

  public TreePath(Object singleComponent) {
    this(new Object[] {singleComponent});
  }

  public TreePath(Object[] path) {
    if (path == null || path.length == 0) {
      throw new IllegalArgumentException("path in TreePath must be non null and not empty.");
    }
    for (Object o : path) {
      if (o == null) {
        throw new IllegalArgumentException("Path elements must be non-null");
      }
    }
    components = path.clone();
  }

  protected TreePath(TreePath parent, Object last) {
    if (last == null) {
      throw new IllegalArgumentException("path in TreePath must be non null.");
    }
    Object[] base = parent == null ? new Object[0] : parent.components;
    components = Arrays.copyOf(base, base.length + 1);
    components[base.length] = last;
  }

  protected TreePath(Object[] path, int length) {
    components = Arrays.copyOf(path, length);
    if (components[length - 1] == null) {
      throw new IllegalArgumentException("Path elements must be non-null");
    }
  }

  public Object[] getPath() { return components.clone(); }

  public Object getLastPathComponent() { return components[components.length - 1]; }

  public int getPathCount() { return components.length; }

  public Object getPathComponent(int index) {
    if (index < 0 || index >= components.length) {
      throw new IllegalArgumentException("Index " + index + " is out of the specified range");
    }
    return components[index];
  }

  public TreePath getParentPath() {
    return components.length > 1 ? new TreePath(components, components.length - 1) : null;
  }

  public TreePath pathByAddingChild(Object child) {
    if (child == null) {
      throw new NullPointerException("Null child not allowed");
    }
    return new TreePath(this, child);
  }

  public boolean isDescendant(TreePath other) {
    if (other == null || other.components.length < components.length) {
      return false;
    }
    for (int i = 0; i < components.length; i++) {
      if (!components[i].equals(other.components[i])) {
        return false;
      }
    }
    return true;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof TreePath && Arrays.equals(components, ((TreePath) o).components);
  }

  public int hashCode() { return getLastPathComponent().hashCode(); }

  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < components.length; i++) {
      sb.append(i > 0 ? ", " : "").append(components[i]);
    }
    return sb.append(']').toString();
  }
}
