/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.PaintContext;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public final class TexturePaintContext implements PaintContext {
  private final BufferedImage image;
  private final double ax;
  private final double ay;
  private final double aw;
  private final double ah;
  private final AffineTransform inverse;

  public TexturePaintContext(TexturePaint p, AffineTransform xform) {
    image = p.getImage();
    Rectangle2D r = p.getAnchorRect();
    ax = r.getX();
    ay = r.getY();
    aw = r.getWidth();
    ah = r.getHeight();
    AffineTransform inv;
    try {
      inv = xform == null ? new AffineTransform() : xform.createInverse();
    } catch (NoninvertibleTransformException e) {
      inv = new AffineTransform();
    }
    inverse = inv;
  }

  public void dispose() { }

  public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }

  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster r = getColorModel().createCompatibleWritableRaster(w, h);
    int[] row = new int[w];
    double[] pt = new double[2];
    int iw = image.getWidth();
    int ih = image.getHeight();
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        pt[0] = x + i + 0.5;
        pt[1] = y + j + 0.5;
        inverse.transform(pt, 0, pt, 0, 1);
        double u = (pt[0] - ax) / aw;
        double v = (pt[1] - ay) / ah;
        int tx = (int) Math.floor((u - Math.floor(u)) * iw);
        int ty = (int) Math.floor((v - Math.floor(v)) * ih);
        if (tx >= iw) {
          tx = iw - 1;
        }
        if (ty >= ih) {
          ty = ih - 1;
        }
        row[i] = image.getRGB(tx, ty);
      }
      r.setDataElements(0, j, w, 1, row);
    }
    return r;
  }
}
