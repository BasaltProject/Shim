/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.util.Hashtable;

public class CropImageFilter extends ImageFilter {
  int cropX;
  int cropY;
  int cropW;
  int cropH;

  public CropImageFilter(int x, int y, int w, int h) {
    cropX = x;
    cropY = y;
    cropW = w;
    cropH = h;
  }

  public void setProperties(Hashtable<?, ?> props) {
    Hashtable<Object, Object> p = new Hashtable<>();
    if (props != null) {
      p.putAll(props);
    }
    p.put("croprect", new java.awt.Rectangle(cropX, cropY, cropW, cropH));
    super.setProperties(p);
  }

  public void setDimensions(int w, int h) {
    consumer.setDimensions(cropW, cropH);
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    int x1 = x;
    if (x1 < cropX) {
      x1 = cropX;
    }
    int x2 = addWithoutOverflow(x, w);
    if (x2 > cropX + cropW) {
      x2 = cropX + cropW;
    }
    int y1 = y;
    if (y1 < cropY) {
      y1 = cropY;
    }
    int y2 = addWithoutOverflow(y, h);
    if (y2 > cropY + cropH) {
      y2 = cropY + cropH;
    }
    if (x1 >= x2 || y1 >= y2) {
      return;
    }
    consumer.setPixels(x1 - cropX, y1 - cropY, (x2 - x1), (y2 - y1), model, pixels,
        off + (y1 - y) * scansize + (x1 - x), scansize);
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    int x1 = x;
    if (x1 < cropX) {
      x1 = cropX;
    }
    int x2 = addWithoutOverflow(x, w);
    if (x2 > cropX + cropW) {
      x2 = cropX + cropW;
    }
    int y1 = y;
    if (y1 < cropY) {
      y1 = cropY;
    }
    int y2 = addWithoutOverflow(y, h);
    if (y2 > cropY + cropH) {
      y2 = cropY + cropH;
    }
    if (x1 >= x2 || y1 >= y2) {
      return;
    }
    consumer.setPixels(x1 - cropX, y1 - cropY, (x2 - x1), (y2 - y1), model, pixels,
        off + (y1 - y) * scansize + (x1 - x), scansize);
  }

  private int addWithoutOverflow(int x, int w) {
    int x2 = x + w;
    if (x > 0 && w > 0 && x2 < 0) {
      x2 = Integer.MAX_VALUE;
    } else if (x < 0 && w < 0 && x2 > 0) {
      x2 = Integer.MIN_VALUE;
    }
    return x2;
  }
}
