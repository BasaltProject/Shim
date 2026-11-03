/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

public class ExpandVetoException extends java.lang.Exception {
  protected javax.swing.event.TreeExpansionEvent event;
  public ExpandVetoException(javax.swing.event.TreeExpansionEvent p0) {
  }
  public ExpandVetoException(javax.swing.event.TreeExpansionEvent p0, java.lang.String p1) {
    super(p1);
  }
}
