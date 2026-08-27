/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSRule {
  short UNKNOWN_RULE = (short) 0;
  short STYLE_RULE = (short) 1;
  short CHARSET_RULE = (short) 2;
  short IMPORT_RULE = (short) 3;
  short MEDIA_RULE = (short) 4;
  short FONT_FACE_RULE = (short) 5;
  short PAGE_RULE = (short) 6;

  java.lang.String getCssText();
  org.w3c.dom.css.CSSRule getParentRule();
  org.w3c.dom.css.CSSStyleSheet getParentStyleSheet();
  short getType();
  void setCssText(java.lang.String string) throws org.w3c.dom.DOMException;
}
