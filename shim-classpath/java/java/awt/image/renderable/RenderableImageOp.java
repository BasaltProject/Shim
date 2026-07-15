/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image.renderable;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.Vector;

public class RenderableImageOp implements RenderableImage {
  ContextualRenderedImageFactory myCRIF;
  ParameterBlock paramBlock;

  public RenderableImageOp(ContextualRenderedImageFactory CRIF, ParameterBlock paramBlock) {
    this.myCRIF = CRIF;
    this.paramBlock = (ParameterBlock) paramBlock.clone();
  }

  public Vector<RenderableImage> getSources() { return getRenderableSources(); }

  private Vector<RenderableImage> getRenderableSources() {
    Vector<RenderableImage> sources = null;
    if (paramBlock.getNumSources() > 0) {
      sources = new Vector<>();
      int i = 0;
      while (i < paramBlock.getNumSources()) {
        Object o = paramBlock.getSource(i);
        if (o instanceof RenderableImage) {
          sources.add((RenderableImage) o);
          i++;
        } else {
          break;
        }
      }
    }
    return sources;
  }

  public Object getProperty(String name) { return myCRIF.getProperty(paramBlock, name); }

  public String[] getPropertyNames() { return myCRIF.getPropertyNames(); }

  public boolean isDynamic() { return myCRIF.isDynamic(); }

  public float getWidth() { return (float) myCRIF.getBounds2D(paramBlock).getWidth(); }

  public float getHeight() { return (float) myCRIF.getBounds2D(paramBlock).getHeight(); }

  public float getMinX() { return (float) myCRIF.getBounds2D(paramBlock).getMinX(); }

  public float getMinY() { return (float) myCRIF.getBounds2D(paramBlock).getMinY(); }

  public ParameterBlock setParameterBlock(ParameterBlock paramBlock) {
    ParameterBlock oldParamBlock = this.paramBlock;
    this.paramBlock = (ParameterBlock) paramBlock.clone();
    return oldParamBlock;
  }

  public ParameterBlock getParameterBlock() { return paramBlock; }

  public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) {
    if (w == 0 && h == 0) {
      throw new IllegalArgumentException("Width and height cannot both be zero.");
    }
    double sx = (double) w / getWidth();
    double sy = (double) h / getHeight();
    if (w == 0) {
      sx = sy;
    }
    if (h == 0) {
      sy = sx;
    }
    AffineTransform usr2dev = AffineTransform.getScaleInstance(sx, sy);
    RenderContext newRC = new RenderContext(usr2dev, hints);
    return createRendering(newRC);
  }

  public RenderedImage createDefaultRendering() {
    AffineTransform usr2dev = new AffineTransform();
    RenderContext newRC = new RenderContext(usr2dev);
    return createRendering(newRC);
  }

  public RenderedImage createRendering(RenderContext renderContext) {
    RenderedImage image = null;
    RenderContext rcOut = null;
    ParameterBlock renderedParamBlock = (ParameterBlock) paramBlock.clone();
    int numSources = paramBlock.getNumSources();
    if (numSources > 0) {
      Vector<Object> renderedSources = new Vector<>();
      for (int i = 0; i < numSources; i++) {
        Object o = paramBlock.getSource(i);
        if (o instanceof RenderableImage) {
          RenderableImage src = (RenderableImage) o;
          RenderContext rcIn = myCRIF.mapRenderContext(i, renderContext, paramBlock, this);
          RenderedImage r = src.createRendering(rcIn != null ? rcIn : renderContext);
          renderedSources.addElement(r);
        } else {
          renderedSources.addElement(o);
        }
      }
      renderedParamBlock.setSources(renderedSources);
    }
    return myCRIF.create(renderContext, renderedParamBlock);
  }
}
