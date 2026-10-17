/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface DTDHandler {
  void notationDecl(String p0, String p1, String p2) throws SAXException;

  void unparsedEntityDecl(String p0, String p1, String p2, String p3) throws SAXException;
}
