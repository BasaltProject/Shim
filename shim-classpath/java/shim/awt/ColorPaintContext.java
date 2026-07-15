/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.PaintContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class ColorPaintContext implements PaintContext {
  private final int argb;
  private WritableRaster saved;

  public ColorPaintContext(int argb, ColorModel cm) {
    this.argb = argb;
  }

  public void dispose() { }

  public ColorModel getColorModel() {
    return ColorModel.getRGBdefault();
  }

  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster r = saved;
    if (r == null || r.getWidth() < w || r.getHeight() < h) {
      r = getColorModel().createCompatibleWritableRaster(w, h);
      int[] row = new int[w];
      java.util.Arrays.fill(row, argb);
      for (int j = 0; j < h; j++) {
        r.setDataElements(0, j, w, 1, row);
      }
      saved = r;
    }
    return r;
  }
}
