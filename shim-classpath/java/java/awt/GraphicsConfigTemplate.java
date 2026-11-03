/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class GraphicsConfigTemplate implements java.io.Serializable {
  public static final int REQUIRED = 1;
  public static final int PREFERRED = 2;
  public static final int UNNECESSARY = 3;
  public GraphicsConfigTemplate() {
  }
  public abstract java.awt.GraphicsConfiguration getBestConfiguration(java.awt.GraphicsConfiguration[] p0);
  public abstract boolean isGraphicsConfigSupported(java.awt.GraphicsConfiguration p0);
}
