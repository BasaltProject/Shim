/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface TypeInfo {
  int DERIVATION_RESTRICTION = 1;
  int DERIVATION_EXTENSION = 2;
  int DERIVATION_UNION = 4;
  int DERIVATION_LIST = 8;

  String getTypeName();

  String getTypeNamespace();

  boolean isDerivedFrom(String p0, String p1, int p2);
}
