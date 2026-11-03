/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public interface MultiResolutionImage {
  java.awt.Image getResolutionVariant(double p0, double p1);
  java.util.List<java.awt.Image> getResolutionVariants();
}
