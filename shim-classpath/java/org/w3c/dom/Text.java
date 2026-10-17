/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface Text extends CharacterData {
  Text splitText(int p0) throws DOMException;

  boolean isElementContentWhitespace();

  String getWholeText();

  Text replaceWholeText(String p0) throws DOMException;
}
