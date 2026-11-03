/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.util.EventObject;
import javax.swing.tree.TreePath;

public class TreeExpansionEvent extends EventObject {
  protected TreePath path;

  public TreeExpansionEvent(Object source, TreePath path) {
    super(source);
    this.path = path;
  }

  public TreePath getPath() { return path; }
}
