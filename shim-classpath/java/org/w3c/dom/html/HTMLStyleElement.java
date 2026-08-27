/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLStyleElement extends org.w3c.dom.html.HTMLElement {
  boolean getDisabled();
  java.lang.String getMedia();
  java.lang.String getType();
  void setDisabled(boolean flag);
  void setMedia(java.lang.String string);
  void setType(java.lang.String string);
}
