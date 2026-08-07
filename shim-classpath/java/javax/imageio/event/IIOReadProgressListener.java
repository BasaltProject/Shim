/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.event;

public interface IIOReadProgressListener extends java.util.EventListener {
  void imageComplete(javax.imageio.ImageReader p0);
  void sequenceStarted(javax.imageio.ImageReader p0, int p1);
  void sequenceComplete(javax.imageio.ImageReader p0);
  void imageStarted(javax.imageio.ImageReader p0, int p1);
  void imageProgress(javax.imageio.ImageReader p0, float p1);
  void thumbnailStarted(javax.imageio.ImageReader p0, int p1, int p2);
  void thumbnailProgress(javax.imageio.ImageReader p0, float p1);
  void thumbnailComplete(javax.imageio.ImageReader p0);
  void readAborted(javax.imageio.ImageReader p0);
}
