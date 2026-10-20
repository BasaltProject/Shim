/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.util.Enumeration;

public interface NamingEnumeration<T> extends Enumeration<T> {
  T next() throws NamingException;

  boolean hasMore() throws NamingException;

  void close() throws NamingException;
}
