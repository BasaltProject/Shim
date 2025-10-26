/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.util.Collection;

public interface CompositeData {
  CompositeType getCompositeType();

  Object get(String key);

  Object[] getAll(String[] keys);

  boolean containsKey(String key);

  boolean containsValue(Object value);

  Collection<?> values();

  boolean equals(Object obj);

  int hashCode();

  String toString();
}
