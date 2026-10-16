/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

public abstract class ListResourceBundle extends ResourceBundle {
  private volatile Map<String, Object> lookup;

  public ListResourceBundle() { }

  public final Object handleGetObject(String key) {
    if (lookup == null) {
      loadLookup();
    }
    if (key == null) {
      throw new NullPointerException();
    }
    return lookup.get(key);
  }

  public Enumeration<String> getKeys() {
    if (lookup == null) {
      loadLookup();
    }
    ResourceBundle parent = this.parent;
    return new ResourceBundleEnumeration(lookup.keySet(), parent != null ? parent.getKeys() : null);
  }

  protected Set<String> handleKeySet() {
    if (lookup == null) {
      loadLookup();
    }
    return lookup.keySet();
  }

  protected abstract Object[][] getContents();

  private synchronized void loadLookup() {
    if (lookup != null) {
      return;
    }
    Object[][] contents = getContents();
    HashMap<String, Object> temp = new HashMap<String, Object>(contents.length);
    for (Object[] content : contents) {
      String key = (String) content[0];
      Object value = content[1];
      if (key == null || value == null) {
        throw new NullPointerException();
      }
      temp.put(key, value);
    }
    lookup = temp;
  }
}
