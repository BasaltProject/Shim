/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSSerializer {
  org.w3c.dom.DOMConfiguration getDomConfig();

  String getNewLine();

  void setNewLine(String p0);

  LSSerializerFilter getFilter();

  void setFilter(LSSerializerFilter p0);

  boolean write(org.w3c.dom.Node p0, LSOutput p1) throws LSException;

  boolean writeToURI(org.w3c.dom.Node p0, String p1) throws LSException;

  String writeToString(org.w3c.dom.Node p0) throws org.w3c.dom.DOMException, LSException;
}
