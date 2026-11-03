/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BaseMultiResolutionImage extends AbstractMultiResolutionImage {
  private final int baseImageIndex;
  private final Image[] resolutionVariants;

  public BaseMultiResolutionImage(Image... resolutionVariants) { this(0, resolutionVariants); }

  public BaseMultiResolutionImage(int baseImageIndex, Image... resolutionVariants) {
    Objects.requireNonNull(resolutionVariants, "Resolution variants array is null");
    if (resolutionVariants.length == 0) {
      throw new IllegalArgumentException("Resolution variants array is empty");
    }
    for (Image resolutionVariant : resolutionVariants) {
      Objects.requireNonNull(resolutionVariant, "Resolution variant is null");
    }
    if (baseImageIndex < 0 || baseImageIndex >= resolutionVariants.length) {
      throw new IndexOutOfBoundsException("Invalid base image index: " + baseImageIndex);
    }
    this.baseImageIndex = baseImageIndex;
    this.resolutionVariants = Arrays.copyOf(resolutionVariants, resolutionVariants.length);
  }

  public Image getResolutionVariant(double destImageWidth, double destImageHeight) {
    checkSize(destImageWidth, destImageHeight);
    for (Image rvImage : resolutionVariants) {
      if (destImageWidth <= rvImage.getWidth(null) && destImageHeight <= rvImage.getHeight(null)) {
        return rvImage;
      }
    }
    return resolutionVariants[resolutionVariants.length - 1];
  }

  private static void checkSize(double width, double height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException(String.format("Width (%s) or height (%s) cannot be <= 0", width, height));
    }
    if (!Double.isFinite(width) || !Double.isFinite(height)) {
      throw new IllegalArgumentException(String.format("Width (%s) or height (%s) is not finite", width, height));
    }
  }

  public List<Image> getResolutionVariants() { return Collections.unmodifiableList(Arrays.asList(resolutionVariants)); }

  protected Image getBaseImage() { return resolutionVariants[baseImageIndex]; }
}
