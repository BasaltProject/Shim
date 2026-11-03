/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public interface ImageProducer {
  void addConsumer(java.awt.image.ImageConsumer p0);
  boolean isConsumer(java.awt.image.ImageConsumer p0);
  void removeConsumer(java.awt.image.ImageConsumer p0);
  void startProduction(java.awt.image.ImageConsumer p0);
  void requestTopDownLeftRightResend(java.awt.image.ImageConsumer p0);
}
