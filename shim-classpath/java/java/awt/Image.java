/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ReplicateScaleFilter;

public abstract class Image {
  private static final ImageCapabilities defaultImageCaps = new ImageCapabilities(false);

  protected float accelerationPriority = .5f;

  public static final Object UndefinedProperty = new Object();

  public static final int SCALE_DEFAULT = 1;
  public static final int SCALE_FAST = 2;
  public static final int SCALE_SMOOTH = 4;
  public static final int SCALE_REPLICATE = 8;
  public static final int SCALE_AREA_AVERAGING = 16;

  public Image() { }

  public abstract int getWidth(ImageObserver observer);

  public abstract int getHeight(ImageObserver observer);

  public abstract ImageProducer getSource();

  public abstract Graphics getGraphics();

  public abstract Object getProperty(String name, ImageObserver observer);

  public Image getScaledInstance(int width, int height, int hints) {
    int sw = getWidth(null);
    int sh = getHeight(null);
    if (width < 0 && height < 0) {
      throw new IllegalArgumentException("Width and height cannot both be negative");
    }
    if (width < 0) {
      width = Math.max(1, (int) Math.round((double) height * sw / sh));
    } else if (height < 0) {
      height = Math.max(1, (int) Math.round((double) width * sh / sw));
    }
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }
    BufferedImage src = shim.awt.ImageSupport.toBuffered(this);
    return shim.awt.ImageSupport.scale(src, width, height, (hints & (SCALE_FAST | SCALE_REPLICATE)) != 0);
  }

  public void flush() {
    if (shim.awt.ImageSupport.isAccelerated(this)) {
      return;
    }
  }

  public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
    return defaultImageCaps;
  }

  public void setAccelerationPriority(float priority) {
    if (priority < 0 || priority > 1) {
      throw new IllegalArgumentException("Priority must be a value between 0 and 1, inclusive");
    }
    accelerationPriority = priority;
  }

  public float getAccelerationPriority() {
    return accelerationPriority;
  }
}
