/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.html;

public interface HTMLCollection {
  int getLength();
  org.w3c.dom.Node item(int i);
  org.w3c.dom.Node namedItem(java.lang.String string);
}
