/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image.renderable;

public interface RenderableImage {
  java.lang.String HINTS_OBSERVED = "HINTS_OBSERVED";
  java.lang.Object getProperty(java.lang.String p0);
  boolean isDynamic();
  java.awt.image.RenderedImage createScaledRendering(int p0, int p1, java.awt.RenderingHints p2);
  java.awt.image.RenderedImage createDefaultRendering();
  java.awt.image.RenderedImage createRendering(java.awt.image.renderable.RenderContext p0);
  java.util.Vector<java.awt.image.renderable.RenderableImage> getSources();
  java.lang.String[] getPropertyNames();
  float getWidth();
  float getHeight();
  float getMinX();
  float getMinY();
}
