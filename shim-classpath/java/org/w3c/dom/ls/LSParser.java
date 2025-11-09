/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSParser {
  short ACTION_APPEND_AS_CHILDREN = 1;
  short ACTION_REPLACE_CHILDREN = 2;
  short ACTION_INSERT_BEFORE = 3;
  short ACTION_INSERT_AFTER = 4;
  short ACTION_REPLACE = 5;

  org.w3c.dom.DOMConfiguration getDomConfig();

  LSParserFilter getFilter();

  void setFilter(LSParserFilter p0);

  boolean getAsync();

  boolean getBusy();

  org.w3c.dom.Document parse(LSInput p0) throws org.w3c.dom.DOMException, LSException;

  org.w3c.dom.Document parseURI(String p0) throws org.w3c.dom.DOMException, LSException;

  org.w3c.dom.Node parseWithContext(LSInput p0, org.w3c.dom.Node p1, short p2) throws org.w3c.dom.DOMException,
      LSException;

  void abort();
}
