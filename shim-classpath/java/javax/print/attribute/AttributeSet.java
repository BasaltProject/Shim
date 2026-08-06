/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

public interface AttributeSet {
  boolean remove(java.lang.Class<?> p0);
  boolean remove(javax.print.attribute.Attribute p0);
  int size();
  javax.print.attribute.Attribute get(java.lang.Class<?> p0);
  boolean equals(java.lang.Object p0);
  int hashCode();
  void clear();
  boolean isEmpty();
  boolean add(javax.print.attribute.Attribute p0);
  javax.print.attribute.Attribute[] toArray();
  boolean addAll(javax.print.attribute.AttributeSet p0);
  boolean containsValue(javax.print.attribute.Attribute p0);
  boolean containsKey(java.lang.Class<?> p0);
}
