/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.ext;

public interface DeclHandler {
  void elementDecl(String p0, String p1) throws org.xml.sax.SAXException;

  void attributeDecl(String p0, String p1, String p2, String p3, String p4) throws org.xml.sax.SAXException;

  void internalEntityDecl(String p0, String p1) throws org.xml.sax.SAXException;

  void externalEntityDecl(String p0, String p1, String p2) throws org.xml.sax.SAXException;
}
