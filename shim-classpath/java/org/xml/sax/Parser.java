/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface Parser {
  void setLocale(java.util.Locale p0) throws SAXException;

  void setEntityResolver(EntityResolver p0);

  void setDTDHandler(DTDHandler p0);

  void setDocumentHandler(DocumentHandler p0);

  void setErrorHandler(ErrorHandler p0);

  void parse(InputSource p0) throws SAXException, java.io.IOException;

  void parse(String p0) throws SAXException, java.io.IOException;
}
