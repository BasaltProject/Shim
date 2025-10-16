/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.ext;

public interface Locator2 extends org.xml.sax.Locator {
  String getXMLVersion();

  String getEncoding();
}
