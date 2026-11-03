/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

public abstract class AbstractMultiResolutionImage extends Image implements MultiResolutionImage {
  public AbstractMultiResolutionImage() { }

  public int getWidth(ImageObserver observer) { return getBaseImage().getWidth(observer); }

  public int getHeight(ImageObserver observer) { return getBaseImage().getHeight(observer); }

  public ImageProducer getSource() { return getBaseImage().getSource(); }

  public Graphics getGraphics() {
    throw new UnsupportedOperationException("getGraphics() not supported on Multi-Resolution Images");
  }

  public Object getProperty(String name, ImageObserver observer) { return getBaseImage().getProperty(name, observer); }

  protected abstract Image getBaseImage();

  public abstract Image getResolutionVariant(double destImageWidth, double destImageHeight);

  public abstract List<Image> getResolutionVariants();
}
