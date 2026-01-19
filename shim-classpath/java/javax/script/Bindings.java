/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.util.Map;

public interface Bindings extends Map<String, Object> {
  Object put(String name, Object value);

  void putAll(Map<? extends String, ? extends Object> toMerge);

  boolean containsKey(Object key);

  Object get(Object key);

  Object remove(Object key);
}
