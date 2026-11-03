/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.util.EventObject;

public class DropTargetEvent extends EventObject {
  private static final long serialVersionUID = 2821229066521922993L;
  protected DropTargetContext context;

  public DropTargetEvent(DropTargetContext dtc) {
    super(dtc.getDropTarget());
    context = dtc;
  }

  public DropTargetContext getDropTargetContext() {
    return context;
  }
}
