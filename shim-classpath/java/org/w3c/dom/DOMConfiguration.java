/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface DOMConfiguration {
  void setParameter(String p0, Object p1) throws DOMException;

  Object getParameter(String p0) throws DOMException;

  boolean canSetParameter(String p0, Object p1);

  DOMStringList getParameterNames();
}
