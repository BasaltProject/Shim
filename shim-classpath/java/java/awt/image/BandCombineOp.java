/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class BandCombineOp implements RasterOp {
  float[][] matrix;
  int nrows = 0;
  int ncols = 0;
  RenderingHints hints;

  public BandCombineOp(float[][] matrix, RenderingHints hints) {
    nrows = matrix.length;
    ncols = matrix[0].length;
    this.matrix = new float[nrows][];
    for (int i = 0; i < nrows; i++) {
      if (ncols > matrix[i].length) {
        throw new IndexOutOfBoundsException("row " + i + " too short");
      }
      this.matrix[i] = java.util.Arrays.copyOf(matrix[i], ncols);
    }
    this.hints = hints;
  }

  public final float[][] getMatrix() {
    float[][] ret = new float[nrows][];
    for (int i = 0; i < nrows; i++) {
      ret[i] = java.util.Arrays.copyOf(matrix[i], ncols);
    }
    return ret;
  }

  public WritableRaster filter(Raster src, WritableRaster dst) {
    int nBands = src.getNumBands();
    if (ncols != nBands && ncols != (nBands + 1)) {
      throw new IllegalArgumentException("Number of columns in the matrix (" + ncols + ") must be equal to the number of bands ([+1]) in src (" + nBands + ").");
    }
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    } else if (nrows != dst.getNumBands()) {
      throw new IllegalArgumentException("Number of rows in the matrix (" + nrows + ") must be equal to the number of bands ([+1]) in dst (" + nBands + ").");
    }
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        for (int r = 0; r < nrows; r++) {
          float sum = ncols > nBands ? matrix[r][nBands] : 0f;
          for (int c = 0; c < nBands; c++) {
            sum += matrix[r][c] * src.getSample(src.getMinX() + x, src.getMinY() + y, c);
          }
          int max = shim.awt.OpSupport.maxSample(dst, r);
          int v = (int) sum;
          dst.setSample(dst.getMinX() + x, dst.getMinY() + y, r, v < 0 ? 0 : v > max ? max : v);
        }
      }
    }
    return dst;
  }

  public final Rectangle2D getBounds2D(Raster src) { return src.getBounds(); }

  public WritableRaster createCompatibleDestRaster(Raster src) {
    int nBands = src.getNumBands();
    if ((ncols != nBands) && (ncols != (nBands + 1))) {
      throw new IllegalArgumentException("Number of columns in the matrix (" + ncols + ") must be equal to the number of bands ([+1]) in src (" + nBands + ").");
    }
    if (src.getNumBands() == nrows) {
      return src.createCompatibleWritableRaster();
    }
    throw new IllegalArgumentException("Don't know how to create a  compatible Raster with " + nrows + " bands.");
  }

  public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return shim.awt.OpSupport.point(srcPt, dstPt); }

  public final RenderingHints getRenderingHints() { return hints; }
}
