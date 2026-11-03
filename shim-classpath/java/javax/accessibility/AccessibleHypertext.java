/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleHypertext extends javax.accessibility.AccessibleText {
  int getLinkCount();
  javax.accessibility.AccessibleHyperlink getLink(int p0);
  int getLinkIndex(int p0);
}
