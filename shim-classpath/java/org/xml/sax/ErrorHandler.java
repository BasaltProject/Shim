/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface ErrorHandler {
  void warning(SAXParseException p0) throws SAXException;

  void error(SAXParseException p0) throws SAXException;

  void fatalError(SAXParseException p0) throws SAXException;
}
