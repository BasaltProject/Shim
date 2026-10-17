/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface Attributes {
  int getLength();

  String getURI(int p0);

  String getLocalName(int p0);

  String getQName(int p0);

  String getType(int p0);

  String getValue(int p0);

  int getIndex(String p0, String p1);

  int getIndex(String p0);

  String getType(String p0, String p1);

  String getType(String p0);

  String getValue(String p0, String p1);

  String getValue(String p0);
}
