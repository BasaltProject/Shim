/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.AlphaComposite;
import java.awt.CompositeContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class AlphaCompositeContext implements CompositeContext {
  private final int rule;
  private final float extra;
  private final ColorModel srcCm;
  private final ColorModel dstCm;

  public AlphaCompositeContext(AlphaComposite c, ColorModel src, ColorModel dst) {
    rule = c.getRule();
    extra = c.getAlpha();
    srcCm = src;
    dstCm = dst;
  }

  public void dispose() { }

  public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
    int w = Math.min(src.getWidth(), dstIn.getWidth());
    int h = Math.min(src.getHeight(), dstIn.getHeight());
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int s = srcCm.getRGB(src.getDataElements(src.getMinX() + x, src.getMinY() + y, null));
        int d = dstCm.getRGB(dstIn.getDataElements(dstIn.getMinX() + x, dstIn.getMinY() + y, null));
        int r = Blend.compose(rule, s, d, extra);
        dstOut.setDataElements(dstOut.getMinX() + x, dstOut.getMinY() + y, dstCm.getDataElements(r, null));
      }
    }
  }
}
