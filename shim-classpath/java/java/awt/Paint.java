/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface Paint extends java.awt.Transparency {
  java.awt.PaintContext createContext(java.awt.image.ColorModel p0, java.awt.Rectangle p1, java.awt.geom.Rectangle2D p2, java.awt.geom.AffineTransform p3, java.awt.RenderingHints p4);
}
