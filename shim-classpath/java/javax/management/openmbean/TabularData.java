/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.util.Collection;
import java.util.Set;

public interface TabularData {
  TabularType getTabularType();

  Object[] calculateIndex(CompositeData value);

  int size();

  boolean isEmpty();

  boolean containsKey(Object[] key);

  boolean containsValue(CompositeData value);

  CompositeData get(Object[] key);

  void put(CompositeData value);

  CompositeData remove(Object[] key);

  void putAll(CompositeData[] values);

  void clear();

  Set<?> keySet();

  Collection<?> values();

  @Override
  boolean equals(Object obj);

  @Override
  int hashCode();

  @Override
  String toString();
}
