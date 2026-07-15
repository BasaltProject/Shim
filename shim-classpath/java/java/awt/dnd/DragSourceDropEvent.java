/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

public class DragSourceDropEvent extends DragSourceEvent {
  private static final long serialVersionUID = -5571321229470821891L;
  private final boolean dropSuccess;
  private final int dropAction;

  public DragSourceDropEvent(DragSourceContext dsc, int action, boolean success) {
    super(dsc);
    dropSuccess = success;
    dropAction = action;
  }

  public DragSourceDropEvent(DragSourceContext dsc, int action, boolean success, int x, int y) {
    super(dsc, x, y);
    dropSuccess = success;
    dropAction = action;
  }

  public DragSourceDropEvent(DragSourceContext dsc) {
    super(dsc);
    dropSuccess = false;
    dropAction = DnDConstants.ACTION_NONE;
  }

  public boolean getDropSuccess() {
    return dropSuccess;
  }

  public int getDropAction() {
    return dropAction;
  }
}
