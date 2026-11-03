/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Point;
import java.util.EventObject;

public class DragSourceEvent extends EventObject {
  private static final long serialVersionUID = -763287114604032641L;
  private final boolean locationSpecified;
  private final int x;
  private final int y;

  public DragSourceEvent(DragSourceContext dsc) {
    super(dsc);
    locationSpecified = false;
    x = 0;
    y = 0;
  }

  public DragSourceEvent(DragSourceContext dsc, int x, int y) {
    super(dsc);
    locationSpecified = true;
    this.x = x;
    this.y = y;
  }

  public DragSourceContext getDragSourceContext() {
    return (DragSourceContext) getSource();
  }

  public Point getLocation() {
    return locationSpecified ? new Point(x, y) : null;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
