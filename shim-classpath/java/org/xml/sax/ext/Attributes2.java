/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.ext;

public interface Attributes2 extends org.xml.sax.Attributes {
  boolean isDeclared(int p0);

  boolean isDeclared(String p0);

  boolean isDeclared(String p0, String p1);

  boolean isSpecified(int p0);

  boolean isSpecified(String p0, String p1);

  boolean isSpecified(String p0);
}
