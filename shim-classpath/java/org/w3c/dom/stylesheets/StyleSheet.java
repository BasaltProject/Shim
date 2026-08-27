/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.stylesheets;

public interface StyleSheet {
  boolean getDisabled();
  java.lang.String getHref();
  org.w3c.dom.stylesheets.MediaList getMedia();
  org.w3c.dom.Node getOwnerNode();
  org.w3c.dom.stylesheets.StyleSheet getParentStyleSheet();
  java.lang.String getTitle();
  java.lang.String getType();
  void setDisabled(boolean flag);
}
