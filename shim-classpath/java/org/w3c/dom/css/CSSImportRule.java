/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSImportRule extends org.w3c.dom.css.CSSRule {
  java.lang.String getHref();
  org.w3c.dom.stylesheets.MediaList getMedia();
  org.w3c.dom.css.CSSStyleSheet getStyleSheet();
}
