/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface DOMLocator {
  int getLineNumber();

  int getColumnNumber();

  int getByteOffset();

  int getUtf16Offset();

  Node getRelatedNode();

  String getUri();
}
