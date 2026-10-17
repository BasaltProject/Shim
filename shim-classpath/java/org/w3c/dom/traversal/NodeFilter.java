/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.traversal;

public interface NodeFilter {
  short FILTER_ACCEPT = 1;
  short FILTER_REJECT = 2;
  short FILTER_SKIP = 3;
  int SHOW_ALL = -1;
  int SHOW_ELEMENT = 1;
  int SHOW_ATTRIBUTE = 2;
  int SHOW_TEXT = 4;
  int SHOW_CDATA_SECTION = 8;
  int SHOW_ENTITY_REFERENCE = 16;
  int SHOW_ENTITY = 32;
  int SHOW_PROCESSING_INSTRUCTION = 64;
  int SHOW_COMMENT = 128;
  int SHOW_DOCUMENT = 256;
  int SHOW_DOCUMENT_TYPE = 512;
  int SHOW_DOCUMENT_FRAGMENT = 1024;
  int SHOW_NOTATION = 2048;

  short acceptNode(org.w3c.dom.Node p0);
}
