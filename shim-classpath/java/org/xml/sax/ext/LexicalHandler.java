/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.ext;

public interface LexicalHandler {
  void startDTD(String p0, String p1, String p2) throws org.xml.sax.SAXException;

  void endDTD() throws org.xml.sax.SAXException;

  void startEntity(String p0) throws org.xml.sax.SAXException;

  void endEntity(String p0) throws org.xml.sax.SAXException;

  void startCDATA() throws org.xml.sax.SAXException;

  void endCDATA() throws org.xml.sax.SAXException;

  void comment(char[] p0, int p1, int p2) throws org.xml.sax.SAXException;
}
