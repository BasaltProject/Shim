/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image.renderable;

public interface ContextualRenderedImageFactory extends java.awt.image.renderable.RenderedImageFactory {
  java.lang.Object getProperty(java.awt.image.renderable.ParameterBlock p0, java.lang.String p1);
  java.awt.image.RenderedImage create(java.awt.image.renderable.RenderContext p0, java.awt.image.renderable.ParameterBlock p1);
  java.awt.image.renderable.RenderContext mapRenderContext(int p0, java.awt.image.renderable.RenderContext p1, java.awt.image.renderable.ParameterBlock p2, java.awt.image.renderable.RenderableImage p3);
  boolean isDynamic();
  java.lang.String[] getPropertyNames();
  java.awt.geom.Rectangle2D getBounds2D(java.awt.image.renderable.ParameterBlock p0);
}
