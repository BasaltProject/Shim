/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSValueList extends org.w3c.dom.css.CSSValue {
  int getLength();
  org.w3c.dom.css.CSSValue item(int i);
}
