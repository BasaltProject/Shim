/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.ext;

public interface EntityResolver2 extends org.xml.sax.EntityResolver {
  org.xml.sax.InputSource getExternalSubset(String p0, String p1) throws org.xml.sax.SAXException, java.io.IOException;

  org.xml.sax.InputSource resolveEntity(String p0, String p1, String p2, String p3) throws org.xml.sax.SAXException,
      java.io.IOException;
}
