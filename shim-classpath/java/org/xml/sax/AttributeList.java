/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface AttributeList {
  int getLength();

  String getName(int p0);

  String getType(int p0);

  String getValue(int p0);

  String getType(String p0);

  String getValue(String p0);
}
