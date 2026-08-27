/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.stylesheets;

public interface MediaList {
  void appendMedium(java.lang.String string) throws org.w3c.dom.DOMException;
  void deleteMedium(java.lang.String string) throws org.w3c.dom.DOMException;
  int getLength();
  java.lang.String getMediaText();
  java.lang.String item(int i);
  void setMediaText(java.lang.String string) throws org.w3c.dom.DOMException;
}
