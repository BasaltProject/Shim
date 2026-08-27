/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLMetaElement extends org.w3c.dom.html.HTMLElement {
  java.lang.String getContent();
  java.lang.String getHttpEquiv();
  java.lang.String getName();
  java.lang.String getScheme();
  void setContent(java.lang.String string);
  void setHttpEquiv(java.lang.String string);
  void setName(java.lang.String string);
  void setScheme(java.lang.String string);
}
