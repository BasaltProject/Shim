/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface DocumentCSS extends org.w3c.dom.stylesheets.DocumentStyle {
  org.w3c.dom.css.CSSStyleDeclaration getOverrideStyle(org.w3c.dom.Element element, java.lang.String string);
}
