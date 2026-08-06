/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.undo;

public interface StateEditable {
  java.lang.String RCSID = "$Id: StateEditable.java,v 1.2 1997/09/08 19:39:08 marklin Exp $";
  void storeState(java.util.Hashtable<java.lang.Object, java.lang.Object> p0);
  void restoreState(java.util.Hashtable<?, ?> p0);
}
