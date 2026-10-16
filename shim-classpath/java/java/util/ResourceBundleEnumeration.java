/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class ResourceBundleEnumeration implements Enumeration<String> {
  private final Set<String> set;
  private final Iterator<String> iterator;
  private final Enumeration<String> enumeration;
  private String next;

  ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
    this.set = set;
    this.iterator = set.iterator();
    this.enumeration = enumeration;
  }

  public boolean hasMoreElements() {
    if (next == null) {
      if (iterator.hasNext()) {
        next = iterator.next();
      } else if (enumeration != null) {
        while (next == null && enumeration.hasMoreElements()) {
          next = enumeration.nextElement();
          if (set.contains(next)) {
            next = null;
          }
        }
      }
    }
    return next != null;
  }

  public String nextElement() {
    if (hasMoreElements()) {
      String result = next;
      next = null;
      return result;
    }
    throw new NoSuchElementException();
  }
}
