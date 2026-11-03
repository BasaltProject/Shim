/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.geom;

public final class GeneralPath extends Path2D.Float {
  public GeneralPath() {
    super(WIND_NON_ZERO, 20);
  }

  public GeneralPath(int rule) {
    super(rule, 20);
  }

  public GeneralPath(int rule, int initialCapacity) {
    super(rule, initialCapacity);
  }

  public GeneralPath(java.awt.Shape s) {
    super(s, null);
  }

  GeneralPath(Path2D p) {
    super(WIND_NON_ZERO, 20);
    append(p.getPathIterator(null), false);
    setWindingRule(p.getWindingRule());
  }
}
