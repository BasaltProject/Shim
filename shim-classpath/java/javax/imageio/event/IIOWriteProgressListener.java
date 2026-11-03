/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.event;

public interface IIOWriteProgressListener extends java.util.EventListener {
  void imageComplete(javax.imageio.ImageWriter p0);
  void imageStarted(javax.imageio.ImageWriter p0, int p1);
  void imageProgress(javax.imageio.ImageWriter p0, float p1);
  void thumbnailStarted(javax.imageio.ImageWriter p0, int p1, int p2);
  void thumbnailProgress(javax.imageio.ImageWriter p0, float p1);
  void thumbnailComplete(javax.imageio.ImageWriter p0);
  void writeAborted(javax.imageio.ImageWriter p0);
}
