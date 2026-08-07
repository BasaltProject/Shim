/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.event;

public interface IIOReadUpdateListener extends java.util.EventListener {
  void passStarted(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int[] p9);
  void passComplete(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1);
  void thumbnailPassStarted(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int[] p9);
  void thumbnailUpdate(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1, int p2, int p3, int p4, int p5, int p6, int p7, int[] p8);
  void thumbnailPassComplete(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1);
  void imageUpdate(javax.imageio.ImageReader p0, java.awt.image.BufferedImage p1, int p2, int p3, int p4, int p5, int p6, int p7, int[] p8);
}
