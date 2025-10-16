/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public interface XMLReader {
  boolean getFeature(String p0) throws SAXNotRecognizedException, SAXNotSupportedException;

  void setFeature(String p0, boolean p1) throws SAXNotRecognizedException, SAXNotSupportedException;

  Object getProperty(String p0) throws SAXNotRecognizedException, SAXNotSupportedException;

  void setProperty(String p0, Object p1) throws SAXNotRecognizedException, SAXNotSupportedException;

  void setEntityResolver(EntityResolver p0);

  EntityResolver getEntityResolver();

  void setDTDHandler(DTDHandler p0);

  DTDHandler getDTDHandler();

  void setContentHandler(ContentHandler p0);

  ContentHandler getContentHandler();

  void setErrorHandler(ErrorHandler p0);

  ErrorHandler getErrorHandler();

  void parse(InputSource p0) throws java.io.IOException, SAXException;

  void parse(String p0) throws java.io.IOException, SAXException;
}
