/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLElement extends org.w3c.dom.Element {
  java.lang.String getClassName();
  java.lang.String getDir();
  java.lang.String getId();
  java.lang.String getLang();
  java.lang.String getTitle();
  void setClassName(java.lang.String string);
  void setDir(java.lang.String string);
  void setId(java.lang.String string);
  void setLang(java.lang.String string);
  void setTitle(java.lang.String string);
}
