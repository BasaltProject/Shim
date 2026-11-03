/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public interface ImageConsumer {
  int RANDOMPIXELORDER = 1;
  int TOPDOWNLEFTRIGHT = 2;
  int COMPLETESCANLINES = 4;
  int SINGLEPASS = 8;
  int SINGLEFRAME = 16;
  int IMAGEERROR = 1;
  int SINGLEFRAMEDONE = 2;
  int STATICIMAGEDONE = 3;
  int IMAGEABORTED = 4;
  void setProperties(java.util.Hashtable<?, ?> p0);
  void imageComplete(int p0);
  void setDimensions(int p0, int p1);
  void setColorModel(java.awt.image.ColorModel p0);
  void setHints(int p0);
  void setPixels(int p0, int p1, int p2, int p3, java.awt.image.ColorModel p4, byte[] p5, int p6, int p7);
  void setPixels(int p0, int p1, int p2, int p3, java.awt.image.ColorModel p4, int[] p5, int p6, int p7);
}
