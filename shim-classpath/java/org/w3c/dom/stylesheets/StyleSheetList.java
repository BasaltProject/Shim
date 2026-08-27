/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.stylesheets;

public interface StyleSheetList {
  int getLength();
  org.w3c.dom.stylesheets.StyleSheet item(int i);
}
