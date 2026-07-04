/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface Composite {
  java.awt.CompositeContext createContext(java.awt.image.ColorModel p0, java.awt.image.ColorModel p1, java.awt.RenderingHints p2);
}
