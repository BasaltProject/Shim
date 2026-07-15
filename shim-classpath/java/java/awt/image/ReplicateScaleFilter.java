/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.util.Hashtable;

public class ReplicateScaleFilter extends ImageFilter {
  protected int srcWidth;
  protected int srcHeight;
  protected int destWidth;
  protected int destHeight;
  protected int[] srccols;
  protected int[] srcrows;
  protected Object outpixbuf;

  public ReplicateScaleFilter(int width, int height) {
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Width (" + width + ") and height (" + height + ") must be non-zero");
    }
    destWidth = width;
    destHeight = height;
  }

  public void setProperties(Hashtable<?, ?> props) {
    Hashtable<Object, Object> p = new Hashtable<>();
    if (props != null) {
      p.putAll(props);
    }
    String key = "rescale";
    String val = destWidth + "x" + destHeight;
    Object o = p.get(key);
    if (o != null && o instanceof String) {
      val = ((String) o) + ", " + val;
    }
    p.put(key, val);
    super.setProperties(p);
  }

  public void setDimensions(int w, int h) {
    srcWidth = w;
    srcHeight = h;
    if (destWidth < 0) {
      if (destHeight < 0) {
        destWidth = srcWidth;
        destHeight = srcHeight;
      } else {
        destWidth = srcWidth * destHeight / srcHeight;
      }
    } else if (destHeight < 0) {
      destHeight = srcHeight * destWidth / srcWidth;
    }
    consumer.setDimensions(destWidth, destHeight);
  }

  private void calculateMaps() {
    srccols = new int[destWidth];
    for (int x = 0; x < destWidth; x++) {
      srccols[x] = (2 * x * srcWidth + srcWidth) / (2 * destWidth);
    }
    srcrows = new int[destHeight];
    for (int y = 0; y < destHeight; y++) {
      srcrows[y] = (2 * y * srcHeight + srcHeight) / (2 * destHeight);
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    if (srccols == null) {
      calculateMaps();
    }
    int sx = x + w;
    int sy = y + h;
    for (int dy = 0; dy < destHeight; dy++) {
      int srow = srcrows[dy];
      if (srow < y || srow >= sy) {
        continue;
      }
      byte[] row = new byte[destWidth];
      int count = 0;
      int first = -1;
      for (int dx = 0; dx < destWidth; dx++) {
        int scol = srccols[dx];
        if (scol >= x && scol < sx) {
          if (first < 0) {
            first = dx;
          }
          row[count++] = pixels[off + (srow - y) * scansize + (scol - x)];
        }
      }
      if (count > 0) {
        consumer.setPixels(first, dy, count, 1, model, row, 0, destWidth);
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    if (srccols == null) {
      calculateMaps();
    }
    int sx = x + w;
    int sy = y + h;
    for (int dy = 0; dy < destHeight; dy++) {
      int srow = srcrows[dy];
      if (srow < y || srow >= sy) {
        continue;
      }
      int[] row = new int[destWidth];
      int count = 0;
      int first = -1;
      for (int dx = 0; dx < destWidth; dx++) {
        int scol = srccols[dx];
        if (scol >= x && scol < sx) {
          if (first < 0) {
            first = dx;
          }
          row[count++] = pixels[off + (srow - y) * scansize + (scol - x)];
        }
      }
      if (count > 0) {
        consumer.setPixels(first, dy, count, 1, model, row, 0, destWidth);
      }
    }
  }
}
