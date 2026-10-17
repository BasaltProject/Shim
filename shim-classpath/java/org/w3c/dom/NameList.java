/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface NameList {
  String getName(int p0);

  String getNamespaceURI(int p0);

  int getLength();

  boolean contains(String p0);

  boolean containsNS(String p0, String p1);
}
